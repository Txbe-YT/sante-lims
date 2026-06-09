package com.santediagnostics.dao;

import com.santediagnostics.db.DatabaseManager;
import com.santediagnostics.models.User;
import org.mindrot.jbcrypt.BCrypt;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

import java.time.LocalDateTime;
import java.sql.Timestamp;

public class UserDAO {

    private Connection getConn() {
        return DatabaseManager.getInstance().getConnection();
    }

    // Find user by email (used for login)
    public User findByEmail(String email) {
        String sql = "SELECT * FROM users WHERE email = ?";
        try {
            PreparedStatement stmt = getConn().prepareStatement(sql);
            stmt.setString(1, email);
            ResultSet rs = stmt.executeQuery();
            if (rs.next()) {
                return mapRow(rs);
            }
        } catch (SQLException e) {
            System.err.println("findByEmail error: " + e.getMessage());
        }
        return null;
    }

    // Find user by ID
    public User findById(int id) {
        String sql = "SELECT * FROM users WHERE id = ?";
        try {
            PreparedStatement stmt = getConn().prepareStatement(sql);
            stmt.setInt(1, id);
            ResultSet rs = stmt.executeQuery();
            if (rs.next()) {
                return mapRow(rs);
            }
        } catch (SQLException e) {
            System.err.println("findById error: " + e.getMessage());
        }
        return null;
    }

    // Get all users by role
    public List<User> findByRole(String role) {
        List<User> users = new ArrayList<>();
        String sql = "SELECT * FROM users WHERE role = ? ORDER BY created_at DESC";
        try {
            PreparedStatement stmt = getConn().prepareStatement(sql);
            stmt.setString(1, role);
            ResultSet rs = stmt.executeQuery();
            while (rs.next()) {
                users.add(mapRow(rs));
            }
        } catch (SQLException e) {
            System.err.println("findByRole error: " + e.getMessage());
        }
        return users;
    }

    // Get all users
    public List<User> findAll() {
        List<User> users = new ArrayList<>();
        String sql = "SELECT * FROM users ORDER BY created_at DESC";
        try {
            PreparedStatement stmt = getConn().prepareStatement(sql);
            ResultSet rs = stmt.executeQuery();
            while (rs.next()) {
                users.add(mapRow(rs));
            }
        } catch (SQLException e) {
            System.err.println("findAll error: " + e.getMessage());
        }
        return users;
    }

    // Create a new user (by staff)
    public boolean createUser(String fullName, String email, String plainPassword,
                              String role, int createdBy) {
        String hashedPassword = BCrypt.hashpw(plainPassword, BCrypt.gensalt());
        String sql = "INSERT INTO users (full_name, email, password_hash, role, " +
                "is_verified, force_password_change, created_by) " +
                "VALUES (?, ?, ?, ?, TRUE, TRUE, ?)";
        try {
            PreparedStatement stmt = getConn().prepareStatement(sql);
            stmt.setString(1, fullName);
            stmt.setString(2, email);
            stmt.setString(3, hashedPassword);
            stmt.setString(4, role);
            stmt.setInt(5, createdBy);
            stmt.executeUpdate();
            return true;
        } catch (SQLException e) {
            System.err.println("createUser error: " + e.getMessage());
            return false;
        }
    }

    // Self-register customer (email not yet verified)
    public boolean registerCustomer(String fullName, String email, String plainPassword) {
        String hashedPassword = BCrypt.hashpw(plainPassword, BCrypt.gensalt());
        String sql = "INSERT INTO users (full_name, email, password_hash, role, " +
                "is_verified, force_password_change) " +
                "VALUES (?, ?, ?, 'CUSTOMER', FALSE, FALSE)";
        try {
            PreparedStatement stmt = getConn().prepareStatement(sql);
            stmt.setString(1, fullName);
            stmt.setString(2, email);
            stmt.setString(3, hashedPassword);
            stmt.executeUpdate();
            return true;
        } catch (SQLException e) {
            System.err.println("registerCustomer error: " + e.getMessage());
            return false;
        }
    }

