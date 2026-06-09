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

    // Get all test requests (for Super Admin and Lab Attendant queue)
    public List<TestRequest> findAll() {
        List<TestRequest> list = new ArrayList<>();
        String sql = "SELECT tr.*, u.full_name AS customer_name, " +
                "tt.name AS test_type_name, tt.price AS test_price, tt.tat_hours " +
                "FROM test_requests tr " +
                "JOIN users u ON tr.customer_id = u.id " +
                "JOIN test_types tt ON tr.test_type_id = tt.id " +
                "ORDER BY tr.ordered_at DESC";
        try {
            PreparedStatement stmt = getConn().prepareStatement(sql);
            ResultSet rs = stmt.executeQuery();
            while (rs.next()) {
                list.add(mapRow(rs));
            }
        } catch (SQLException e) {
            System.err.println("TestRequestDAO findAll error: " + e.getMessage());
        }
        return list;
    }

    // Get requests by customer ID (for customer dashboard)
    public List<TestRequest> findByCustomerId(int customerId) {
        List<TestRequest> list = new ArrayList<>();
        String sql = "SELECT tr.*, u.full_name AS customer_name, " +
                "tt.name AS test_type_name, tt.price AS test_price, tt.tat_hours " +
                "FROM test_requests tr " +
                "JOIN users u ON tr.customer_id = u.id " +
                "JOIN test_types tt ON tr.test_type_id = tt.id " +
                "WHERE tr.customer_id = ? " +
                "ORDER BY tr.ordered_at DESC";
        try {
            PreparedStatement stmt = getConn().prepareStatement(sql);
            stmt.setInt(1, customerId);
            ResultSet rs = stmt.executeQuery();
            while (rs.next()) {
                list.add(mapRow(rs));
            }
        } catch (SQLException e) {
            System.err.println("TestRequestDAO findByCustomerId error: " + e.getMessage());
        }
        return list;
    }

    // Get all unpaid requests
    public List<TestRequest> findUnpaid() {
        List<TestRequest> list = new ArrayList<>();
        String sql = "SELECT tr.*, u.full_name AS customer_name, " +
                "tt.name AS test_type_name, tt.price AS test_price, tt.tat_hours " +
                "FROM test_requests tr " +
                "JOIN users u ON tr.customer_id = u.id " +
                "JOIN test_types tt ON tr.test_type_id = tt.id " +
                "WHERE tr.payment_status = 'UNPAID' " +
                "ORDER BY tr.ordered_at DESC";
        try {
            PreparedStatement stmt = getConn().prepareStatement(sql);
            ResultSet rs = stmt.executeQuery();
            while (rs.next()) {
                list.add(mapRow(rs));
            }
        } catch (SQLException e) {
            System.err.println("TestRequestDAO findUnpaid error: " + e.getMessage());
        }
        return list;
    }

    // Create a new test request
    public boolean create(int customerId, int testTypeId) {
        String sql = "INSERT INTO test_requests (customer_id, test_type_id, " +
                "status, payment_status) VALUES (?, ?, 'PENDING', 'UNPAID')";
        try {
            PreparedStatement stmt = getConn().prepareStatement(sql);
            stmt.setInt(1, customerId);
            stmt.setInt(2, testTypeId);
            stmt.executeUpdate();
            return true;
        } catch (SQLException e) {
            System.err.println("TestRequestDAO create error: " + e.getMessage());
            return false;
        }
    }

    // Mark request as paid
    public boolean markAsPaid(int requestId, String paymentReference, int markedBy) {
        String sql = "UPDATE test_requests SET payment_status = 'PAID', " +
                "payment_reference = ?, paid_at = CURRENT_TIMESTAMP, " +
                "marked_paid_by = ? WHERE id = ?";
        try {
            PreparedStatement stmt = getConn().prepareStatement(sql);
            stmt.setString(1, paymentReference);
            stmt.setInt(2, markedBy);
            stmt.setInt(3, requestId);
            stmt.executeUpdate();
            return true;
        } catch (SQLException e) {
            System.err.println("TestRequestDAO markAsPaid error: " + e.getMessage());
            return false;
        }
    }

    // Update request status
    public boolean updateStatus(int requestId, String newStatus) {
        String sql = "UPDATE test_requests SET status = ? WHERE id = ?";
        try {
            PreparedStatement stmt = getConn().prepareStatement(sql);
            stmt.setString(1, newStatus);
            stmt.setInt(2, requestId);
            stmt.executeUpdate();
            return true;
        } catch (SQLException e) {
            System.err.println("TestRequestDAO updateStatus error: " + e.getMessage());
            return false;
        }
    }

    // Find by ID
    public TestRequest findById(int id) {
        String sql = "SELECT tr.*, u.full_name AS customer_name, " +
                "tt.name AS test_type_name, tt.price AS test_price, tt.tat_hours " +
                "FROM test_requests tr " +
                "JOIN users u ON tr.customer_id = u.id " +
                "JOIN test_types tt ON tr.test_type_id = tt.id " +
                "WHERE tr.id = ?";
        try {
            PreparedStatement stmt = getConn().prepareStatement(sql);
            stmt.setInt(1, id);
            ResultSet rs = stmt.executeQuery();
            if (rs.next()) {
                return mapRow(rs);
            }
        } catch (SQLException e) {
            System.err.println("TestRequestDAO findById error: " + e.getMessage());
        }
        return null;
    }

    // Map row to TestRequest object
    private TestRequest mapRow(ResultSet rs) throws SQLException {
        TestRequest tr = new TestRequest();
        tr.setId(rs.getInt("id"));
        tr.setCustomerId(rs.getInt("customer_id"));
        tr.setTestTypeId(rs.getInt("test_type_id"));
        tr.setStatus(rs.getString("status"));
        tr.setPaymentStatus(rs.getString("payment_status"));
        tr.setPaymentReference(rs.getString("payment_reference"));
        tr.setCustomerName(rs.getString("customer_name"));
        tr.setTestTypeName(rs.getString("test_type_name"));
        tr.setTestPrice(rs.getDouble("test_price"));
        tr.setTatHours(rs.getInt("tat_hours"));
        Timestamp orderedAt = rs.getTimestamp("ordered_at");
        if (orderedAt != null) {
            tr.setOrderedAt(orderedAt.toLocalDateTime());
        }
        Timestamp paidAt = rs.getTimestamp("paid_at");
        if (paidAt != null) {
            tr.setPaidAt(paidAt.toLocalDateTime());
        }
        return tr;
    }

    // Find latest unpaid request for a customer
