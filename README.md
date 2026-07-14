# TraceLens AI

TraceLens AI is an AI-powered digital evidence analysis and investigation platform built using Spring Boot.

The platform is designed to help authorised investigators organise digital evidence, extract useful information, identify suspicious patterns, generate chronological timelines, and prepare structured investigation reports.

## Current Development Status

### Day 1 — Backend Foundation

- Initialised the Spring Boot Maven project
- Configured MySQL, Spring Data JPA, and Hibernate
- Added environment-based database credentials
- Implemented a reusable API response structure
- Created the system-status REST endpoint
- Added live database-connectivity verification
- Added global REST exception handling
- Connected and published the project to GitHub

### Day 2 — Authentication and Security

- Created the `User` JPA entity and `Role` enum
- Added the Spring Data JPA user repository
- Added unique email protection
- Implemented secure user registration
- Added request validation and field-level error responses
- Normalised email addresses and full names
- Implemented BCrypt password hashing
- Assigned the default `INVESTIGATOR` role
- Added duplicate-email conflict handling
- Implemented email-and-password login
- Added JWT access-token generation and validation
- Configured stateless Spring Security
- Added a database-backed `UserDetailsService`
- Added the protected current-user endpoint
- Added handling for invalid credentials and disabled accounts

## Technology Stack

- Java 17 compatible
- Spring Boot 4.1.0
- Spring Web MVC
- Spring Data JPA
- Spring Security
- OAuth2 Resource Server
- JWT authentication
- BCrypt password hashing
- Hibernate
- MySQL
- Maven

## Current Project Structure

```text
com.tracelens
├── auth
│   ├── controller
│   │   └── AuthController.java
│   ├── dto
│   │   ├── LoginRequest.java
│   │   ├── LoginResponse.java
│   │   ├── RegisterRequest.java
│   │   └── UserResponse.java
│   └── service
│       └── AuthService.java
│
├── common
│   └── ApiResponse.java
│
├── exception
│   ├── DuplicateEmailException.java
│   ├── ErrorResponse.java
│   ├── GlobalExceptionHandler.java
│   └── UserNotFoundException.java
│
├── security
│   ├── CustomUserDetailsService.java
│   ├── JwtProperties.java
│   ├── JwtService.java
│   └── SecurityConfig.java
│
├── system
│   ├── controller
│   │   └── SystemStatusController.java
│   └── service
│       └── SystemStatusService.java
│
├── user
│   ├── entity
│   │   ├── Role.java
│   │   └── User.java
│   └── repository
│       └── UserRepository.java
│
└── TracelensBackendApplication.java
```

## Environment Variables

The application requires the following environment variables:

```text
DB_PASSWORD=your_mysql_password
JWT_SECRET=your_base64_encoded_jwt_secret
```

Optional environment variables:

```text
DB_USERNAME=root
DB_URL=jdbc:mysql://localhost:3306/tracelens_db
```

Do not commit real passwords, JWT secrets, API keys, or authentication tokens to GitHub.

### Eclipse / Spring Tools Setup

Open:

```text
Run
→ Run Configurations
→ Spring Boot App
→ TraceLens Backend
→ Environment
```

Add:

```text
DB_PASSWORD
JWT_SECRET
```

The real values remain in the local run configuration and are not stored in the source code.

## Database Setup

Create the MySQL database before running the application:

```sql
CREATE DATABASE IF NOT EXISTS tracelens_db
CHARACTER SET utf8mb4
COLLATE utf8mb4_unicode_ci;
```

Select the database:

```sql
USE tracelens_db;
```

Hibernate creates and updates the required tables from the JPA entities.

The current database contains the following main table:

```text
users
```

The `users` table stores:

- User ID
- Full name
- Email address
- BCrypt password hash
- Role
- Account status
- Creation timestamp
- Update timestamp

## Application Configuration

The database password and JWT secret are loaded through environment variables:

```properties
spring.datasource.username=${DB_USERNAME:root}
spring.datasource.password=${DB_PASSWORD}

app.security.jwt.secret=${JWT_SECRET}
app.security.jwt.issuer=tracelens-backend
app.security.jwt.access-token-expiration-minutes=60
```

Real credentials must never be written directly inside `application.properties`.

## Running the Application

Run:

