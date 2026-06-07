package com.santediagnostics.controllers;

import com.santediagnostics.audit.AuditService;
import com.santediagnostics.dao.UserDAO;
import com.santediagnostics.models.User;
import com.santediagnostics.navigation.NavigationManager;
import com.santediagnostics.session.SessionManager;
import javafx.fxml.FXML;
import javafx.scene.control.*;

public class LoginController {

    @FXML private TextField emailField;
    @FXML private PasswordField passwordField;
    @FXML private Label errorLabel;
    @FXML private Button loginButton;

    private UserDAO userDAO = new UserDAO();

    @FXML
    private void handleLogin() {
        String email = emailField.getText().trim();
        String password = passwordField.getText();

        // Basic validation
        if (email.isEmpty() || password.isEmpty()) {
            errorLabel.setText("Please enter your email and password.");
            return;
        }

        // Find user by email
        User user = userDAO.findByEmail(email);

        if (user == null) {
            errorLabel.setText("No account found with that email.");
            return;
        }

        // Check password
        if (!userDAO.checkPassword(password, user.getPasswordHash())) {
            errorLabel.setText("Incorrect password. Please try again.");
            return;
        }

        // Check email verification (customers only)
        if (user.getRole().equals("CUSTOMER") && !user.isVerified()) {
            errorLabel.setText("Please verify your email before logging in.");
            return;
        }

        // Login successful — save to session
        SessionManager.getInstance().login(user);

        // Log the action
        AuditService.getInstance().log("USER_LOGIN", "users",
                user.getId(), user.getFullName() + " logged in.");

        // Check if force password change is required
        if (user.isForcePasswordChange()) {
            NavigationManager.getInstance().navigateTo(
                    NavigationManager.FORCE_PASSWORD_CHANGE,
                    "Change Your Password"
            );
            return;
        }

        // Route to correct dashboard based on role
        navigateToDashboard(user.getRole());
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

    @FXML
    private void handleRegister() {
        // Daudu's screen — navigate to registration
        NavigationManager.getInstance().navigateTo(
                "Register.fxml",
                "Create Account"
        );
    }
}