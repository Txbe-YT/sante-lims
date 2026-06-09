package com.santediagnostics.controllers;

import com.santediagnostics.audit.AuditService;
import com.santediagnostics.models.TestRequest;
import com.santediagnostics.navigation.NavigationManager;
import com.santediagnostics.session.SessionManager;
import javafx.fxml.FXML;
import javafx.scene.control.Label;
import javafx.scene.control.TableColumn;
import javafx.beans.property.SimpleStringProperty;

public class CustomerDashboardController {

    @FXML private Label welcomeLabel;

    @FXML
    public void initialize() {
        String name = SessionManager.getInstance().getCurrentUser().getFullName();
        welcomeLabel.setText("Welcome, " + name);
        setupTimeRemainingColumn();
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

    private void setupTimeRemainingColumn(){
        TableColumn<TestRequest, String> timeRemainingCol = new TableColumn<>("Time Remaining");
        timeRemainingCol.setCellValueFactory(cellData -> new SimpleStringProperty(cellData.getValue().getTimeRemainingFormatted()));
        timeRemainingCol.setPrefWidth(130);
        timeRemainingCol.setCellFactory(col -> new CountdownTimerCell());
    }
}