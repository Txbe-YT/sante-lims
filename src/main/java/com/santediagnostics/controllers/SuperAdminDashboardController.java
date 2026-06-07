package com.santediagnostics.controllers;

import com.santediagnostics.audit.AuditService;
import com.santediagnostics.navigation.NavigationManager;
import com.santediagnostics.session.SessionManager;
import javafx.fxml.FXML;
import javafx.scene.control.Label;

public class SuperAdminDashboardController {

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
    private void goToUserManagement() {
        NavigationManager.getInstance().navigateTo(
                NavigationManager.USER_MANAGEMENT,
                "User Management"
        );
    }

    @FXML
    private void goToTestBuilder() {
        NavigationManager.getInstance().navigateTo(
                NavigationManager.TEST_BUILDER,
                "Test Builder"
        );
    }

    @FXML
    private void goToAuditTrail() {
        NavigationManager.getInstance().navigateTo(
                NavigationManager.AUDIT_TRAIL,
                "Audit Trail"
        );
    }

    @FXML
    private void goToTestRequests() {
        NavigationManager.getInstance().navigateTo(
                NavigationManager.TEST_REQUEST_QUEUE,
                "Test Request Queue"
        );
    }
}