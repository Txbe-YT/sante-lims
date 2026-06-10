package com.santediagnostics.controllers;

import com.santediagnostics.audit.AuditService;
import com.santediagnostics.dao.ResultDAO;
import com.santediagnostics.dao.TestRequestDAO;
import com.santediagnostics.models.Result;
import com.santediagnostics.models.TestRequest;
import com.santediagnostics.navigation.NavigationManager;
import com.santediagnostics.session.SessionManager;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.collections.transformation.FilteredList;

import java.awt.Desktop;
import java.io.File;

public class CustomerDashboardController {

    @FXML private Label welcomeLabel;
    @FXML private Label activeTestsCount;
    @FXML private Label pendingPaymentCount;
    @FXML private Label completedTestsCount;
    
    @FXML private TableView<TestRequest> testRequestsTable;
    @FXML private TableColumn<TestRequest, Void> actionsColumn;
    
    @FXML private ComboBox<String> statusFilter;
    @FXML private ComboBox<String> paymentFilter;
    @FXML private TextField searchField;
    
    private TestRequestDAO testRequestDAO = new TestRequestDAO();
    private ObservableList<TestRequest> masterData = FXCollections.observableArrayList();
    private FilteredList<TestRequest> filteredData = new FilteredList<>(masterData, p -> true);

    @FXML
    public void initialize() {
        welcomeLabel.setText("Welcome, " + SessionManager.getInstance().getCurrentUser().getFullName());
        
        setupTimeRemainingColumn();
        setupActionsColumn();
        
        if(statusFilter != null) {
            statusFilter.setItems(FXCollections.observableArrayList("All Status", "Pending", "Processing", "Completed"));
            statusFilter.getSelectionModel().selectFirst();
            statusFilter.setOnAction(e -> applyFilters());
        }
        if(paymentFilter != null) {
            paymentFilter.setItems(FXCollections.observableArrayList("All Payment", "Paid", "Pending"));
            paymentFilter.getSelectionModel().selectFirst();
            paymentFilter.setOnAction(e -> applyFilters());
        }
        if(searchField != null) {
            searchField.textProperty().addListener((obs, oldV, newV) -> applyFilters());
        }

        testRequestsTable.setItems(filteredData);
        loadDashboardData();
    }

    private void loadDashboardData() {
        int userId = SessionManager.getInstance().getCurrentUserId();
        
        int pending = testRequestDAO.countByCustomerIdAndStatus(userId, "status", "PENDING");
        int processing = testRequestDAO.countByCustomerIdAndStatus(userId, "status", "PROCESSING");
        int awaiting = testRequestDAO.countByCustomerIdAndStatus(userId, "status", "AWAITING_VALIDATION");
        int validated = testRequestDAO.countByCustomerIdAndStatus(userId, "status", "VALIDATED");
        int complete = testRequestDAO.countByCustomerIdAndStatus(userId, "status", "COMPLETE");
        
        activeTestsCount.setText(String.valueOf(pending + processing + awaiting));
        pendingPaymentCount.setText(String.valueOf(testRequestDAO.countByCustomerIdAndStatus(userId, "payment_status", "UNPAID")));
        completedTestsCount.setText(String.valueOf(validated + complete));
        
        masterData.setAll(testRequestDAO.findByCustomerId(userId));
    }

