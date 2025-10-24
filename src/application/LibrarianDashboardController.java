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

public class LibrarianDashboardController {

    @FXML
    private Label welcomeLabel;
    
    @FXML
    private Label totalBooksLabel;
    
    @FXML
    private Label booksBorrowedLabel;
    
    @FXML
    private Label overdueBooksLabel;
    
    @FXML
    private Label totalMembersLabel;
    
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
        loadLibrarianStats();
    }
    
    private void loadLibrarianStats() {
        try {
            Connection conn = DBConnection.getConnection();
            if (conn == null) return;
            
            // Total books
            String totalBooksQuery = "SELECT COUNT(*) FROM books";
            ResultSet totalBooksRs = conn.createStatement().executeQuery(totalBooksQuery);
            totalBooksRs.next();
            int totalBooks = totalBooksRs.getInt(1);
            
            // Books borrowed
            String borrowedQuery = "SELECT COUNT(*) FROM borrowings WHERE status = 'borrowed'";
            ResultSet borrowedRs = conn.createStatement().executeQuery(borrowedQuery);
            borrowedRs.next();
            int borrowedCount = borrowedRs.getInt(1);
            
            // Overdue books
            String overdueQuery = "SELECT COUNT(*) FROM borrowings WHERE status = 'borrowed' AND due_date < CURDATE()";
            ResultSet overdueRs = conn.createStatement().executeQuery(overdueQuery);
            overdueRs.next();
            int overdueCount = overdueRs.getInt(1);
            
            // Total members
            String membersQuery = "SELECT COUNT(*) FROM members";
            ResultSet membersRs = conn.createStatement().executeQuery(membersQuery);
            membersRs.next();
            int totalMembers = membersRs.getInt(1);
            
            // Update labels
            totalBooksLabel.setText("Total Books: " + totalBooks);
            booksBorrowedLabel.setText("Books Borrowed: " + borrowedCount);
            overdueBooksLabel.setText("Overdue Books: " + overdueCount);
            totalMembersLabel.setText("Total Members: " + totalMembers);
            
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
    private void openBookManagement(ActionEvent event) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/resources/viewbooks.fxml"));
            Parent root = loader.load();
            Stage stage = new Stage();
            stage.setTitle("Book Management - Librarian Portal");
            stage.setScene(new Scene(root, 1200, 800));
            stage.setMinWidth(1000);
            stage.setMinHeight(700);
            stage.show();
        } catch (Exception e) {
            e.printStackTrace();
            showAlert("Error", "Failed to open Book Management: " + e.getMessage());
        }
    }

    @FXML
    private void openMemberManagement(ActionEvent event) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/resources/member_management.fxml"));
            Parent root = loader.load();
            Stage stage = new Stage();
            stage.setTitle("Member Management - Librarian Portal");
            stage.setScene(new Scene(root, 1200, 800));
            stage.setMinWidth(1000);
            stage.setMinHeight(700);
            stage.show();
        } catch (Exception e) {
            e.printStackTrace();
            showAlert("Error", "Failed to open Member Management: " + e.getMessage());
        }
    }

    @FXML
    private void openBorrowReturn(ActionEvent event) {
        try {
            showAlert("Borrow/Return Processing", "Feature coming soon!\n\nThis will allow:\n• Process book borrowings\n• Process book returns\n• Check member eligibility\n• Calculate due dates\n• Handle renewals");
        } catch (Exception e) {
            e.printStackTrace();
            showAlert("Error", "Failed to open Borrow/Return: " + e.getMessage());
        }
    }

    @FXML
    private void openFineManagement(ActionEvent event) {
        try {
            showAlert("Fine Management", "Feature coming soon!\n\nThis will allow:\n• View all fines\n• Calculate overdue fines\n• Process fine payments\n• Waive fines\n• Generate fine reports");
        } catch (Exception e) {
            e.printStackTrace();
            showAlert("Error", "Failed to open Fine Management: " + e.getMessage());
        }
    }

    @FXML
    private void openReports(ActionEvent event) {
        try {
            showAlert("Reports", "Feature coming soon!\n\nThis will include:\n• Borrowing statistics\n• Popular books report\n• Overdue books report\n• Member activity report\n• Fine collection report");
        } catch (Exception e) {
            e.printStackTrace();
            showAlert("Error", "Failed to open Reports: " + e.getMessage());
        }
    }

    @FXML
    private void openAdvancedSearch(ActionEvent event) {
        try {
            showAlert("Advanced Search", "Feature coming soon!\n\nThis will allow:\n• Search by multiple criteria\n• Filter by category, year, author\n• Save search queries\n• Export search results\n• Advanced book analytics");
        } catch (Exception e) {
            e.printStackTrace();
            showAlert("Error", "Failed to open Advanced Search: " + e.getMessage());
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