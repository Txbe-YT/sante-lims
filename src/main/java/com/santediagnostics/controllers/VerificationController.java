package com.santediagnostics.controllers;

import com.santediagnostics.dao.UserDAO;
import com.santediagnostics.navigation.NavigationManager;
import javafx.fxml.FXML;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;

public class VerificationController {
    
    @FXML private TextField tokenField;
    @FXML private Label statusLabel;
    @FXML private Label messageLabel;
    
    private UserDAO userDAO = new UserDAO();
    
    @FXML
    public void initialize() {
        // Ready for user input
    }
    
    @FXML
    private void handleVerify() {
        String inputToken = tokenField.getText().trim();
        
        if (inputToken.isEmpty()) {
            statusLabel.setText("Please paste your token.");
            statusLabel.setStyle("-fx-text-fill: #e53e3e; -fx-font-size: 14px; -fx-font-weight: bold;");
            return;
        }
        
        boolean success = userDAO.verifyEmailByToken(inputToken);
        
        if (success) {
            statusLabel.setText("✓ Email Verified Successfully!");
            statusLabel.setStyle("-fx-text-fill: #38a169; -fx-font-size: 14px; -fx-font-weight: bold;");
            messageLabel.setText("Your account has been activated. You can now return to the login screen.");
            tokenField.setDisable(true); // Lock the field after success
        } else {
            statusLabel.setText("✗ Verification Failed");
            statusLabel.setStyle("-fx-text-fill: #e53e3e; -fx-font-size: 14px; -fx-font-weight: bold;");
            messageLabel.setText("The verification token is invalid or has expired.");
        }
    }
    
    @FXML
    private void goToLogin() {
        NavigationManager.getInstance().navigateTo(
                NavigationManager.LOGIN,
                "Sante Diagnostics LIMS — Login"
        );
    }
}