package application;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.Node;
import javafx.scene.control.*;
import javafx.stage.Stage;
import java.sql.*;

public class MyProfileController {

    @FXML private Label studentNameLabel;
    @FXML private Label studentIdLabel;
    @FXML private Label emailLabel;
    @FXML private Label phoneLabel;
    @FXML private Label departmentLabel;
    @FXML private Label yearLabel;
    @FXML private Label joinDateLabel;
    @FXML private Label statusLabel;
    @FXML private Label totalBorrowingsLabel;
    @FXML private Label currentBorrowingsLabel;
    @FXML private Label totalFinesLabel;
    @FXML private Label paidFinesLabel;
    
    // Personal Information Fields
    @FXML private TextField firstNameField;
    @FXML private TextField lastNameField;
    @FXML private TextField emailField;
    @FXML private TextField phoneField;
    @FXML private TextField addressField;
    @FXML private DatePicker dateOfBirthPicker;
    
    // Academic Information Fields
    @FXML private TextField studentIdField;
    @FXML private ComboBox<String> departmentCombo;
    @FXML private ComboBox<String> yearCombo;
    @FXML private TextField majorField;
    @FXML private TextField advisorField;
    @FXML private TextField gpaField;
    
    // Account Settings Fields
    @FXML private TextField usernameField;
    @FXML private PasswordField currentPasswordField;
    @FXML private PasswordField newPasswordField;
    @FXML private PasswordField confirmPasswordField;
    
    private int userId;
    private String studentName;
    private int memberId;

    public void setUserInfo(int userId, String studentName) {
        this.userId = userId;
        this.studentName = studentName;
        if (studentNameLabel != null) {
            studentNameLabel.setText(studentName);
        }
        initializeComboBoxes();
        loadProfileData();
    }

    private void initializeComboBoxes() {
        departmentCombo.getItems().addAll(
            "Computer Science", "Engineering", "Business", "Arts", "Science", 
            "Medicine", "Law", "Education", "Social Sciences", "Other"
        );
        
        yearCombo.getItems().addAll("1st Year", "2nd Year", "3rd Year", "4th Year", "Graduate");
    }

    private void loadProfileData() {
        try {
            Connection conn = DBConnection.getConnection();
            if (conn == null) return;
            
            // Get user and member information
            String query = "SELECT u.username, u.email, m.* FROM users u " +
                          "JOIN members m ON u.id = m.user_id " +
                          "WHERE u.id = ?";
            
            PreparedStatement stmt = conn.prepareStatement(query);
            stmt.setInt(1, userId);
            ResultSet rs = stmt.executeQuery();
            
            if (rs.next()) {
                memberId = rs.getInt("id");
                
                // Update summary labels
                studentIdLabel.setText("ID: " + rs.getString("student_id"));
                emailLabel.setText("Email: " + rs.getString("email"));
                phoneLabel.setText("Phone: " + rs.getString("phone"));
                departmentLabel.setText("Department: " + rs.getString("department"));
                yearLabel.setText("Year: " + rs.getString("year"));
                joinDateLabel.setText("Joined: " + rs.getString("join_date"));
                statusLabel.setText("Status: " + rs.getString("status"));
                
                // Load statistics
                loadStatistics();
                
                // Populate form fields
                populateFormFields(rs);
            }
            
            conn.close();
        } catch (Exception e) {
            e.printStackTrace();
            showAlert("Error", "Failed to load profile data: " + e.getMessage());
        }
    }

