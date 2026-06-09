package com.santediagnostics.dao;

import com.santediagnostics.db.DatabaseManager;
import com.santediagnostics.models.BankDetails;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class BankDetailsDAO {
    
    private Connection getConn() {
        return DatabaseManager.getInstance().getConnection();
    }
    
    // Get all active bank accounts
    public List<BankDetails> findActive() {
        List<BankDetails> list = new ArrayList<>();
        String sql = "SELECT * FROM bank_details WHERE is_active = TRUE ORDER BY bank_name";
        try {
            PreparedStatement stmt = getConn().prepareStatement(sql);
            ResultSet rs = stmt.executeQuery();
            while (rs.next()) {
                BankDetails b = new BankDetails();
                b.setId(rs.getInt("id"));
                b.setBankName(rs.getString("bank_name"));
                b.setAccountName(rs.getString("account_name"));
                b.setAccountNumber(rs.getString("account_number"));
                b.setActive(rs.getBoolean("is_active"));
                list.add(b);
            }
        } catch (SQLException e) {
            System.err.println("BankDetailsDAO findActive error: " + e.getMessage());
        }
        return list;
    }
}