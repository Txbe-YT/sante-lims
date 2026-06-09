package com.santediagnostics.controllers;

import com.santediagnostics.dao.UserDAO;
import com.santediagnostics.navigation.NavigationManager;
import javafx.fxml.FXML;
import javafx.scene.control.Label;

public class VerificationController {
    
    @FXML private Label statusLabel;
    @FXML private Label messageLabel;
    
    private UserDAO userDAO = new UserDAO();
    private String token;
    
    @FXML
    public void initialize() {
        // Token would be passed via navigation manager
        // For now, we'll simulate - you need to pass token from navigation
    }
    
    public void setToken(String token) {
        this.token = token;
        verifyEmail();
    }
    
    private void verifyEmail() {
        boolean success = userDAO.verifyEmail(token);
        
        if (success) {
            statusLabel.setText("✓ Email Verified Successfully!");
            statusLabel.setStyle("-fx-text-fill: #38a169; -fx-font-size: 14px; -fx-font-weight: bold;");
            messageLabel.setText("Your account has been activated. You can now log in to the system.");
        } else {
            statusLabel.setText("✗ Verification Failed");
            statusLabel.setStyle("-fx-text-fill: #e53e3e; -fx-font-size: 14px; -fx-font-weight: bold;");
            messageLabel.setText("The verification link is invalid or has expired. Please register again.");
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
