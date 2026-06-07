package com.santediagnostics.controllers;

import com.santediagnostics.audit.AuditService;
import com.santediagnostics.dao.TestTypeDAO;
import com.santediagnostics.models.TestType;
import com.santediagnostics.navigation.NavigationManager;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;

public class TestBuilderController {

    @FXML private TextField nameField;
    @FXML private ComboBox<String> categoryComboBox;
    @FXML private TextField priceField;
    @FXML private TextField tatField;
    @FXML private ComboBox<String> formatComboBox;
    @FXML private Label errorLabel;
    @FXML private Label successLabel;

    @FXML private TableView<TestType> testTypesTable;
    @FXML private TableColumn<TestType, String> nameColumn;
    @FXML private TableColumn<TestType, String> categoryColumn;
    @FXML private TableColumn<TestType, Double> priceColumn;
    @FXML private TableColumn<TestType, Integer> tatColumn;
    @FXML private TableColumn<TestType, String> formatColumn;

    private TestTypeDAO testTypeDAO = new TestTypeDAO();
    private ObservableList<TestType> testTypeList = FXCollections.observableArrayList();

    @FXML
    public void initialize() {
        categoryComboBox.setItems(FXCollections.observableArrayList(
                "Blood", "Imaging", "Biopsy", "Other"
        ));
        categoryComboBox.getSelectionModel().selectFirst();

        formatComboBox.setItems(FXCollections.observableArrayList(
                "numeric", "text", "PDF", "image"
        ));
        formatComboBox.getSelectionModel().selectFirst();

        nameColumn.setCellValueFactory(new PropertyValueFactory<>("name"));
        categoryColumn.setCellValueFactory(new PropertyValueFactory<>("category"));
        priceColumn.setCellValueFactory(new PropertyValueFactory<>("price"));
        tatColumn.setCellValueFactory(new PropertyValueFactory<>("tatHours"));
        formatColumn.setCellValueFactory(new PropertyValueFactory<>("resultFormat"));

        loadTestTypes();
    }

    private void loadTestTypes() {
        testTypeList.clear();
        testTypeList.addAll(testTypeDAO.findAll());
        testTypesTable.setItems(testTypeList);
    }

    @FXML
    private void handleCreate() {
        errorLabel.setText("");
        successLabel.setText("");

        String name = nameField.getText().trim();
        String category = categoryComboBox.getValue();
        String priceText = priceField.getText().trim();
        String tatText = tatField.getText().trim();
        String format = formatComboBox.getValue();

        if (name.isEmpty() || priceText.isEmpty() || tatText.isEmpty()) {
            errorLabel.setText("Please fill in all fields.");
            return;
        }

        double price;
        int tat;

        try {
            price = Double.parseDouble(priceText);
        } catch (NumberFormatException e) {
            errorLabel.setText("Price must be a valid number.");
            return;
        }

        try {
            tat = Integer.parseInt(tatText);
        } catch (NumberFormatException e) {
            errorLabel.setText("TAT must be a whole number (hours).");
            return;
        }

        if (price <= 0) {
            errorLabel.setText("Price must be greater than zero.");
            return;
        }

        if (tat <= 0) {
            errorLabel.setText("TAT must be greater than zero.");
            return;
        }

        boolean success = testTypeDAO.create(name, category, price, tat, format);

        if (success) {
            AuditService.getInstance().log(
                    "TEST_TYPE_CREATED",
                    "test_types",
                    -1,
                    "Created test type: " + name + " (" + category + ") at ₦" + price
            );

            successLabel.setText("Test type '" + name + "' created successfully!");
            nameField.clear();
            priceField.clear();
            tatField.clear();
            categoryComboBox.getSelectionModel().selectFirst();
            formatComboBox.getSelectionModel().selectFirst();
            loadTestTypes();
        } else {
            errorLabel.setText("Failed to create test type. Please try again.");
        }
    }

    @FXML
    private void handleDelete() {
        errorLabel.setText("");
        successLabel.setText("");

        TestType selected = testTypesTable.getSelectionModel().getSelectedItem();

        if (selected == null) {
            errorLabel.setText("Please select a test type to delete.");
            return;
        }

        Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
        alert.setTitle("Confirm Delete");
        alert.setHeaderText("Delete " + selected.getName() + "?");
        alert.setContentText("This action cannot be undone.");

        alert.showAndWait().ifPresent(response -> {
            if (response == ButtonType.OK) {
                boolean success = testTypeDAO.delete(selected.getId());
                if (success) {
                    AuditService.getInstance().log(
                            "TEST_TYPE_DELETED",
                            "test_types",
                            selected.getId(),
                            "Deleted test type: " + selected.getName()
                    );
                    successLabel.setText("Test type deleted successfully.");
                    loadTestTypes();
                } else {
                    errorLabel.setText("Failed to delete test type.");
                }
            }
        });
    }

    @FXML
    private void goBack() {
        NavigationManager.getInstance().navigateTo(
                NavigationManager.SUPER_ADMIN_DASHBOARD,
                "Super Admin Dashboard"
        );
    }
}