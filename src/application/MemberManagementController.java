package application;

import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;
import javafx.scene.Node;
import javafx.event.ActionEvent;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.scene.layout.HBox;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;

public class MemberManagementController {

    @FXML
    private TableView<Member> membersTable;
    
    @FXML
    private TableColumn<Member, String> studentIdCol;
    
    @FXML
    private TableColumn<Member, String> nameCol;
    
    @FXML
    private TableColumn<Member, String> emailCol;
    
    @FXML
    private TableColumn<Member, String> phoneCol;
    
    @FXML
    private TableColumn<Member, String> departmentCol;
    
    @FXML
    private TableColumn<Member, Integer> yearCol;
    
    @FXML
    private TableColumn<Member, String> statusCol;
    
    @FXML
    private TableColumn<Member, Double> finesCol;
    
    @FXML
    private TableColumn<Member, String> actionsCol;
    
    @FXML
    private TextField searchField;
    
    @FXML
    private ComboBox<String> departmentFilter;
    
    @FXML
    private ComboBox<String> statusFilter;
    
    @FXML
    private Label totalMembersLabel;
    
    @FXML
    private Label activeMembersLabel;
    
    @FXML
    private Label blockedMembersLabel;
    
    @FXML
    private Label totalFinesLabel;
    
    private ObservableList<Member> membersList = FXCollections.observableArrayList();
    
    @FXML
    private void initialize() {
        setupTableColumns();
        setupFilters();
        loadMembers();
        updateStatistics();
        
        // Add search functionality
        searchField.textProperty().addListener((observable, oldValue, newValue) -> {
            filterMembers();
        });
    }
    
    private void setupTableColumns() {
        studentIdCol.setCellValueFactory(new PropertyValueFactory<>("studentId"));
        nameCol.setCellValueFactory(new PropertyValueFactory<>("name"));
        emailCol.setCellValueFactory(new PropertyValueFactory<>("email"));
        phoneCol.setCellValueFactory(new PropertyValueFactory<>("phone"));
        departmentCol.setCellValueFactory(new PropertyValueFactory<>("department"));
        yearCol.setCellValueFactory(new PropertyValueFactory<>("yearOfStudy"));
        statusCol.setCellValueFactory(new PropertyValueFactory<>("status"));
        finesCol.setCellValueFactory(new PropertyValueFactory<>("totalFines"));
        
        // Setup actions column
        actionsCol.setCellFactory(param -> new TableCell<Member, String>() {
            private final Button blockButton = new Button("Block");
            private final Button unblockButton = new Button("Unblock");
            private final Button viewButton = new Button("View");
            
            {
                blockButton.setStyle("-fx-background-color: #e74c3c; -fx-text-fill: white; -fx-font-size: 10px;");
                unblockButton.setStyle("-fx-background-color: #27ae60; -fx-text-fill: white; -fx-font-size: 10px;");
                viewButton.setStyle("-fx-background-color: #3498db; -fx-text-fill: white; -fx-font-size: 10px;");
                
                blockButton.setOnAction(e -> {
                    Member member = getTableView().getItems().get(getIndex());
                    blockMember(member);
                });
                
                unblockButton.setOnAction(e -> {
                    Member member = getTableView().getItems().get(getIndex());
                    unblockMember(member);
                });
                
                viewButton.setOnAction(e -> {
                    Member member = getTableView().getItems().get(getIndex());
                    viewMemberDetails(member);
                });
            }
            
            @Override
            protected void updateItem(String item, boolean empty) {
                super.updateItem(item, empty);
                if (empty) {
                    setGraphic(null);
                } else {
                    Member member = getTableView().getItems().get(getIndex());
                    HBox buttons = new HBox(5);
                    buttons.getChildren().add(viewButton);
                    
                    if ("active".equals(member.getStatus())) {
                        buttons.getChildren().add(blockButton);
                    } else {
                        buttons.getChildren().add(unblockButton);
                    }
                    
                    setGraphic(buttons);
                }
            }
        });
    }
    
    private void setupFilters() {
        departmentFilter.getItems().addAll("All", "Computer Science", "Electrical Engineering", 
            "Mechanical Engineering", "Civil Engineering", "Information Technology", 
            "Business Administration", "Economics", "Mathematics", "Physics", 
            "Chemistry", "Biology", "English Literature", "History", "Psychology", "Sociology");
        departmentFilter.setValue("All");
        
        statusFilter.getItems().addAll("All", "active", "blocked", "suspended");
        statusFilter.setValue("All");
        
        departmentFilter.setOnAction(e -> filterMembers());
        statusFilter.setOnAction(e -> filterMembers());
    }
    
    private void loadMembers() {
        try {
            Connection conn = DBConnection.getConnection();
            if (conn == null) return;
            
            String query = """
                SELECT m.student_id, m.name, m.email, m.phone, m.department, 
                       m.year_of_study, m.status, m.total_fines, m.created_date
                FROM members m
                ORDER BY m.created_date DESC
                """;
            
            PreparedStatement stmt = conn.prepareStatement(query);
            ResultSet rs = stmt.executeQuery();
            
            membersList.clear();
            while (rs.next()) {
                Member member = new Member(
                    rs.getString("student_id"),
                    rs.getString("name"),
                    rs.getString("email"),
                    rs.getString("phone"),
                    rs.getString("department"),
                    rs.getInt("year_of_study"),
                    rs.getString("status"),
                    rs.getDouble("total_fines"),
                    rs.getTimestamp("created_date")
                );
                membersList.add(member);
            }
            
            membersTable.setItems(membersList);
            conn.close();
            
        } catch (Exception e) {
            e.printStackTrace();
            showAlert("Error", "Failed to load members: " + e.getMessage());
        }
    }
    
