package com.santediagnostics;

import com.santediagnostics.navigation.NavigationManager;
import javafx.application.Application;
import javafx.stage.Stage;

public class MainApp extends Application {

    @Override
    public void start(Stage primaryStage) {
        NavigationManager.getInstance().setPrimaryStage(primaryStage);

        primaryStage.setTitle("Sante Diagnostics LIMS");
        primaryStage.setWidth(1200);
        primaryStage.setHeight(750);
        primaryStage.setResizable(true);

        // Navigate to login screen on startup
        NavigationManager.getInstance().navigateTo(
                NavigationManager.LOGIN,
                "Sante Diagnostics LIMS — Login"
        );
    }

    public static void main(String[] args) {
        launch(args);
    }
}