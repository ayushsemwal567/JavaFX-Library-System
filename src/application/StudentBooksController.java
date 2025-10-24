package application;

import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.stage.Stage;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.scene.Node;
import javafx.event.ActionEvent;
import java.sql.*;

public class StudentBooksController {
    
    @FXML private TableView<Book> booksTable;
    @FXML private TableColumn<Book, Integer> idColumn;
    @FXML private TableColumn<Book, String> titleColumn;
    @FXML private TableColumn<Book, String> authorColumn;
    @FXML private TableColumn<Book, Integer> yearColumn;
    @FXML private TableColumn<Book, String> isbnColumn;
    @FXML private TableColumn<Book, Integer> availableColumn;
    @FXML private TableColumn<Book, String> borrowColumn;
    @FXML private TextField searchField;
    @FXML private Label totalBooksLabel;
    @FXML private Label availableBooksLabel;
    @FXML private Label borrowedBooksLabel;
    
    private ObservableList<Book> booksList = FXCollections.observableArrayList();
    private int currentUserId;
    
    public void setUserId(int userId) {
        this.currentUserId = userId;
    }
    
    @FXML
    public void initialize() {
        setupTableColumns();
        loadBooks();
        updateStatistics();
    }
    
    private void setupTableColumns() {
        idColumn.setCellValueFactory(new PropertyValueFactory<>("id"));
        titleColumn.setCellValueFactory(new PropertyValueFactory<>("title"));
        authorColumn.setCellValueFactory(new PropertyValueFactory<>("author"));
        yearColumn.setCellValueFactory(new PropertyValueFactory<>("year"));
        isbnColumn.setCellValueFactory(new PropertyValueFactory<>("isbn"));
        availableColumn.setCellValueFactory(new PropertyValueFactory<>("available"));
        
        // Setup borrow button column
        borrowColumn.setCellFactory(param -> new TableCell<Book, String>() {
            private final Button borrowButton = new Button("Borrow");
            
            {
                borrowButton.setStyle("-fx-background-color: #27ae60; -fx-text-fill: white; -fx-font-size: 10px;");
                borrowButton.setOnAction(e -> {
                    Book book = getTableView().getItems().get(getIndex());
                    borrowBook(book);
                });
            }
            
            @Override
            protected void updateItem(String item, boolean empty) {
                super.updateItem(item, empty);
                if (empty) {
                    setGraphic(null);
                } else {
                    Book book = getTableView().getItems().get(getIndex());
                    if (book.getAvailable() > 0) {
                        borrowButton.setText("Borrow");
                        borrowButton.setStyle("-fx-background-color: #27ae60; -fx-text-fill: white; -fx-font-size: 10px;");
                        borrowButton.setDisable(false);
                    } else {
                        borrowButton.setText("Not Available");
                        borrowButton.setStyle("-fx-background-color: #95a5a6; -fx-text-fill: white; -fx-font-size: 10px;");
                        borrowButton.setDisable(true);
                    }
                    setGraphic(borrowButton);
                }
            }
        });
    }
    
    @FXML
    private void loadBooks() {
        booksList.clear();
        try {
            Connection conn = DBConnection.getConnection();
            if (conn == null) {
                showAlert("Database Error", "Could not connect to database.");
                return;
            }
            
            // Only show books that are available for borrowing
            String query = "SELECT * FROM books WHERE available > 0 ORDER BY title";
            PreparedStatement stmt = conn.prepareStatement(query);
            ResultSet rs = stmt.executeQuery();
            
            while (rs.next()) {
                Book book = new Book(
                    rs.getInt("id"),
                    rs.getString("title"),
                    rs.getString("author"),
                    rs.getInt("year"),
                    rs.getString("isbn"),
                    rs.getInt("quantity"),
                    rs.getInt("available")
                );
                booksList.add(book);
            }
            
            booksTable.setItems(booksList);
            conn.close();
        } catch (Exception e) {
            e.printStackTrace();
            showAlert("Error", "Failed to load books: " + e.getMessage());
        }
    }
    
    @FXML
    private void searchBooks() {
        String searchTerm = searchField.getText().trim();
        if (searchTerm.isEmpty()) {
            loadBooks();
            return;
        }
        
        booksList.clear();
        try {
            Connection conn = DBConnection.getConnection();
            String query = "SELECT * FROM books WHERE (title LIKE ? OR author LIKE ? OR isbn LIKE ?) AND available > 0 ORDER BY title";
            PreparedStatement stmt = conn.prepareStatement(query);
            String searchPattern = "%" + searchTerm + "%";
            stmt.setString(1, searchPattern);
            stmt.setString(2, searchPattern);
            stmt.setString(3, searchPattern);
            
            ResultSet rs = stmt.executeQuery();
            while (rs.next()) {
                Book book = new Book(
                    rs.getInt("id"),
                    rs.getString("title"),
                    rs.getString("author"),
                    rs.getInt("year"),
                    rs.getString("isbn"),
                    rs.getInt("quantity"),
                    rs.getInt("available")
                );
                booksList.add(book);
            }
            
            booksTable.setItems(booksList);
            conn.close();
        } catch (Exception e) {
            e.printStackTrace();
            showAlert("Error", "Search failed: " + e.getMessage());
        }
    }
    
    @FXML
    private void refreshBooks() {
        loadBooks();
        searchField.clear();
        updateStatistics();
    }
    
