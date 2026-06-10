package com.santediagnostics.controllers;

import com.santediagnostics.dao.ResultDAO;
import com.santediagnostics.dao.SampleDAO;
import com.santediagnostics.dao.TestRequestDAO;
import com.santediagnostics.models.Result;
import com.santediagnostics.models.Sample;
import com.santediagnostics.models.TestRequest;
import com.santediagnostics.navigation.NavigationManager;
import com.santediagnostics.session.SessionManager;
import com.santediagnostics.utils.FileStorageService;
import com.santediagnostics.utils.EmailService;
import com.santediagnostics.dao.UserDAO;
import com.santediagnostics.models.User;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.stage.FileChooser;

import java.awt.Desktop;
import java.io.File;

public class ResultValidationController {
    @FXML private TableView<Sample> uploadTable;
    @FXML private TableColumn<Sample, Integer> upSampleIdCol;
    @FXML private TableColumn<Sample, Integer> upReqIdCol;
    @FXML private TableColumn<Sample, String> upPatientCol;
    @FXML private TableColumn<Sample, String> upTestCol;
    @FXML private Label selectedFileLabel;
    @FXML private Label uploadStatusLabel;

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
    private EmailService emailService = new EmailService();
    private UserDAO userDAO = new UserDAO();

    private ObservableList<Sample> processingSamples = FXCollections.observableArrayList();
    private ObservableList<Result> pendingResults = FXCollections.observableArrayList();

    @FXML
    public void initialize() {
        upSampleIdCol.setCellValueFactory(new PropertyValueFactory<>("id"));
        upReqIdCol.setCellValueFactory(new PropertyValueFactory<>("testRequestId"));
        upPatientCol.setCellValueFactory(new PropertyValueFactory<>("customerName"));
        upTestCol.setCellValueFactory(new PropertyValueFactory<>("testTypeName"));

        verReqIdCol.setCellValueFactory(new PropertyValueFactory<>("testRequestId"));
        verPatientCol.setCellValueFactory(new PropertyValueFactory<>("customerName"));
        verTestCol.setCellValueFactory(new PropertyValueFactory<>("testName"));
        verUploaderCol.setCellValueFactory(new PropertyValueFactory<>("uploadedBy"));

        loadTables();
    }

    private void loadTables() {
        processingSamples.clear();
        sampleDAO.findAll().stream()
                .filter(s -> s.getStatus() != null && s.getStatus().equals("PROCESSING"))
                .forEach(processingSamples::add);
        uploadTable.setItems(processingSamples);

        pendingResults.clear();
        pendingResults.addAll(resultDAO.getPendingVerifications());
        verifyTable.setItems(pendingResults);
    }

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
        if (selectedSample == null || currentSelectedFile == null) return;

        try {
            int currentUserId = SessionManager.getInstance().getCurrentUserId();
            String savedPath = fileService.saveFile(currentSelectedFile);
            boolean uploadSuccess = resultDAO.uploadResult(selectedSample.getTestRequestId(), savedPath, currentUserId);
            
            if (uploadSuccess) {
                sampleDAO.updateStatus(selectedSample.getId(), "AWAITING_VALIDATION", currentUserId);
                uploadStatusLabel.setStyle("-fx-text-fill: #38a169;");
                uploadStatusLabel.setText("File uploaded! Sent for second-party validation.");
                currentSelectedFile = null;
                selectedFileLabel.setText("No file selected...");
                loadTables();
            }
        } catch (Exception e) { e.printStackTrace(); }
    }

    @FXML private void handlePreview() { /* Preview logic */ }

    @FXML
    private void handleVerifySubmit() {
        Result selectedResult = verifyTable.getSelectionModel().getSelectedItem();
        if (selectedResult == null) return;

        int currentUserId = SessionManager.getInstance().getCurrentUserId();
        if (selectedResult.getUploadedBy() == currentUserId) {
            verifyStatusLabel.setStyle("-fx-text-fill: #e53e3e; -fx-font-weight: bold;");
            verifyStatusLabel.setText("SECURITY BLOCK: You cannot verify a result you uploaded.");
            return;
        }

        boolean verified = resultDAO.verifyResult(selectedResult.getId(), currentUserId);
        if (verified) {
            sampleDAO.findAll().stream()
                .filter(s -> s.getTestRequestId() == selectedResult.getTestRequestId())
                .findFirst()
                .ifPresent(s -> sampleDAO.updateStatus(s.getId(), "VALIDATED", currentUserId));

            // --- INTERCEPT AND FIRE EMAIL TO CUSTOMER ---
            try {
                TestRequestDAO testRequestDAO = new TestRequestDAO();
                TestRequest tr = testRequestDAO.findById(selectedResult.getTestRequestId());
                if (tr != null) {
                    User patient = userDAO.findById(tr.getCustomerId());
                    if (patient != null) {
                        emailService.sendResultReadyEmail(
                            patient.getEmail(),
                            patient.getFullName(),
                            tr.getTestTypeName(),
                            String.valueOf(tr.getId())
                        );
                    }
                }
            } catch (Exception e) { System.err.println("Mail Dispatch Failure: " + e.getMessage()); }

            verifyStatusLabel.setStyle("-fx-text-fill: #38a169;");
            verifyStatusLabel.setText("Result successfully verified and patient notified via email!");
            loadTables();
        }
    }

    @FXML private void goBack() { NavigationManager.getInstance().navigateTo(NavigationManager.LAB_ATTENDANT_DASHBOARD, "Dashboard"); }
}