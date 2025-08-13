package com.mycompany.payrollsystem;

import com.mycompany.payrollsystem.utils.DatabaseManager;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;

public class PayrollSystem extends Application {

    @Override
    public void start(Stage primaryStage) throws Exception {
        // make sure the employees table exists before launching UI
        DatabaseManager.initAllTables();

        // load login screen
        FXMLLoader loader = new FXMLLoader(getClass().getResource("/login.fxml"));
        Parent root = loader.load();
        primaryStage.setTitle("Payroll System - Login");
        primaryStage.setScene(new Scene(root));
        primaryStage.show();
    }

    public static void main(String[] args) {
        // launch the JavaFX app
        launch(args);
    }
}
