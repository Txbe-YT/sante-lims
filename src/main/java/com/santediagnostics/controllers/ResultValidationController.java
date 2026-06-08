/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.santediagnostics.controllers;

/**
 *
 * @author dasil
 */

import com.santediagnostics.dao.ResultDAO;
import com.santediagnostics.dao.SampleDAO;
import com.santediagnostics.models.Result;
import com.santediagnostics.models.Sample;
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

public class ResultValidationController {
    // Tab 1: Upload Controls
    @FXML private TableView<Sample> uploadTable;
    @FXML private TableColumn<Sample, Integer> upSampleIdCol;
    @FXML private TableColumn<Sample, Integer> upReqIdCol;
    @FXML private TableColumn<Sample, String> upPatientCol;
    @FXML private TableColumn<Sample, String> upTestCol;
    @FXML private Label selectedFileLabel;
    @FXML private Label uploadStatusLabel;

    // Tab 2: Verify Controls
    @FXML private TableView<Result> verifyTable;
    @FXML private TableColumn<Result, Integer> verReqIdCol;
    @FXML private TableColumn<Result, String> verPatientCol;
    @FXML private TableColumn<Result, String> verTestCol;
    @FXML private TableColumn<Result, Integer> verUploaderCol;
    @FXML private Label verifyStatusLabel;

    private SampleDAO sampleDAO = new SampleDAO();
    private ResultDAO resultDAO = new ResultDAO();
    private FileStorageService fileService = new FileStorageService();
    private File currentSelectedFile = null;

    private ObservableList<Sample> processingSamples = FXCollections.observableArrayList();
    private ObservableList<Result> pendingResults = FXCollections.observableArrayList();

    @FXML
    public void initialize() {
        // Setup Upload Table
        upSampleIdCol.setCellValueFactory(new PropertyValueFactory<>("id"));
        upReqIdCol.setCellValueFactory(new PropertyValueFactory<>("testRequestId"));
        upPatientCol.setCellValueFactory(new PropertyValueFactory<>("customerName"));
        upTestCol.setCellValueFactory(new PropertyValueFactory<>("testTypeName"));

        // Setup Verify Table
        verReqIdCol.setCellValueFactory(new PropertyValueFactory<>("testRequestId"));
        verPatientCol.setCellValueFactory(new PropertyValueFactory<>("customerName"));
        verTestCol.setCellValueFactory(new PropertyValueFactory<>("testName"));
        verUploaderCol.setCellValueFactory(new PropertyValueFactory<>("uploadedBy"));

        loadTables();
    }

    private void loadTables() {
        // Load Tab 1: Samples that are "PROCESSING" and need results uploaded
        processingSamples.clear();
        sampleDAO.findAll().stream()
                .filter(s -> s.getStatus() != null && s.getStatus().equals("PROCESSING"))
                .forEach(processingSamples::add);
        uploadTable.setItems(processingSamples);

        // Load Tab 2: Results that are uploaded but unverified
        pendingResults.clear();
        pendingResults.addAll(resultDAO.getPendingVerifications());
        verifyTable.setItems(pendingResults);
    }

    // --- TAB 1: UPLOAD LOGIC ---

    @FXML
    private void handleSelectFile() {
        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("Select PDF or Image Result");
        fileChooser.getExtensionFilters().addAll(
                new FileChooser.ExtensionFilter("Medical Documents", "*.pdf", "*.png", "*.jpg", "*.jpeg")
        );
        
        File file = fileChooser.showOpenDialog(NavigationManager.getInstance().getPrimaryStage());
        if (file != null) {
            currentSelectedFile = file;
            selectedFileLabel.setText(file.getName());
            uploadStatusLabel.setText("");
        }
    }

