package com.santediagnostics.dao;

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

    // Update the state machine for a sample and automatically sync with test_requests table
    public boolean updateStatus(int sampleId, String newStatus, int userId) {
        String timeColumnSql = "";
        if (newStatus.equals("COLLECTED")) {
            timeColumnSql = ", collected_at = CURRENT_TIMESTAMP ";
        } else if (newStatus.equals("PROCESSING")) {
            timeColumnSql = ", processed_at = CURRENT_TIMESTAMP ";
        } else if (newStatus.equals("VALIDATED")) {
            timeColumnSql = ", validated_at = CURRENT_TIMESTAMP ";
        }

        String sql = "UPDATE samples SET status = ?, updated_by = ? " + timeColumnSql + " WHERE id = ?";
        
        Connection conn = getConn();
        try {
            conn.setAutoCommit(false); // Begin transaction block

            try (PreparedStatement stmt = conn.prepareStatement(sql)) {
                stmt.setString(1, newStatus);
                stmt.setInt(2, userId);
                stmt.setInt(3, sampleId);
                int rows = stmt.executeUpdate();
                if (rows == 0) {
                    conn.rollback();
                    conn.setAutoCommit(true);
                    return false;
                }
            }

            // --- SYNC STATUS TO PATIENT DASHBOARD (CHECK CONSTRAINT COMPLIANT) ---
            String syncSql = "UPDATE test_requests SET status = ? WHERE id = (SELECT test_request_id FROM samples WHERE id = ?)";
            String trStatus = "PENDING";
            if (newStatus.equals("COLLECTED") || newStatus.equals("PROCESSING")) {
                trStatus = "PROCESSING";
            } else if (newStatus.equals("AWAITING_VALIDATION")) {
                trStatus = "AWAITING_VALIDATION";
            } else if (newStatus.equals("VALIDATED")) {
                trStatus = "VALIDATED"; 
            }

            try (PreparedStatement syncStmt = conn.prepareStatement(syncSql)) {
                syncStmt.setString(1, trStatus);
                syncStmt.setInt(2, sampleId);
                syncStmt.executeUpdate();
            }

            conn.commit(); // Securely save both tables
            conn.setAutoCommit(true);

            AuditService.getInstance().log(
                    "SAMPLE_STATUS_UPDATED", 
                    "samples", 
                    sampleId, 
                    "Sample #" + sampleId + " status changed to " + newStatus
            );
            return true;
        } catch (SQLException e) {
            System.err.println("SampleDAO updateStatus error: " + e.getMessage());
            try { conn.rollback(); conn.setAutoCommit(true); } catch (SQLException ex) { ex.printStackTrace(); }
        }
        return false;
    }

    public List<Sample> findAll() {
        List<Sample> list = new ArrayList<>();
        // CRITICAL FIX: Added tr.customer_id and tr.ordered_at to the SQL query
        String sql = "SELECT s.*, tr.id AS req_id, tr.customer_id, tr.ordered_at, u.full_name AS customer_name, tt.name AS test_name " +
                     "FROM samples s " +
                     "JOIN test_requests tr ON s.test_request_id = tr.id " +
                     "JOIN users u ON tr.customer_id = u.id " +
                     "JOIN test_types tt ON tr.test_type_id = tt.id " +
                     "ORDER BY s.id DESC";
        try {
            PreparedStatement stmt = getConn().prepareStatement(sql);
            ResultSet rs = stmt.executeQuery();
            while (rs.next()) {
                Sample s = new Sample();
                s.setId(rs.getInt("id"));
                s.setTestRequestId(rs.getInt("test_request_id"));
                s.setStatus(rs.getString("status"));
                s.setCustomerName(rs.getString("customer_name"));
                
                // This will no longer throw an error!
                s.setCustomerId(rs.getInt("customer_id")); 
                
                s.setTestTypeName(rs.getString("test_name"));
                s.setUpdatedBy(rs.getInt("updated_by"));
                
                Timestamp collected = rs.getTimestamp("collected_at");
                if (collected != null) s.setCollectedAt(collected.toLocalDateTime());
                
                Timestamp processed = rs.getTimestamp("processed_at");
                if (processed != null) s.setProcessedAt(processed.toLocalDateTime());
                
                Timestamp validated = rs.getTimestamp("validated_at");
                if (validated != null) s.setValidatedAt(validated.toLocalDateTime());
                
                // Fallback for new samples so the UI table doesn't crash looking for a date
                if (s.getLastActionTime() == null) {
                    Timestamp orderedAt = rs.getTimestamp("ordered_at");
                    if (orderedAt != null) s.setCollectedAt(orderedAt.toLocalDateTime()); 
                }
                
                list.add(s);
            }
        } catch (SQLException e) {
            System.err.println("SampleDAO findAll error: " + e.getMessage());
        }
        return list;
    }
}