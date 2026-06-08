/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.santediagnostics.models;

/**
 *
 * @author dasil
 */

import java.time.LocalDateTime;

public class Result {
    private int id;
    private int testRequestId;
    private String filePath; // Where FileStorageService saves the PDF/Image
    private boolean isVerified;
    private int uploadedBy;
    private int verifiedBy;
    private LocalDateTime uploadedAt;

    public Result() {}

    public int getId() { return id; }
    public void setId(int id) { this.id = id; }

    public int getTestRequestId() { return testRequestId; }
    public void setTestRequestId(int testRequestId) { this.testRequestId = testRequestId; }

    public String getFilePath() { return filePath; }
    public void setFilePath(String filePath) { this.filePath = filePath; }

    public boolean isVerified() { return isVerified; }
    public void setVerified(boolean verified) { isVerified = verified; }

    public int getUploadedBy() { return uploadedBy; }
    public void setUploadedBy(int uploadedBy) { this.uploadedBy = uploadedBy; }

    public int getVerifiedBy() { return verifiedBy; }
    public void setVerifiedBy(int verifiedBy) { this.verifiedBy = verifiedBy; }

    public LocalDateTime getUploadedAt() { return uploadedAt; }
    public void setUploadedAt(LocalDateTime uploadedAt) { this.uploadedAt = uploadedAt; }
    
    private String customerName;
    private String testName;

    public String getCustomerName() { return customerName; }
    public void setCustomerName(String customerName) { this.customerName = customerName; }

    public String getTestName() { return testName; }
    public void setTestName(String testName) { this.testName = testName; }
}
