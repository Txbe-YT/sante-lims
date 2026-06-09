package com.santediagnostics.models;

/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */

/**
 *
 * @author dasil
 */

import java.time.LocalDateTime;

public class Sample {
    private int id;
    private int testRequestId;
    private String status;
    private LocalDateTime collectedAt;
    private LocalDateTime processedAt;
    private LocalDateTime validatedAt;
    private int updatedBy;
    private int customerId;
    


    // Extra fields for UI display
    private String testTypeName;
    private String customerName;

    public Sample() {}

    public int getId() { return id; }
    public void setId(int id) { this.id = id; }

    public int getTestRequestId() { return testRequestId; }
    public void setTestRequestId(int testRequestId) { this.testRequestId = testRequestId; }

    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }

    public LocalDateTime getCollectedAt() { return collectedAt; }
    public void setCollectedAt(LocalDateTime collectedAt) { this.collectedAt = collectedAt; }

    public LocalDateTime getProcessedAt() { return processedAt; }
    public void setProcessedAt(LocalDateTime processedAt) { this.processedAt = processedAt; }

    public LocalDateTime getValidatedAt() { return validatedAt; }
    public void setValidatedAt(LocalDateTime validatedAt) { this.validatedAt = validatedAt; }

    public int getUpdatedBy() { return updatedBy; }
    public void setUpdatedBy(int updatedBy) { this.updatedBy = updatedBy; }

    public String getTestTypeName() { return testTypeName; }
    public void setTestTypeName(String testTypeName) { this.testTypeName = testTypeName; }

    private int getCustomerId(){return customerId;}
    public void setCustomerId(int customerId) { this.customerId = customerId; }

    public String getCustomerName() { return customerName; }
    public void setCustomerName(String customerName) { this.customerName = customerName; }

    // Helper to show the most recent timestamp in the UI
    public LocalDateTime getLastActionTime() {
        if (validatedAt != null) return validatedAt;
        if (processedAt != null) return processedAt;
        if (collectedAt != null) return collectedAt;
        return null;
    }
}