package com.santediagnostics.controllers;

import com.santediagnostics.dao.TestRequestDAO;
import com.santediagnostics.navigation.NavigationManager;
import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.scene.control.*;

public class PaymentController {

    @FXML private Label requestIdLabel;
    @FXML private Label amountLabel;
    @FXML private TextField paymentRefField;
    @FXML private Label statusLabel;

    private TestRequestDAO testRequestDAO = new TestRequestDAO();
    private int currentRequestId = -1;

    @FXML
    public void initialize() {
        // Load the specific request passed from TestCatalog
        Object passedIdObj = NavigationManager.getInstance().getParameter("requestId");
        Object passedAmountObj = NavigationManager.getInstance().getParameter("amount");
        
        if (passedIdObj != null && passedAmountObj != null) {
            currentRequestId = (int) passedIdObj;
            double amount = (double) passedAmountObj;
            requestIdLabel.setText("Request ID: #" + currentRequestId);
            amountLabel.setText(String.format("Total Due: ₦%,.2f", amount));
        } else {
            statusLabel.setText("No pending payment found.");
            statusLabel.setStyle("-fx-text-fill: #e53e3e;");
        }
    }

    @FXML
    private void handlePaymentConfirmation() {
        if (currentRequestId == -1) {
            statusLabel.setText("No pending payment to confirm.");
            return;
        }
        
        String ref = paymentRefField.getText().trim();
        if (ref.isEmpty()) {
            statusLabel.setStyle("-fx-text-fill: #e53e3e;");
            statusLabel.setText("Please enter the transaction reference.");
            return;
        }
        
        boolean success = testRequestDAO.updatePaymentReference(currentRequestId, ref);
        
        if (success) {
            statusLabel.setStyle("-fx-text-fill: #38a169;");
            statusLabel.setText("Payment reference recorded!");
            
            // Navigate back to dashboard after saving
            Platform.runLater(() -> 
                NavigationManager.getInstance().navigateTo(
                    "CustomerDashboard.fxml", "My Dashboard"
                )
            );
        } else {
            statusLabel.setStyle("-fx-text-fill: #e53e3e;");
            statusLabel.setText("Failed to record payment.");
        }
    }

    @FXML
    private void goBack() {
        NavigationManager.getInstance().navigateTo("TestCatalog.fxml", "Order Tests");
    }
}