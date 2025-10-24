package application;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.Node;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.HBox;
import javafx.stage.Stage;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import java.sql.*;
import java.time.LocalDate;
import java.time.temporal.ChronoUnit;

public class MyBorrowingsController {

    @FXML private Label studentNameLabel;
    @FXML private Label currentBorrowingsLabel;
    @FXML private Label overdueLabel;
    @FXML private Label totalFinesLabel;
    
    // Current Borrowings Table
    @FXML private TableView<BorrowingRecord> currentBorrowingsTable;
    @FXML private TableColumn<BorrowingRecord, String> bookTitleCol;
    @FXML private TableColumn<BorrowingRecord, String> authorCol;
    @FXML private TableColumn<BorrowingRecord, String> borrowDateCol;
    @FXML private TableColumn<BorrowingRecord, String> dueDateCol;
    @FXML private TableColumn<BorrowingRecord, String> daysLeftCol;
    @FXML private TableColumn<BorrowingRecord, String> fineAmountCol;
    @FXML private TableColumn<BorrowingRecord, String> actionsCol;
    
    // Borrowing History Table
    @FXML private TableView<BorrowingRecord> borrowingHistoryTable;
    @FXML private TableColumn<BorrowingRecord, String> historyBookTitleCol;
    @FXML private TableColumn<BorrowingRecord, String> historyAuthorCol;
    @FXML private TableColumn<BorrowingRecord, String> historyBorrowDateCol;
    @FXML private TableColumn<BorrowingRecord, String> historyReturnDateCol;
    @FXML private TableColumn<BorrowingRecord, String> historyStatusCol;
    @FXML private TableColumn<BorrowingRecord, String> historyFineCol;
    
    // Due Dates Table
    @FXML private TableView<BorrowingRecord> dueDatesTable;
    @FXML private TableColumn<BorrowingRecord, String> dueBookTitleCol;
    @FXML private TableColumn<BorrowingRecord, String> dueDateCol2;
    @FXML private TableColumn<BorrowingRecord, String> daysUntilDueCol;
    @FXML private TableColumn<BorrowingRecord, String> urgencyCol;
    @FXML private TableColumn<BorrowingRecord, String> fineRateCol;
    
    private int userId;
    private String studentName;
    private ObservableList<BorrowingRecord> currentBorrowings = FXCollections.observableArrayList();
    private ObservableList<BorrowingRecord> borrowingHistory = FXCollections.observableArrayList();
    private ObservableList<BorrowingRecord> dueDates = FXCollections.observableArrayList();

    public void setUserInfo(int userId, String studentName) {
        this.userId = userId;
        this.studentName = studentName;
        if (studentNameLabel != null) {
            studentNameLabel.setText(studentName);
        }
        initializeTables();
        loadData();
    }

