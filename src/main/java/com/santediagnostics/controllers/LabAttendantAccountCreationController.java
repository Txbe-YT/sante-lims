package com.santediagnostics.controllers;

/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */

/**
 *
 * @author dasil
 */

import com.santediagnostics.audit.AuditService;
import com.santediagnostics.dao.UserDAO;
import com.santediagnostics.navigation.NavigationManager;
import com.santediagnostics.session.SessionManager;
import javafx.fxml.FXML;
import javafx.scene.control.*;

public class LabAttendantAccountCreationController {
    @FXML private TextField fullNameField;
    @FXML private TextField emailField;
    @FXML private PasswordField passwordField;
    @FXML private Label statusLabel;

    private UserDAO userDAO = new UserDAO();

    @FXML
    private void handleCreatePatient() {
        String fullName = fullNameField.getText().trim();
        String email = emailField.getText().trim();
        String password = passwordField.getText();

        if (fullName.isEmpty() || email.isEmpty() || password.isEmpty()) {
            statusLabel.setStyle("-fx-text-fill: #e53e3e;");
            statusLabel.setText("Please fill in all fields.");
            return;
        }

        if (userDAO.findByEmail(email) != null) {
            statusLabel.setStyle("-fx-text-fill: #e53e3e;");
            statusLabel.setText("An account with this email already exists.");
            return;
        }

        int createdBy = SessionManager.getInstance().getCurrentUserId();
        // Create user with CUSTOMER role
        boolean success = userDAO.createUser(fullName, email, password, "CUSTOMER", createdBy);

        if (success) {
            AuditService.getInstance().log("PATIENT_CREATED", "users", -1, "Lab Attendant created patient account for " + fullName);
            statusLabel.setStyle("-fx-text-fill: #38a169;");
            statusLabel.setText("Patient account created! They must change their password on first login.");
            fullNameField.clear();
            emailField.clear();
            passwordField.clear();
        } else {
            statusLabel.setStyle("-fx-text-fill: #e53e3e;");
            statusLabel.setText("Failed to create account.");
        }
    }

    @FXML
    private void goBack() {
        NavigationManager.getInstance().navigateTo(NavigationManager.LAB_ATTENDANT_DASHBOARD, "Lab Attendant Dashboard");
    }
}