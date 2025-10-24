-- Library Management System Database Script
-- Database: librarydb
-- User: root
-- Password: Rohan@321

-- Create database
CREATE DATABASE IF NOT EXISTS librarydb;
USE librarydb;

-- Drop existing tables if they exist (for clean setup)
DROP TABLE IF EXISTS borrowings;
DROP TABLE IF EXISTS books;
DROP TABLE IF EXISTS members;
DROP TABLE IF EXISTS users;

-- Create users table
CREATE TABLE users (
    id INT AUTO_INCREMENT PRIMARY KEY,
    username VARCHAR(50) UNIQUE NOT NULL,
    password VARCHAR(100) NOT NULL,
    role VARCHAR(20) DEFAULT 'librarian',
    created_date TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

-- Create books table
CREATE TABLE books (
    id INT AUTO_INCREMENT PRIMARY KEY,
    title VARCHAR(200) NOT NULL,
    author VARCHAR(100) NOT NULL,
    year INT,
    isbn VARCHAR(20),
    quantity INT DEFAULT 1,
    available INT DEFAULT 1,
    created_date TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

-- Create members table
CREATE TABLE members (
    id INT AUTO_INCREMENT PRIMARY KEY,
    name VARCHAR(100) NOT NULL,
    email VARCHAR(100) UNIQUE,
    phone VARCHAR(20),
    address TEXT,
    created_date TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

-- Create borrowings table
CREATE TABLE borrowings (
    id INT AUTO_INCREMENT PRIMARY KEY,
    book_id INT,
    member_id INT,
    borrow_date DATE,
    return_date DATE,
    status ENUM('borrowed', 'returned', 'overdue') DEFAULT 'borrowed',
    created_date TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (book_id) REFERENCES books(id) ON DELETE CASCADE,
    FOREIGN KEY (member_id) REFERENCES members(id) ON DELETE CASCADE
);

-- Insert default admin user
INSERT INTO users (username, password, role) VALUES 
('admin', 'admin123', 'admin'),
('librarian', 'lib123', 'librarian');

-- Insert sample books
INSERT INTO books (title, author, year, isbn, quantity, available) VALUES 
('The Great Gatsby', 'F. Scott Fitzgerald', 1925, '978-0743273565', 3, 3),
('To Kill a Mockingbird', 'Harper Lee', 1960, '978-0446310789', 2, 2),
('1984', 'George Orwell', 1949, '978-0451524935', 4, 4),
('Pride and Prejudice', 'Jane Austen', 1813, '978-0141439518', 2, 2),
('The Catcher in the Rye', 'J.D. Salinger', 1951, '978-0316769488', 3, 3),
('Lord of the Flies', 'William Golding', 1954, '978-0399501487', 2, 2),
('Animal Farm', 'George Orwell', 1945, '978-0451526342', 3, 3),
('The Hobbit', 'J.R.R. Tolkien', 1937, '978-0547928241', 2, 2),
('Fahrenheit 451', 'Ray Bradbury', 1953, '978-1451673319', 2, 2),
('Brave New World', 'Aldous Huxley', 1932, '978-0060850524', 2, 2);

-- Insert sample members
INSERT INTO members (name, email, phone, address) VALUES 
('John Smith', 'john.smith@email.com', '555-0101', '123 Main St, Anytown, USA'),
('Sarah Johnson', 'sarah.j@email.com', '555-0102', '456 Oak Ave, Somewhere, USA'),
('Michael Brown', 'mike.brown@email.com', '555-0103', '789 Pine Rd, Elsewhere, USA'),
('Emily Davis', 'emily.davis@email.com', '555-0104', '321 Elm St, Nowhere, USA'),
('David Wilson', 'david.wilson@email.com', '555-0105', '654 Maple Dr, Anywhere, USA'),
('Lisa Anderson', 'lisa.anderson@email.com', '555-0106', '987 Cedar Ln, Someplace, USA'),
('Robert Taylor', 'rob.taylor@email.com', '555-0107', '147 Birch Way, Everywhere, USA'),
('Jennifer Martinez', 'jen.martinez@email.com', '555-0108', '258 Spruce Ct, Nowhere, USA'),
('Christopher Garcia', 'chris.garcia@email.com', '555-0109', '369 Willow Pl, Anywhere, USA'),
('Amanda Rodriguez', 'amanda.rodriguez@email.com', '555-0110', '741 Aspen Blvd, Somewhere, USA');

-- Insert sample borrowings
INSERT INTO borrowings (book_id, member_id, borrow_date, return_date, status) VALUES 
(1, 1, '2024-01-15', '2024-01-29', 'returned'),
(2, 2, '2024-01-20', '2024-02-03', 'borrowed'),
(3, 3, '2024-01-25', '2024-02-08', 'borrowed'),
(4, 4, '2024-01-10', '2024-01-24', 'returned'),
(5, 5, '2024-01-30', '2024-02-13', 'borrowed');

-- Update book availability based on borrowings
UPDATE books SET available = available - 1 WHERE id IN (2, 3, 5);

-- Create indexes for better performance
CREATE INDEX idx_books_title ON books(title);
CREATE INDEX idx_books_author ON books(author);
CREATE INDEX idx_books_isbn ON books(isbn);
CREATE INDEX idx_members_name ON members(name);
CREATE INDEX idx_members_email ON members(email);
CREATE INDEX idx_borrowings_book_id ON borrowings(book_id);
CREATE INDEX idx_borrowings_member_id ON borrowings(member_id);
CREATE INDEX idx_borrowings_status ON borrowings(status);

-- Create views for common queries
CREATE VIEW available_books AS
SELECT id, title, author, year, isbn, quantity, available 
FROM books 
WHERE available > 0;

CREATE VIEW borrowed_books AS
SELECT b.id, b.title, b.author, m.name as member_name, 
       br.borrow_date, br.return_date, br.status
FROM books b
JOIN borrowings br ON b.id = br.book_id
JOIN members m ON br.member_id = m.id
WHERE br.status = 'borrowed';

CREATE VIEW overdue_books AS
SELECT b.id, b.title, b.author, m.name as member_name, 
       br.borrow_date, br.return_date, 
       DATEDIFF(CURDATE(), br.return_date) as days_overdue
FROM books b
JOIN borrowings br ON b.id = br.book_id
JOIN members m ON br.member_id = m.id
WHERE br.status = 'borrowed' AND br.return_date < CURDATE();

-- Show table information
SELECT 'Database Setup Complete!' as message;
SELECT COUNT(*) as total_users FROM users;
SELECT COUNT(*) as total_books FROM books;
SELECT COUNT(*) as total_members FROM members;
SELECT COUNT(*) as total_borrowings FROM borrowings; 