package application;

import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;

public class LoginController {

    @FXML
    private TextField usernameField;

    @FXML
    private PasswordField passwordField;

    @FXML
    private Button loginButton;

    @FXML
    private Label messageLabel;

    @FXML
    private void handleLogin() {
        String username = usernameField.getText();
        String password = passwordField.getText();

        if (username.isEmpty() || password.isEmpty()) {
            messageLabel.setText("Please enter both username and password.");
            return;
        }

        try {
            Connection conn = DBConnection.getConnection();
            if (conn == null) {
                messageLabel.setText("Database connection failed. Please check your MySQL server.");
                return;
            }
            
            String query = "SELECT * FROM users WHERE username=? AND password=? AND is_active=TRUE";
            PreparedStatement stmt = conn.prepareStatement(query);
            stmt.setString(1, username);
            stmt.setString(2, password);
            ResultSet rs = stmt.executeQuery();
            
            if (rs.next()) {
                // Store user info for later use
                String role = rs.getString("role");
                String fullName = rs.getString("full_name");
                int userId = rs.getInt("id");
                
                // Load appropriate dashboard based on role
                String fxmlFile;
                String title;
                
                switch (role.toLowerCase()) {
                    case "librarian":
                        fxmlFile = "/resources/librarian_dashboard.fxml";
                        title = "Librarian Dashboard - Library Management System";
                        break;
                    case "student":
                        fxmlFile = "/resources/student_dashboard.fxml";
                        title = "Student Portal - Library Management System";
                        break;
                    default:
                        messageLabel.setText("Invalid user role.");
                        return;
                }
                
                FXMLLoader loader = new FXMLLoader(getClass().getResource(fxmlFile));
                Parent dashboardRoot = loader.load();
                
                // Set user info in the appropriate controller
                if (role.equals("librarian")) {
                    LibrarianDashboardController librarianController = loader.getController();
                    librarianController.setUserInfo(username, role, userId);
                } else if (role.equals("student")) {
                    StudentDashboardController studentController = loader.getController();
                    studentController.setUserInfo(username, role, userId);
                }
                
                Stage stage = new Stage();
                stage.setTitle(title);
                stage.setScene(new Scene(dashboardRoot, 1200, 800));
                stage.setMinWidth(1000);
                stage.setMinHeight(700);
                stage.show();

                Stage currentStage = (Stage) loginButton.getScene().getWindow();
                currentStage.close();
            } else {
                messageLabel.setText("Invalid username or password.");
            }
            conn.close();
        } catch (Exception e) {
            e.printStackTrace();
            messageLabel.setText("Login error: " + e.getMessage());
        }
    }
    
    @FXML
    private void openStudentRegistration() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/resources/student_registration.fxml"));
            Parent root = loader.load();
            Stage stage = new Stage();
            stage.setTitle("Student Registration - Library Management System");
            stage.setScene(new Scene(root, 600, 700));
            stage.setResizable(false);
            stage.show();
        } catch (Exception e) {
            e.printStackTrace();
            messageLabel.setText("Failed to open registration: " + e.getMessage());
        }
    }
}