    private void initializeTables() {
        // Initialize Current Borrowings Table
        bookTitleCol.setCellValueFactory(new PropertyValueFactory<>("bookTitle"));
        authorCol.setCellValueFactory(new PropertyValueFactory<>("author"));
        borrowDateCol.setCellValueFactory(new PropertyValueFactory<>("borrowDate"));
        dueDateCol.setCellValueFactory(new PropertyValueFactory<>("dueDate"));
        daysLeftCol.setCellValueFactory(new PropertyValueFactory<>("daysLeft"));
        fineAmountCol.setCellValueFactory(new PropertyValueFactory<>("fineAmount"));
        
        // Add action buttons to current borrowings
        actionsCol.setCellFactory(param -> new TableCell<BorrowingRecord, String>() {
            private final Button renewButton = new Button("Renew");
            private final Button returnButton = new Button("Return");
            
            {
                renewButton.setStyle("-fx-background-color: #3498db; -fx-text-fill: white; -fx-font-size: 10px; -fx-padding: 5;");
                returnButton.setStyle("-fx-background-color: #27ae60; -fx-text-fill: white; -fx-font-size: 10px; -fx-padding: 5;");
                
                renewButton.setOnAction(e -> {
                    BorrowingRecord record = getTableView().getItems().get(getIndex());
                    renewBook(record);
                });
                
                returnButton.setOnAction(e -> {
                    BorrowingRecord record = getTableView().getItems().get(getIndex());
                    returnBook(record);
                });
            }
            
            @Override
            protected void updateItem(String item, boolean empty) {
                super.updateItem(item, empty);
                if (empty) {
                    setGraphic(null);
                } else {
                    HBox buttons = new HBox(5);
                    buttons.getChildren().addAll(renewButton, returnButton);
                    setGraphic(buttons);
                }
            }
        });
        
        // Initialize Borrowing History Table
        historyBookTitleCol.setCellValueFactory(new PropertyValueFactory<>("bookTitle"));
        historyAuthorCol.setCellValueFactory(new PropertyValueFactory<>("author"));
        historyBorrowDateCol.setCellValueFactory(new PropertyValueFactory<>("borrowDate"));
        historyReturnDateCol.setCellValueFactory(new PropertyValueFactory<>("returnDate"));
        historyStatusCol.setCellValueFactory(new PropertyValueFactory<>("status"));
        historyFineCol.setCellValueFactory(new PropertyValueFactory<>("finePaid"));
        
        // Initialize Due Dates Table
        dueBookTitleCol.setCellValueFactory(new PropertyValueFactory<>("bookTitle"));
        dueDateCol2.setCellValueFactory(new PropertyValueFactory<>("dueDate"));
        daysUntilDueCol.setCellValueFactory(new PropertyValueFactory<>("daysUntilDue"));
        urgencyCol.setCellValueFactory(new PropertyValueFactory<>("urgency"));
        fineRateCol.setCellValueFactory(new PropertyValueFactory<>("fineRate"));
        
        // Set table data
        currentBorrowingsTable.setItems(currentBorrowings);
        borrowingHistoryTable.setItems(borrowingHistory);
        dueDatesTable.setItems(dueDates);
    }

    private void loadData() {
        loadCurrentBorrowings();
        loadBorrowingHistory();
        loadDueDates();
        updateSummaryCards();
    }

    private void loadCurrentBorrowings() {
        currentBorrowings.clear();
        try {
            Connection conn = DBConnection.getConnection();
            if (conn == null) return;
            
            String query = "SELECT b.id, bk.title, bk.author, b.borrow_date, b.due_date, b.status " +
                          "FROM borrowings b " +
                          "JOIN books bk ON b.book_id = bk.id " +
                          "JOIN members m ON b.member_id = m.id " +
                          "WHERE m.user_id = ? AND b.status = 'borrowed' " +
                          "ORDER BY b.due_date ASC";
            
            PreparedStatement stmt = conn.prepareStatement(query);
            stmt.setInt(1, userId);
            ResultSet rs = stmt.executeQuery();
            
            while (rs.next()) {
                BorrowingRecord record = new BorrowingRecord();
                record.setId(rs.getInt("id"));
                record.setBookTitle(rs.getString("title"));
                record.setAuthor(rs.getString("author"));
                record.setBorrowDate(rs.getString("borrow_date"));
                record.setDueDate(rs.getString("due_date"));
                record.setStatus(rs.getString("status"));
                
                // Calculate days left and fine amount
                LocalDate dueDate = LocalDate.parse(rs.getString("due_date"));
                LocalDate today = LocalDate.now();
                long daysLeft = ChronoUnit.DAYS.between(today, dueDate);
                
                if (daysLeft < 0) {
                    record.setDaysLeft("Overdue by " + Math.abs(daysLeft) + " days");
                    record.setFineAmount("$" + String.format("%.2f", Math.abs(daysLeft) * 0.50));
                } else {
                    record.setDaysLeft(daysLeft + " days");
                    record.setFineAmount("$0.00");
                }
                
                currentBorrowings.add(record);
            }
            
            conn.close();
        } catch (Exception e) {
            e.printStackTrace();
            showAlert("Error", "Failed to load current borrowings: " + e.getMessage());
        }
    }

