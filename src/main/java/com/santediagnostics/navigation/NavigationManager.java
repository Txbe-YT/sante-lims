package com.santediagnostics.navigation;

import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

public class NavigationManager {

    private static NavigationManager instance;
    private Stage primaryStage;
    
    // ADDED: A secure map to pass data (like tokens) between screens
    private Map<String, Object> parameters = new HashMap<>();

    private NavigationManager() {}

    public static NavigationManager getInstance() {
        if (instance == null) {
            instance = new NavigationManager();
        }
        return instance;
    }

    public void setPrimaryStage(Stage stage) {
        this.primaryStage = stage;
    }

    public Stage getPrimaryStage() {
        return primaryStage;
    }

    // ADDED: Methods to save and retrieve cross-screen parameters
    public void setParameter(String key, Object value) {
        parameters.put(key, value);
    }

    public Object getParameter(String key) {
        return parameters.get(key);
    }

    public void navigateTo(String fxmlFile) {
        try {
            FXMLLoader loader = new FXMLLoader(
                    getClass().getResource("/views/" + fxmlFile)
            );
            Parent root = loader.load();
            Scene scene = new Scene(root);
            primaryStage.setScene(scene);
            primaryStage.show();
        } catch (IOException e) {
            System.err.println("Navigation error: " + e.getMessage());
        }
    }

    public void navigateTo(String fxmlFile, String title) {
        navigateTo(fxmlFile);
        primaryStage.setTitle(title);
    }

    // Screen name constants
    public static final String LOGIN = "Login.fxml";
    public static final String FORCE_PASSWORD_CHANGE = "ForcePasswordChange.fxml";
    public static final String SUPER_ADMIN_DASHBOARD = "SuperAdminDashboard.fxml";
    public static final String LAB_ATTENDANT_DASHBOARD = "LabAttendantDashboard.fxml";
    public static final String CUSTOMER_DASHBOARD = "CustomerDashboard.fxml";
    public static final String TEST_BUILDER = "TestBuilder.fxml";
    public static final String AUDIT_TRAIL = "AuditTrail.fxml";
    public static final String TEST_REQUEST_QUEUE = "TestRequestQueue.fxml";
    public static final String USER_MANAGEMENT = "UserManagement.fxml";
}