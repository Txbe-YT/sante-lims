package com.santediagnostics.dao;

import com.santediagnostics.db.DatabaseManager;
import com.santediagnostics.models.TestType;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class TestTypeDAO {

    private Connection getConn() {
        return DatabaseManager.getInstance().getConnection();
    }

    // Get all test types
    public List<TestType> findAll() {
        List<TestType> list = new ArrayList<>();
        String sql = "SELECT * FROM test_types ORDER BY name ASC";
        try {
            PreparedStatement stmt = getConn().prepareStatement(sql);
            ResultSet rs = stmt.executeQuery();
            while (rs.next()) {
                list.add(mapRow(rs));
            }
        } catch (SQLException e) {
            System.err.println("TestTypeDAO findAll error: " + e.getMessage());
        }
        return list;
    }

    // Find by ID
    public TestType findById(int id) {
        String sql = "SELECT * FROM test_types WHERE id = ?";
        try {
            PreparedStatement stmt = getConn().prepareStatement(sql);
            stmt.setInt(1, id);
            ResultSet rs = stmt.executeQuery();
            if (rs.next()) {
                return mapRow(rs);
            }
        } catch (SQLException e) {
            System.err.println("TestTypeDAO findById error: " + e.getMessage());
        }
        return null;
    }

    // Create new test type
    public boolean create(String name, String category, double price,
                          int tatHours, String resultFormat) {
        String sql = "INSERT INTO test_types (name, category, price, tat_hours, result_format) " +
                "VALUES (?, ?, ?, ?, ?)";
        try {
            PreparedStatement stmt = getConn().prepareStatement(sql);
            stmt.setString(1, name);
            stmt.setString(2, category);
            stmt.setDouble(3, price);
            stmt.setInt(4, tatHours);
            stmt.setString(5, resultFormat);
            stmt.executeUpdate();
            return true;
        } catch (SQLException e) {
            System.err.println("TestTypeDAO create error: " + e.getMessage());
            return false;
        }
    }

    // Update test type
    public boolean update(int id, String name, String category, double price,
                          int tatHours, String resultFormat) {
        String sql = "UPDATE test_types SET name = ?, category = ?, price = ?, " +
                "tat_hours = ?, result_format = ? WHERE id = ?";
        try {
            PreparedStatement stmt = getConn().prepareStatement(sql);
            stmt.setString(1, name);
            stmt.setString(2, category);
            stmt.setDouble(3, price);
            stmt.setInt(4, tatHours);
            stmt.setString(5, resultFormat);
            stmt.setInt(6, id);
            stmt.executeUpdate();
            return true;
        } catch (SQLException e) {
            System.err.println("TestTypeDAO update error: " + e.getMessage());
            return false;
        }
    }

    // Delete test type
    public boolean delete(int id) {
        String sql = "DELETE FROM test_types WHERE id = ?";
        try {
            PreparedStatement stmt = getConn().prepareStatement(sql);
            stmt.setInt(1, id);
            stmt.executeUpdate();
            return true;
        } catch (SQLException e) {
            System.err.println("TestTypeDAO delete error: " + e.getMessage());
            return false;
        }
    }

    // Map row to TestType object
    private TestType mapRow(ResultSet rs) throws SQLException {
        TestType t = new TestType();
        t.setId(rs.getInt("id"));
        t.setName(rs.getString("name"));
        t.setCategory(rs.getString("category"));
        t.setPrice(rs.getDouble("price"));
        t.setTatHours(rs.getInt("tat_hours"));
        t.setResultFormat(rs.getString("result_format"));
        Timestamp createdAt = rs.getTimestamp("created_at");
        if (createdAt != null) {
            t.setCreatedAt(createdAt.toLocalDateTime());
        }
        return t;
    }
}