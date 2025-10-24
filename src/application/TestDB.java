package application;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.Statement;

public class TestDB {
    public static void main(String[] args) {
        System.out.println("Testing Library Management System Database...");
        
        try {
            // Test database connection
            System.out.println("1. Testing database connection...");
            Connection conn = DBConnection.getConnection();
            if (conn != null) {
                System.out.println("✓ Database connection successful!");
            } else {
                System.out.println("✗ Database connection failed!");
                return;
            }
            
            // Test table creation
            System.out.println("2. Creating database tables...");
            DBConnection.createDatabase();
            System.out.println("✓ Database tables created successfully!");
            
            // Test user table
            System.out.println("3. Testing user table...");
            Statement stmt = conn.createStatement();
            ResultSet rs = stmt.executeQuery("SELECT COUNT(*) FROM users");
            rs.next();
            int userCount = rs.getInt(1);
            System.out.println("✓ Users table has " + userCount + " records");
            
            // Test books table
            System.out.println("4. Testing books table...");
            rs = stmt.executeQuery("SELECT COUNT(*) FROM books");
            rs.next();
            int bookCount = rs.getInt(1);
            System.out.println("✓ Books table has " + bookCount + " records");
            
            // Test members table
            System.out.println("5. Testing members table...");
            rs = stmt.executeQuery("SELECT COUNT(*) FROM members");
            rs.next();
            int memberCount = rs.getInt(1);
            System.out.println("✓ Members table has " + memberCount + " records");
            
            // Test borrowings table
            System.out.println("6. Testing borrowings table...");
            rs = stmt.executeQuery("SELECT COUNT(*) FROM borrowings");
            rs.next();
            int borrowingCount = rs.getInt(1);
            System.out.println("✓ Borrowings table has " + borrowingCount + " records");
            
            conn.close();
            System.out.println("\n✓ All database tests passed! The system is ready to use.");
            System.out.println("Default login credentials: admin / admin123");
            
        } catch (Exception e) {
            System.err.println("✗ Database test failed: " + e.getMessage());
            e.printStackTrace();
        }
    }
}
