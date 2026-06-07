package com.santediagnostics.models;

public class BankDetails {

    private int id;
    private String bankName;
    private String accountName;
    private String accountNumber;
    private boolean isActive;

    public BankDetails() {}

    public BankDetails(int id, String bankName, String accountName, String accountNumber) {
        this.id = id;
        this.bankName = bankName;
        this.accountName = accountName;
        this.accountNumber = accountNumber;
    }

    public int getId() { return id; }
    public void setId(int id) { this.id = id; }

    public String getBankName() { return bankName; }
    public void setBankName(String bankName) { this.bankName = bankName; }

    public String getAccountName() { return accountName; }
    public void setAccountName(String accountName) { this.accountName = accountName; }

    public String getAccountNumber() { return accountNumber; }
    public void setAccountNumber(String accountNumber) { this.accountNumber = accountNumber; }

    public boolean isActive() { return isActive; }
    public void setActive(boolean active) { isActive = active; }

    @Override
    public String toString() {
        return bankName + " - " + accountName + " (" + accountNumber + ")";
    }
}