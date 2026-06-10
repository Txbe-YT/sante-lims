package com.santediagnostics.dao;

import com.santediagnostics.db.DatabaseManager;
import com.santediagnostics.models.TestRequest;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class TestRequestDAO {

    private Connection getConn() {
        return DatabaseManager.getInstance().getConnection();
    }

    // --- DASHBOARD DATA METHODS ---
    public List<TestRequest> findByCustomerId(int customerId) {
        List<TestRequest> list = new ArrayList<>();
        String sql = "SELECT tr.*, u.full_name AS customer_name, tt.name AS test_type_name, tt.price AS test_price, tt.tat_hours " +
                     "FROM test_requests tr JOIN users u ON tr.customer_id = u.id JOIN test_types tt ON tr.test_type_id = tt.id " +
                     "WHERE tr.customer_id = ? ORDER BY tr.ordered_at DESC";
        try (PreparedStatement stmt = getConn().prepareStatement(sql)) {
            stmt.setInt(1, customerId);
            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) list.add(mapRow(rs));
            }
        } catch (SQLException e) { System.err.println(e.getMessage()); }
        return list;
    }

    public int countByCustomerIdAndStatus(int customerId, String col, String val) {
        String sql = "SELECT COUNT(*) FROM test_requests WHERE customer_id = ? AND " + col + " = ?";
        try (PreparedStatement pstmt = getConn().prepareStatement(sql)) {
            pstmt.setInt(1, customerId);
            pstmt.setString(2, val);
            try (ResultSet rs = pstmt.executeQuery()) {
                if (rs.next()) return rs.getInt(1);
            }
        } catch (SQLException e) { System.err.println(e.getMessage()); }
        return 0;
    }

    // --- ADMIN / QUEUE METHODS ---
    public List<TestRequest> findAll() {
        List<TestRequest> list = new ArrayList<>();
        String sql = "SELECT tr.*, u.full_name AS customer_name, tt.name AS test_type_name, tt.price AS test_price, tt.tat_hours " +
                     "FROM test_requests tr JOIN users u ON tr.customer_id = u.id JOIN test_types tt ON tr.test_type_id = tt.id ORDER BY tr.ordered_at DESC";
        try (PreparedStatement stmt = getConn().prepareStatement(sql); ResultSet rs = stmt.executeQuery()) {
            while (rs.next()) list.add(mapRow(rs));
        } catch (SQLException e) { System.err.println(e.getMessage()); }
        return list;
    }

    public List<TestRequest> findUnpaid() {
        List<TestRequest> list = new ArrayList<>();
        String sql = "SELECT tr.*, u.full_name AS customer_name, tt.name AS test_type_name, tt.price AS test_price, tt.tat_hours " +
                     "FROM test_requests tr JOIN users u ON tr.customer_id = u.id JOIN test_types tt ON tr.test_type_id = tt.id " +
                     "WHERE tr.payment_status = 'UNPAID' ORDER BY tr.ordered_at DESC";
        try (PreparedStatement stmt = getConn().prepareStatement(sql); ResultSet rs = stmt.executeQuery()) {
            while (rs.next()) list.add(mapRow(rs));
        } catch (SQLException e) { System.err.println(e.getMessage()); }
        return list;
    }

    // --- PAYMENT FLOW METHODS ---
    public int createRequestAndGetId(int customerId, int testTypeId) {
        String sql = "INSERT INTO test_requests (customer_id, test_type_id, status, payment_status, ordered_at) " +
                     "VALUES (?, ?, 'PENDING', 'UNPAID', CURRENT_TIMESTAMP) RETURNING id";
        
        // CRITICAL FIX: The connection is NO LONGER inside the try() parentheses here!
        try (PreparedStatement pstmt = getConn().prepareStatement(sql)) {
            pstmt.setInt(1, customerId);
            pstmt.setInt(2, testTypeId);
            try (ResultSet rs = pstmt.executeQuery()) {
                if (rs.next()) return rs.getInt(1);
            }
        } catch (SQLException e) { System.err.println(e.getMessage()); }
        return -1;
    }

    public boolean updatePaymentReference(int requestId, String ref) {
        String sql = "UPDATE test_requests SET payment_reference = ? WHERE id = ?";
        try (PreparedStatement stmt = getConn().prepareStatement(sql)) {
            stmt.setString(1, ref);
            stmt.setInt(2, requestId);
            return stmt.executeUpdate() > 0;
        } catch (SQLException e) { return false; }
    }

    // THIS METHOD LINKS PAYMENT DIRECTLY TO THE SAMPLE QUEUE
    public boolean markAsPaid(int requestId, String ref, int markedBy) {
        String updateTestSql = "UPDATE test_requests SET payment_status = 'PAID', payment_reference = ?, " +
                               "paid_at = CURRENT_TIMESTAMP, marked_paid_by = ? WHERE id = ?";
                               
        String insertSampleSql = "INSERT INTO samples (test_request_id, status, updated_by) " +
                                 "VALUES (?, 'PENDING_COLLECTION', ?)";
                                 
        Connection conn = getConn();
        try {
            conn.setAutoCommit(false); // Start Transaction
            
            try (PreparedStatement stmt1 = conn.prepareStatement(updateTestSql)) {
                stmt1.setString(1, ref);
                stmt1.setInt(2, markedBy);
                stmt1.setInt(3, requestId);
                stmt1.executeUpdate();
            }
            
            try (PreparedStatement stmt2 = conn.prepareStatement(insertSampleSql)) {
                stmt2.setInt(1, requestId);
                stmt2.setInt(2, markedBy);
                stmt2.executeUpdate();
            }
            
            conn.commit(); // Save both!
            conn.setAutoCommit(true);
            return true;
            
        } catch (SQLException e) {
            try {
                conn.rollback();
                conn.setAutoCommit(true);
            } catch (SQLException ex) { System.err.println(ex.getMessage()); }
            return false;
        }
    }

    public TestRequest findById(int id) {
        String sql = "SELECT tr.*, u.full_name AS customer_name, tt.name AS test_type_name, tt.price AS test_price, tt.tat_hours " +
                     "FROM test_requests tr JOIN users u ON tr.customer_id = u.id JOIN test_types tt ON tr.test_type_id = tt.id " +
                     "WHERE tr.id = ?";
        try (PreparedStatement stmt = getConn().prepareStatement(sql)) {
            stmt.setInt(1, id);
            try(ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) return mapRow(rs);
            }
        } catch (SQLException e) { System.err.println(e.getMessage()); }
        return null;
    }

    // --- MAPPING ---
    private TestRequest mapRow(ResultSet rs) throws SQLException {
        TestRequest tr = new TestRequest();
        tr.setId(rs.getInt("id"));
        tr.setCustomerId(rs.getInt("customer_id"));
        tr.setCustomerName(rs.getString("customer_name"));
        tr.setTestTypeName(rs.getString("test_type_name"));
        tr.setTestPrice(rs.getDouble("test_price"));
        tr.setStatus(rs.getString("status"));
        tr.setPaymentStatus(rs.getString("payment_status"));
        tr.setPaymentReference(rs.getString("payment_reference"));
        tr.setTatHours(rs.getInt("tat_hours"));
        Timestamp orderedAt = rs.getTimestamp("ordered_at");
        if (orderedAt != null) tr.setOrderedAt(orderedAt.toLocalDateTime());
        
        
        return tr;
    }
}