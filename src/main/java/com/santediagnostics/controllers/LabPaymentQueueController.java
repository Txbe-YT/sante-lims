/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.santediagnostics.controllers;

/**
 *
 * @author dasil
 */

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

public class LabPaymentQueueController {
    
    @FXML private TableView<TestRequest> requestsTable;
    @FXML private TableColumn<TestRequest, Integer> idColumn;
    @FXML private TableColumn<TestRequest, String> customerColumn;
    @FXML private TableColumn<TestRequest, String> testColumn;
    @FXML private TableColumn<TestRequest, Double> priceColumn;
    @FXML private TableColumn<TestRequest, String> paymentColumn;
    @FXML private TableColumn<TestRequest, String> orderedColumn;
    @FXML private ComboBox<String> filterComboBox;
    @FXML private TextField paymentRefField;
    @FXML private Label statusLabel;

    private TestRequestDAO testRequestDAO = new TestRequestDAO();
    private ObservableList<TestRequest> requestList = FXCollections.observableArrayList();

    @FXML
    public void initialize() {
        filterComboBox.setItems(FXCollections.observableArrayList("Unpaid Only", "All", "Paid Only"));
        filterComboBox.getSelectionModel().selectFirst(); // Default to showing only what needs attention

        idColumn.setCellValueFactory(new PropertyValueFactory<>("id"));
        customerColumn.setCellValueFactory(new PropertyValueFactory<>("customerName"));
        testColumn.setCellValueFactory(new PropertyValueFactory<>("testTypeName"));
        priceColumn.setCellValueFactory(new PropertyValueFactory<>("testPrice"));
        paymentColumn.setCellValueFactory(new PropertyValueFactory<>("paymentStatus"));
        orderedColumn.setCellValueFactory(cellData -> {
            if (cellData.getValue().getOrderedAt() != null) {
                return new SimpleStringProperty(
                        cellData.getValue().getOrderedAt().format(DateTimeFormatter.ofPattern("dd MMM yyyy HH:mm"))
                );
            }
            return new SimpleStringProperty("N/A");
        });

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

        loadData();
    }

    private void loadData() {
        requestList.clear();
        String selected = filterComboBox.getValue();
        
        if (selected.equals("Unpaid Only")) {
            requestList.addAll(testRequestDAO.findUnpaid());
        } else if (selected.equals("All")) {
            requestList.addAll(testRequestDAO.findAll());
        } else {
            testRequestDAO.findAll().stream()
                    .filter(r -> r.getPaymentStatus().equals("PAID"))
                    .forEach(requestList::add);
        }
        requestsTable.setItems(requestList);
    }

    @FXML
    private void handleFilter() {
        loadData();
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
                    "LAB_PAYMENT_MARKED_PAID",
                    "test_requests",
                    selected.getId(),
                    "Lab Attendant marked Request #" + selected.getId() + " as paid. Ref: " + ref
            );
            statusLabel.setStyle("-fx-text-fill: #38a169;");
            statusLabel.setText("Request #" + selected.getId() + " marked as paid!");
            paymentRefField.clear();
            loadData(); // Refresh the table
        } else {
            statusLabel.setStyle("-fx-text-fill: #e53e3e;");
            statusLabel.setText("Failed to update payment status.");
        }
    }

    @FXML
    private void goBack() {
        // This specific back button now returns exactly to the Lab Attendant Dashboard
        NavigationManager.getInstance().navigateTo(
                NavigationManager.LAB_ATTENDANT_DASHBOARD,
                "Lab Attendant Dashboard"
        );
    }
}