    private void borrowBook(Book book) {
        if (currentUserId == 0) {
            showAlert("Error", "User not identified. Please login again.");
            return;
        }
        
        try {
            Connection conn = DBConnection.getConnection();
            
            // Check if student is blocked
            String checkStatus = "SELECT status FROM members WHERE user_id = ?";
            PreparedStatement statusStmt = conn.prepareStatement(checkStatus);
            statusStmt.setInt(1, currentUserId);
            ResultSet statusRs = statusStmt.executeQuery();
            
            if (statusRs.next()) {
                String status = statusRs.getString("status");
                if (!"active".equals(status)) {
                    showAlert("Cannot Borrow", "Your account is " + status + ". Please contact the librarian.");
                    conn.close();
                    return;
                }
            }
            
            // Check if student has overdue books
            String checkOverdue = """
                SELECT COUNT(*) FROM borrowings b 
                JOIN members m ON b.member_id = m.id 
                WHERE m.user_id = ? AND b.status = 'borrowed' AND b.due_date < CURDATE()
                """;
            PreparedStatement overdueStmt = conn.prepareStatement(checkOverdue);
            overdueStmt.setInt(1, currentUserId);
            ResultSet overdueRs = overdueStmt.executeQuery();
            overdueRs.next();
            int overdueCount = overdueRs.getInt(1);
            
            if (overdueCount > 0) {
                showAlert("Cannot Borrow", "You have " + overdueCount + " overdue book(s). Please return them first.");
                conn.close();
                return;
            }
            
            // Get member ID
            String getMemberId = "SELECT id FROM members WHERE user_id = ?";
            PreparedStatement memberStmt = conn.prepareStatement(getMemberId);
            memberStmt.setInt(1, currentUserId);
            ResultSet memberRs = memberStmt.executeQuery();
            
            if (!memberRs.next()) {
                showAlert("Error", "Member record not found.");
                conn.close();
                return;
            }
            
            int memberId = memberRs.getInt("id");
            
            // Create borrowing record
            String insertBorrowing = """
                INSERT INTO borrowings (book_id, member_id, borrowed_by, borrow_date, due_date, status) 
                VALUES (?, ?, ?, CURDATE(), DATE_ADD(CURDATE(), INTERVAL 14 DAY), 'borrowed')
                """;
            PreparedStatement borrowStmt = conn.prepareStatement(insertBorrowing);
            borrowStmt.setInt(1, book.getId());
            borrowStmt.setInt(2, memberId);
            borrowStmt.setInt(3, currentUserId);
            borrowStmt.executeUpdate();
            
            // Update book availability
            String updateBook = "UPDATE books SET available = available - 1 WHERE id = ?";
            PreparedStatement updateStmt = conn.prepareStatement(updateBook);
            updateStmt.setInt(1, book.getId());
            updateStmt.executeUpdate();
            
            conn.close();
            
            showAlert("Success", "Book '" + book.getTitle() + "' borrowed successfully!\nDue date: " + 
                     java.time.LocalDate.now().plusDays(14));
            
            // Refresh the table
            loadBooks();
            updateStatistics();
            
        } catch (Exception e) {
            e.printStackTrace();
            showAlert("Error", "Failed to borrow book: " + e.getMessage());
        }
    }
    
    private void updateStatistics() {
        try {
            Connection conn = DBConnection.getConnection();
            
            // Total books
            String totalQuery = "SELECT COUNT(*) FROM books";
            ResultSet totalRs = conn.createStatement().executeQuery(totalQuery);
            totalRs.next();
            int totalBooks = totalRs.getInt(1);
            
            // Available books
            String availableQuery = "SELECT COUNT(*) FROM books WHERE available > 0";
            ResultSet availableRs = conn.createStatement().executeQuery(availableQuery);
            availableRs.next();
            int availableBooks = availableRs.getInt(1);
            
            // Student's borrowed books
            String borrowedQuery = """
                SELECT COUNT(*) FROM borrowings b 
                JOIN members m ON b.member_id = m.id 
                WHERE m.user_id = ? AND b.status = 'borrowed'
                """;
            PreparedStatement borrowedStmt = conn.prepareStatement(borrowedQuery);
            borrowedStmt.setInt(1, currentUserId);
            ResultSet borrowedRs = borrowedStmt.executeQuery();
            borrowedRs.next();
            int borrowedBooks = borrowedRs.getInt(1);
            
            totalBooksLabel.setText("Total Books: " + totalBooks);
            availableBooksLabel.setText("Available: " + availableBooks);
            borrowedBooksLabel.setText("Borrowed: " + borrowedBooks);
            
            conn.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
    
    @FXML
    private void goBackToDashboard(ActionEvent event) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/resources/student_dashboard.fxml"));
            Parent root = loader.load();
            Stage stage = (Stage) ((Node) event.getSource()).getScene().getWindow();
            stage.setScene(new Scene(root, 1200, 800));
            stage.setTitle("Student Portal - Library Management System");
            stage.show();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
    
    @FXML
    private void openMyBorrowings(ActionEvent event) {
        showAlert("My Borrowings", "Feature coming soon!\n\nThis will show:\n• Currently borrowed books\n• Due dates\n• Return status\n• Fine information");
    }
    
    private void showAlert(String title, String content) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(content);
        alert.showAndWait();
    }
    
    // Book data class
    public static class Book {
        private int id;
        private String title;
        private String author;
        private int year;
        private String isbn;
        private int quantity;
        private int available;
        
        public Book(int id, String title, String author, int year, String isbn, int quantity, int available) {
            this.id = id;
            this.title = title;
            this.author = author;
            this.year = year;
            this.isbn = isbn;
            this.quantity = quantity;
            this.available = available;
        }
        
        // Getters
        public int getId() { return id; }
        public String getTitle() { return title; }
        public String getAuthor() { return author; }
        public int getYear() { return year; }
        public String getIsbn() { return isbn; }
        public int getQuantity() { return quantity; }
        public int getAvailable() { return available; }
    }
} 