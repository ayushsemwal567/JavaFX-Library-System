package application;

import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;
import javafx.scene.Node;
import javafx.event.ActionEvent;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.Random;

public class StudentRegistrationController {

    @FXML
    private TextField nameField;
    
    @FXML
    private TextField emailField;
    
    @FXML
    private TextField phoneField;
    
    @FXML
    private ComboBox<String> departmentCombo;
    
    @FXML
    private ComboBox<Integer> yearCombo;
    
    @FXML
    private TextArea addressArea;
    
    @FXML
    private Label messageLabel;
    
    @FXML
    private void initialize() {
        // Initialize department options
        departmentCombo.getItems().addAll(
            "Computer Science",
            "Electrical Engineering", 
            "Mechanical Engineering",
            "Civil Engineering",
            "Information Technology",
            "Business Administration",
            "Economics",
            "Mathematics",
            "Physics",
            "Chemistry",
            "Biology",
            "English Literature",
            "History",
            "Psychology",
            "Sociology"
        );
        
        // Initialize year options
        yearCombo.getItems().addAll(1, 2, 3, 4);
    }
    
    @FXML
    private void handleRegistration(ActionEvent event) {
        // Validate required fields
        if (nameField.getText().trim().isEmpty() || 
            emailField.getText().trim().isEmpty() ||
            departmentCombo.getValue() == null ||
            yearCombo.getValue() == null) {
            messageLabel.setText("Please fill all required fields!");
            return;
        }
        
        // Validate email format
        if (!emailField.getText().contains("@")) {
            messageLabel.setText("Please enter a valid email address!");
            return;
        }
        
        try {
            Connection conn = DBConnection.getConnection();
            if (conn == null) {
                messageLabel.setText("Database connection failed!");
                return;
            }
            
            // Check if email already exists
            String checkEmail = "SELECT COUNT(*) FROM users WHERE email = ?";
            PreparedStatement checkStmt = conn.prepareStatement(checkEmail);
            checkStmt.setString(1, emailField.getText().trim());
            ResultSet rs = checkStmt.executeQuery();
            rs.next();
            if (rs.getInt(1) > 0) {
                messageLabel.setText("Email already registered!");
                conn.close();
                return;
            }
            
            // Generate auto credentials
            String studentId = generateStudentId();
            String username = generateUsername(nameField.getText());
            String password = generatePassword();
            
            // Insert user record
            String insertUser = "INSERT INTO users (username, password, role, full_name, email) VALUES (?, ?, 'student', ?, ?)";
            PreparedStatement userStmt = conn.prepareStatement(insertUser);
            userStmt.setString(1, username);
            userStmt.setString(2, password);
            userStmt.setString(3, nameField.getText().trim());
            userStmt.setString(4, emailField.getText().trim());
            userStmt.executeUpdate();
            
            // Get the user ID
            int userId = 0;
            String getUserId = "SELECT id FROM users WHERE username = ?";
            PreparedStatement idStmt = conn.prepareStatement(getUserId);
            idStmt.setString(1, username);
            ResultSet idRs = idStmt.executeQuery();
            if (idRs.next()) {
                userId = idRs.getInt("id");
            }
            
            // Insert member record
            String insertMember = "INSERT INTO members (user_id, student_id, name, email, phone, address, department, year_of_study) VALUES (?, ?, ?, ?, ?, ?, ?, ?)";
            PreparedStatement memberStmt = conn.prepareStatement(insertMember);
            memberStmt.setInt(1, userId);
            memberStmt.setString(2, studentId);
            memberStmt.setString(3, nameField.getText().trim());
            memberStmt.setString(4, emailField.getText().trim());
            memberStmt.setString(5, phoneField.getText().trim());
            memberStmt.setString(6, addressArea.getText().trim());
            memberStmt.setString(7, departmentCombo.getValue());
            memberStmt.setInt(8, yearCombo.getValue());
            memberStmt.executeUpdate();
            
            conn.close();
            
            // Show success message with credentials
            showCredentialsDialog(username, password, studentId);
            
        } catch (Exception e) {
            e.printStackTrace();
            messageLabel.setText("Registration failed: " + e.getMessage());
        }
    }
    
    private String generateStudentId() {
        Random random = new Random();
        int year = java.time.LocalDate.now().getYear();
        int randomNum = random.nextInt(10000);
        return String.format("STU%d%04d", year, randomNum);
    }
    
    private String generateUsername(String fullName) {
        String[] names = fullName.toLowerCase().split("\\s+");
        if (names.length >= 2) {
            return names[0] + "." + names[names.length - 1] + new Random().nextInt(100);
        } else {
            return names[0] + new Random().nextInt(1000);
        }
    }
    
    private String generatePassword() {
        String chars = "ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz0123456789";
        StringBuilder password = new StringBuilder();
        Random random = new Random();
        for (int i = 0; i < 8; i++) {
            password.append(chars.charAt(random.nextInt(chars.length())));
        }
        return password.toString();
    }
    
    private void showCredentialsDialog(String username, String password, String studentId) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle("Registration Successful!");
        alert.setHeaderText("Your account has been created successfully!");
        
        String content = String.format("""
            🎓 Student ID: %s
            👤 Username: %s
            🔑 Password: %s
            
            Please save these credentials!
            You can now login to your student portal.
            """, studentId, username, password);
        
        alert.setContentText(content);
        alert.showAndWait();
        
        // Go back to login
        goBackToLogin(null);
    }
    
    @FXML
    private void goBackToLogin(ActionEvent event) {
        try {
            Parent root = FXMLLoader.load(getClass().getResource("/resources/login.fxml"));
            Stage stage = (Stage) ((Node) (event != null ? event.getSource() : nameField)).getScene().getWindow();
            stage.setScene(new Scene(root, 400, 600));
            stage.setTitle("Library Management Login");
            stage.setResizable(false);
            stage.show();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
} 