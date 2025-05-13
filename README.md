# Library Management System

## Project Overview

This project is a comprehensive Library Management System developed using Spring Boot 3, Java 21, and PostgreSQL database. The system allows librarians to manage books, users, and borrowing/returning processes. It also includes user authentication, authorization, API documentation, and testing functionality.

## Table of Contents

- [Technology Stack](#technology-stack)
- [ER Diagram](#er-diagram)
- [Project Structure](#project-structure)
- [Entities](#entities)
- [API Endpoints](#api-endpoints)
- [JWT and Security](#jwt-and-security)
- [Installation and Running](#installation-and-running)
- [Docker Setup](#docker-setup)
- [Test Coverage](#test-coverage)
- [Postman Collection](#postman-collection)
- [Screenshots](#screenshots)
- [License](#license)

## Technology Stack

- **Backend**:
  - Spring Boot 3.4.5
  - Java 21
  - Spring Data JPA
  - Spring Security
  - Spring Validation
  - Spring DevTools
  - JWT Authentication (acces + refresh token / role based authentication)
  - Swagger/OpenAPI Documentation

- **Database**:
  - PostgreSQL
  - H2 Database (for testing)

- **Build Tool**:
  - Maven

- **Testing**:
  - JUnit 5
  - Mockito
  - Spring Boot Test
  - Spring Security Test
  - AssertJ
  - Jacoco (for test coverage)

- **DevOps**:
  - Docker & Docker Compose
  - Git

- **Other Tools**:
  - Lombok
  - SLF4J/Logback (for logging)

## Project Structure

```
library-management-system/
├── 📁 src/
│   ├── 📁 main/
│   │   ├── 📁 java/com/getir/aau/librarymanagementsystem/
│   │   │   ├── 📁 config/
│   │   │   ├── 📁 controller/
│   │   │   ├── 📁 exception/
│   │   │   ├── 📁 initializer/
│   │   │   ├── 📁 model/
│   │   │   │   ├── 📁 dto/
│   │   │   │   ├── 📁 entity/
│   │   │   │   └── 📁 mapper/
│   │   │   ├── 📁 repository/
│   │   │   ├── 📁 security/
│   │   │   │   ├── 📁 auth/
│   │   │   │   ├── 📁 exception/
│   │   │   │   ├── 📁 jwt/
│   │   │   │   ├── 📁 token/
│   │   │   │   ├── 📄 CustomUserDetailsService.java
│   │   │   │   └── 📄 SecurityUtils.java
│   │   │   ├── 📁 service/
│   │   │   │   ├── 📁 impl/
│   │   │   │   ├── 📄 AuthorService.java
│   │   │   │   ├── 📄 BookService.java
│   │   │   │   ├── 📄 BorrowItemService.java
│   │   │   │   ├── 📄 BorrowRecordService.java
│   │   │   │   ├── 📄 CategoryService.java
│   │   │   │   └── 📄 UserService.java
│   │   │   └── 📄 LibraryManagementSystemApplication.java
│   │   └── 📁 resources/
│   │       ├── 📁 data/
│   │       ├── 📁 static/
│   │       ├── 📁 templates/
│   │       ├── 📄 application.yml
│   │       └── 📄 application-docker.yml
│
│   └── 📁 test/
│       └── 📁 java/com/getir/aau/librarymanagementsystem/
│           ├── 📁 integration/
│           │   ├── 📁 security/
│           │   └── 📁 service/
│           ├── 📁 unit/
│           │   ├── 📁 controller/
│           │   ├── 📁 security/
│           │   └── 📁 service/
│           └── 📄 LibraryManagementSystemApplicationTests.java
├── 📄 docker-compose.yml
├── 📄 Dockerfile
├── 📄 pom.xml
└── 📄 README.md
```

## ER Diagram

Below is the ER Diagram showing the database schema used in the project:

![library_db](https://github.com/user-attachments/assets/49a83124-4375-491b-b4d8-d8146638207c)

## Entities

> All data is validated using `javax.validation` constraints, ensuring proper format and input integrity.
> DTOs are implemented as immutable Java `record`s, promoting concise, thread-safe, and boilerplate-free code.

### 📘 Author

Stores author details with unique name and description. Each author can be associated with multiple books via a one-to-many relationship.

### 📗 Book

Contains book metadata such as title, ISBN, description, genre, publication date, and number of copies. Each book is linked to one author and one category. Borrowing logic is embedded using rich domain methods (`borrow`, `returnBook`, `updateAvailability`).

### 📒 Category

Defines categories/genres that group books. Each category can be associated with multiple books.

### 📕 BorrowItem

Represents a single item in a user's borrowing transaction. It is related to the `User`, `Book`, and `BorrowRecord` entities.

Each `BorrowItem` stores the `borrow date`, `due date`, `return date`, and whether it has been returned.
When a book is returned, the `markAsReturned()` method is called to finalize the return and update the book's availability. This structure is essential for `tracking active` and `overdue loans`.

### 📙 BorrowRecord

Represents a user's borrowing transaction, which can contain multiple borrowed books (`BorrowItem`). It includes details such as borrow date and due date, and is linked to the user who initiated the borrowing. Acts as a wrapper to group multiple `BorrowItem` instances for reporting, tracking, and due management.

Tracks a user's borrowing session. Contains the borrow date, due date, and a list of `BorrowItem` entries. Establishes a many-to-one relationship with `User`.

### 👤 User

Stores user details such as name, email, password, and phone number. Implements `UserDetails` for Spring Security integration. Each user is associated with a role and can have multiple borrowings and tokens.
Represents the system users, including login credentials and role information. Can borrow multiple books.

### 📘 Role

Represents a user's role in the system with a name and description. Connects users to their permissions.

### 🟪 ERole (Enum)

Enumerates the available roles: `ROLE_USER`, `ROLE_LIBRARIAN`. Used in the `Role` entity for Spring Security.

### 🛡️ Token

Represents `access` and `refresh tokens` assigned to users. Each token has a unique value and is typed as `BEARER`. Tokens can be `revoked` or `expired`. This structure enables multi-session management and helps track the user's active authenticated sessions. Each token is linked to a single user.

## Key Features

### 📚 Book Management

* **Add Book**: Librarians can add books with details like title, author, ISBN, publication date, and genre. Input is validated.
* **View Book Details**: All authenticated users can view book details.
* **Search Books**: Users can search books by title, category name, author name, ISBN, or genre. Pagination is supported.
* **Update Book**: Librarians can update book details.
* **Delete Book**: Librarians can remove books from the system.

### 👤 User Management

* **User Registration**: All users can register with the default role: ROLE\_USER.
* **Role Management**: Roles (LIBRARIAN or USER) can be assigned by LIBRARIAN users.
* **View User**: Librarians can view all users. Users can view their own information.
* **Update User**: Librarians can update any user. Users can update their own information.
* **Delete User**: Librarians can remove users from the system.

### 🔄 Borrowing & Returning

* **Borrow a Book**
  Users can borrow multiple books in a single transaction. The system checks:

  * Whether the user has reached the maximum borrow limit
  * Whether the user has overdue items
  * Whether the selected books are available

* **Return a Book**
  Librarians and users can mark books as returned. The system:

  * Sets the return date
  * Automatically updates the book's availability and number of copies

* **Borrow Record Management**
  Librarians can:

  * View all borrow records with pagination
  * Filter borrow records by user, email, or date range
  * Check if a user is eligible to borrow (overdue records and active borrow status)
  * Check book availability based on stock and status

* **Borrow Item Insights**
  The system provides:

  * All borrow items per user
  * Active borrow items and overdue item checks
  * Count of active borrow items per user
  * Overdue borrow item reports
  * Borrow records filtered by book, author, or category
  * Borrow statistics by date range

## User Flow

### 🔐 Registration and Login

* Every newly registered user is assigned the `ROLE_USER` by default.
* A librarian must register first, then another librarian can assign them the `ROLE_LIBRARIAN`.

### 📚 Book Borrowing Flow

* When a user borrows a book:

  * A new `BorrowRecord` is created.
  * Each book is registered as a `BorrowItem` under that record.
  * Borrow date and due date are automatically calculated.

* When a user returns a book:

  * Return date is saved.
  * The book's copy count and availability are updated.
  * The corresponding `BorrowItem` and `BorrowRecord` are marked as returned.
  * The user's borrow eligibility is recalculated.

This structured flow ensures data integrity and accurate tracking of library operations.

## API Endpoints

### 🛡️ Authentication

| Method | Endpoint             | Description                                      | Access Permission |
| ------ | -------------------- | ------------------------------------------------ | ----------------- |
| POST   | `/api/auth/login`    | Authenticate user and get tokens                 | Public            |
| POST   | `/api/auth/logout`   | Logout user (revoke current token)               | Public            |
| POST   | `/api/auth/refresh`  | Refresh access token using a valid refresh token | Public            |
| POST   | `/api/auth/register` | Register a new user                              | Public            |

### 👤 Author Management

| Method | Endpoint            | Description               | Access Permission |
| ------ | ------------------- | ------------------------- | ----------------- |
| GET    | `/api/authors`      | Get all authors           | LIBRARIAN         |
| POST   | `/api/authors`      | Create a new author       | LIBRARIAN         |
| GET    | `/api/authors/{id}` | Get author by ID          | LIBRARIAN         |
| PUT    | `/api/authors/{id}` | Update an existing author | LIBRARIAN         |
| DELETE | `/api/authors/{id}` | Delete an author          | LIBRARIAN         |

### 📚 Book Management

| Method | Endpoint                             | Description              | Access Permission |
| ------ | ------------------------------------ | ------------------------ | ----------------- |
| POST   | `/api/books`                         | Create a new book        | LIBRARIAN         |
| GET    | `/api/books/{id}`                    | Get book by ID           | USER, LIBRARIAN   |
| PUT    | `/api/books/{id}`                    | Update an existing book  | LIBRARIAN         |
| DELETE | `/api/books/{id}`                    | Delete a book            | LIBRARIAN         |
| GET    | `/api/books/author/{authorId}`       | Get books by author ID   | USER, LIBRARIAN   |
| GET    | `/api/books/author/name`             | Get books by author name | USER, LIBRARIAN   |
| GET    | `/api/books/available`               | Get available books      | USER, LIBRARIAN   |
| GET    | `/api/books/category/{categoryId}`   | Get books by category ID | USER, LIBRARIAN   |
| GET    | `/api/books/count/author/{authorId}` | Count books by author ID | USER, LIBRARIAN   |
| GET    | `/api/books/genre`                   | Get books by genre       | USER, LIBRARIAN   |
| GET    | `/api/books/isbn/{isbn}`             | Get book by ISBN         | USER, LIBRARIAN   |
| GET    | `/api/books/search`                  | Search books by keyword  | USER, LIBRARIAN   |
| GET    | `/api/books/title`                   | Get books by title       | USER, LIBRARIAN   |
| GET    | `/api/books/unavailable`             | Get unavailable books    | LIBRARIAN         |

### 🔄 Borrow Items

| Method | Endpoint                                        | Description                          | Access Permission |
| ------ | ----------------------------------------------- | ------------------------------------ | ----------------- |
| PUT    | `/api/borrow-items/{itemId}/return`             | Return a borrowed book               | LIBRARIAN         |
| GET    | `/api/borrow-items/book/{bookId}`               | Get borrow items by book ID          | LIBRARIAN         |
| GET    | `/api/borrow-items/date-range`                  | Get borrow items by date range       | LIBRARIAN         |
| GET    | `/api/borrow-items/overdue`                     | Get overdue borrow items             | LIBRARIAN         |
| GET    | `/api/borrow-items/user/{userId}`               | Get borrow items by user ID          | USER, LIBRARIAN   |
| GET    | `/api/borrow-items/user/{userId}/active`        | Get active borrow items for a user   | USER, LIBRARIAN   |
| GET    | `/api/borrow-items/user/{userId}/count-active`  | Count active borrow items for a user | USER, LIBRARIAN   |
| GET    | `/api/borrow-items/user/{userId}/exist-overdue` | Check if user has overdue items      | USER, LIBRARIAN   |

### 📋 Borrow Records

| Method | Endpoint                                         | Description                                   | Access Permission |
| ------ | ------------------------------------------------ | --------------------------------------------- | ----------------- |
| GET    | `/api/borrow-records`                            | Get all borrow records (paginated)            | LIBRARIAN         |
| POST   | `/api/borrow-records`                            | Create a new borrow record                    | USER, LIBRARIAN   |
| GET    | `/api/borrow-records/{id}`                       | Get borrow record by ID                       | USER, LIBRARIAN   |
| GET    | `/api/borrow-records/book-availability/{bookId}` | Check if a book is available for borrowing    | LIBRARIAN         |
| GET    | `/api/borrow-records/check-eligibility/{userId}` | Check if user is eligible to borrow books     | LIBRARIAN         |
| GET    | `/api/borrow-records/filter`                     | Filter borrow records by email and date range | LIBRARIAN         |
| GET    | `/api/borrow-records/user/{userId}`              | Get borrow records by user ID                 | USER, LIBRARIAN   |
| GET    | `/api/borrow-records/user/{userId}/active`       | Get active borrow records by user ID          | USER, LIBRARIAN   |

### 🗂️ Category Management

| Method | Endpoint               | Description                 | Access Permission |
| ------ | ---------------------- | --------------------------- | ----------------- |
| GET    | `/api/categories`      | Get all categories          | LIBRARIAN         |
| POST   | `/api/categories`      | Create a new category       | LIBRARIAN         |
| GET    | `/api/categories/{id}` | Get category by ID          | LIBRARIAN         |
| PUT    | `/api/categories/{id}` | Update an existing category | LIBRARIAN         |
| DELETE | `/api/categories/{id}` | Delete a category           | LIBRARIAN         |

### 👥 User Management

| Method | Endpoint               | Description                            | Access Permission |
| ------ | ---------------------- | -------------------------------------- | ----------------- |
| GET    | `/api/users`           | Get all users                          | LIBRARIAN         |
| POST   | `/api/users`           | Create a new user (default role: USER) | LIBRARIAN         |
| GET    | `/api/users/{id}`      | Get user by ID                         | LIBRARIAN         |
| PUT    | `/api/users/{id}`      | Update a user                          | LIBRARIAN         |
| DELETE | `/api/users/{id}`      | Delete a user                          | LIBRARIAN         |
| PUT    | `/api/users/{id}/role` | Change a user's role                   | LIBRARIAN         |
| GET    | `/api/users/email`     | Get user by email                      | LIBRARIAN         |
| GET    | `/api/users/me`        | Get current user's own information     | USER, LIBRARIAN   |

## JWT and Security

Authentication and authorization are implemented using Spring Security and JWT. The system has the following features:

* Endpoints for user registration, login, logout, token refresh, and access control
* Passwords are hashed using BCrypt for security
* Stateless authentication using signed JWTs
* Access tokens expire in 1 day
* Refresh tokens expire in 7 days
* Role-based authorization with two roles: USER and LIBRARIAN

## Installation and Running

### Requirements

- Java 21
- Maven
- PostgreSQL

### Steps

1. Clone the project:
   ```bash
   git clone https://github.com/yourGithubUserName/LibraryManagementSystem.git
   cd LibraryManagementSystem
   ```

2. Set up PostgreSQL database: (only required if you're not using Docker or prefer manual setup)
   ```sql
   CREATE DATABASE library_db;
   CREATE USER your_database_user WITH PASSWORD 'your_database_password';
   GRANT ALL PRIVILEGES ON DATABASE library_db TO your_database_user;
   ```

3. Edit the `application.yml` file:
   ```yaml
   server:
     port: 8080

   spring:
     application:
     name: LibraryManagementSystem

   datasource:
    url: jdbc:postgresql://localhost:5432/library_db
    username: your_database_user
    password: your_database_password
    driver-class-name: org.postgresql.Driver
   
   application:
    security:
      jwt:
        secret-key: your-secret-key
        expiration: 86400000 # 1 day
        refresh-token:
          expiration: 604800000 # 7 days
   ```

4. Build and run the project:
   ```bash
   mvn clean package
   java -jar target/library-management-system-1.0.0.jar
   ```

5. The application will run on port 8080: [http://localhost:8080](http://localhost:8080/swagger-ui/index.html)

## Docker Setup

### Requirements

* Docker
* Docker Compose

### Steps

1. Clone the project:

   ```bash
   git clone https://github.com/<your-username>/LibraryManagementSystem.git
   cd LibraryManagementSystem
   ```

2. Build the Docker image and start the services:

   ```bash
   docker-compose up -d
   ```

3. Access the application:
   [http://localhost:8080/swagger-ui.html](http://localhost:8080/swagger-ui.html)

This command:

* Starts a PostgreSQL 16 database in the `library-db` container
* Builds the Spring Boot app and runs it in the `library-management-app` container
* Creates the necessary network and volume configurations automatically

To stop the services:

```bash
docker-compose down
```

To remove all containers and volumes:

```bash
docker-compose down -v
```

## Test Coverage

### Test Scope

This project includes both `unit tests` and `integration tests`, comprehensive test coverage for **business logic, security, and REST API endpoints**.

>Tests are performed using the H2 in-memory database.

### Technologies Used

* **JUnit 5**: Core testing framework
* **Mockito**: For mocking dependencies
* **AssertJ**: For fluent and expressive assertions

### Test Types

* **Unit Tests**: For individual service and utility methods
* **Integration Tests**: For verifying controller and service behavior with Spring context
* **Security Tests**: For testing role-based access and JWT validation

### Running Tests

To run all tests:

```bash
mvn test
```

Jacoco coverage report will be generated at:

```
target/site/jacoco/index.html
```

## Postman Collection

A Postman collection is provided for testing the API endpoints. The collection file can be found at `link` in the project's root directory.

### Included Features
- 🔐 **Authentication**: Register, login, logout, refresh token
- 📚 **Books**: Create, update, delete, search, and list books
- 👥 **Users**: View, edit, and manage users (by librarians)
- 🔄 **Borrowing**: Borrow, return, and track books
- 🗂 **Categories and Authors**: Full CRUD operations

### Environment Setup
The collection uses a Postman environment with variables such as:
- `{{baseUrl}}`: API base URL (e.g., `http://localhost:8080`)
- `{{token}}`: JWT access token (auto-filled after login)

### Authorization Script (Pre-request)
A JavaScript snippet is included to automatically save the access token:
```javascript
pm.environment.set("token", pm.response.json().accessToken);
```

## Screenshots

#### 📊 Swagger API Documentation

![Swagger API Documentation](https://github.com/user-attachments/assets/0c2cd5f6-9ddb-477c-ac0c-d7ebc7fd4d28)

#### 📈 Test Coverage (Jacoco)

![TestCoverage](https://github.com/user-attachments/assets/308c1cdd-22b8-4342-bb9c-dddfd423df50)

## License

This project is licensed under the MIT License.
