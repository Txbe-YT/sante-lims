package com.santediagnostics.controllers;

import com.santediagnostics.audit.AuditService;
import com.santediagnostics.dao.SampleDAO;
import com.santediagnostics.models.Sample;
import com.santediagnostics.navigation.NavigationManager;
import com.santediagnostics.session.SessionManager;
import javafx.fxml.FXML;
import javafx.scene.control.Label;
import java.util.List;

public class LabAttendantDashboardController {

    @FXML private Label welcomeLabel;
    @FXML private Label pendingCollectionsLabel;
    @FXML private Label processingLabel;
    @FXML private Label awaitingVerificationLabel;

    private SampleDAO sampleDAO = new SampleDAO();

    @FXML
    public void initialize() {
        String name = SessionManager.getInstance().getCurrentUser().getFullName();
        welcomeLabel.setText("Welcome, " + name);
        
        // Dynamically compute and display live lab statistics
        loadDashboardStats();
    }

    private void loadDashboardStats() {
        try {
            List<Sample> allSamples = sampleDAO.findAll();
            
            long pendingCollections = allSamples.stream()
                    .filter(s -> "PENDING_COLLECTION".equals(s.getStatus()))
                    .count();
                    
            long processing = allSamples.stream()
                    .filter(s -> "COLLECTED".equals(s.getStatus()) || "PROCESSING".equals(s.getStatus()))
                    .count();
                    
            long awaitingVerification = allSamples.stream()
                    .filter(s -> "AWAITING_VALIDATION".equals(s.getStatus()))
                    .count();

            // Safely assign values if the UI labels are present
            if (pendingCollectionsLabel != null) {
                pendingCollectionsLabel.setText(String.valueOf(pendingCollections));
            }
            if (processingLabel != null) {
                processingLabel.setText(String.valueOf(processing));
            }
            if (awaitingVerificationLabel != null) {
                awaitingVerificationLabel.setText(String.valueOf(awaitingVerification));
            }
        } catch (Exception e) {
            System.err.println("Error loading dashboard metrics: " + e.getMessage());
        }
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