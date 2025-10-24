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

public class MyFinesController {

    @FXML private Label studentNameLabel;
    @FXML private Label totalFinesLabel;
    @FXML private Label paidFinesLabel;
    @FXML private Label outstandingFinesLabel;
    @FXML private Label fineRateLabel;
    
    // Current Fines Table
    @FXML private TableView<FineRecord> currentFinesTable;
    @FXML private TableColumn<FineRecord, String> bookTitleCol;
    @FXML private TableColumn<FineRecord, String> dueDateCol;
    @FXML private TableColumn<FineRecord, String> daysOverdueCol;
    @FXML private TableColumn<FineRecord, String> fineAmountCol;
    @FXML private TableColumn<FineRecord, String> actionsCol;
    
    // Fine History Table
    @FXML private TableView<FineRecord> fineHistoryTable;
    @FXML private TableColumn<FineRecord, String> historyBookTitleCol;
    @FXML private TableColumn<FineRecord, String> historyFineDateCol;
    @FXML private TableColumn<FineRecord, String> historyAmountCol;
    @FXML private TableColumn<FineRecord, String> historyStatusCol;
    @FXML private TableColumn<FineRecord, String> historyPaymentDateCol;
    
    // Payment Methods
    @FXML private ComboBox<String> paymentMethodCombo;
    @FXML private TextField cardNumberField;
    @FXML private TextField expiryDateField;
    @FXML private TextField cvvField;
    @FXML private TextField amountField;
    @FXML private Label selectedFineLabel;
    
    private int userId;
    private String studentName;
    private ObservableList<FineRecord> currentFines = FXCollections.observableArrayList();
    private ObservableList<FineRecord> fineHistory = FXCollections.observableArrayList();
    private FineRecord selectedFine;

    public void setUserInfo(int userId, String studentName) {
        this.userId = userId;
        this.studentName = studentName;
        if (studentNameLabel != null) {
            studentNameLabel.setText(studentName);
        }
        initializeTables();
        initializePaymentMethods();
        loadData();
    }

    private void initializeTables() {
        // Initialize Current Fines Table
        bookTitleCol.setCellValueFactory(new PropertyValueFactory<>("bookTitle"));
        dueDateCol.setCellValueFactory(new PropertyValueFactory<>("dueDate"));
        daysOverdueCol.setCellValueFactory(new PropertyValueFactory<>("daysOverdue"));
        fineAmountCol.setCellValueFactory(new PropertyValueFactory<>("fineAmount"));
        
        // Add action buttons to current fines
        actionsCol.setCellFactory(param -> new TableCell<FineRecord, String>() {
            private final Button payButton = new Button("Pay Fine");
            private final Button detailsButton = new Button("Details");
            
            {
                payButton.setStyle("-fx-background-color: #27ae60; -fx-text-fill: white; -fx-font-size: 10px; -fx-padding: 5;");
                detailsButton.setStyle("-fx-background-color: #3498db; -fx-text-fill: white; -fx-font-size: 10px; -fx-padding: 5;");
                
                payButton.setOnAction(e -> {
                    FineRecord record = getTableView().getItems().get(getIndex());
                    selectFineForPayment(record);
                });
                
                detailsButton.setOnAction(e -> {
                    FineRecord record = getTableView().getItems().get(getIndex());
                    showFineDetails(record);
                });
            }
            
            @Override
            protected void updateItem(String item, boolean empty) {
                super.updateItem(item, empty);
                if (empty) {
                    setGraphic(null);
                } else {
                    HBox buttons = new HBox(5);
                    buttons.getChildren().addAll(payButton, detailsButton);
                    setGraphic(buttons);
                }
            }
        });
        
        // Initialize Fine History Table
        historyBookTitleCol.setCellValueFactory(new PropertyValueFactory<>("bookTitle"));
        historyFineDateCol.setCellValueFactory(new PropertyValueFactory<>("fineDate"));
        historyAmountCol.setCellValueFactory(new PropertyValueFactory<>("fineAmount"));
        historyStatusCol.setCellValueFactory(new PropertyValueFactory<>("status"));
        historyPaymentDateCol.setCellValueFactory(new PropertyValueFactory<>("paymentDate"));
        
        // Set table data
        currentFinesTable.setItems(currentFines);
        fineHistoryTable.setItems(fineHistory);
    }

