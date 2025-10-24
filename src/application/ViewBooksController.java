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
import java.sql.*;
import java.util.Optional;

public class ViewBooksController {
    
    @FXML private TableView<Book> booksTable;
    @FXML private TableColumn<Book, Integer> idColumn;
    @FXML private TableColumn<Book, String> titleColumn;
    @FXML private TableColumn<Book, String> authorColumn;
    @FXML private TableColumn<Book, Integer> yearColumn;
    @FXML private TableColumn<Book, String> isbnColumn;
    @FXML private TableColumn<Book, Integer> quantityColumn;
    @FXML private TableColumn<Book, Integer> availableColumn;
    @FXML private TextField searchField;
    
    private ObservableList<Book> booksList = FXCollections.observableArrayList();
    
    @FXML
    public void initialize() {
        setupTableColumns();
        loadBooks();
    }
    
    private void setupTableColumns() {
        idColumn.setCellValueFactory(new PropertyValueFactory<>("id"));
        titleColumn.setCellValueFactory(new PropertyValueFactory<>("title"));
        authorColumn.setCellValueFactory(new PropertyValueFactory<>("author"));
        yearColumn.setCellValueFactory(new PropertyValueFactory<>("year"));
        isbnColumn.setCellValueFactory(new PropertyValueFactory<>("isbn"));
        quantityColumn.setCellValueFactory(new PropertyValueFactory<>("quantity"));
        availableColumn.setCellValueFactory(new PropertyValueFactory<>("available"));
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
            
            String query = "SELECT * FROM books ORDER BY title";
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
            String query = "SELECT * FROM books WHERE title LIKE ? OR author LIKE ? OR isbn LIKE ? ORDER BY title";
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
    }
    
    @FXML
    private void openAddBook() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/resources/addbook.fxml"));
            Parent root = loader.load();
            Stage stage = new Stage();
            stage.setTitle("Add New Book");
            stage.setScene(new Scene(root));
            stage.show();
            
            // Refresh the books list when the add book window is closed
            stage.setOnHidden(e -> loadBooks());
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
    
    @FXML
    private void editBook() {
        Book selectedBook = booksTable.getSelectionModel().getSelectedItem();
        if (selectedBook == null) {
            showAlert("Selection Required", "Please select a book to edit.");
            return;
        }
        
        // For now, just show book details
        showAlert("Book Details", 
            "Title: " + selectedBook.getTitle() + "\n" +
            "Author: " + selectedBook.getAuthor() + "\n" +
            "Year: " + selectedBook.getYear() + "\n" +
            "ISBN: " + selectedBook.getIsbn() + "\n" +
            "Quantity: " + selectedBook.getQuantity() + "\n" +
            "Available: " + selectedBook.getAvailable());
    }
    
    @FXML
    private void deleteBook() {
        Book selectedBook = booksTable.getSelectionModel().getSelectedItem();
        if (selectedBook == null) {
            showAlert("Selection Required", "Please select a book to delete.");
            return;
        }
        
        Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
        alert.setTitle("Confirm Delete");
        alert.setHeaderText("Delete Book");
        alert.setContentText("Are you sure you want to delete '" + selectedBook.getTitle() + "'?");
        
        Optional<ButtonType> result = alert.showAndWait();
        if (result.isPresent() && result.get() == ButtonType.OK) {
            try {
                Connection conn = DBConnection.getConnection();
                String query = "DELETE FROM books WHERE id = ?";
                PreparedStatement stmt = conn.prepareStatement(query);
                stmt.setInt(1, selectedBook.getId());
                stmt.executeUpdate();
                conn.close();
                
                loadBooks();
                showAlert("Success", "Book deleted successfully.");
            } catch (Exception e) {
                e.printStackTrace();
                showAlert("Error", "Failed to delete book: " + e.getMessage());
            }
        }
    }
    
    @FXML
    private void closeWindow() {
        Stage stage = (Stage) booksTable.getScene().getWindow();
        stage.close();
    }
    
    private void showAlert(String title, String content) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(content);
        alert.showAndWait();
    }
    
    // Book model class
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
        
        // Setters
        public void setId(int id) { this.id = id; }
        public void setTitle(String title) { this.title = title; }
        public void setAuthor(String author) { this.author = author; }
        public void setYear(int year) { this.year = year; }
        public void setIsbn(String isbn) { this.isbn = isbn; }
        public void setQuantity(int quantity) { this.quantity = quantity; }
        public void setAvailable(int available) { this.available = available; }
    }
} 