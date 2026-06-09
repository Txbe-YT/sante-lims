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
import com.santediagnostics.models.Result;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class ResultDAO {
    private Connection getConn() {
        return DatabaseManager.getInstance().getConnection();
    }

    // Step 1: Uploading the file
    public boolean uploadResult(int testRequestId, String filePath, int uploaderId) {
        String sql = "INSERT INTO results (test_request_id, file_path, uploaded_by, is_verified) VALUES (?, ?, ?, FALSE)";
        try {
            PreparedStatement stmt = getConn().prepareStatement(sql);
            stmt.setInt(1, testRequestId);
            stmt.setString(2, filePath);
            stmt.setInt(3, uploaderId);
            stmt.executeUpdate();

            AuditService.getInstance().log("RESULT_UPLOADED", "results", -1, "Result uploaded for Request #" + testRequestId);
            return true;
        } catch (SQLException e) {
            System.err.println("ResultDAO uploadResult error: " + e.getMessage());
            return false;
        }
    }

    // Step 2: Verifying the result
    public boolean verifyResult(int resultId, int verifierId) {
        // First, check who uploaded it to prevent self-verification
        String checkSql = "SELECT uploaded_by FROM results WHERE id = ?";
        try {
            PreparedStatement checkStmt = getConn().prepareStatement(checkSql);
            checkStmt.setInt(1, resultId);
            ResultSet rs = checkStmt.executeQuery();
            
            if (rs.next()) {
                int uploaderId = rs.getInt("uploaded_by");
                if (uploaderId == verifierId) {
                    System.err.println("Verification Failed: Uploader cannot verify their own result.");
                    return false; // Violates two-step verification
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }

        // If safe, verify it
        String updateSql = "UPDATE results SET is_verified = TRUE, verified_by = ? WHERE id = ?";
        try {
            PreparedStatement stmt = getConn().prepareStatement(updateSql);
            stmt.setInt(1, verifierId);
            stmt.setInt(2, resultId);
            stmt.executeUpdate();

            AuditService.getInstance().log("RESULT_VERIFIED", "results", resultId, "Result verified by User #" + verifierId);
            return true;
        } catch (SQLException e) {
            System.err.println("ResultDAO verifyResult error: " + e.getMessage());
            return false;
        }
    }
    
    // Fetch all results awaiting second-party verification
    public java.util.List<com.santediagnostics.models.Result> getPendingVerifications() {
        java.util.List<com.santediagnostics.models.Result> list = new java.util.ArrayList<>();
        String sql = "SELECT r.*, u.full_name AS customer_name, tt.name AS test_name " +
                     "FROM results r " +
                     "JOIN test_requests tr ON r.test_request_id = tr.id " +
                     "JOIN users u ON tr.customer_id = u.id " +
                     "JOIN test_types tt ON tr.test_type_id = tt.id " +
                     "WHERE r.is_verified = FALSE " +
                     "ORDER BY r.uploaded_at DESC";
        try {
            PreparedStatement stmt = getConn().prepareStatement(sql);
            ResultSet rs = stmt.executeQuery();
            while (rs.next()) {
                com.santediagnostics.models.Result r = new com.santediagnostics.models.Result();
                r.setId(rs.getInt("id"));
                r.setTestRequestId(rs.getInt("test_request_id"));
                r.setFilePath(rs.getString("file_path"));
                r.setVerified(rs.getBoolean("is_verified"));
                r.setUploadedBy(rs.getInt("uploaded_by"));
                r.setCustomerName(rs.getString("customer_name"));
                r.setTestName(rs.getString("test_name"));
                
                Timestamp uploaded = rs.getTimestamp("uploaded_at");
                if (uploaded != null) r.setUploadedAt(uploaded.toLocalDateTime());
                
                list.add(r);
            }
        } catch (SQLException e) {
            System.err.println("ResultDAO getPending error: " + e.getMessage());
        }
        return list;
    }
    // Get verified results for a specific customer (for Result Vault)
public List<Result> getVerifiedResultsByCustomer(int customerId) {
    List<Result> list = new ArrayList<>();
    String sql = "SELECT r.*, tt.name AS test_name, u.full_name AS customer_name " +
                 "FROM results r " +
                 "JOIN test_requests tr ON r.test_request_id = tr.id " +
                 "JOIN users u ON tr.customer_id = u.id " +
                 "JOIN test_types tt ON tr.test_type_id = tt.id " +
                 "WHERE tr.customer_id = ? AND r.is_verified = TRUE " +
                 "ORDER BY r.uploaded_at DESC";
    try {
        PreparedStatement stmt = getConn().prepareStatement(sql);
        stmt.setInt(1, customerId);
        ResultSet rs = stmt.executeQuery();
        while (rs.next()) {
            Result r = new Result();
            r.setId(rs.getInt("id"));
            r.setTestRequestId(rs.getInt("test_request_id"));
            r.setFilePath(rs.getString("file_path"));
            r.setVerified(rs.getBoolean("is_verified"));
            r.setUploadedBy(rs.getInt("uploaded_by"));
            r.setVerifiedBy(rs.getInt("verified_by"));
            r.setCustomerName(rs.getString("customer_name"));
            r.setTestName(rs.getString("test_name"));
            
            Timestamp uploadedAt = rs.getTimestamp("uploaded_at");
            if (uploadedAt != null) r.setUploadedAt(uploadedAt.toLocalDateTime());
            
            list.add(r);
        }
    } catch (SQLException e) {
        System.err.println("getVerifiedResultsByCustomer error: " + e.getMessage());
    }
    return list;
}
}

