package com.santediagnostics.controllers;

import com.santediagnostics.audit.AuditService;
import com.santediagnostics.dao.UserDAO;
import com.santediagnostics.models.User;
import com.santediagnostics.navigation.NavigationManager;
import com.santediagnostics.session.SessionManager;
import javafx.fxml.FXML;
import javafx.scene.control.*;

public class ForcePasswordChangeController {

    @FXML private PasswordField newPasswordField;
    @FXML private PasswordField confirmPasswordField;
    @FXML private Label errorLabel;
    @FXML private Label successLabel;

    private UserDAO userDAO = new UserDAO();

    @FXML
    private void handlePasswordChange() {
        String newPassword = newPasswordField.getText();
        String confirmPassword = confirmPasswordField.getText();

        // Clear previous messages
        errorLabel.setText("");
        successLabel.setText("");

        // Validation
        if (newPassword.isEmpty() || confirmPassword.isEmpty()) {
            errorLabel.setText("Please fill in both fields.");
            return;
        }

        if (newPassword.length() < 6) {
            errorLabel.setText("Password must be at least 6 characters.");
            return;
        }

        if (!newPassword.equals(confirmPassword)) {
            errorLabel.setText("Passwords do not match.");
            return;
        }

        // Get current user
        User currentUser = SessionManager.getInstance().getCurrentUser();

        // Update password in database
        boolean success = userDAO.updatePassword(currentUser.getId(), newPassword);

        if (success) {
            // Log the action
            AuditService.getInstance().log(
                    "PASSWORD_CHANGED",
                    "users",
                    currentUser.getId(),
                    currentUser.getFullName() + " changed their password."
            );

            successLabel.setText("Password updated successfully! Redirecting...");

            // Update session user so force flag is cleared
            currentUser.setForcePasswordChange(false);

            // Small delay then navigate to dashboard
            new Thread(() -> {
                try {
                    Thread.sleep(1500);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
                javafx.application.Platform.runLater(() -> {
                    navigateToDashboard(currentUser.getRole());
                });
            }).start();

        } else {
            errorLabel.setText("Something went wrong. Please try again.");
        }
    }

    private void navigateToDashboard(String role) {
        switch (role) {
            case "SUPER_ADMIN":
                NavigationManager.getInstance().navigateTo(
                        NavigationManager.SUPER_ADMIN_DASHBOARD,
                        "Super Admin Dashboard"
                );
                break;
            case "LAB_ATTENDANT":
                NavigationManager.getInstance().navigateTo(
                        NavigationManager.LAB_ATTENDANT_DASHBOARD,
                        "Lab Attendant Dashboard"
                );
                break;
            case "CUSTOMER":
                NavigationManager.getInstance().navigateTo(
                        NavigationManager.CUSTOMER_DASHBOARD,
                        "My Dashboard"
                );
                break;
        }
    }
}