package application;

import javafx.fxml.FXML;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.TextField;
import javafx.scene.control.Label;
import javafx.stage.Stage;
import java.sql.Connection;
import java.sql.PreparedStatement;

public class AddBookController {

    @FXML
    private TextField titleField;

    @FXML
    private TextField authorField;

    @FXML
    private TextField yearField;

    @FXML
    private TextField isbnField;

    @FXML
    private TextField quantityField;

    @FXML
    private Button addBookButton;

    @FXML
    private Label messageLabel;

    @FXML
    private void handleAddBook() {
        String title = titleField.getText().trim();
        String author = authorField.getText().trim();
        String yearStr = yearField.getText().trim();
        String isbn = isbnField.getText().trim();
        String quantityStr = quantityField.getText().trim();

        // Validation
        if (title.isEmpty() || author.isEmpty()) {
            messageLabel.setText("Title and Author are required fields.");
            return;
        }

        try {
            int year = yearStr.isEmpty() ? 0 : Integer.parseInt(yearStr);
            int quantity = quantityStr.isEmpty() ? 1 : Integer.parseInt(quantityStr);

            // Additional validation
            if (year != 0 && (year < 1000 || year > 2024)) {
                messageLabel.setText("Please enter a valid year between 1000 and 2024.");
                return;
            }
            
            if (quantity <= 0) {
                messageLabel.setText("Quantity must be greater than 0.");
                return;
            }

            Connection conn = DBConnection.getConnection();
            if (conn == null) {
                messageLabel.setText("Database connection failed.");
                return;
            }

            String query = "INSERT INTO books (title, author, year, isbn, quantity, available) VALUES (?, ?, ?, ?, ?, ?)";
            PreparedStatement stmt = conn.prepareStatement(query);
            stmt.setString(1, title);
            stmt.setString(2, author);
            stmt.setInt(3, year);
            stmt.setString(4, isbn);
            stmt.setInt(5, quantity);
            stmt.setInt(6, quantity);
            stmt.executeUpdate();
            conn.close();

            // Clear fields after successful addition
            clearForm();
            messageLabel.setText("");

            Alert alert = new Alert(Alert.AlertType.INFORMATION);
            alert.setTitle("Success");
            alert.setHeaderText("Book Added Successfully");
            alert.setContentText("The book '" + title + "' has been added to the library catalog.");
            alert.showAndWait();
        } catch (NumberFormatException e) {
            messageLabel.setText("Please enter valid numbers for year and quantity.");
        } catch (Exception e) {
            e.printStackTrace();
            messageLabel.setText("Error adding book: " + e.getMessage());
        }
    }
    
    @FXML
    private void clearForm() {
        titleField.clear();
        authorField.clear();
        yearField.clear();
        isbnField.clear();
        quantityField.clear();
        messageLabel.setText("");
    }
    
    @FXML
    private void closeWindow() {
        Stage stage = (Stage) titleField.getScene().getWindow();
        stage.close();
    }
}
