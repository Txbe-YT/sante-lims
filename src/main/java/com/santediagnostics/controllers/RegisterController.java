package com.santediagnostics.controllers;

import com.santediagnostics.dao.UserDAO;
import com.santediagnostics.navigation.NavigationManager;
import javafx.fxml.FXML;
import javafx.scene.control.*;

public class RegisterController {

    @FXML private TextField fullNameField;
    @FXML private TextField emailField;
    @FXML private PasswordField passwordField;
    @FXML private PasswordField confirmPasswordField;
    @FXML private Label errorLabel;
    @FXML private Label successLabel;

    private UserDAO userDAO = new UserDAO();

    @FXML
    private void handleRegister() {
        errorLabel.setText("");
        successLabel.setText("");

        String fullName = fullNameField.getText().trim();
        String email = emailField.getText().trim();
        String password = passwordField.getText();
        String confirm = confirmPasswordField.getText();

        if (fullName.isEmpty() || email.isEmpty() || password.isEmpty()) {
            errorLabel.setText("Please fill in all fields.");
            return;
        }

        if (!email.contains("@")) {
            errorLabel.setText("Please enter a valid email address.");
            return;
        }

        if (password.length() < 6) {
            errorLabel.setText("Password must be at least 6 characters.");
            return;
        }

        if (!password.equals(confirm)) {
            errorLabel.setText("Passwords do not match.");
            return;
        }

        if (userDAO.findByEmail(email) != null) {
            errorLabel.setText("An account with this email already exists.");
            return;
        }

        // Daudu will add email verification here
        boolean success = userDAO.registerCustomer(fullName, email, password);

        if (success) {
            successLabel.setText("Account created! Please verify your email before logging in. (Daudu will implement email verification)");
            fullNameField.clear();
            emailField.clear();
            passwordField.clear();
            confirmPasswordField.clear();
        } else {
            errorLabel.setText("Registration failed. Please try again.");
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