    private void initializePaymentMethods() {
        paymentMethodCombo.getItems().addAll("Credit Card", "Debit Card", "PayPal", "Cash");
        paymentMethodCombo.setValue("Credit Card");
        
        // Add listeners for payment method changes
        paymentMethodCombo.setOnAction(e -> {
            String method = paymentMethodCombo.getValue();
            if ("Cash".equals(method)) {
                cardNumberField.setDisable(true);
                expiryDateField.setDisable(true);
                cvvField.setDisable(true);
            } else {
                cardNumberField.setDisable(false);
                expiryDateField.setDisable(false);
                cvvField.setDisable(false);
            }
        });
    }

    private void loadData() {
        loadCurrentFines();
        loadFineHistory();
        updateSummaryCards();
    }

    private void loadCurrentFines() {
        currentFines.clear();
        try {
            Connection conn = DBConnection.getConnection();
            if (conn == null) return;
            
            String query = "SELECT b.id, bk.title, b.due_date, b.fine_amount " +
                          "FROM borrowings b " +
                          "JOIN books bk ON b.book_id = bk.id " +
                          "JOIN members m ON b.member_id = m.id " +
                          "WHERE m.user_id = ? AND b.status = 'borrowed' AND b.due_date < CURDATE() " +
                          "ORDER BY b.due_date ASC";
            
            PreparedStatement stmt = conn.prepareStatement(query);
            stmt.setInt(1, userId);
            ResultSet rs = stmt.executeQuery();
            
            while (rs.next()) {
                FineRecord record = new FineRecord();
                record.setId(rs.getInt("id"));
                record.setBookTitle(rs.getString("title"));
                record.setDueDate(rs.getString("due_date"));
                record.setFineAmount(rs.getDouble("fine_amount"));
                
                // Calculate days overdue
                LocalDate dueDate = LocalDate.parse(rs.getString("due_date"));
                LocalDate today = LocalDate.now();
                long daysOverdue = ChronoUnit.DAYS.between(dueDate, today);
                record.setDaysOverdue(daysOverdue + " days");
                
                currentFines.add(record);
            }
            
            conn.close();
        } catch (Exception e) {
            e.printStackTrace();
            showAlert("Error", "Failed to load current fines: " + e.getMessage());
        }
    }

    private void loadFineHistory() {
        fineHistory.clear();
        try {
            Connection conn = DBConnection.getConnection();
            if (conn == null) return;
            
            String query = "SELECT b.id, bk.title, b.due_date, b.fine_amount, b.payment_date, b.payment_status " +
                          "FROM borrowings b " +
                          "JOIN books bk ON b.book_id = bk.id " +
                          "JOIN members m ON b.member_id = m.id " +
                          "WHERE m.user_id = ? AND b.fine_amount > 0 " +
                          "ORDER BY b.payment_date DESC";
            
            PreparedStatement stmt = conn.prepareStatement(query);
            stmt.setInt(1, userId);
            ResultSet rs = stmt.executeQuery();
            
            while (rs.next()) {
                FineRecord record = new FineRecord();
                record.setId(rs.getInt("id"));
                record.setBookTitle(rs.getString("title"));
                record.setFineDate(rs.getString("due_date"));
                record.setFineAmount(rs.getDouble("fine_amount"));
                record.setPaymentDate(rs.getString("payment_date"));
                record.setStatus(rs.getString("payment_status"));
                
                fineHistory.add(record);
            }
            
            conn.close();
        } catch (Exception e) {
            e.printStackTrace();
            showAlert("Error", "Failed to load fine history: " + e.getMessage());
        }
    }