    private void loadBorrowingHistory() {
        borrowingHistory.clear();
        try {
            Connection conn = DBConnection.getConnection();
            if (conn == null) return;
            
            String query = "SELECT b.id, bk.title, bk.author, b.borrow_date, b.return_date, b.status, b.fine_amount " +
                          "FROM borrowings b " +
                          "JOIN books bk ON b.book_id = bk.id " +
                          "JOIN members m ON b.member_id = m.id " +
                          "WHERE m.user_id = ? AND b.status = 'returned' " +
                          "ORDER BY b.return_date DESC";
            
            PreparedStatement stmt = conn.prepareStatement(query);
            stmt.setInt(1, userId);
            ResultSet rs = stmt.executeQuery();
            
            while (rs.next()) {
                BorrowingRecord record = new BorrowingRecord();
                record.setId(rs.getInt("id"));
                record.setBookTitle(rs.getString("title"));
                record.setAuthor(rs.getString("author"));
                record.setBorrowDate(rs.getString("borrow_date"));
                record.setReturnDate(rs.getString("return_date"));
                record.setStatus(rs.getString("status"));
                record.setFinePaid("$" + String.format("%.2f", rs.getDouble("fine_amount")));
                
                borrowingHistory.add(record);
            }
            
            conn.close();
        } catch (Exception e) {
            e.printStackTrace();
            showAlert("Error", "Failed to load borrowing history: " + e.getMessage());
        }
    }

    private void loadDueDates() {
        dueDates.clear();
        try {
            Connection conn = DBConnection.getConnection();
            if (conn == null) return;
            
            String query = "SELECT b.id, bk.title, b.due_date " +
                          "FROM borrowings b " +
                          "JOIN books bk ON b.book_id = bk.id " +
                          "JOIN members m ON b.member_id = m.id " +
                          "WHERE m.user_id = ? AND b.status = 'borrowed' " +
                          "ORDER BY b.due_date ASC";
            
            PreparedStatement stmt = conn.prepareStatement(query);
            stmt.setInt(1, userId);
            ResultSet rs = stmt.executeQuery();
            
            while (rs.next()) {
                BorrowingRecord record = new BorrowingRecord();
                record.setId(rs.getInt("id"));
                record.setBookTitle(rs.getString("title"));
                record.setDueDate(rs.getString("due_date"));
                
                // Calculate days until due and urgency
                LocalDate dueDate = LocalDate.parse(rs.getString("due_date"));
                LocalDate today = LocalDate.now();
                long daysUntilDue = ChronoUnit.DAYS.between(today, dueDate);
                
                if (daysUntilDue < 0) {
                    record.setDaysUntilDue("Overdue by " + Math.abs(daysUntilDue) + " days");
                    record.setUrgency("Critical");
                } else if (daysUntilDue <= 3) {
                    record.setDaysUntilDue(daysUntilDue + " days");
                    record.setUrgency("Urgent");
                } else if (daysUntilDue <= 7) {
                    record.setDaysUntilDue(daysUntilDue + " days");
                    record.setUrgency("Soon");
                } else {
                    record.setDaysUntilDue(daysUntilDue + " days");
                    record.setUrgency("Safe");
                }
                
                record.setFineRate("$0.50/day");
                dueDates.add(record);
            }
            
            conn.close();
        } catch (Exception e) {
            e.printStackTrace();
            showAlert("Error", "Failed to load due dates: " + e.getMessage());
        }
    }

