package com.santediagnostics.controllers;

import com.santediagnostics.dao.BankDetailsDAO;
import com.santediagnostics.dao.TestRequestDAO;
import com.santediagnostics.models.BankDetails;
import com.santediagnostics.models.TestRequest;
import com.santediagnostics.navigation.NavigationManager;
import com.santediagnostics.session.SessionManager;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.collections.FXCollections;

public class PaymentControl {

    @FXML private Label bankNameLabel;
    @FXML private Label accountNameLabel;
    @FXML private Label accountNumberLabel;
    @FXML private Label amountLabel;
    @FXML private Label requestIdLabel;
    @FXML private TextField paymentRefField;
    @FXML private Label statusLabel;
    @FXML private ComboBox<BankDetails> bankSelector;

    private BankDetailsDAO bankDetailsDAO = new BankDetailsDAO();
    private TestRequestDAO testRequestDAO = new TestRequestDAO();
    private TestRequest currentRequest;

    @FXML
    public void initialize() {
        // Load active bank accounts
        bankSelector.setItems(FXCollections.observableArrayList(bankDetailsDAO.findActive()));
        bankSelector.setCellFactory(lv -> new ListCell<BankDetails>() {
            @Override
            protected void updateItem(BankDetails item, boolean empty) {
                super.updateItem(item, empty);
                setText(empty ? null : item.getBankName());
            }
        });
        
        bankSelector.getSelectionModel().selectedItemProperty().addListener(
            (obs, oldVal, newVal) -> updateBankDisplay(newVal));
        
        // Get the most recent unpaid request for this customer
        loadCurrentRequest();
    }
    
    private void loadCurrentRequest() {
        int customerId = SessionManager.getInstance().getCurrentUserId();
        currentRequest = testRequestDAO.findLatestUnpaidByCustomer(customerId);
        
        if (currentRequest != null) {
            requestIdLabel.setText("Request #" + currentRequest.getId());
            amountLabel.setText(String.format("₦%,.2f", currentRequest.getTestPrice()));
        } else {
            statusLabel.setText("No pending payment found.");
            statusLabel.setStyle("-fx-text-fill: #e53e3e;");
        }
    }
    
    private void updateBankDisplay(BankDetails bank) {
        if (bank != null) {
            bankNameLabel.setText(bank.getBankName());
            accountNameLabel.setText(bank.getAccountName());
            accountNumberLabel.setText(bank.getAccountNumber());
        }
    }

    @FXML
    private void handlePaymentConfirmation() {
        if (currentRequest == null) {
            statusLabel.setText("No pending payment to confirm.");
            return;
        }
        
        String ref = paymentRefField.getText().trim();
        if (ref.isEmpty()) {
            statusLabel.setStyle("-fx-text-fill: #e53e3e;");
            statusLabel.setText("Please enter the payment reference number from your bank transfer.");
            return;
        }
        
        // For now, we're just recording the reference
        // In production, you'd validate with bank API
        boolean success = testRequestDAO.updatePaymentReference(currentRequest.getId(), ref);
        
        if (success) {
            statusLabel.setStyle("-fx-text-fill: #38a169;");
            statusLabel.setText("Payment reference recorded! The lab will verify your payment shortly.");
            
            // Navigate back to dashboard after delay
            new Thread(() -> {
                try { Thread.sleep(3000); } catch (InterruptedException e) {}
                javafx.application.Platform.runLater(() -> 
                    NavigationManager.getInstance().navigateTo(
                        NavigationManager.CUSTOMER_DASHBOARD,
                        "My Dashboard"
                    )
                );
            }).start();
        } else {
            statusLabel.setStyle("-fx-text-fill: #e53e3e;");
            statusLabel.setText("Failed to record payment. Please try again.");
        }
    }

    @FXML
    private void goBack() {
        NavigationManager.getInstance().navigateTo(
                "TestCatalog.fxml",
                "Order Tests"
        );
    }
} 
    

