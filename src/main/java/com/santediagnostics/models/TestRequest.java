package com.santediagnostics.models;

import java.time.LocalDateTime;

public class TestRequest {

    private int id;
    private int customerId;
    private int testTypeId;
    private String status;
    private String paymentStatus;
    private String paymentReference;
    private LocalDateTime orderedAt;
    private LocalDateTime paidAt;
    private int markedPaidBy;

    // Extra fields for display purposes
    private String customerName;
    private String testTypeName;
    private double testPrice;
    private int tatHours;

    public TestRequest() {}

    public TestRequest(int id, int customerId, int testTypeId, String status, String paymentStatus) {
        this.id = id;
        this.customerId = customerId;
        this.testTypeId = testTypeId;
        this.status = status;
        this.paymentStatus = paymentStatus;
    }

    public int getId() { return id; }
    public void setId(int id) { this.id = id; }

    public int getCustomerId() { return customerId; }
    public void setCustomerId(int customerId) { this.customerId = customerId; }

    public int getTestTypeId() { return testTypeId; }
    public void setTestTypeId(int testTypeId) { this.testTypeId = testTypeId; }

    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }

    public String getPaymentStatus() { return paymentStatus; }
    public void setPaymentStatus(String paymentStatus) { this.paymentStatus = paymentStatus; }

    public String getPaymentReference() { return paymentReference; }
    public void setPaymentReference(String paymentReference) { this.paymentReference = paymentReference; }

    public LocalDateTime getOrderedAt() { return orderedAt; }
    public void setOrderedAt(LocalDateTime orderedAt) { this.orderedAt = orderedAt; }

    public LocalDateTime getPaidAt() { return paidAt; }
    public void setPaidAt(LocalDateTime paidAt) { this.paidAt = paidAt; }

    public int getMarkedPaidBy() { return markedPaidBy; }
    public void setMarkedPaidBy(int markedPaidBy) { this.markedPaidBy = markedPaidBy; }

    public String getCustomerName() { return customerName; }
    public void setCustomerName(String customerName) { this.customerName = customerName; }

    public String getTestTypeName() { return testTypeName; }
    public void setTestTypeName(String testTypeName) { this.testTypeName = testTypeName; }

    public double getTestPrice() { return testPrice; }
    public void setTestPrice(double testPrice) { this.testPrice = testPrice; }

    public int getTatHours() { return tatHours; }
    public void setTatHours(int tatHours) { this.tatHours = tatHours; }

    @Override
    public String toString() {
        return "Request #" + id + " - " + testTypeName + " [" + paymentStatus + "]";
    }
}