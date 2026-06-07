package com.santediagnostics.controllers;

import com.santediagnostics.audit.AuditService;
import com.santediagnostics.dao.TestRequestDAO;
import com.santediagnostics.models.TestRequest;
import com.santediagnostics.navigation.NavigationManager;
import com.santediagnostics.session.SessionManager;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;

import java.time.format.DateTimeFormatter;

public class TestRequestQueueController {

    @FXML private TableView<TestRequest> requestsTable;
    @FXML private TableColumn<TestRequest, Integer> idColumn;
    @FXML private TableColumn<TestRequest, String> customerColumn;
    @FXML private TableColumn<TestRequest, String> testColumn;
    @FXML private TableColumn<TestRequest, Double> priceColumn;
    @FXML private TableColumn<TestRequest, String> statusColumn;
    @FXML private TableColumn<TestRequest, String> paymentColumn;
    @FXML private TableColumn<TestRequest, String> orderedColumn;
    @FXML private ComboBox<String> filterComboBox;
    @FXML private TextField paymentRefField;
    @FXML private Label statusLabel;

    private TestRequestDAO testRequestDAO = new TestRequestDAO();
    private ObservableList<TestRequest> requestList = FXCollections.observableArrayList();

    @FXML
    public void initialize() {
        filterComboBox.setItems(FXCollections.observableArrayList(
                "All", "Unpaid Only", "Paid Only"
        ));
        filterComboBox.getSelectionModel().selectFirst();

        idColumn.setCellValueFactory(new PropertyValueFactory<>("id"));
        customerColumn.setCellValueFactory(new PropertyValueFactory<>("customerName"));
        testColumn.setCellValueFactory(new PropertyValueFactory<>("testTypeName"));
        priceColumn.setCellValueFactory(new PropertyValueFactory<>("testPrice"));
        statusColumn.setCellValueFactory(new PropertyValueFactory<>("status"));
        paymentColumn.setCellValueFactory(new PropertyValueFactory<>("paymentStatus"));
        orderedColumn.setCellValueFactory(cellData -> {
            if (cellData.getValue().getOrderedAt() != null) {
                return new SimpleStringProperty(
                        cellData.getValue().getOrderedAt()
                                .format(DateTimeFormatter.ofPattern("dd MMM yyyy HH:mm"))
                );
            }
            return new SimpleStringProperty("N/A");
        });

        // Color code payment status
        paymentColumn.setCellFactory(col -> new TableCell<>() {
            @Override
            protected void updateItem(String item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || item == null) {
                    setText(null);
                    setStyle("");
                } else {
                    setText(item);
                    if (item.equals("PAID")) {
                        setStyle("-fx-text-fill: #38a169; -fx-font-weight: bold;");
                    } else {
                        setStyle("-fx-text-fill: #e53e3e; -fx-font-weight: bold;");
                    }
                }
            }
        });

        loadAll();
    }

    private void loadAll() {
        requestList.clear();
        requestList.addAll(testRequestDAO.findAll());
        requestsTable.setItems(requestList);
    }

    @FXML
    private void handleFilter() {
        String selected = filterComboBox.getValue();
        requestList.clear();
        if (selected == null || selected.equals("All")) {
            requestList.addAll(testRequestDAO.findAll());
        } else if (selected.equals("Unpaid Only")) {
            requestList.addAll(testRequestDAO.findUnpaid());
        } else {
            testRequestDAO.findAll().stream()
                    .filter(r -> r.getPaymentStatus().equals("PAID"))
                    .forEach(requestList::add);
        }
        requestsTable.setItems(requestList);
    }

    @FXML
    private void handleMarkPaid() {
        statusLabel.setText("");

        TestRequest selected = requestsTable.getSelectionModel().getSelectedItem();
        if (selected == null) {
            statusLabel.setStyle("-fx-text-fill: #e53e3e;");
            statusLabel.setText("Please select a request first.");
            return;
        }

        if (selected.getPaymentStatus().equals("PAID")) {
            statusLabel.setStyle("-fx-text-fill: #e53e3e;");
            statusLabel.setText("This request is already marked as paid.");
            return;
        }

        String ref = paymentRefField.getText().trim();
        if (ref.isEmpty()) {
            statusLabel.setStyle("-fx-text-fill: #e53e3e;");
            statusLabel.setText("Please enter a payment reference number.");
            return;
        }

        int markedBy = SessionManager.getInstance().getCurrentUserId();
        boolean success = testRequestDAO.markAsPaid(selected.getId(), ref, markedBy);

        if (success) {
            AuditService.getInstance().log(
                    "PAYMENT_MARKED_PAID",
                    "test_requests",
                    selected.getId(),
                    "Request #" + selected.getId() + " marked as paid. Ref: " + ref
            );
            statusLabel.setStyle("-fx-text-fill: #38a169;");
            statusLabel.setText("Request #" + selected.getId() + " marked as paid!");
            paymentRefField.clear();
            loadAll();
        } else {
            statusLabel.setStyle("-fx-text-fill: #e53e3e;");
            statusLabel.setText("Failed to update payment status.");
        }
    }

    @FXML
    private void goBack() {
        NavigationManager.getInstance().navigateTo(
                NavigationManager.SUPER_ADMIN_DASHBOARD,
                "Super Admin Dashboard"
        );
    }
}