package com.mycompany.payrollsystem;

import com.mycompany.payrollsystem.utils.DatabaseManager;
import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;

/**
 * entry point for the payroll system application.
 * extends JavaFX Application and sets up the main login screen.
 *
 * responsible for initializing the database tables,
 * loading the login view, and applying the css stylesheet.
 */
public class PayrollSystem extends Application {

    @Override
    public void start(Stage primaryStage) throws Exception {
        /**
        * starts the javafx application.
        * initializes the database tables, loads the login screen,
        * and sets up the primary stage.
        *
        * @param primaryStage the main stage provided by JavaFX
        * @throws Exception if fxml or database init fails
        */
        
        //init all tables when the pp first launches
        DatabaseManager.initAllTables();

        //load login screen from fxml
        FXMLLoader loader = new FXMLLoader(getClass().getResource("/login.fxml"));
        Parent root = loader.load();
        primaryStage.setTitle("ABCCompany Payroll - Login");
        
        //set up the scene
        Scene scene = new Scene(root, 800, 700);
        primaryStage.setResizable(false);
        
        //get css for consistent styling
        scene.getStylesheets().add(getClass().getResource("/app.css").toExternalForm());
        primaryStage.setScene(scene);
        primaryStage.show();
    }
     /**
     * standard main method to launch the javafx app.
     *
     * @param args command line arguments
     */
    public static void main(String[] args) {
        //launch the JavaFX app
        launch(args);
    }
}