```text
TracelensBackendApplication.java
```

as a Spring Boot application.

The backend starts at:

```text
http://localhost:8080
```

A successful startup should display messages similar to:

```text
HikariPool-1 - Start completed
Tomcat started on port 8080
Started TracelensBackendApplication
```

## Available APIs

### System Status

```http
GET /api/system/status
```

This is a public endpoint used to verify that the Spring Boot application and MySQL database are connected.

Example local URL:

```text
http://localhost:8080/api/system/status
```

A successful response includes:

```json
{
  "success": true,
  "message": "TraceLens backend is running successfully",
  "data": {
    "applicationStatus": "UP",
    "databaseStatus": "CONNECTED"
  }
}
```

### Register User

```http
POST /api/auth/register
```

Example request:

```json
{
  "fullName": "Basit Mahmood",
  "email": "basit.test@example.com",
  "password": "TraceLens@123"
}
```

A successful registration returns:

```text
201 Created
```

New users receive the default role:

```text
INVESTIGATOR
```

Passwords are stored as BCrypt hashes and are never returned through the API.

### Login

```http
POST /api/auth/login
```

Example request:

```json
{
  "email": "basit.test@example.com",
  "password": "TraceLens@123"
}
```

A successful login returns:

- JWT access token
- Token type
- Token expiration time
- Safe user details

Example response structure:

```json
{
  "success": true,
  "message": "Login completed successfully",
  "data": {
    "accessToken": "jwt-token",
    "tokenType": "Bearer",
    "expiresIn": 3600,
    "user": {
      "id": 1,
      "fullName": "Basit Mahmood",
      "email": "basit.test@example.com",
      "role": "INVESTIGATOR",
      "active": true
    }
  }
}
```

### Get Current User

```http
GET /api/auth/me
```

This is a protected endpoint.

Send the access token using the following request header:

```http
Authorization: Bearer <access-token>
```

The endpoint returns the currently authenticated user’s safe profile information.

## Current Security Rules

The following endpoints are public:

```text
GET  /api/system/status
POST /api/auth/register
POST /api/auth/login
```

All other endpoints require a valid JWT access token.

JWT access tokens currently expire after:

```text
60 minutes
```

The application uses stateless authentication, which means the backend does not store HTTP login sessions.

## Validation Rules

### Registration

Full name:

- Required
- Minimum 2 characters
- Maximum 100 characters

Email:

- Required
- Must use a valid email format
- Maximum 150 characters
- Must be unique

Password:

- Required
- Minimum 8 characters
- Maximum 64 characters
- Must contain one uppercase letter
- Must contain one lowercase letter
- Must contain one number
- Must contain one supported special character

## Error Handling

The API currently handles:

- Invalid registration fields with `400 Bad Request`
- Duplicate email registrations with `409 Conflict`
- Invalid login credentials with `401 Unauthorized`
- Missing or invalid JWT tokens with `401 Unauthorized`
- Disabled user accounts with `403 Forbidden`
- Missing users with `404 Not Found`
- Database constraint conflicts with `409 Conflict`
- Unexpected server errors with `500 Internal Server Error`

Example validation-error structure:

```json
{
  "success": false,
  "status": 400,
  "error": "Bad Request",
  "message": "Request validation failed",
  "path": "/api/auth/register",
  "fieldErrors": {
    "email": "Enter a valid email address",
    "password": "Password must contain an uppercase letter, lowercase letter, number and special character"
  }
}
```

## Planned Features

- Investigation case management
- Case ownership and role-based access
- Case status and priority tracking
- Digital evidence upload
- PDF, TXT, CSV, and JSON text extraction
- SHA-256 evidence-integrity verification
- Spring AI integration
- AI-powered evidence analysis
- Suspicious-activity detection
- Entity extraction
- Investigation timeline generation
- Investigator notes
- Final investigation reports
- Dashboard and analytics
- Automated testing
- Deployment

## Git Workflow

Development is committed incrementally using GitHub Desktop.

Daily workflow:

```text
Complete and test a feature
→ Review changed files
→ Commit to main
→ Push origin
```

Secrets, build output, uploaded evidence, logs, and generated reports are excluded through `.gitignore`.

## Disclaimer

AI-generated findings are investigative aids and must be independently verified before being used for legal, disciplinary, or security decisions.