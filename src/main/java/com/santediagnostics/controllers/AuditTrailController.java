package com.santediagnostics.controllers;

import com.santediagnostics.dao.AuditDAO;
import com.santediagnostics.models.AuditLog;
import com.santediagnostics.navigation.NavigationManager;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;

import java.time.format.DateTimeFormatter;

public class AuditTrailController {

    @FXML private TableView<AuditLog> auditTable;
    @FXML private TableColumn<AuditLog, Integer> idColumn;
    @FXML private TableColumn<AuditLog, String> userColumn;
    @FXML private TableColumn<AuditLog, String> actionColumn;
    @FXML private TableColumn<AuditLog, String> tableColumn;
    @FXML private TableColumn<AuditLog, String> detailsColumn;
    @FXML private TableColumn<AuditLog, String> timeColumn;
    @FXML private TextField searchField;

    private AuditDAO auditDAO = new AuditDAO();
    private ObservableList<AuditLog> auditList = FXCollections.observableArrayList();

    @FXML
    public void initialize() {
        idColumn.setCellValueFactory(new PropertyValueFactory<>("id"));
        userColumn.setCellValueFactory(new PropertyValueFactory<>("userFullName"));
        actionColumn.setCellValueFactory(new PropertyValueFactory<>("action"));
        tableColumn.setCellValueFactory(new PropertyValueFactory<>("targetTable"));
        detailsColumn.setCellValueFactory(new PropertyValueFactory<>("details"));
        timeColumn.setCellValueFactory(cellData -> {
            if (cellData.getValue().getPerformedAt() != null) {
                return new SimpleStringProperty(
                        cellData.getValue().getPerformedAt()
                                .format(DateTimeFormatter.ofPattern("dd MMM yyyy HH:mm:ss"))
                );
            }
            return new SimpleStringProperty("N/A");
        });

        // Make table not editable
        auditTable.setEditable(false);

        loadAll();
    }

    private void loadAll() {
        auditList.clear();
        auditList.addAll(auditDAO.findAll());
        auditTable.setItems(auditList);
    }

    @FXML
    private void handleSearch() {
        String query = searchField.getText().trim();
        if (query.isEmpty()) {
            loadAll();
            return;
        }
        auditList.clear();
        auditList.addAll(auditDAO.findByAction(query));
        auditTable.setItems(auditList);
    }

    @FXML
    private void handleShowAll() {
        searchField.clear();
        loadAll();
    }

    @FXML
    private void goBack() {
        NavigationManager.getInstance().navigateTo(
                NavigationManager.SUPER_ADMIN_DASHBOARD,
                "Super Admin Dashboard"
        );
    }
}