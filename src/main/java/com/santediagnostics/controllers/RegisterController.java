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
    @FXML private Button registerButton;

    private UserDAO userDAO = new UserDAO();
    private EmailServcie emailService = new EmailService();

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

        String verificationToken = TokenGenerator.generate();

        // Daudu will add email verification here
        boolean success = userDAO.registerCustomerWithToken(fullName, email, password, verificationToken);

        if (success) {
            emailService.sendVerificationEmail(email, fullName, verificationToken);
            
            successLabel.setText("Account created! A verification email has been sent to " + email + 
                                 "\nPlease check your inbox to verify your account.");

            successLabel.setStyle("-fx-text-fill: #38a169;");
            fullNameField.clear();
            emailField.clear();
            passwordField.clear();
            confirmPasswordField.clear();

            registerButton.setDisable(true);

            showEmailVerificationInfo();
        } else {
            errorLabel.setText("Registration failed. Please try again.");
        }
    }

    private void showEmailVerificationInfo() {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle("Email Verification Required");
        alert.setHeaderText("Account Created Successfully!");
        alert.setContentText("A verification link has been sent to your email address.\n\n" +
                            "Please check your inbox and click the verification link to activate your account.\n\n" +
                            "After verification, you can log in to the system.");
        alert.showAndWait();
    }

    @FXML
    private void goToLogin() {
        NavigationManager.getInstance().navigateTo(
                NavigationManager.LOGIN,
                "Sante Diagnostics LIMS — Login"
        );
    }
}