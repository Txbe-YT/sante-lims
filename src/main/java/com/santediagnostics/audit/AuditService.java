package com.santediagnostics.audit;

import com.santediagnostics.db.DatabaseManager;
import com.santediagnostics.session.SessionManager;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;

public class AuditService {

    private static AuditService instance;

    private AuditService() {}

    public static AuditService getInstance() {
        if (instance == null) {
            instance = new AuditService();
        }
        return instance;
    }

    public void log(String action, String targetTable, int targetId, String details) {
        int userId = SessionManager.getInstance().getCurrentUserId();
        logWithUser(userId, action, targetTable, targetId, details);
    }

    public void log(String action, String details) {
        log(action, null, -1, details);
    }


    public void logWithUser(int userId, String action, String targetTable, int targetId, String details) {
        String sql = "INSERT INTO audit_log (user_id, action, target_table, target_id, details) " +
                "VALUES (?, ?, ?, ?, ?)";

        try {
            Connection conn = DatabaseManager.getInstance().getConnection();
            PreparedStatement stmt = conn.prepareStatement(sql);
            stmt.setInt(1, userId);
            stmt.setString(2, action);
            stmt.setString(3, targetTable);

            if (targetId == -1) {
                stmt.setNull(4, java.sql.Types.INTEGER);
            } else {
                stmt.setInt(4, targetId);
            }

            stmt.setString(5, details);
            stmt.executeUpdate();

        } catch (SQLException e) {
            System.err.println("Audit log failed: " + e.getMessage());
        }
    }
}