    private void filterMembers() {
        String searchText = searchField.getText().toLowerCase();
        String departmentFilterValue = departmentFilter.getValue();
        String statusFilterValue = statusFilter.getValue();
        
        ObservableList<Member> filteredList = FXCollections.observableArrayList();
        
        for (Member member : membersList) {
            boolean matchesSearch = searchText.isEmpty() ||
                member.getName().toLowerCase().contains(searchText) ||
                member.getEmail().toLowerCase().contains(searchText) ||
                member.getStudentId().toLowerCase().contains(searchText);
                
            boolean matchesDepartment = "All".equals(departmentFilterValue) ||
                departmentFilterValue.equals(member.getDepartment());
                
            boolean matchesStatus = "All".equals(statusFilterValue) ||
                statusFilterValue.equals(member.getStatus());
            
            if (matchesSearch && matchesDepartment && matchesStatus) {
                filteredList.add(member);
            }
        }
        
        membersTable.setItems(filteredList);
    }
    
    private void updateStatistics() {
        int total = membersList.size();
        int active = 0;
        int blocked = 0;
        double totalFines = 0.0;
        
        for (Member member : membersList) {
            if ("active".equals(member.getStatus())) {
                active++;
            } else {
                blocked++;
            }
            totalFines += member.getTotalFines();
        }
        
        totalMembersLabel.setText("Total Members: " + total);
        activeMembersLabel.setText("Active: " + active);
        blockedMembersLabel.setText("Blocked: " + blocked);
        totalFinesLabel.setText("Total Fines: $" + String.format("%.2f", totalFines));
    }
    
    private void blockMember(Member member) {
        try {
            Connection conn = DBConnection.getConnection();
            String query = "UPDATE members SET status = 'blocked' WHERE student_id = ?";
            PreparedStatement stmt = conn.prepareStatement(query);
            stmt.setString(1, member.getStudentId());
            stmt.executeUpdate();
            conn.close();
            
            loadMembers();
            updateStatistics();
            showAlert("Success", "Member " + member.getName() + " has been blocked.");
            
        } catch (Exception e) {
            e.printStackTrace();
            showAlert("Error", "Failed to block member: " + e.getMessage());
        }
    }
    
    private void unblockMember(Member member) {
        try {
            Connection conn = DBConnection.getConnection();
            String query = "UPDATE members SET status = 'active' WHERE student_id = ?";
            PreparedStatement stmt = conn.prepareStatement(query);
            stmt.setString(1, member.getStudentId());
            stmt.executeUpdate();
            conn.close();
            
            loadMembers();
            updateStatistics();
            showAlert("Success", "Member " + member.getName() + " has been unblocked.");
            
        } catch (Exception e) {
            e.printStackTrace();
            showAlert("Error", "Failed to unblock member: " + e.getMessage());
        }
    }
    
    private void viewMemberDetails(Member member) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle("Member Details");
        alert.setHeaderText("Details for " + member.getName());
        
        String content = String.format("""
            🎓 Student ID: %s
            👤 Name: %s
            📧 Email: %s
            📞 Phone: %s
            🏫 Department: %s
            📚 Year: %d
            📊 Status: %s
            💰 Total Fines: $%.2f
            📅 Registered: %s
            """, 
            member.getStudentId(), member.getName(), member.getEmail(),
            member.getPhone(), member.getDepartment(), member.getYearOfStudy(),
            member.getStatus(), member.getTotalFines(), member.getCreatedDate());
        
        alert.setContentText(content);
        alert.showAndWait();
    }
    
    @FXML
    private void handleSearch(ActionEvent event) {
        filterMembers();
    }
    
    @FXML
    private void refreshTable(ActionEvent event) {
        loadMembers();
        updateStatistics();
    }
    
    @FXML
    private void goBackToDashboard(ActionEvent event) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/resources/librarian_dashboard.fxml"));
            Parent root = loader.load();
            Stage stage = (Stage) ((Node) event.getSource()).getScene().getWindow();
            stage.setScene(new Scene(root, 1200, 800));
            stage.setTitle("Librarian Dashboard - Library Management System");
            stage.show();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
    
    @FXML
    private void exportReport(ActionEvent event) {
        showAlert("Export Report", "Feature coming soon!\n\nThis will export member data to CSV/PDF format.");
    }
    
    private void showAlert(String title, String content) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(content);
        alert.showAndWait();
    }
    
    // Member data class
    public static class Member {
        private String studentId, name, email, phone, department, status;
        private int yearOfStudy;
        private double totalFines;
        private java.sql.Timestamp createdDate;
        
        public Member(String studentId, String name, String email, String phone, 
                     String department, int yearOfStudy, String status, 
                     double totalFines, java.sql.Timestamp createdDate) {
            this.studentId = studentId;
            this.name = name;
            this.email = email;
            this.phone = phone;
            this.department = department;
            this.yearOfStudy = yearOfStudy;
            this.status = status;
            this.totalFines = totalFines;
            this.createdDate = createdDate;
        }
        
        // Getters
        public String getStudentId() { return studentId; }
        public String getName() { return name; }
        public String getEmail() { return email; }
        public String getPhone() { return phone; }
        public String getDepartment() { return department; }
        public int getYearOfStudy() { return yearOfStudy; }
        public String getStatus() { return status; }
        public double getTotalFines() { return totalFines; }
        public java.sql.Timestamp getCreatedDate() { return createdDate; }
    }
} 