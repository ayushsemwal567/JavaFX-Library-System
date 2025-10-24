package application;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;
import javafx.scene.Node;
import javafx.fxml.FXMLLoader;
import javafx.scene.control.Label;
import javafx.scene.control.Alert;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;

public class StudentDashboardController {

    @FXML
    private Label welcomeLabel;
    
    @FXML
    private Label booksBorrowedLabel;
    
    @FXML
    private Label totalFinesLabel;
    
    @FXML
    private Label accountStatusLabel;
    
    private String currentUser;
    private String userRole;
    private int userId;

    public void setUserInfo(String username, String role, int userId) {
        this.currentUser = username;
        this.userRole = role;
        this.userId = userId;
        if (welcomeLabel != null) {
            welcomeLabel.setText("Welcome, " + username + " (" + role + ")");
        }
        loadStudentStats();
    }
    
    private void loadStudentStats() {
        try {
            Connection conn = DBConnection.getConnection();
            if (conn == null) return;
            
            // Get member ID for this user
            String memberQuery = "SELECT id, status, total_fines FROM members WHERE user_id = ?";
            PreparedStatement memberStmt = conn.prepareStatement(memberQuery);
            memberStmt.setInt(1, userId);
            ResultSet memberRs = memberStmt.executeQuery();
            
            if (memberRs.next()) {
                int memberId = memberRs.getInt("id");
                String status = memberRs.getString("status");
                double totalFines = memberRs.getDouble("total_fines");
                
                // Count borrowed books
                String borrowQuery = "SELECT COUNT(*) FROM borrowings WHERE member_id = ? AND status = 'borrowed'";
                PreparedStatement borrowStmt = conn.prepareStatement(borrowQuery);
                borrowStmt.setInt(1, memberId);
                ResultSet borrowRs = borrowStmt.executeQuery();
                borrowRs.next();
                int borrowedCount = borrowRs.getInt(1);
                
                // Update labels
                booksBorrowedLabel.setText("Books Borrowed: " + borrowedCount);
                totalFinesLabel.setText("Total Fines: $" + String.format("%.2f", totalFines));
                accountStatusLabel.setText("Status: " + status.substring(0, 1).toUpperCase() + status.substring(1));
                
                // Color coding for status
                if (status.equals("active")) {
                    accountStatusLabel.setStyle("-fx-text-fill: #27ae60; -fx-font-weight: bold;");
                } else {
                    accountStatusLabel.setStyle("-fx-text-fill: #e74c3c; -fx-font-weight: bold;");
                }
            }
            
            conn.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @FXML
    private void handleLogout(ActionEvent event) {
        try {
            Parent root = FXMLLoader.load(getClass().getResource("/resources/login.fxml"));
            Stage stage = (Stage) ((Node) event.getSource()).getScene().getWindow();
            stage.setScene(new Scene(root, 400, 500));
            stage.setTitle("Library Management Login");
            stage.setResizable(false);
            stage.show();
        } catch (Exception e) {
            e.printStackTrace();
            showAlert("Error", "Logout failed: " + e.getMessage());
        }
    }

    @FXML
    private void openBrowseBooks(ActionEvent event) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/resources/student_books.fxml"));
            Parent root = loader.load();
            
            // Set the user ID in the controller
            StudentBooksController controller = loader.getController();
            controller.setUserId(userId);
            
            Stage stage = new Stage();
            stage.setTitle("Browse Books - Student Portal");
            stage.setScene(new Scene(root, 1000, 700));
            stage.setMinWidth(800);
            stage.setMinHeight(600);
            stage.show();
        } catch (Exception e) {
            e.printStackTrace();
            showAlert("Error", "Failed to open Browse Books: " + e.getMessage());
        }
    }

    @FXML
    private void openMyBorrowings(ActionEvent event) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/resources/my_borrowings.fxml"));
            Parent root = loader.load();
            
            // Set the user info in the controller
            MyBorrowingsController controller = loader.getController();
            controller.setUserInfo(userId, currentUser);
            
            Stage stage = new Stage();
            stage.setTitle("My Borrowings - Student Portal");
            stage.setScene(new Scene(root, 1200, 800));
            stage.setMinWidth(1000);
            stage.setMinHeight(700);
            stage.show();
        } catch (Exception e) {
            e.printStackTrace();
            showAlert("Error", "Failed to open My Borrowings: " + e.getMessage());
        }
    }

    @FXML
    private void openMyFines(ActionEvent event) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/resources/my_fines.fxml"));
            Parent root = loader.load();
            
            // Set the user info in the controller
            MyFinesController controller = loader.getController();
            controller.setUserInfo(userId, currentUser);
            
            Stage stage = new Stage();
            stage.setTitle("My Fines - Student Portal");
            stage.setScene(new Scene(root, 1200, 800));
            stage.setMinWidth(1000);
            stage.setMinHeight(700);
            stage.show();
        } catch (Exception e) {
            e.printStackTrace();
            showAlert("Error", "Failed to open My Fines: " + e.getMessage());
        }
    }

    @FXML
    private void openSearchBooks(ActionEvent event) {
        try {
            showAlert("Search Books", "Feature coming soon!\n\nThis will allow:\n• Search by title, author, ISBN\n• Filter by category\n• Advanced search options\n• Save search preferences");
        } catch (Exception e) {
            e.printStackTrace();
            showAlert("Error", "Failed to open Search Books: " + e.getMessage());
        }
    }

    @FXML
    private void openDueDates(ActionEvent event) {
        try {
            showAlert("Due Dates", "Feature coming soon!\n\nThis will show:\n• Upcoming due dates\n• Overdue notifications\n• Extension requests\n• Return reminders");
        } catch (Exception e) {
            e.printStackTrace();
            showAlert("Error", "Failed to open Due Dates: " + e.getMessage());
        }
    }

    @FXML
    private void openMyProfile(ActionEvent event) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/resources/my_profile.fxml"));
            Parent root = loader.load();
            
            // Set the user info in the controller
            MyProfileController controller = loader.getController();
            controller.setUserInfo(userId, currentUser);
            
            Stage stage = new Stage();
            stage.setTitle("My Profile - Student Portal");
            stage.setScene(new Scene(root, 1200, 800));
            stage.setMinWidth(1000);
            stage.setMinHeight(700);
            stage.show();
        } catch (Exception e) {
            e.printStackTrace();
            showAlert("Error", "Failed to open My Profile: " + e.getMessage());
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