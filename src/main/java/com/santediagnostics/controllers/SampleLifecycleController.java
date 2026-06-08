/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.santediagnostics.controllers;

/**
 *
 * @author dasil
 */

import com.santediagnostics.dao.SampleDAO;
import com.santediagnostics.models.Sample;
import com.santediagnostics.navigation.NavigationManager;
import com.santediagnostics.session.SessionManager;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;

import java.time.format.DateTimeFormatter;

public class SampleLifecycleController {
   @FXML private TableView<Sample> samplesTable;
    @FXML private TableColumn<Sample, Integer> idColumn;
    @FXML private TableColumn<Sample, Integer> reqIdColumn;
    @FXML private TableColumn<Sample, String> customerColumn;
    @FXML private TableColumn<Sample, String> testColumn;
    @FXML private TableColumn<Sample, String> statusColumn;
    @FXML private TableColumn<Sample, String> updatedColumn;
    
    @FXML private ComboBox<String> filterComboBox;
    @FXML private ComboBox<String> statusComboBox;
    @FXML private Label actionLabel;

    private SampleDAO sampleDAO = new SampleDAO();
    private ObservableList<Sample> sampleList = FXCollections.observableArrayList();

    // The strictly defined lifecycle states matching your SQL CHECK constraint
    private final String[] LIFECYCLE_STATES = {
        "PENDING_COLLECTION", "COLLECTED", "PROCESSING", "AWAITING_VALIDATION", "VALIDATED"
    };

    @FXML
    public void initialize() {
        try {
            // Setup dropdowns
            filterComboBox.setItems(FXCollections.observableArrayList("All Active", "PENDING_COLLECTION", "COLLECTED", "PROCESSING", "AWAITING_VALIDATION"));
            statusComboBox.setItems(FXCollections.observableArrayList(LIFECYCLE_STATES));

            // Map columns to Sample model properties
            idColumn.setCellValueFactory(new PropertyValueFactory<>("id"));
            reqIdColumn.setCellValueFactory(new PropertyValueFactory<>("testRequestId"));
            customerColumn.setCellValueFactory(new PropertyValueFactory<>("customerName"));
            testColumn.setCellValueFactory(new PropertyValueFactory<>("testTypeName"));
            statusColumn.setCellValueFactory(new PropertyValueFactory<>("status"));
            
            // Format the most recent timestamp available for this sample
            updatedColumn.setCellValueFactory(cellData -> {
                if (cellData.getValue().getLastActionTime() != null) {
                    return new SimpleStringProperty(
                            cellData.getValue().getLastActionTime().format(DateTimeFormatter.ofPattern("dd MMM yyyy HH:mm"))
                    );
                }
                return new SimpleStringProperty("N/A");
            });

            // Color-code the statuses so the Lab Attendant can quickly see what needs work
            statusColumn.setCellFactory(col -> new TableCell<>() {
                @Override
                protected void updateItem(String item, boolean empty) {
                    super.updateItem(item, empty);
                    if (empty || item == null) {
                        setText(null);
                        setStyle("");
                    } else {
                        setText(item);
                        setStyle("-fx-font-weight: bold;");
                        switch (item) {
                            case "PENDING_COLLECTION": setStyle("-fx-text-fill: #e53e3e; -fx-font-weight: bold;"); break; // Red
                            case "COLLECTED": setStyle("-fx-text-fill: #dd6b20; -fx-font-weight: bold;"); break; // Orange
                            case "PROCESSING": setStyle("-fx-text-fill: #3182ce; -fx-font-weight: bold;"); break; // Blue
                            case "AWAITING_VALIDATION": setStyle("-fx-text-fill: #805ad5; -fx-font-weight: bold;"); break; // Purple
                            case "VALIDATED": setStyle("-fx-text-fill: #38a169; -fx-font-weight: bold;"); break; // Green
                        }
                    }
                }
            });

            // Select first item AFTER setup is complete to trigger loadData safely
            filterComboBox.getSelectionModel().selectFirst();
            
        } catch (Exception e) {
            System.err.println("CRASH IN INITIALIZE: ");
            e.printStackTrace();
        }
    }

    private void loadData() {
        try {
            sampleList.clear();
            String selectedFilter = filterComboBox.getValue();
            
            // Safeguard against nulls during JavaFX view initialization
            if (selectedFilter == null) return; 
            
            if (selectedFilter.equals("All Active")) {
                // Hide already validated ones by default to keep the operational queue clean
                sampleDAO.findAll().stream()
                        .filter(s -> s.getStatus() != null && !s.getStatus().equals("VALIDATED"))
                        .forEach(sampleList::add);
            } else {
                sampleDAO.findAll().stream()
                        .filter(s -> s.getStatus() != null && s.getStatus().equals(selectedFilter))
                        .forEach(sampleList::add);
            }
            samplesTable.setItems(sampleList);
        } catch (Exception e) {
            System.err.println("CRASH IN LOADDATA: ");
            e.printStackTrace();
        }
    }

    @FXML
    private void handleFilter() {
        loadData();
    }

    @FXML
    private void handleUpdateStatus() {
        actionLabel.setText("");

        Sample selectedSample = samplesTable.getSelectionModel().getSelectedItem();
        String newStatus = statusComboBox.getValue();

        // Validations
        if (selectedSample == null) {
            actionLabel.setStyle("-fx-text-fill: #e53e3e;");
            actionLabel.setText("Please select a sample from the table first.");
            return;
        }

        if (newStatus == null) {
            actionLabel.setStyle("-fx-text-fill: #e53e3e;");
            actionLabel.setText("Please select a target status from the dropdown.");
            return;
        }

        if (selectedSample.getStatus() != null && selectedSample.getStatus().equals(newStatus)) {
            actionLabel.setStyle("-fx-text-fill: #e53e3e;");
            actionLabel.setText("Sample is already in this state.");
            return;
        }

        int currentUserId = SessionManager.getInstance().getCurrentUserId();
        
        // Use the DAO to update DB and automatically log to Audit Trail
        boolean success = sampleDAO.updateStatus(selectedSample.getId(), newStatus, currentUserId);

        if (success) {
            actionLabel.setStyle("-fx-text-fill: #38a169;");
            actionLabel.setText("Sample #" + selectedSample.getId() + " updated to " + newStatus);
            statusComboBox.getSelectionModel().clearSelection();
            loadData(); // Refresh the table to show updated status and timestamp
        } else {
            actionLabel.setStyle("-fx-text-fill: #e53e3e;");
            actionLabel.setText("Database error: Could not update sample.");
        }
    }

    @FXML
    private void goBack() {
        NavigationManager.getInstance().navigateTo(
                NavigationManager.LAB_ATTENDANT_DASHBOARD,
                "Lab Attendant Dashboard"
        );
    }
}