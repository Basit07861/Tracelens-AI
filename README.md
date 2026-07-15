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

### Day 3 — Investigation Case Management

- Created the `InvestigationCase` JPA entity
- Added case-status and case-priority enums
- Connected investigation cases to their user owners
- Added unique human-readable case numbers
- Added case lifecycle timestamps
- Added indexes for owner, status, priority, and creation time
- Implemented authenticated case creation
- Implemented case retrieval, update, status update, and deletion
- Enforced JWT-based case ownership
- Added case request validation and normalisation
- Added keyword search across case number, title, and description
- Added status and priority filtering
- Added pagination and sorting
- Restricted sorting to approved fields
- Added validation for invalid pagination and sort values
- Added safe handling for unsupported enum values
- Fixed searchable MySQL `TEXT` mapping for Hibernate

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
- GitHub Desktop

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
│   ├── ApiResponse.java
│   └── PageResponse.java
│
├── exception
│   ├── CaseNotFoundException.java
│   ├── DuplicateEmailException.java
│   ├── ErrorResponse.java
│   ├── GlobalExceptionHandler.java
│   ├── InvalidRequestException.java
│   └── UserNotFoundException.java
│
├── investigation
│   ├── controller
│   │   └── InvestigationCaseController.java
│   ├── dto
│   │   ├── CaseResponse.java
│   │   ├── CreateCaseRequest.java
│   │   ├── UpdateCaseRequest.java
│   │   └── UpdateCaseStatusRequest.java
│   ├── entity
│   │   ├── CasePriority.java
│   │   ├── CaseStatus.java
│   │   └── InvestigationCase.java
│   ├── repository
│   │   ├── InvestigationCaseRepository.java
│   │   └── InvestigationCaseSpecifications.java
│   └── service
│       └── InvestigationCaseService.java
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

The application requires these environment variables:

```text
DB_PASSWORD=your_mysql_password
JWT_SECRET=your_base64_encoded_jwt_secret
```

Optional environment variables:

```text
DB_USERNAME=root
DB_URL=jdbc:mysql://localhost:3306/tracelens_db
```

Do not commit real passwords, JWT secrets, API keys, access tokens, or uploaded evidence to GitHub.

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

The real values remain inside the local run configuration and are not stored in the source code.

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

Hibernate creates and updates the database tables from the JPA entities.

The current database contains:

```text
users
investigation_cases
```

### `users` Table

Stores:

- User ID
- Full name
- Email address
- BCrypt password hash
- Role
- Account status
- Creation timestamp
- Update timestamp

### `investigation_cases` Table

Stores:

- Internal case ID
- Unique case number
- Case title
- Case description
- Status
- Priority
- Owner ID
- Creation timestamp
- Update timestamp

Every investigation case is connected to an existing user through the `owner_id` foreign key.

## Application Configuration

Database credentials and JWT configuration are loaded through environment variables:

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

## Authentication

The application uses stateless JWT authentication.

Authentication flow:

```text
Register user
→ Password is hashed using BCrypt
→ Log in using email and password
→ Receive signed JWT access token
→ Send token in Authorization header
→ Access protected endpoints
```

Protected requests must include:

```http
Authorization: Bearer <access-token>
```

JWT access tokens currently expire after:

```text
60 minutes
```

## Available APIs

### System Status

```http
GET /api/system/status
```

Public endpoint used to verify the Spring Boot application and MySQL connection.

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

---

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

