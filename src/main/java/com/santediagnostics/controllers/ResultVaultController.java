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
import javafx.stage.FileChooser;

import java.awt.Desktop;
import java.io.File;
import java.time.format.DateTimeFormatter;

public class ResultVaultController {

    @FXML private TableView<Result> resultsTable;
    @FXML private TableColumn<Result, Integer> idColumn;
    @FXML private TableColumn<Result, String> testColumn;
    @FXML private TableColumn<Result, String> dateColumn;
    @FXML private Label statusLabel;
    
    // Kept for FXML compatibility if your old layout still has it
    @FXML private TabPane tabPane; 

    private ResultDAO resultDAO = new ResultDAO();
    private FileStorageService fileService = new FileStorageService();
    private ObservableList<Result> resultList = FXCollections.observableArrayList();

    @FXML
    public void initialize() {
        if(idColumn != null) idColumn.setCellValueFactory(new PropertyValueFactory<>("testRequestId"));
        if(testColumn != null) testColumn.setCellValueFactory(new PropertyValueFactory<>("testName"));
        
        if(dateColumn != null) {
            dateColumn.setCellValueFactory(cellData -> {
                if (cellData.getValue().getUploadedAt() != null) {
                    return new javafx.beans.property.SimpleStringProperty(
                        cellData.getValue().getUploadedAt().format(DateTimeFormatter.ofPattern("dd MMM yyyy"))
                    );
                }
                return new javafx.beans.property.SimpleStringProperty("N/A");
            });
        }

        loadResults();
    }

    private void loadResults() {
        int customerId = SessionManager.getInstance().getCurrentUserId();
        resultList.clear();
        resultList.addAll(resultDAO.getVerifiedResultsByCustomer(customerId));
        if(resultsTable != null) resultsTable.setItems(resultList);
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
        
        if (resultFile != null && resultFile.exists() && Desktop.isDesktopSupported()) {
            try {
                // Instantly open the file (PDF or Image) in the computer's native viewer!
                Desktop.getDesktop().open(resultFile);
                statusLabel.setText("");
            } catch (Exception e) {
                statusLabel.setStyle("-fx-text-fill: #e53e3e;");
                statusLabel.setText("Could not open file: " + e.getMessage());
            }
        } else {
            statusLabel.setStyle("-fx-text-fill: #e53e3e;");
            statusLabel.setText("Result file not found or Desktop viewer not supported.");
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
        
        // Dynamically get the right file extension (e.g. .pdf or .png)
        String extension = ".pdf"; 
        if (selected.getFilePath() != null && selected.getFilePath().contains(".")) {
            extension = selected.getFilePath().substring(selected.getFilePath().lastIndexOf("."));
        }
        fileChooser.setInitialFileName("result_" + selected.getTestRequestId() + extension);
        
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