package application;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;
import javafx.stage.StageStyle;

public class LibraryApp extends Application {

    @Override
    public void start(Stage primaryStage) {
        try {
            // Initialize database
            System.out.println("Initializing database...");
            DBConnection.createDatabase();
            
            // Load login screen
            Parent root = FXMLLoader.load(getClass().getResource("/resources/login.fxml"));
            primaryStage.setTitle("Library Management System - Login");
            primaryStage.setScene(new Scene(root, 400, 500));
            primaryStage.setResizable(false);
            primaryStage.setMinWidth(400);
            primaryStage.setMinHeight(500);
            primaryStage.show();
            
        } catch (Exception e) {
            System.err.println("Error starting application: " + e.getMessage());
            e.printStackTrace();
        }
    }

    public static void main(String[] args) {
        launch(args);
    }
}