    // Update password
    public boolean updatePassword(int userId, String newPlainPassword) {
        String hashedPassword = BCrypt.hashpw(newPlainPassword, BCrypt.gensalt());
        String sql = "UPDATE users SET password_hash = ?, force_password_change = FALSE WHERE id = ?";
        try {
            PreparedStatement stmt = getConn().prepareStatement(sql);
            stmt.setString(1, hashedPassword);
            stmt.setInt(2, userId);
            stmt.executeUpdate();
            return true;
        } catch (SQLException e) {
            System.err.println("updatePassword error: " + e.getMessage());
            return false;
        }
    }

    // Verify email
    public boolean verifyEmail(int userId) {
        String sql = "UPDATE users SET is_verified = TRUE WHERE id = ?";
        try {
            PreparedStatement stmt = getConn().prepareStatement(sql);
            stmt.setInt(1, userId);
            stmt.executeUpdate();
            return true;
        } catch (SQLException e) {
            System.err.println("verifyEmail error: " + e.getMessage());
            return false;
        }
    }

    // Check password
    public boolean checkPassword(String plainPassword, String hashedPassword) {
        return BCrypt.checkpw(plainPassword, hashedPassword);
    }

    // Map database row to User object
    private User mapRow(ResultSet rs) throws SQLException {
        User user = new User();
        user.setId(rs.getInt("id"));
        user.setFullName(rs.getString("full_name"));
        user.setEmail(rs.getString("email"));
        user.setPasswordHash(rs.getString("password_hash"));
        user.setRole(rs.getString("role"));
        user.setVerified(rs.getBoolean("is_verified"));
        user.setForcePasswordChange(rs.getBoolean("force_password_change"));
        Timestamp createdAt = rs.getTimestamp("created_at");
        if (createdAt != null) {
            user.setCreatedAt(createdAt.toLocalDateTime());
        }
        return user;
    }

    public boolean registerCustomerWithToken(String fullName, String email, String plainPassword, String token) {
        String hashedPassword = BCrypt.hashpw(plainPassword, BCrypt.gensalt());
        LocalDateTime tokenExpiry = LocalDateTime.now().plusHours(24);
        
        String sql = "INSERT INTO users (full_name, email, password_hash, role, " +
                     "is_verified, force_password_change, verification_token, token_expiry) " +
                     "VALUES (?, ?, ?, 'CUSTOMER', FALSE, TRUE, ?, ?)";
        try {
            PreparedStatement stmt = getConn().prepareStatement(sql);
            stmt.setString(1, fullName);
            stmt.setString(2, email);
            stmt.setString(3, hashedPassword);
            stmt.setString(4, token);
            stmt.setTimestamp(5, Timestamp.valueOf(tokenExpiry));
            stmt.executeUpdate();
            return true;
        } catch (SQLException e) {
            System.err.println("registerCustomerWithToken error: " + e.getMessage());
            return false;
        }
    }
    
    // Verify email by token
    public boolean verifyEmailByToken(String token) {
        String sql = "UPDATE users SET is_verified = TRUE, verification_token = NULL, " +
                     "token_expiry = NULL, force_password_change = FALSE " +
                     "WHERE verification_token = ? AND token_expiry > ?";
        try {
            PreparedStatement stmt = getConn().prepareStatement(sql);
            stmt.setString(1, token);
            stmt.setTimestamp(2, Timestamp.valueOf(LocalDateTime.now()));
            int updated = stmt.executeUpdate();
            return updated > 0;
        } catch (SQLException e) {
            System.err.println("verifyEmailByToken error: " + e.getMessage());
            return false;
        }
    }
    
    // Update user profile (name only)
    public boolean updateProfile(int userId, String newFullName) {
        String sql = "UPDATE users SET full_name = ? WHERE id = ?";
        try {
            PreparedStatement stmt = getConn().prepareStatement(sql);
            stmt.setString(1, newFullName);
            stmt.setInt(2, userId);
            stmt.executeUpdate();
            return true;
        } catch (SQLException e) {
            System.err.println("updateProfile error: " + e.getMessage());
            return false;
        }
    }
    
    // Verify current password before change
    public boolean verifyCurrentPassword(int userId, String plainPassword) {
        User user = findById(userId);
        if (user == null) return false;
        return BCrypt.checkpw(plainPassword, user.getPasswordHash());
    }
    
    // Change password with old password verification
    public boolean changePassword(int userId, String oldPassword, String newPassword) {
        if (!verifyCurrentPassword(userId, oldPassword)) {
            return false;
        }
        return updatePassword(userId, newPassword);
    }
}