    private void updateSummaryCards() {
        try {
            Connection conn = DBConnection.getConnection();
            if (conn == null) return;
            
            // Get member ID
            String memberQuery = "SELECT id, total_fines FROM members WHERE user_id = ?";
            PreparedStatement memberStmt = conn.prepareStatement(memberQuery);
            memberStmt.setInt(1, userId);
            ResultSet memberRs = memberStmt.executeQuery();
            
            if (memberRs.next()) {
                int memberId = memberRs.getInt("id");
                double totalFines = memberRs.getDouble("total_fines");
                
                // Count current borrowings
                String currentQuery = "SELECT COUNT(*) FROM borrowings WHERE member_id = ? AND status = 'borrowed'";
                PreparedStatement currentStmt = conn.prepareStatement(currentQuery);
                currentStmt.setInt(1, memberId);
                ResultSet currentRs = currentStmt.executeQuery();
                currentRs.next();
                int currentCount = currentRs.getInt(1);
                
                // Count overdue books
                String overdueQuery = "SELECT COUNT(*) FROM borrowings b " +
                                    "JOIN members m ON b.member_id = m.id " +
                                    "WHERE m.user_id = ? AND b.status = 'borrowed' AND b.due_date < CURDATE()";
                PreparedStatement overdueStmt = conn.prepareStatement(overdueQuery);
                overdueStmt.setInt(1, userId);
                ResultSet overdueRs = overdueStmt.executeQuery();
                overdueRs.next();
                int overdueCount = overdueRs.getInt(1);
                
                // Update labels
                currentBorrowingsLabel.setText("Current: " + currentCount);
                overdueLabel.setText("Overdue: " + overdueCount);
                totalFinesLabel.setText("Fines: $" + String.format("%.2f", totalFines));
            }
            
            conn.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @FXML
    private void refreshCurrentBorrowings(ActionEvent event) {
        loadCurrentBorrowings();
        updateSummaryCards();
    }

    @FXML
    private void refreshBorrowingHistory(ActionEvent event) {
        loadBorrowingHistory();
    }

    @FXML
    private void refreshDueDates(ActionEvent event) {
        loadDueDates();
    }

    private void renewBook(BorrowingRecord record) {
        try {
            Connection conn = DBConnection.getConnection();
            if (conn == null) return;
            
            // Check if book can be renewed (not overdue)
            LocalDate dueDate = LocalDate.parse(record.getDueDate());
            LocalDate today = LocalDate.now();
            
            if (today.isAfter(dueDate)) {
                showAlert("Renewal Failed", "Cannot renew overdue books. Please return the book first.");
                return;
            }
            
            // Extend due date by 14 days
            LocalDate newDueDate = dueDate.plusDays(14);
            
            String updateQuery = "UPDATE borrowings SET due_date = ? WHERE id = ?";
            PreparedStatement stmt = conn.prepareStatement(updateQuery);
            stmt.setString(1, newDueDate.toString());
            stmt.setInt(2, record.getId());
            stmt.executeUpdate();
            
            conn.close();
            
            showAlert("Success", "Book renewed successfully! New due date: " + newDueDate);
            loadData(); // Refresh all data
        } catch (Exception e) {
            e.printStackTrace();
            showAlert("Error", "Failed to renew book: " + e.getMessage());
        }
    }

    private void returnBook(BorrowingRecord record) {
        showAlert("Return Book", "Please return this book to the library desk.\n\nBook: " + record.getBookTitle() + "\nDue Date: " + record.getDueDate());
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

    // Data class for borrowing records
    public static class BorrowingRecord {
        private int id;
        private String bookTitle;
        private String author;
        private String borrowDate;
        private String dueDate;
        private String returnDate;
        private String status;
        private String daysLeft;
        private String fineAmount;
        private String finePaid;
        private String daysUntilDue;
        private String urgency;
        private String fineRate;

        // Getters and Setters
        public int getId() { return id; }
        public void setId(int id) { this.id = id; }
        
        public String getBookTitle() { return bookTitle; }
        public void setBookTitle(String bookTitle) { this.bookTitle = bookTitle; }
        
        public String getAuthor() { return author; }
        public void setAuthor(String author) { this.author = author; }
        
        public String getBorrowDate() { return borrowDate; }
        public void setBorrowDate(String borrowDate) { this.borrowDate = borrowDate; }
        
        public String getDueDate() { return dueDate; }
        public void setDueDate(String dueDate) { this.dueDate = dueDate; }
        
        public String getReturnDate() { return returnDate; }
        public void setReturnDate(String returnDate) { this.returnDate = returnDate; }
        
        public String getStatus() { return status; }
        public void setStatus(String status) { this.status = status; }
        
        public String getDaysLeft() { return daysLeft; }
        public void setDaysLeft(String daysLeft) { this.daysLeft = daysLeft; }
        
        public String getFineAmount() { return fineAmount; }
        public void setFineAmount(String fineAmount) { this.fineAmount = fineAmount; }
        
        public String getFinePaid() { return finePaid; }
        public void setFinePaid(String finePaid) { this.finePaid = finePaid; }
        
        public String getDaysUntilDue() { return daysUntilDue; }
        public void setDaysUntilDue(String daysUntilDue) { this.daysUntilDue = daysUntilDue; }
        
        public String getUrgency() { return urgency; }
        public void setUrgency(String urgency) { this.urgency = urgency; }
        
        public String getFineRate() { return fineRate; }
        public void setFineRate(String fineRate) { this.fineRate = fineRate; }
    }
} 