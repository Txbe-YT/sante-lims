package com.santediagnostics.controllers;

import com.santediagnostics.dao.TestTypeDAO;
import com.santediagnostics.dao.TestRequestDAO;
import com.santediagnostics.models.TestType;
import com.santediagnostics.models.TestRequest;
import com.santediagnostics.navigation.NavigationManager;
import com.santediagnostics.session.SessionManager;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;

public class TestCatalog {

    @FXML private TableView<TestType> testTypesTable;
    @FXML private TableColumn<TestType, String> nameColumn;
    @FXML private TableColumn<TestType, String> categoryColumn;
    @FXML private TableColumn<TestType, Double> priceColumn;
    @FXML private TableColumn<TestType, Integer> tatColumn;
    @FXML private TextField searchField;
    @FXML private ComboBox<String> categoryFilter;
    @FXML private Label welcomeLabel;
    @FXML private Label statusLabel;

    private TestTypeDAO testTypeDAO = new TestTypeDAO();
    private TestRequestDAO testRequestDAO = new TestRequestDAO();
    private ObservableList<TestType> testTypeList = FXCollections.observableArrayList();

    @FXML
    public void initialize() {
        String name = SessionManager.getInstance().getCurrentUser().getFullName();
        welcomeLabel.setText("Welcome, " + name + "! Browse Available Tests");

        nameColumn.setCellValueFactory(new PropertyValueFactory<>("name"));
        categoryColumn.setCellValueFactory(new PropertyValueFactory<>("category"));
        priceColumn.setCellValueFactory(new PropertyValueFactory<>("price"));
        tatColumn.setCellValueFactory(new PropertyValueFactory<>("tatHours"));

        categoryFilter.setItems(FXCollections.observableArrayList("All", "Blood", "Imaging", "Biopsy", "Other"));
        categoryFilter.getSelectionModel().selectFirst();

        loadTests();
    }

    private void loadTests() {
        testTypeList.clear();
        String selectedCategory = categoryFilter.getValue();
        
        if (selectedCategory == null || selectedCategory.equals("All")) {
            testTypeList.addAll(testTypeDAO.findAll());
        } else {
            testTypeList.addAll(testTypeDAO.findByCategory(selectedCategory));
        }
        
        testTypesTable.setItems(testTypeList);
    }

    @FXML
    private void handleSearch() {
        String query = searchField.getText().trim().toLowerCase();
        if (query.isEmpty()) {
            loadTests();
            return;
        }
        
        testTypeList.clear();
        testTypeDAO.findAll().stream()
            .filter(t -> t.getName().toLowerCase().contains(query) || 
                        t.getCategory().toLowerCase().contains(query))
            .forEach(testTypeList::add);
        testTypesTable.setItems(testTypeList);
    }

    @FXML
    private void handleFilter() {
        loadTests();
    }

    @FXML
    private void handleOrder() {
        TestType selected = testTypesTable.getSelectionModel().getSelectedItem();
        
        if (selected == null) {
            statusLabel.setStyle("-fx-text-fill: #e53e3e;");
            statusLabel.setText("Please select a test to order.");
            return;
        }

        int customerId = SessionManager.getInstance().getCurrentUserId();
        boolean success = testRequestDAO.createRequest(customerId, selected.getId());

        if (success) {
            statusLabel.setStyle("-fx-text-fill: #38a169;");
            statusLabel.setText("Test ordered successfully! Redirecting to payment...");
            
            // Navigate to payment screen
            NavigationManager.getInstance().navigateTo(
                    "PaymentScreen.fxml",
                    "Complete Payment"
            );
        } else {
            statusLabel.setStyle("-fx-text-fill: #e53e3e;");
            statusLabel.setText("Failed to place order. Please try again.");
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