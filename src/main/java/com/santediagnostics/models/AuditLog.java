package com.santediagnostics.models;

import java.time.LocalDateTime;

public class AuditLog {

    private int id;
    private int userId;
    private String action;
    private String targetTable;
    private int targetId;
    private String details;
    private LocalDateTime performedAt;

    // Extra field for display
    private String userFullName;

    public AuditLog() {}

    public AuditLog(int userId, String action, String targetTable, int targetId, String details) {
        this.userId = userId;
        this.action = action;
        this.targetTable = targetTable;
        this.targetId = targetId;
        this.details = details;
    }

    public int getId() { return id; }
    public void setId(int id) { this.id = id; }

    public int getUserId() { return userId; }
    public void setUserId(int userId) { this.userId = userId; }

    public String getAction() { return action; }
    public void setAction(String action) { this.action = action; }

    public String getTargetTable() { return targetTable; }
    public void setTargetTable(String targetTable) { this.targetTable = targetTable; }

    public int getTargetId() { return targetId; }
    public void setTargetId(int targetId) { this.targetId = targetId; }

    public String getDetails() { return details; }
    public void setDetails(String details) { this.details = details; }

    public LocalDateTime getPerformedAt() { return performedAt; }
    public void setPerformedAt(LocalDateTime performedAt) { this.performedAt = performedAt; }

    public String getUserFullName() { return userFullName; }
    public void setUserFullName(String userFullName) { this.userFullName = userFullName; }

    @Override
    public String toString() {
        return "[" + performedAt + "] " + userFullName + " - " + action;
    }
}