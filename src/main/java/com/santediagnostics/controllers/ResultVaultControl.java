package com.santediagnostics.controllers;

import com.santediagnostics.dao.ResultDAO;
import com.santediagnostics.models.Result;
import com.santediagnostics.navigation.NavigationManager;
import com.santediagnostics.session.SessionManager;
import com.santediagnostics.utils.FileStorageService;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.web.WebView;
import javafx.stage.FileChooser;

import java.awt.Desktop;
import java.io.File;
import java.time.format.DateTimeFormatter;

public class ResultVaultControl {

    @FXML private TableView<Result> resultsTable;
    @FXML private TableColumn<Result, Integer> idColumn;
    @FXML private TableColumn<Result, String> testColumn;
    @FXML private TableColumn<Result, String> dateColumn;
    @FXML private WebView pdfViewer;
    @FXML private Label statusLabel;
    @FXML private TabPane tabPane;

    private ResultDAO resultDAO = new ResultDAO();
    private FileStorageService fileService = new FileStorageService();
    private ObservableList<Result> resultList = FXCollections.observableArrayList();

    @FXML
    public void initialize() {
        idColumn.setCellValueFactory(new PropertyValueFactory<>("testRequestId"));
        testColumn.setCellValueFactory(new PropertyValueFactory<>("testName"));
        dateColumn.setCellValueFactory(cellData -> {
            if (cellData.getValue().getUploadedAt() != null) {
                return new javafx.beans.property.SimpleStringProperty(
                    cellData.getValue().getUploadedAt().format(DateTimeFormatter.ofPattern("dd MMM yyyy"))
                );
            }
            return new javafx.beans.property.SimpleStringProperty("N/A");
        });

        loadResults();
    }

    private void loadResults() {
        int customerId = SessionManager.getInstance().getCurrentUserId();
        resultList.clear();
        resultList.addAll(resultDAO.getVerifiedResultsByCustomer(customerId));
        resultsTable.setItems(resultList);
    }

    @FXML
    private void handleViewResult() {
        Result selected = resultsTable.getSelectionModel().getSelectedItem();
        
        if (selected == null) {
            statusLabel.setStyle("-fx-text-fill: #e53e3e;");
            statusLabel.setText("Please select a result to view.");
            return;
        }

        File resultFile = fileService.getFile(selected.getFilePath());
        if (resultFile != null && resultFile.exists()) {
            if (selected.getFilePath().toLowerCase().endsWith(".pdf")) {
                // View PDF in WebView
                pdfViewer.getEngine().load(resultFile.toURI().toString());
                tabPane.getSelectionModel().select(1); // Switch to PDF viewer tab
            } else {
                // Open image in default viewer
                try {
                    Desktop.getDesktop().open(resultFile);
                } catch (Exception e) {
                    statusLabel.setText("Could not open file: " + e.getMessage());
                }
            }
            statusLabel.setText("");
        } else {
            statusLabel.setStyle("-fx-text-fill: #e53e3e;");
            statusLabel.setText("Result file not found.");
        }
    }

    @FXML
    private void handleDownload() {
        Result selected = resultsTable.getSelectionModel().getSelectedItem();
        
        if (selected == null) {
            statusLabel.setStyle("-fx-text-fill: #e53e3e;");
            statusLabel.setText("Please select a result to download.");
            return;
        }

        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("Save Result");
        fileChooser.setInitialFileName("result_" + selected.getTestRequestId() + ".pdf");
        
        File sourceFile = fileService.getFile(selected.getFilePath());
        if (sourceFile != null && sourceFile.exists()) {
            File destFile = fileChooser.showSaveDialog(null);
            if (destFile != null) {
                try {
                    java.nio.file.Files.copy(sourceFile.toPath(), destFile.toPath(), 
                                             java.nio.file.StandardCopyOption.REPLACE_EXISTING);
                    statusLabel.setStyle("-fx-text-fill: #38a169;");
                    statusLabel.setText("File downloaded successfully!");
                } catch (Exception e) {
                    statusLabel.setStyle("-fx-text-fill: #e53e3e;");
                    statusLabel.setText("Download failed: " + e.getMessage());
                }
            }
        } else {
            statusLabel.setStyle("-fx-text-fill: #e53e3e;");
            statusLabel.setText("Source file not found.");
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