    private void applyFilters() {
        String selectedStatus = statusFilter.getValue();
        String selectedPayment = paymentFilter.getValue();
        String searchText = searchField.getText() == null ? "" : searchField.getText().toLowerCase().trim();

        filteredData.setPredicate(request -> {
            // 1. Safe Mapping of Database Constraints to Dropdown View States
            if (selectedStatus != null && !selectedStatus.equals("All Status")) {
                String currentStatus = request.getStatus();
                if (selectedStatus.equals("Pending")) {
                    if (!"PENDING".equalsIgnoreCase(currentStatus)) return false;
                } else if (selectedStatus.equals("Processing")) {
                    if (!"PROCESSING".equalsIgnoreCase(currentStatus) && 
                        !"COLLECTED".equalsIgnoreCase(currentStatus) && 
                        !"AWAITING_VALIDATION".equalsIgnoreCase(currentStatus)) return false;
                } else if (selectedStatus.equals("Completed")) {
                    if (!"VALIDATED".equalsIgnoreCase(currentStatus) && 
                        !"COMPLETE".equalsIgnoreCase(currentStatus)) return false;
                }
            }

            // 2. Payment Dropdown Filter
            if (selectedPayment != null && !selectedPayment.equals("All Payment")) {
                String currentPayment = request.getPaymentStatus();
                if (selectedPayment.equals("Paid") && !"PAID".equalsIgnoreCase(currentPayment)) return false;
                if (selectedPayment.equals("Pending") && !"UNPAID".equalsIgnoreCase(currentPayment)) return false;
            }

            // 3. Search Bar Filter
            if (!searchText.isEmpty()) {
                boolean matchesName = request.getTestTypeName() != null && request.getTestTypeName().toLowerCase().contains(searchText);
                boolean matchesId = String.valueOf(request.getId()).contains(searchText);
                if (!matchesName && !matchesId) return false;
            }

            return true;
        });
    }

    private void setupTimeRemainingColumn(){
        if (testRequestsTable == null) return;
        TableColumn<TestRequest, String> timeRemainingCol = new TableColumn<>("Time Remaining");
        timeRemainingCol.setCellValueFactory(cellData -> new SimpleStringProperty(cellData.getValue().getTimeRemainingFormatted()));
        timeRemainingCol.setPrefWidth(130);
        timeRemainingCol.setCellFactory(col -> new CountdownTimerCell());
        testRequestsTable.getColumns().add(timeRemainingCol);
    }

    private void setupActionsColumn() {
        if (actionsColumn == null) return;
        actionsColumn.setCellFactory(param -> new TableCell<TestRequest, Void>() {
            private final Button viewBtn = new Button("View Result");
            {
                viewBtn.setStyle("-fx-background-color: #0F7173; -fx-text-fill: white; -fx-cursor: hand; -fx-background-radius: 5;");
                viewBtn.setOnAction(event -> {
                    TestRequest request = (TestRequest) getTableRow().getItem();
                    if (request == null) return;

                    ResultDAO resultDAO = new ResultDAO();
                    Result result = resultDAO.getVerifiedResultByTestRequest(request.getId()); // Looks up result
                    if (result != null && result.getFilePath() != null) {
                        try {
                            File pdfFile = new File(result.getFilePath());
                            if (pdfFile.exists()) Desktop.getDesktop().open(pdfFile);
                            else showAlert("File Not Found", "The result file could not be located.");
                        } catch (Exception e) { showAlert("Error", "Could not open the file."); }
                    } else { showAlert("Not Ready", "Result is not yet available."); }
                });
            }
            @Override
            protected void updateItem(Void item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || getTableRow() == null || getTableRow().getItem() == null) {
                    setGraphic(null);
                } else {
                    TestRequest request = (TestRequest) getTableRow().getItem();
                    // Resolves check constraint matching to unlock the view action button
                    if ("VALIDATED".equalsIgnoreCase(request.getStatus()) || "COMPLETE".equalsIgnoreCase(request.getStatus())) {
                        setGraphic(viewBtn);
                    } else {
                        setGraphic(null);
                    }
                }
            }
        });
    }

    private void showAlert(String title, String message) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }
    @FXML
    private void handleSearch() {
        applyFilters(); 
    }
    @FXML private void handleNewTestRequest() { NavigationManager.getInstance().navigateTo("TestCatalog.fxml", "Order New Test"); }
    @FXML private void handleViewHistory() { NavigationManager.getInstance().navigateTo("ResultVault.fxml", "My Results Vault"); }
    @FXML private void handleLogout() { SessionManager.getInstance().logout(); NavigationManager.getInstance().navigateTo(NavigationManager.LOGIN, "Login"); }
}