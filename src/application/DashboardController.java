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

public class DashboardController {

    @FXML
    private Label welcomeLabel;
    
    private String currentUser;
    private String userRole;

    public void setUserInfo(String username, String role) {
        this.currentUser = username;
        this.userRole = role;
        if (welcomeLabel != null) {
            welcomeLabel.setText("Welcome, " + username + " (" + role + ")");
        }
    }

    @FXML
    private void handleLogout(ActionEvent event) {
        try {
            System.out.println("Logout button clicked");
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
    private void openAddBook(ActionEvent event) {
        try {
            System.out.println("Add Book button clicked");
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/resources/addbook.fxml"));
            Parent root = loader.load();
            Stage stage = new Stage();
            stage.setTitle("Add New Book - Library Management System");
            stage.setScene(new Scene(root, 500, 600));
            stage.setResizable(false);
            stage.show();
        } catch (Exception e) {
            e.printStackTrace();
            showAlert("Error", "Failed to open Add Book: " + e.getMessage());
        }
    }

    @FXML
    private void openViewBooks(ActionEvent event) {
        try {
            System.out.println("View Books button clicked");
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/resources/viewbooks.fxml"));
            Parent root = loader.load();
            Stage stage = new Stage();
            stage.setTitle("Book Catalog - Library Management System");
            stage.setScene(new Scene(root, 1000, 700));
            stage.setMinWidth(800);
            stage.setMinHeight(600);
            stage.show();
        } catch (Exception e) {
            e.printStackTrace();
            showAlert("Error", "Failed to open View Books: " + e.getMessage());
        }
    }

    @FXML
    private void openAddMember(ActionEvent event) {
        try {
            System.out.println("Add Member button clicked");
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/resources/addmember.fxml"));
            Parent root = loader.load();
            Stage stage = new Stage();
            stage.setTitle("Add New Member - Library Management System");
            stage.setScene(new Scene(root, 500, 600));
            stage.setResizable(false);
            stage.show();
        } catch (Exception e) {
            e.printStackTrace();
            showAlert("Error", "Failed to open Add Member: " + e.getMessage());
        }
    }

    @FXML
    private void openBorrowBook(ActionEvent event) {
        try {
            System.out.println("Borrow Book button clicked");
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/resources/borrowbook.fxml"));
            Parent root = loader.load();
            Stage stage = new Stage();
            stage.setTitle("Borrow Book - Library Management System");
            stage.setScene(new Scene(root, 500, 600));
            stage.setResizable(false);
            stage.show();
        } catch (Exception e) {
            e.printStackTrace();
            showAlert("Error", "Failed to open Borrow Book: " + e.getMessage());
        }
    }
    
    @FXML
    private void openReports(ActionEvent event) {
        try {
            System.out.println("Reports button clicked");
            showAlert("Reports", "Reports feature coming soon!\n\nThis will include:\n• Book borrowing statistics\n• Member activity reports\n• Overdue book reports\n• Popular books analysis");
        } catch (Exception e) {
            e.printStackTrace();
            showAlert("Error", "Failed to open Reports: " + e.getMessage());
        }
    }
    
    @FXML
    private void openSettings(ActionEvent event) {
        try {
            System.out.println("Settings button clicked");
            showAlert("Settings", "Settings feature coming soon!\n\nThis will include:\n• Database configuration\n• User management\n• System preferences\n• Backup and restore options");
        } catch (Exception e) {
            e.printStackTrace();
            showAlert("Error", "Failed to open Settings: " + e.getMessage());
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