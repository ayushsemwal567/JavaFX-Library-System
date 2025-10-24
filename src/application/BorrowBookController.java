package application;

import javafx.fxml.FXML;
import javafx.scene.control.Alert;
import javafx.scene.control.ComboBox;
import javafx.scene.control.DatePicker;
import javafx.scene.control.Label;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.time.LocalDate;

public class BorrowBookController {

    @FXML
    private ComboBox<String> bookComboBox;

    @FXML
    private ComboBox<String> memberComboBox;

    @FXML
    private DatePicker borrowDatePicker;

    @FXML
    private DatePicker returnDatePicker;

    @FXML
    private Label messageLabel;

    @FXML
    public void initialize() {
        loadBooks();
        loadMembers();
        borrowDatePicker.setValue(LocalDate.now());
        returnDatePicker.setValue(LocalDate.now().plusDays(14)); // Default 2 weeks
    }

    private void loadBooks() {
        try {
            Connection conn = DBConnection.getConnection();
            String query = "SELECT id, title, author FROM books WHERE available > 0";
            PreparedStatement stmt = conn.prepareStatement(query);
            ResultSet rs = stmt.executeQuery();
            
            ObservableList<String> books = FXCollections.observableArrayList();
            while (rs.next()) {
                books.add(rs.getInt("id") + " - " + rs.getString("title") + " by " + rs.getString("author"));
            }
            bookComboBox.setItems(books);
            conn.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void loadMembers() {
        try {
            Connection conn = DBConnection.getConnection();
            String query = "SELECT id, name FROM members";
            PreparedStatement stmt = conn.prepareStatement(query);
            ResultSet rs = stmt.executeQuery();
            
            ObservableList<String> members = FXCollections.observableArrayList();
            while (rs.next()) {
                members.add(rs.getInt("id") + " - " + rs.getString("name"));
            }
            memberComboBox.setItems(members);
            conn.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @FXML
    private void handleBorrowBook() {
        if (bookComboBox.getValue() == null || memberComboBox.getValue() == null) {
            messageLabel.setText("Please select both book and member.");
            return;
        }

        try {
            // Extract IDs from combo box values
            int bookId = Integer.parseInt(bookComboBox.getValue().split(" - ")[0]);
            int memberId = Integer.parseInt(memberComboBox.getValue().split(" - ")[0]);
            
            LocalDate borrowDate = borrowDatePicker.getValue();
            LocalDate returnDate = returnDatePicker.getValue();

            Connection conn = DBConnection.getConnection();
            
            // Check if book is available
            String checkQuery = "SELECT available FROM books WHERE id = ?";
            PreparedStatement checkStmt = conn.prepareStatement(checkQuery);
            checkStmt.setInt(1, bookId);
            ResultSet rs = checkStmt.executeQuery();
            
            if (rs.next() && rs.getInt("available") > 0) {
                // Insert borrowing record
                String borrowQuery = "INSERT INTO borrowings (book_id, member_id, borrow_date, return_date) VALUES (?, ?, ?, ?)";
                PreparedStatement borrowStmt = conn.prepareStatement(borrowQuery);
                borrowStmt.setInt(1, bookId);
                borrowStmt.setInt(2, memberId);
                borrowStmt.setDate(3, java.sql.Date.valueOf(borrowDate));
                borrowStmt.setDate(4, java.sql.Date.valueOf(returnDate));
                borrowStmt.executeUpdate();

                // Update book availability
                String updateQuery = "UPDATE books SET available = available - 1 WHERE id = ?";
                PreparedStatement updateStmt = conn.prepareStatement(updateQuery);
                updateStmt.setInt(1, bookId);
                updateStmt.executeUpdate();

                conn.close();

                Alert alert = new Alert(Alert.AlertType.INFORMATION);
                alert.setTitle("Success");
                alert.setHeaderText(null);
                alert.setContentText("Book borrowed successfully!");
                alert.showAndWait();

                // Refresh the book list
                loadBooks();
            } else {
                messageLabel.setText("Book is not available for borrowing.");
            }
        } catch (Exception e) {
            e.printStackTrace();
            messageLabel.setText("Error borrowing book: " + e.getMessage());
        }
    }
} 