    private void updateSummaryCards() {
        try {
            Connection conn = DBConnection.getConnection();
            if (conn == null) return;
            
            // Get member fines info
            String query = "SELECT total_fines, paid_fines FROM members WHERE user_id = ?";
            PreparedStatement stmt = conn.prepareStatement(query);
            stmt.setInt(1, userId);
            ResultSet rs = stmt.executeQuery();
            
            if (rs.next()) {
                double totalFines = rs.getDouble("total_fines");
                double paidFines = rs.getDouble("paid_fines");
                double outstandingFines = totalFines - paidFines;
                
                totalFinesLabel.setText("Total: $" + String.format("%.2f", totalFines));
                paidFinesLabel.setText("Paid: $" + String.format("%.2f", paidFines));
                outstandingFinesLabel.setText("Outstanding: $" + String.format("%.2f", outstandingFines));
                fineRateLabel.setText("Rate: $0.50/day");
            }
            
            conn.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void selectFineForPayment(FineRecord record) {
        selectedFine = record;
        selectedFineLabel.setText("Selected: " + record.getBookTitle() + " - $" + String.format("%.2f", record.getFineAmount()));
        amountField.setText(String.format("%.2f", record.getFineAmount()));
    }

    private void showFineDetails(FineRecord record) {
        String details = "Book: " + record.getBookTitle() + "\n" +
                        "Due Date: " + record.getDueDate() + "\n" +
                        "Days Overdue: " + record.getDaysOverdue() + "\n" +
                        "Fine Amount: $" + String.format("%.2f", record.getFineAmount()) + "\n" +
                        "Fine Rate: $0.50 per day";
        
        showAlert("Fine Details", details);
    }

    @FXML
    private void processPayment(ActionEvent event) {
        if (selectedFine == null) {
            showAlert("Error", "Please select a fine to pay first.");
            return;
        }
        
        String paymentMethod = paymentMethodCombo.getValue();
        String amount = amountField.getText();
        
        if (amount.isEmpty()) {
            showAlert("Error", "Please enter payment amount.");
            return;
        }
        
        try {
            double paymentAmount = Double.parseDouble(amount);
            
            // Validate payment method
            if (!"Cash".equals(paymentMethod)) {
                if (cardNumberField.getText().isEmpty() || expiryDateField.getText().isEmpty() || cvvField.getText().isEmpty()) {
                    showAlert("Error", "Please fill in all payment details.");
                    return;
                }
            }
            
            // Process payment
            Connection conn = DBConnection.getConnection();
            if (conn == null) return;
            
            // Update borrowing record
            String updateQuery = "UPDATE borrowings SET payment_date = CURDATE(), payment_status = 'paid' WHERE id = ?";
            PreparedStatement stmt = conn.prepareStatement(updateQuery);
            stmt.setInt(1, selectedFine.getId());
            stmt.executeUpdate();
            
            // Update member's paid fines
            String memberQuery = "UPDATE members SET paid_fines = paid_fines + ? WHERE user_id = ?";
            PreparedStatement memberStmt = conn.prepareStatement(memberQuery);
            memberStmt.setDouble(1, paymentAmount);
            memberStmt.setInt(2, userId);
            memberStmt.executeUpdate();
            
            conn.close();
            
            showAlert("Success", "Payment processed successfully!\n\nAmount: $" + String.format("%.2f", paymentAmount) + "\nMethod: " + paymentMethod);
            
            // Clear form and refresh data
            selectedFine = null;
            selectedFineLabel.setText("Selected: None");
            amountField.clear();
            cardNumberField.clear();
            expiryDateField.clear();
            cvvField.clear();
            
            loadData();
        } catch (Exception e) {
            e.printStackTrace();
            showAlert("Error", "Payment failed: " + e.getMessage());
        }
    }

    @FXML
    private void refreshCurrentFines(ActionEvent event) {
        loadCurrentFines();
        updateSummaryCards();
    }

    @FXML
    private void refreshFineHistory(ActionEvent event) {
        loadFineHistory();
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

    @FXML
    private void payAllFines(javafx.event.ActionEvent event) {
        showAlert("Pay All Fines", "This feature is not implemented yet.");
    }

    private void showAlert(String title, String content) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(content);
        alert.showAndWait();
    }

    // Data class for fine records
    public static class FineRecord {
        private int id;
        private String bookTitle;
        private String dueDate;
        private String fineDate;
        private double fineAmount;
        private String daysOverdue;
        private String status;
        private String paymentDate;

        // Getters and Setters
        public int getId() { return id; }
        public void setId(int id) { this.id = id; }
        
        public String getBookTitle() { return bookTitle; }
        public void setBookTitle(String bookTitle) { this.bookTitle = bookTitle; }
        
        public String getDueDate() { return dueDate; }
        public void setDueDate(String dueDate) { this.dueDate = dueDate; }
        
        public String getFineDate() { return fineDate; }
        public void setFineDate(String fineDate) { this.fineDate = fineDate; }
        
        public double getFineAmount() { return fineAmount; }
        public void setFineAmount(double fineAmount) { this.fineAmount = fineAmount; }
        
        public String getDaysOverdue() { return daysOverdue; }
        public void setDaysOverdue(String daysOverdue) { this.daysOverdue = daysOverdue; }
        
        public String getStatus() { return status; }
        public void setStatus(String status) { this.status = status; }
        
        public String getPaymentDate() { return paymentDate; }
        public void setPaymentDate(String paymentDate) { this.paymentDate = paymentDate; }
    }
} 