public TestRequest findLatestUnpaidByCustomer(int customerId) {
    String sql = "SELECT tr.*, u.full_name AS customer_name, " +
                 "tt.name AS test_type_name, tt.price AS test_price, tt.tat_hours " +
                 "FROM test_requests tr " +
                 "JOIN users u ON tr.customer_id = u.id " +
                 "JOIN test_types tt ON tr.test_type_id = tt.id " +
                 "WHERE tr.customer_id = ? AND tr.payment_status = 'UNPAID' " +
                 "ORDER BY tr.ordered_at DESC LIMIT 1";
    try {
        PreparedStatement stmt = getConn().prepareStatement(sql);
        stmt.setInt(1, customerId);
        ResultSet rs = stmt.executeQuery();
        if (rs.next()) {
            return mapRow(rs);
        }
    } catch (SQLException e) {
        System.err.println("findLatestUnpaidByCustomer error: " + e.getMessage());
    }
    return null;
}

// Update payment reference (customer records it)
public boolean updatePaymentReference(int requestId, String reference) {
    String sql = "UPDATE test_requests SET payment_reference = ? WHERE id = ?";
    try {
        PreparedStatement stmt = getConn().prepareStatement(sql);
        stmt.setString(1, reference);
        stmt.setInt(2, requestId);
        stmt.executeUpdate();
        return true;
    } catch (SQLException e) {
        System.err.println("updatePaymentReference error: " + e.getMessage());
        return false;
    }
}
}