---

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
- Token-expiration time
- Safe user information

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
      "id": 2,
      "fullName": "Basit Mahmood",
      "email": "basit.test@example.com",
      "role": "INVESTIGATOR",
      "active": true
    }
  }
}
```

---

### Get Current User

```http
GET /api/auth/me
```

Protected endpoint that returns the currently authenticated user’s safe profile information.

Request header:

```http
Authorization: Bearer <access-token>
```

---

## Investigation Case APIs

All case endpoints require a valid JWT access token.

### Create Investigation Case

```http
POST /api/cases
```

Example request:

```json
{
  "title": "Suspicious Invoice Investigation",
  "description": "Investigate possible invoice manipulation and an unusual payment request.",
  "priority": "HIGH"
}
```

A successful response returns:

```text
201 Created
```

The backend automatically:

- Generates a unique case number
- Sets the initial status to `OPEN`
- Uses `MEDIUM` when priority is omitted
- Associates the case with the authenticated user
- Adds creation and update timestamps

Example generated case number:

```text
TL-20260715-YV3SBJQ4
```

---

### Retrieve One Investigation Case

```http
GET /api/cases/{caseId}
```

The case is returned only when it belongs to the authenticated user.

A missing or unowned case returns:

```text
404 Not Found
```

This prevents investigators from accessing cases owned by other users.

---

### Update Investigation Case

```http
PUT /api/cases/{caseId}
```

Example request:

```json
{
  "title": "Critical Invoice Manipulation Investigation",
  "description": "Investigate invoice manipulation, payment instructions, and related communication records.",
  "priority": "CRITICAL"
}
```

This endpoint updates:

- Title
- Description
- Priority

Status is updated through a separate endpoint.

---

### Update Case Status

```http
PATCH /api/cases/{caseId}/status
```

Example request:

```json
{
  "status": "IN_PROGRESS"
}
```

Supported values:

```text
OPEN
IN_PROGRESS
COMPLETED
ARCHIVED
```

---

### Delete Investigation Case

```http
DELETE /api/cases/{caseId}
```

Deletes a case only when it belongs to the authenticated user.

A successful response returns:

```text
Investigation case deleted successfully
```

---

### List Investigation Cases

```http
GET /api/cases
```

Returns only the authenticated user’s cases.

Default settings:

```text
page=0
size=10
sortBy=createdAt
sortDirection=desc
```

Example:

```http
GET /api/cases?page=0&size=10
```

The paginated response contains:

- Case records
- Current page number
- Page size
- Total records
- Total pages
- First-page indicator
- Last-page indicator
- Next-page availability
- Previous-page availability
- Sorting information

Example structure:

```json
{
  "success": true,
  "message": "Investigation cases retrieved successfully",
  "data": {
    "content": [],
    "pageNumber": 0,
    "pageSize": 10,
    "totalElements": 0,
    "totalPages": 0,
    "first": true,
    "last": true,
    "hasNext": false,
    "hasPrevious": false,
    "sortBy": "createdAt",
    "sortDirection": "desc"
  }
}
```

## Case Search and Filtering

### Keyword Search

```http
GET /api/cases?keyword=invoice
```

The keyword is searched in:

- Case number
- Case title
- Case description

Search is case-insensitive.

### Status Filter

```http
GET /api/cases?status=IN_PROGRESS
```

Supported values:

```text
OPEN
IN_PROGRESS
COMPLETED
ARCHIVED
```

### Priority Filter

```http
GET /api/cases?priority=HIGH
```

Supported values:

```text
LOW
MEDIUM
HIGH
CRITICAL
```

### Combined Filters

```http
GET /api/cases?keyword=invoice&status=IN_PROGRESS&priority=HIGH
```

### Pagination

```http
GET /api/cases?page=0&size=10
```

Rules:

```text
page must be 0 or greater
size must be between 1 and 100
```

### Sorting

```http
GET /api/cases?sortBy=title&sortDirection=asc
```

Supported sort fields:

```text
id
caseNumber
title
status
priority
createdAt
updatedAt
```

Supported directions:

```text
asc
desc
```

Complete example:

```http
GET /api/cases?keyword=invoice&status=IN_PROGRESS&priority=HIGH&page=0&size=10&sortBy=createdAt&sortDirection=desc
```

## Case Status Values

```text
OPEN
```

The case has been created but active investigation has not started.

```text
IN_PROGRESS
```

The investigator is currently reviewing evidence and recording findings.

```text
COMPLETED
```

The investigation and final reporting work are complete.

```text
ARCHIVED
```

The case is retained for historical reference and is no longer actively managed.

## Case Priority Values

```text
LOW
MEDIUM
HIGH
CRITICAL
```

When priority is omitted during case creation, the backend assigns:

```text
MEDIUM
```

## Current Security Rules

The following endpoints are public:

```text
GET  /api/system/status
POST /api/auth/register
POST /api/auth/login
```

The following endpoints require JWT authentication:

```text
GET    /api/auth/me
POST   /api/cases
GET    /api/cases
GET    /api/cases/{caseId}
PUT    /api/cases/{caseId}
PATCH  /api/cases/{caseId}/status
DELETE /api/cases/{caseId}
```

The application uses stateless authentication, so the backend does not store HTTP login sessions.

## Ownership Protection

Every investigation case is associated with the authenticated user.

The backend does not accept an owner ID from the client.

Ownership is taken from the validated JWT:

```text
JWT subject
→ Authenticated email
→ Database user
→ Investigation case owner
```

Case retrieval, updates, status changes, and deletion verify both:

```text
Case ID matches
AND
Owner email matches authenticated user
```

A case belonging to another investigator is returned as:

```text
404 Not Found
```

This avoids revealing whether another user’s case exists.

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

### Case Creation

Title:

- Required
- Minimum 5 characters
- Maximum 150 characters

Description:

- Required
- Minimum 10 characters
- Maximum 5000 characters

Priority:

- Optional
- Defaults to `MEDIUM`
- Must be a supported `CasePriority` value

### Case Update

Title:

- Required
- Minimum 5 characters
- Maximum 150 characters

Description:

- Required
- Minimum 10 characters
- Maximum 5000 characters

Priority:

- Required
- Must be a supported `CasePriority` value

## Error Handling

The API currently handles:

- Invalid request fields with `400 Bad Request`
- Malformed JSON with `400 Bad Request`
- Unsupported enum values with `400 Bad Request`
- Invalid pagination values with `400 Bad Request`
- Unsupported sorting fields with `400 Bad Request`
- Invalid request-parameter types with `400 Bad Request`
- Duplicate email registrations with `409 Conflict`
- Invalid login credentials with `401 Unauthorized`
- Missing or invalid JWT tokens with `401 Unauthorized`
- Disabled user accounts with `403 Forbidden`
- Missing users with `404 Not Found`
- Missing or unowned investigation cases with `404 Not Found`
- Database constraint conflicts with `409 Conflict`
- Unexpected server errors with `500 Internal Server Error`

Example validation-error structure:

```json
{
  "success": false,
  "status": 400,
  "error": "Bad Request",
  "message": "Request validation failed",
  "path": "/api/cases",
  "fieldErrors": {
    "title": "Case title is required",
    "description": "Case description must contain between 10 and 5000 characters"
  }
}
```

## Planned Features

- Digital evidence upload
- File-type and file-size validation
- Evidence metadata storage
- SHA-256 evidence-integrity verification
- PDF, TXT, CSV, and JSON text extraction
- Spring AI integration
- AI-powered evidence summarisation
- Suspicious-activity detection
- Risk-level classification
- Person, organisation, location, and date extraction
- Investigation timeline generation
- Investigator notes
- Final investigation reports
- Dashboard and analytics
- Automated tests
- API documentation
- Docker deployment

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