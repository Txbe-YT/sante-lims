package com.santediagnostics.controllers;

import com.santediagnostics.audit.AuditService;
import com.santediagnostics.dao.UserDAO;
import com.santediagnostics.models.User;
import com.santediagnostics.navigation.NavigationManager;
import com.santediagnostics.session.SessionManager;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;

import java.time.format.DateTimeFormatter;
import java.util.List;

public class UserManagementController {

    @FXML private TextField fullNameField;
    @FXML private TextField emailField;
    @FXML private PasswordField passwordField;
    @FXML private ComboBox<String> roleComboBox;
    @FXML private Label errorLabel;
    @FXML private Label successLabel;

    @FXML private TableView<User> usersTable;
    @FXML private TableColumn<User, String> nameColumn;
    @FXML private TableColumn<User, String> emailColumn;
    @FXML private TableColumn<User, String> roleColumn;
    @FXML private TableColumn<User, Boolean> verifiedColumn;
    @FXML private TableColumn<User, String> createdColumn;
    @FXML private ComboBox<String> filterComboBox;

    private UserDAO userDAO = new UserDAO();
    private ObservableList<User> userList = FXCollections.observableArrayList();

    @FXML
    public void initialize() {
        // Setup role dropdown based on current user's role
        String currentRole = SessionManager.getInstance().getCurrentRole();
        if (currentRole.equals("SUPER_ADMIN")) {
            roleComboBox.setItems(FXCollections.observableArrayList(
                    "LAB_ATTENDANT", "CUSTOMER"
            ));
        } else {
            roleComboBox.setItems(FXCollections.observableArrayList("CUSTOMER"));
        }
        roleComboBox.getSelectionModel().selectFirst();

        // Setup filter dropdown
        filterComboBox.setItems(FXCollections.observableArrayList(
                "All", "SUPER_ADMIN", "LAB_ATTENDANT", "CUSTOMER"
        ));
        filterComboBox.getSelectionModel().selectFirst();

        // Setup table columns
        nameColumn.setCellValueFactory(new PropertyValueFactory<>("fullName"));
        emailColumn.setCellValueFactory(new PropertyValueFactory<>("email"));
        roleColumn.setCellValueFactory(new PropertyValueFactory<>("role"));
        verifiedColumn.setCellValueFactory(new PropertyValueFactory<>("verified"));
        createdColumn.setCellValueFactory(cellData -> {
            if (cellData.getValue().getCreatedAt() != null) {
                return new javafx.beans.property.SimpleStringProperty(
                        cellData.getValue().getCreatedAt()
                                .format(DateTimeFormatter.ofPattern("dd MMM yyyy HH:mm"))
                );
            }
            return new javafx.beans.property.SimpleStringProperty("N/A");
        });

        // Load all users
        loadUsers();
    }

    private void loadUsers() {
        userList.clear();
        userList.addAll(userDAO.findAll());
        usersTable.setItems(userList);
    }

    @FXML
    private void handleFilter() {
        String selected = filterComboBox.getValue();
        if (selected == null || selected.equals("All")) {
            loadUsers();
        } else {
            userList.clear();
            userList.addAll(userDAO.findByRole(selected));
            usersTable.setItems(userList);
        }
    }

    @FXML
    private void handleCreateUser() {
        errorLabel.setText("");
        successLabel.setText("");

        String fullName = fullNameField.getText().trim();
        String email = emailField.getText().trim();
        String password = passwordField.getText();
        String role = roleComboBox.getValue();

        // Validation
        if (fullName.isEmpty() || email.isEmpty() || password.isEmpty()) {
            errorLabel.setText("Please fill in all fields.");
            return;
        }

        if (!email.contains("@")) {
            errorLabel.setText("Please enter a valid email address.");
            return;
        }

        if (password.length() < 6) {
            errorLabel.setText("Password must be at least 6 characters.");
            return;
        }

        // Check if email already exists
        if (userDAO.findByEmail(email) != null) {
            errorLabel.setText("An account with this email already exists.");
            return;
        }

        int createdBy = SessionManager.getInstance().getCurrentUserId();
        boolean success = userDAO.createUser(fullName, email, password, role, createdBy);

        if (success) {
            AuditService.getInstance().log(
                    "USER_CREATED",
                    "users",
                    -1,
                    "Created " + role + " account for " + fullName + " (" + email + ")"
            );

            successLabel.setText("Account created successfully for " + fullName + "!");

            // Clear form
            fullNameField.clear();
            emailField.clear();
            passwordField.clear();
            roleComboBox.getSelectionModel().selectFirst();

            // Refresh table
            loadUsers();
        } else {
            errorLabel.setText("Failed to create account. Please try again.");
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