    private void loadStatistics() {
        try {
            Connection conn = DBConnection.getConnection();
            if (conn == null) return;
            
            // Count total borrowings
            String totalQuery = "SELECT COUNT(*) FROM borrowings WHERE member_id = ?";
            PreparedStatement totalStmt = conn.prepareStatement(totalQuery);
            totalStmt.setInt(1, memberId);
            ResultSet totalRs = totalStmt.executeQuery();
            totalRs.next();
            int totalBorrowings = totalRs.getInt(1);
            
            // Count current borrowings
            String currentQuery = "SELECT COUNT(*) FROM borrowings WHERE member_id = ? AND status = 'borrowed'";
            PreparedStatement currentStmt = conn.prepareStatement(currentQuery);
            currentStmt.setInt(1, memberId);
            ResultSet currentRs = currentStmt.executeQuery();
            currentRs.next();
            int currentBorrowings = currentRs.getInt(1);
            
            // Get fines information
            String finesQuery = "SELECT total_fines, paid_fines FROM members WHERE id = ?";
            PreparedStatement finesStmt = conn.prepareStatement(finesQuery);
            finesStmt.setInt(1, memberId);
            ResultSet finesRs = finesStmt.executeQuery();
            finesRs.next();
            double totalFines = finesRs.getDouble("total_fines");
            double paidFines = finesRs.getDouble("paid_fines");
            
            // Update labels
            totalBorrowingsLabel.setText("Total: " + totalBorrowings);
            currentBorrowingsLabel.setText("Current: " + currentBorrowings);
            totalFinesLabel.setText("Total: $" + String.format("%.2f", totalFines));
            paidFinesLabel.setText("Paid: $" + String.format("%.2f", paidFines));
            
            conn.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void populateFormFields(ResultSet rs) throws SQLException {
        // Personal Information
        firstNameField.setText(rs.getString("first_name"));
        lastNameField.setText(rs.getString("last_name"));
        emailField.setText(rs.getString("email"));
        phoneField.setText(rs.getString("phone"));
        addressField.setText(rs.getString("address"));
        
        // Academic Information
        studentIdField.setText(rs.getString("student_id"));
        departmentCombo.setValue(rs.getString("department"));
        yearCombo.setValue(rs.getString("year"));
        majorField.setText(rs.getString("major"));
        advisorField.setText(rs.getString("advisor"));
        gpaField.setText(rs.getString("gpa"));
        
        // Account Settings
        usernameField.setText(rs.getString("username"));
    }

    @FXML
    private void savePersonalInfo(ActionEvent event) {
        try {
            Connection conn = DBConnection.getConnection();
            if (conn == null) return;
            
            String updateQuery = "UPDATE members SET first_name = ?, last_name = ?, email = ?, phone = ?, address = ? WHERE id = ?";
            PreparedStatement stmt = conn.prepareStatement(updateQuery);
            stmt.setString(1, firstNameField.getText());
            stmt.setString(2, lastNameField.getText());
            stmt.setString(3, emailField.getText());
            stmt.setString(4, phoneField.getText());
            stmt.setString(5, addressField.getText());
            stmt.setInt(6, memberId);
            stmt.executeUpdate();
            
            // Also update users table
            String userQuery = "UPDATE users SET email = ? WHERE id = ?";
            PreparedStatement userStmt = conn.prepareStatement(userQuery);
            userStmt.setString(1, emailField.getText());
            userStmt.setInt(2, userId);
            userStmt.executeUpdate();
            
            conn.close();
            
            showAlert("Success", "Personal information updated successfully!");
            loadProfileData(); // Refresh data
        } catch (Exception e) {
            e.printStackTrace();
            showAlert("Error", "Failed to update personal information: " + e.getMessage());
        }
    }

    @FXML
    private void saveAcademicInfo(ActionEvent event) {
        try {
            Connection conn = DBConnection.getConnection();
            if (conn == null) return;
            
            String updateQuery = "UPDATE members SET department = ?, year = ?, major = ?, advisor = ?, gpa = ? WHERE id = ?";
            PreparedStatement stmt = conn.prepareStatement(updateQuery);
            stmt.setString(1, departmentCombo.getValue());
            stmt.setString(2, yearCombo.getValue());
            stmt.setString(3, majorField.getText());
            stmt.setString(4, advisorField.getText());
            stmt.setString(5, gpaField.getText());
            stmt.setInt(6, memberId);
            stmt.executeUpdate();
            
            conn.close();
            
            showAlert("Success", "Academic information updated successfully!");
            loadProfileData(); // Refresh data
        } catch (Exception e) {
            e.printStackTrace();
            showAlert("Error", "Failed to update academic information: " + e.getMessage());
        }
    }

    @FXML
    private void changePassword(ActionEvent event) {
        String currentPassword = currentPasswordField.getText();
        String newPassword = newPasswordField.getText();
        String confirmPassword = confirmPasswordField.getText();
        
        if (currentPassword.isEmpty() || newPassword.isEmpty() || confirmPassword.isEmpty()) {
            showAlert("Error", "Please fill in all password fields.");
            return;
        }
        
        if (!newPassword.equals(confirmPassword)) {
            showAlert("Error", "New passwords do not match.");
            return;
        }
        
        if (newPassword.length() < 6) {
            showAlert("Error", "Password must be at least 6 characters long.");
            return;
        }
        
        try {
            Connection conn = DBConnection.getConnection();
            if (conn == null) return;
            
            // Verify current password
            String verifyQuery = "SELECT password FROM users WHERE id = ?";
            PreparedStatement verifyStmt = conn.prepareStatement(verifyQuery);
            verifyStmt.setInt(1, userId);
            ResultSet rs = verifyStmt.executeQuery();
            
            if (rs.next()) {
                String storedPassword = rs.getString("password");
                if (!storedPassword.equals(currentPassword)) {
                    showAlert("Error", "Current password is incorrect.");
                    return;
                }
            }
            
            // Update password
            String updateQuery = "UPDATE users SET password = ? WHERE id = ?";
            PreparedStatement updateStmt = conn.prepareStatement(updateQuery);
            updateStmt.setString(1, newPassword);
            updateStmt.setInt(2, userId);
            updateStmt.executeUpdate();
            
            conn.close();
            
            showAlert("Success", "Password changed successfully!");
            
            // Clear password fields
            currentPasswordField.clear();
            newPasswordField.clear();
            confirmPasswordField.clear();
        } catch (Exception e) {
            e.printStackTrace();
            showAlert("Error", "Failed to change password: " + e.getMessage());
        }
    }

    @FXML
    private void exportProfile(ActionEvent event) {
        try {
            // Create profile summary
            StringBuilder profile = new StringBuilder();
            profile.append("STUDENT PROFILE\n");
            profile.append("===============\n\n");
            profile.append("Personal Information:\n");
            profile.append("Name: ").append(firstNameField.getText()).append(" ").append(lastNameField.getText()).append("\n");
            profile.append("Email: ").append(emailField.getText()).append("\n");
            profile.append("Phone: ").append(phoneField.getText()).append("\n");
            profile.append("Address: ").append(addressField.getText()).append("\n\n");
            
            profile.append("Academic Information:\n");
            profile.append("Student ID: ").append(studentIdField.getText()).append("\n");
            profile.append("Department: ").append(departmentCombo.getValue()).append("\n");
            profile.append("Year: ").append(yearCombo.getValue()).append("\n");
            profile.append("Major: ").append(majorField.getText()).append("\n");
            profile.append("Advisor: ").append(advisorField.getText()).append("\n");
            profile.append("GPA: ").append(gpaField.getText()).append("\n\n");
            
            profile.append("Library Statistics:\n");
            profile.append("Total Borrowings: ").append(totalBorrowingsLabel.getText()).append("\n");
            profile.append("Current Borrowings: ").append(currentBorrowingsLabel.getText()).append("\n");
            profile.append("Total Fines: ").append(totalFinesLabel.getText()).append("\n");
            profile.append("Paid Fines: ").append(paidFinesLabel.getText()).append("\n");
            
            // Show in dialog
            TextArea textArea = new TextArea(profile.toString());
            textArea.setEditable(false);
            textArea.setPrefRowCount(20);
            textArea.setPrefColumnCount(50);
            
            Alert alert = new Alert(Alert.AlertType.INFORMATION);
            alert.setTitle("Profile Export");
            alert.setHeaderText("Your Profile Information");
            alert.getDialogPane().setContent(textArea);
            alert.showAndWait();
            
        } catch (Exception e) {
            e.printStackTrace();
            showAlert("Error", "Failed to export profile: " + e.getMessage());
        }
    }

    @FXML
    private void goBackToDashboard(ActionEvent event) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/resources/student_dashboard.fxml"));
            Parent root = loader.load();
            
            StudentDashboardController controller = loader.getController();
            controller.setUserInfo(studentName, "student", userId);
            
            Stage stage = (Stage) ((Node) event.getSource()).getScene().getWindow();
            stage.setScene(new Scene(root, 1000, 700));
            stage.setTitle("Student Dashboard");
            stage.show();
        } catch (Exception e) {
            e.printStackTrace();
            showAlert("Error", "Failed to return to dashboard: " + e.getMessage());
        }
    }

    private void showAlert(String title, String content) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(content);
        alert.showAndWait();
    }
} 