    @FXML
    private void handleUploadSubmit() {
        Sample selectedSample = uploadTable.getSelectionModel().getSelectedItem();
        
        if (selectedSample == null) {
            uploadStatusLabel.setStyle("-fx-text-fill: #e53e3e;");
            uploadStatusLabel.setText("Please select a processing sample from the table.");
            return;
        }

        if (currentSelectedFile == null) {
            uploadStatusLabel.setStyle("-fx-text-fill: #e53e3e;");
            uploadStatusLabel.setText("Please select a file to upload.");
            return;
        }

        try {
            int currentUserId = SessionManager.getInstance().getCurrentUserId();
            
            // 1. Save file to the secure local directory
            String savedPath = fileService.saveFile(currentSelectedFile);
            
            // 2. Insert into Results table
            boolean uploadSuccess = resultDAO.uploadResult(selectedSample.getTestRequestId(), savedPath, currentUserId);
            
            if (uploadSuccess) {
                // 3. Automatically advance the Sample Lifecycle status
                sampleDAO.updateStatus(selectedSample.getId(), "AWAITING_VALIDATION", currentUserId);
                
                uploadStatusLabel.setStyle("-fx-text-fill: #38a169;");
                uploadStatusLabel.setText("File uploaded! Sent for second-party validation.");
                currentSelectedFile = null;
                selectedFileLabel.setText("No file selected...");
                loadTables(); // Refresh both tabs
            } else {
                uploadStatusLabel.setStyle("-fx-text-fill: #e53e3e;");
                uploadStatusLabel.setText("Database error during upload.");
            }
        } catch (Exception e) {
            uploadStatusLabel.setStyle("-fx-text-fill: #e53e3e;");
            uploadStatusLabel.setText("Failed to save file: " + e.getMessage());
        }
    }

    // --- TAB 2: VERIFY LOGIC ---

    @FXML
    private void handlePreview() {
        Result selectedResult = verifyTable.getSelectionModel().getSelectedItem();
        if (selectedResult == null) {
            verifyStatusLabel.setStyle("-fx-text-fill: #e53e3e;");
            verifyStatusLabel.setText("Select a result to preview.");
            return;
        }

        File fileToOpen = fileService.getFile(selectedResult.getFilePath());
        if (fileToOpen != null && Desktop.isDesktopSupported()) {
            try {
                Desktop.getDesktop().open(fileToOpen);
                verifyStatusLabel.setText("");
            } catch (Exception e) {
                verifyStatusLabel.setStyle("-fx-text-fill: #e53e3e;");
                verifyStatusLabel.setText("Could not open file viewer.");
            }
        } else {
            verifyStatusLabel.setStyle("-fx-text-fill: #e53e3e;");
            verifyStatusLabel.setText("File missing from storage directory.");
        }
    }

    @FXML
    private void handleVerifySubmit() {
        Result selectedResult = verifyTable.getSelectionModel().getSelectedItem();
        
        if (selectedResult == null) {
            verifyStatusLabel.setStyle("-fx-text-fill: #e53e3e;");
            verifyStatusLabel.setText("Select a result to verify.");
            return;
        }

        int currentUserId = SessionManager.getInstance().getCurrentUserId();

        // Enforce Workflow: You cannot verify your own upload
        if (selectedResult.getUploadedBy() == currentUserId) {
            verifyStatusLabel.setStyle("-fx-text-fill: #e53e3e; -fx-font-weight: bold;");
            verifyStatusLabel.setText("SECURITY BLOCK: You cannot verify a result you uploaded. Another attendant must do this.");
            return;
        }

        boolean verified = resultDAO.verifyResult(selectedResult.getId(), currentUserId);
        
        if (verified) {
            // Find the associated sample and mark it completed
            sampleDAO.findAll().stream()
                .filter(s -> s.getTestRequestId() == selectedResult.getTestRequestId())
                .findFirst()
                .ifPresent(s -> sampleDAO.updateStatus(s.getId(), "VALIDATED", currentUserId));

            verifyStatusLabel.setStyle("-fx-text-fill: #38a169;");
            verifyStatusLabel.setText("Result successfully verified and pushed to patient vault!");
            loadTables();
        } else {
            verifyStatusLabel.setStyle("-fx-text-fill: #e53e3e;");
            verifyStatusLabel.setText("Error verifying result in database.");
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