package application;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

public class DBConnection {
    private static final String URL = "jdbc:mysql://127.0.0.1:3306/librarydb";
    private static final String USER = "root";
    private static final String PASSWORD = "@mysql#567";
    
    public static Connection getConnection() {
        try {
            Class.forName("com.mysql.cj.jdbc.Driver");
            return DriverManager.getConnection(URL, USER, PASSWORD);
        } catch (ClassNotFoundException e) {
            System.err.println("MySQL JDBC Driver not found: " + e.getMessage());
            return null;
        } catch (SQLException e) {
            System.err.println("Database connection failed: " + e.getMessage());
            return null;
        }
    }
    
    public static void createDatabase() {
        try {
            // Connect to MySQL without specifying database
            Connection conn = DriverManager.getConnection("jdbc:mysql://localhost:3306", USER, PASSWORD);
            
            // Drop and recreate database to ensure clean schema
            conn.createStatement().execute("DROP DATABASE IF EXISTS librarydb");
            conn.createStatement().execute("CREATE DATABASE librarydb");
            conn.close();
            
            // Connect to the librarydb database
            conn = getConnection();
            
            // Create users table with role-based access (librarian and student only)
            String createUsersTable = """
                CREATE TABLE IF NOT EXISTS users (
                    id INT AUTO_INCREMENT PRIMARY KEY,
                    username VARCHAR(50) UNIQUE NOT NULL,
                    password VARCHAR(100) NOT NULL,
                    role ENUM('librarian', 'student') DEFAULT 'student',
                    email VARCHAR(100),
                    full_name VARCHAR(100),
                    created_date TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
                    is_active BOOLEAN DEFAULT TRUE
                )
            """;
            conn.createStatement().execute(createUsersTable);
            
            // Create books table
            String createBooksTable = """
                CREATE TABLE IF NOT EXISTS books (
                    id INT AUTO_INCREMENT PRIMARY KEY,
                    title VARCHAR(200) NOT NULL,
                    author VARCHAR(100) NOT NULL,
                    year INT,
                    isbn VARCHAR(20),
                    quantity INT DEFAULT 1,
                    available INT DEFAULT 1,
                    category VARCHAR(50),
                    location VARCHAR(50),
                    created_date TIMESTAMP DEFAULT CURRENT_TIMESTAMP
                )
            """;
            conn.createStatement().execute(createBooksTable);
            
            // Create members table (for student information)
            String createMembersTable = """
                CREATE TABLE IF NOT EXISTS members (
                    id INT AUTO_INCREMENT PRIMARY KEY,
                    user_id INT,
                    student_id VARCHAR(20) UNIQUE,
                    name VARCHAR(100) NOT NULL,
                    email VARCHAR(100) UNIQUE,
                    phone VARCHAR(20),
                    address TEXT,
                    department VARCHAR(50),
                    year_of_study INT,
                    status ENUM('active', 'blocked', 'suspended') DEFAULT 'active',
                    total_fines DECIMAL(10,2) DEFAULT 0.00,
                    created_date TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
                    FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE
                )
            """;
            conn.createStatement().execute(createMembersTable);
            
            // Create borrowings table with fines
            String createBorrowingsTable = """
                CREATE TABLE IF NOT EXISTS borrowings (
                    id INT AUTO_INCREMENT PRIMARY KEY,
                    book_id INT,
                    member_id INT,
                    borrowed_by INT,
                    borrow_date DATE,
                    due_date DATE,
                    return_date DATE,
                    status ENUM('borrowed', 'returned', 'overdue') DEFAULT 'borrowed',
                    fine_amount DECIMAL(10,2) DEFAULT 0.00,
                    fine_paid BOOLEAN DEFAULT FALSE,
                    notes TEXT,
                    created_date TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
                    FOREIGN KEY (book_id) REFERENCES books(id) ON DELETE CASCADE,
                    FOREIGN KEY (member_id) REFERENCES members(id) ON DELETE CASCADE,
                    FOREIGN KEY (borrowed_by) REFERENCES users(id)
                )
            """;
            conn.createStatement().execute(createBorrowingsTable);
            
            // Create fine_transactions table
            String createFineTransactionsTable = """
                CREATE TABLE IF NOT EXISTS fine_transactions (
                    id INT AUTO_INCREMENT PRIMARY KEY,
                    borrowing_id INT,
                    member_id INT,
                    amount DECIMAL(10,2) NOT NULL,
                    transaction_type ENUM('charged', 'paid', 'waived') NOT NULL,
                    processed_by INT,
                    payment_method VARCHAR(50),
                    notes TEXT,
                    transaction_date TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
                    FOREIGN KEY (borrowing_id) REFERENCES borrowings(id) ON DELETE CASCADE,
                    FOREIGN KEY (member_id) REFERENCES members(id) ON DELETE CASCADE,
                    FOREIGN KEY (processed_by) REFERENCES users(id)
                )
            """;
            conn.createStatement().execute(createFineTransactionsTable);
            
            // Insert default users (librarian and student only)
            String checkLibrarian = "SELECT COUNT(*) FROM users WHERE username = 'librarian'";
            var rs = conn.createStatement().executeQuery(checkLibrarian);
            rs.next();
            if (rs.getInt(1) == 0) {
                String insertLibrarian = "INSERT INTO users (username, password, role, full_name, email) VALUES ('librarian', 'lib123', 'librarian', 'Library Manager', 'librarian@library.com')";
                conn.createStatement().execute(insertLibrarian);
            }
            
            String checkStudent = "SELECT COUNT(*) FROM users WHERE username = 'student'";
            rs = conn.createStatement().executeQuery(checkStudent);
            rs.next();
            if (rs.getInt(1) == 0) {
                String insertStudent = "INSERT INTO users (username, password, role, full_name, email) VALUES ('student', 'student123', 'student', 'John Student', 'student@university.edu')";
                conn.createStatement().execute(insertStudent);
                
                // Add student member record
                String insertMember = "INSERT INTO members (user_id, student_id, name, email, department, year_of_study) VALUES (LAST_INSERT_ID(), 'STU001', 'John Student', 'student@university.edu', 'Computer Science', 2)";
                conn.createStatement().execute(insertMember);
            }
            
            conn.close();
            System.out.println("Database and tables created successfully!");
            
        } catch (Exception e) {
            System.err.println("Error creating database: " + e.getMessage());
            e.printStackTrace();
        }
    }
}