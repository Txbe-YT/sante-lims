package com.santediagnostics.controllers;

import com.santediagnostics.audit.AuditService;
import com.santediagnostics.navigation.NavigationManager;
import com.santediagnostics.session.SessionManager;
import javafx.fxml.FXML;
import javafx.scene.control.Label;

public class LabAttendantDashboardController {

    @FXML private Label welcomeLabel;

    @FXML
    public void initialize() {
        String name = SessionManager.getInstance().getCurrentUser().getFullName();
        welcomeLabel.setText("Welcome, " + name);
    }

    @FXML
    private void handleLogout() {
        AuditService.getInstance().log(
                "USER_LOGOUT",
                SessionManager.getInstance().getCurrentUser().getFullName() + " logged out."
        );
        SessionManager.getInstance().logout();
        NavigationManager.getInstance().navigateTo(
                NavigationManager.LOGIN,
                "Sante Diagnostics LIMS — Login"
        );
    }
    
    @FXML
    private void goToCreatePatient() {
        NavigationManager.getInstance().navigateTo(
                "LabAttendantAccountCreation.fxml", 
                "Create Patient Account"
        );
    }

    @FXML
    private void goToSampleTracking() {
        NavigationManager.getInstance().navigateTo(
                "SampleLifecycle.fxml", 
                "Sample Lifecycle Tracking"
        );
    }

    @FXML
    private void goToResults() {
        NavigationManager.getInstance().navigateTo(
                "ResultValidation.fxml", 
                "Result Upload & Validation"
        );
    }

    @FXML
    private void goToPaymentQueue() {
        NavigationManager.getInstance().navigateTo(
                "LabPaymentQueue.fxml", 
                "Lab Payment Queue"
        );
    }
}