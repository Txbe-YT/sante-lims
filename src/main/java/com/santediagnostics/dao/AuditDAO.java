package com.santediagnostics.dao;

import com.santediagnostics.db.DatabaseManager;
import com.santediagnostics.models.AuditLog;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class AuditDAO {

    private Connection getConn() {
        return DatabaseManager.getInstance().getConnection();
    }

    // Get all audit logs (for Super Admin viewer)
    public List<AuditLog> findAll() {
        List<AuditLog> list = new ArrayList<>();
        String sql = "SELECT al.*, u.full_name AS user_full_name " +
                "FROM audit_log al " +
                "LEFT JOIN users u ON al.user_id = u.id " +
                "ORDER BY al.performed_at DESC";
        try {
            PreparedStatement stmt = getConn().prepareStatement(sql);
            ResultSet rs = stmt.executeQuery();
            while (rs.next()) {
                list.add(mapRow(rs));
            }
        } catch (SQLException e) {
            System.err.println("AuditDAO findAll error: " + e.getMessage());
        }
        return list;
    }

    // Get audit logs by user ID
    public List<AuditLog> findByUserId(int userId) {
        List<AuditLog> list = new ArrayList<>();
        String sql = "SELECT al.*, u.full_name AS user_full_name " +
                "FROM audit_log al " +
                "LEFT JOIN users u ON al.user_id = u.id " +
                "WHERE al.user_id = ? " +
                "ORDER BY al.performed_at DESC";
        try {
            PreparedStatement stmt = getConn().prepareStatement(sql);
            stmt.setInt(1, userId);
            ResultSet rs = stmt.executeQuery();
            while (rs.next()) {
                list.add(mapRow(rs));
            }
        } catch (SQLException e) {
            System.err.println("AuditDAO findByUserId error: " + e.getMessage());
        }
        return list;
    }

    // Get audit logs by action type
    public List<AuditLog> findByAction(String action) {
        List<AuditLog> list = new ArrayList<>();
        String sql = "SELECT al.*, u.full_name AS user_full_name " +
                "FROM audit_log al " +
                "LEFT JOIN users u ON al.user_id = u.id " +
                "WHERE al.action ILIKE ? " +
                "ORDER BY al.performed_at DESC";
        try {
            PreparedStatement stmt = getConn().prepareStatement(sql);
            stmt.setString(1, "%" + action + "%");
            ResultSet rs = stmt.executeQuery();
            while (rs.next()) {
                list.add(mapRow(rs));
            }
        } catch (SQLException e) {
            System.err.println("AuditDAO findByAction error: " + e.getMessage());
        }
        return list;
    }

    // Map row to AuditLog object
    private AuditLog mapRow(ResultSet rs) throws SQLException {
        AuditLog log = new AuditLog();
        log.setId(rs.getInt("id"));
        log.setUserId(rs.getInt("user_id"));
        log.setAction(rs.getString("action"));
        log.setTargetTable(rs.getString("target_table"));
        log.setTargetId(rs.getInt("target_id"));
        log.setDetails(rs.getString("details"));
        log.setUserFullName(rs.getString("user_full_name"));
        Timestamp performedAt = rs.getTimestamp("performed_at");
        if (performedAt != null) {
            log.setPerformedAt(performedAt.toLocalDateTime());
        }
        return log;
    }
}