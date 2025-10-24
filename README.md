# JavaFX Library Management System

A desktop application for managing a library's catalog, members, and borrowing system. Built with JavaFX and backed by a MySQL database, this application provides a role-based interface for both Librarians and Students.

A key feature of this project is its **automatic database setup**. On the first run, the application automatically connects to your local MySQL server, creates the `librarydb` database, and builds all the necessary tables (`users`, `books`, `members`, `borrowings`, `fine_transactions`).

## Features

* **User Authentication:** Secure login system for all users.
* **Role-Based Access:** Separate dashboards and permissions for Librarians and Students.
* **Librarian Dashboard:**
    * View at-a-glance statistics: Total Books, Borrowed Books, Overdue Books, and Total Members.
    * **Book Management:** Add new books to the catalog.
    * **Member Management:** Add new members (students) to the system.
    * **Borrow/Return System:** A dedicated interface to process book borrowings.
* **Student Dashboard:** (Functionality to be expanded)
* **Automatic Database Setup:** Runs all `CREATE TABLE` scripts on first launch to initialize the database schema.

## Technologies Used

* **Core:** Java
* **GUI:** JavaFX, FXML
* **Database:** MySQL
* **Connector:** JDBC (MySQL Connector/J)

## ⚠️ Important: How to Run This Project

This project requires a MySQL database server and the JavaFX SDK.

### Step 1: Prerequisites

1.  **MySQL Server:** You must have a MySQL server running on your local machine (e.g., via MySQL Workbench, XAMPP). The application is hardcoded to connect to `jdbc:mysql://127.0.0.1:3306/`.
2.  **JavaFX SDK:** You need the JavaFX SDK. You can download it from [GluonHQ](https://gluonhq.com/products/javafx/).
3.  **MySQL Connector/J:** You need the `.jar` file for the MySQL JDBC driver. You can download it from the [MySQL website](https://dev.mysql.com/downloads/connector/j/).

### Step 2: Configure Database Password

Before running, you **MUST** update the database password in the source code to match your local MySQL `root` password.

1.  Open the file: `src/application/DBConnection.java`
2.  Find this line:
    ```java
    private static final String PASSWORD = "@mysql#567"; 
    ```
3.  Change `@mysql#567` to your actual MySQL `root` password.

### Step 3: Configure Your IDE (IntelliJ or Eclipse)

1.  **Add Libraries:**
    * Add the **JavaFX SDK** libraries to your project's build path.
    * Add the **MySQL Connector/J** `.jar` file to your project's build path.
2.  **Add VM Options:** To run a JavaFX application, you must add VM arguments.
    * Go to "Run" -> "Edit Configurations...".
    * In the "VM options" field, add the following (replace `/path/to/your/javafx-sdk` with the real path on your computer):
    ```
    --module-path /path/to/your/javafx-sdk/lib --add-modules javafx.controls,javafx.fxml
    ```

### Step 4: Run the Application

* Run the `src/application/LibraryApp.java` file (not `Main.java`).
* The first time it runs, it will create the `librarydb` database and all tables.

### Step 5: Log In

The database is pre-populated with two default users:

* **Librarian Login**
    * **Username:** `librarian`
    * **Password:** `lib123`
* **Student Login**
    * **Username:** `student`
    * **Password:** `student123`
