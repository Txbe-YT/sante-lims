package com.santediagnostics.controllers;

import com.santediagnostics.dao.TestRequestDAO;
import com.santediagnostics.dao.TestTypeDAO;
import com.santediagnostics.models.TestType;
import com.santediagnostics.navigation.NavigationManager;
import com.santediagnostics.session.SessionManager;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.Label;
import javafx.scene.control.TableView;

public class TestCatalogController {

    @FXML private TableView<TestType> testCatalogTable;
    @FXML private Label statusLabel;

    private TestTypeDAO testTypeDAO = new TestTypeDAO();
    private TestRequestDAO testRequestDAO = new TestRequestDAO();
    private ObservableList<TestType> testList = FXCollections.observableArrayList();

    @FXML
    public void initialize() {
        loadCatalog();
    }

    private void loadCatalog() {
        testList.clear();
        testList.addAll(testTypeDAO.findAll());
        testCatalogTable.setItems(testList);
    }

    @FXML
    private void handleOrderTest() {
        TestType selectedTest = testCatalogTable.getSelectionModel().getSelectedItem();
        
        if (selectedTest == null) {
            statusLabel.setStyle("-fx-text-fill: #e53e3e;");
            statusLabel.setText("Please select a test from the table first.");
            return;
        }

        // 1. Create the request and get the ID
        int requestId = testRequestDAO.createRequestAndGetId(
                SessionManager.getInstance().getCurrentUserId(), 
                selectedTest.getId()
        );
        
        if (requestId != -1) {
            // NEW NAVIGATION LOGIC:
            System.out.println("Order created. Navigating to Payment.fxml..."); // Debugging check
            
            // Pass the data to the navigation manager
            NavigationManager.getInstance().setParameter("requestId", requestId);
            NavigationManager.getInstance().setParameter("amount", selectedTest.getPrice());
            
            // Switch screens
            NavigationManager.getInstance().navigateTo("Payment.fxml", "Make Payment");
            
        } else {
            statusLabel.setStyle("-fx-text-fill: #e53e3e;");
            statusLabel.setText("Failed to create the test request.");
        }
    }

    @FXML
    private void goToDashboard() {
        NavigationManager.getInstance().navigateTo(
                NavigationManager.CUSTOMER_DASHBOARD,
                "My Dashboard"
        );
    }
}