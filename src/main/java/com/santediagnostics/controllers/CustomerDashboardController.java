package com.santediagnostics.controllers;

import com.santediagnostics.audit.AuditService;
import com.santediagnostics.navigation.NavigationManager;
import com.santediagnostics.session.SessionManager;
import javafx.fxml.FXML;
import javafx.scene.control.Label;

public class CustomerDashboardController {

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
}