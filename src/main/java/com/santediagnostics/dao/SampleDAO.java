/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.santediagnostics.dao;

/**
 *
 * @author dasil
 */

import com.santediagnostics.audit.AuditService;
import com.santediagnostics.db.DatabaseManager;
import com.santediagnostics.models.Sample;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class SampleDAO {
    private Connection getConn() {
        return DatabaseManager.getInstance().getConnection();
    }

    // Update the state machine for a sample
    public boolean updateStatus(int sampleId, String newStatus, int userId) {
        
        // Dynamically figure out which timestamp column to update based on the status
        String timeColumnSql = "";
        if (newStatus.equals("COLLECTED")) {
            timeColumnSql = ", collected_at = CURRENT_TIMESTAMP ";
        } else if (newStatus.equals("PROCESSING")) {
            timeColumnSql = ", processed_at = CURRENT_TIMESTAMP ";
        } else if (newStatus.equals("VALIDATED")) {
            timeColumnSql = ", validated_at = CURRENT_TIMESTAMP ";
        }

        String sql = "UPDATE samples SET status = ?, updated_by = ? " + timeColumnSql + " WHERE id = ?";
        
        try {
            PreparedStatement stmt = getConn().prepareStatement(sql);
            stmt.setString(1, newStatus);
            stmt.setInt(2, userId);
            stmt.setInt(3, sampleId);
            int rows = stmt.executeUpdate();
            
            if (rows > 0) {
                AuditService.getInstance().log(
                        "SAMPLE_STATUS_UPDATED", 
                        "samples", 
                        sampleId, 
                        "Sample #" + sampleId + " status changed to " + newStatus
                );
                return true;
            }
        } catch (SQLException e) {
            System.err.println("SampleDAO updateStatus error: " + e.getMessage());
        }
        return false;
    }

    // Get all samples for your tracking dashboard
    public List<Sample> findAll() {
        List<Sample> list = new ArrayList<>();
        String sql = "SELECT s.*, tr.id AS req_id, u.full_name AS customer_name, tt.name AS test_name " +
                     "FROM samples s " +
                     "JOIN test_requests tr ON s.test_request_id = tr.id " +
                     "JOIN users u ON tr.customer_id = u.id " +
                     "JOIN test_types tt ON tr.test_type_id = tt.id " +
                     "ORDER BY s.id DESC"; // Removed ORDER BY updated_at
        try {
            PreparedStatement stmt = getConn().prepareStatement(sql);
            ResultSet rs = stmt.executeQuery();
            while (rs.next()) {
                Sample s = new Sample();
                s.setId(rs.getInt("id"));
                s.setTestRequestId(rs.getInt("test_request_id"));
                s.setStatus(rs.getString("status"));
                s.setCustomerName(rs.getString("customer_name"));
                s.setTestTypeName(rs.getString("test_name"));
                s.setUpdatedBy(rs.getInt("updated_by"));
                
                // Fetch the new timestamps
                Timestamp collected = rs.getTimestamp("collected_at");
                if (collected != null) s.setCollectedAt(collected.toLocalDateTime());
                
                Timestamp processed = rs.getTimestamp("processed_at");
                if (processed != null) s.setProcessedAt(processed.toLocalDateTime());
                
                Timestamp validated = rs.getTimestamp("validated_at");
                if (validated != null) s.setValidatedAt(validated.toLocalDateTime());
                
                list.add(s);
            }
        } catch (SQLException e) {
            System.err.println("SampleDAO findAll error: " + e.getMessage());
        }
        return list;
    }
}