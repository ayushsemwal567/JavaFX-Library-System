package application;

import javafx.fxml.FXML;
import javafx.scene.control.Alert;
import javafx.scene.control.TextField;
import javafx.scene.control.TextArea;
import javafx.scene.control.Label;
import java.sql.Connection;
import java.sql.PreparedStatement;

public class AddMemberController {

    @FXML
    private TextField nameField;

    @FXML
    private TextField emailField;

    @FXML
    private TextField phoneField;

    @FXML
    private TextArea addressField;

    @FXML
    private Label messageLabel;

    @FXML
    private void handleAddMember() {
        String name = nameField.getText().trim();
        String email = emailField.getText().trim();
        String phone = phoneField.getText().trim();
        String address = addressField.getText().trim();

        // Validation
        if (name.isEmpty()) {
            messageLabel.setText("Name is required.");
            return;
        }

        try {
            Connection conn = DBConnection.getConnection();
            if (conn == null) {
                messageLabel.setText("Database connection failed.");
                return;
            }

            String query = "INSERT INTO members (name, email, phone, address) VALUES (?, ?, ?, ?)";
            PreparedStatement stmt = conn.prepareStatement(query);
            stmt.setString(1, name);
            stmt.setString(2, email);
            stmt.setString(3, phone);
            stmt.setString(4, address);
            stmt.executeUpdate();
            conn.close();

            // Clear fields after successful addition
            nameField.clear();
            emailField.clear();
            phoneField.clear();
            addressField.clear();
            messageLabel.setText("");

            Alert alert = new Alert(Alert.AlertType.INFORMATION);
            alert.setTitle("Success");
            alert.setHeaderText(null);
            alert.setContentText("Member added successfully!");
            alert.showAndWait();
        } catch (Exception e) {
            e.printStackTrace();
            messageLabel.setText("Error adding member: " + e.getMessage());
        }
    }
} 