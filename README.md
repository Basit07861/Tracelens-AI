# TraceLens AI

TraceLens AI is an AI-powered digital evidence analysis and investigation platform built using Spring Boot.

The platform is designed to help authorised investigators:

- Create and manage investigation cases
- Upload and organise digital evidence
- Preserve evidence metadata securely
- Verify evidence integrity
- Extract useful information from files
- Identify suspicious patterns
- Generate chronological timelines
- Prepare structured investigation reports

> The evidence extraction and AI-analysis modules are planned for the upcoming development days.

---

## Current Development Status

### Day 1 — Backend Foundation

- Initialised the Spring Boot Maven project
- Configured MySQL, Spring Data JPA and Hibernate
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
- Added indexes for owner, status, priority and creation time
- Implemented authenticated case creation
- Implemented case retrieval, update, status update and deletion
- Enforced JWT-based case ownership
- Added case request validation and normalisation
- Added keyword search across case number, title and description
- Added status and priority filtering
- Added pagination and sorting
- Restricted sorting to approved fields
- Added validation for invalid pagination and sorting values
- Added safe handling for unsupported enum values
- Added reusable paginated API responses
- Fixed searchable MySQL `TEXT` mapping for Hibernate

### Day 4 — Digital Evidence Management

- Created the `Evidence` JPA entity
- Added supported evidence-file-type and processing-status enums
- Connected evidence files to investigation cases
- Added evidence metadata persistence
- Added secure multipart file upload
- Added PDF, TXT, CSV and JSON upload support
- Added configurable maximum upload size
- Added extension and MIME-type validation
- Added empty-file and unsafe-filename validation
- Added optional evidence descriptions
- Generated UUID-based physical filenames
- Stored evidence in case-specific directories
- Prevented path-traversal attacks
- Stored only relative paths in the database
- Prevented internal storage paths from appearing in API responses
- Removed uploaded files when database persistence failed
- Added paginated evidence listing
- Added individual evidence metadata retrieval
- Added authenticated physical evidence download
- Preserved original filenames in download responses
- Enforced case and evidence ownership
- Added transactional evidence deletion
- Removed physical evidence files after successful database commits
- Removed empty case-storage directories
- Added evidence-specific exception handling
- Added upload-size and storage-failure handling

---

## Technology Stack

### Backend

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

### File Management

- Spring multipart upload
- Java NIO file storage
- UUID-based stored filenames
- Case-specific evidence directories
- Secure relative-path handling

### Development Tools

- Eclipse / Spring Tools
- MySQL Workbench
- Git
- GitHub
- GitHub Desktop
- Windows PowerShell

---

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
├── evidence
│   ├── config
│   │   └── EvidenceProperties.java
│   ├── controller
│   │   └── EvidenceController.java
│   ├── dto
│   │   └── EvidenceResponse.java
│   ├── entity
│   │   ├── Evidence.java
│   │   ├── EvidenceFileType.java
│   │   └── EvidenceStatus.java
│   ├── repository
│   │   └── EvidenceRepository.java
│   ├── service
│   │   ├── EvidenceFileValidator.java
│   │   └── EvidenceService.java
│   └── storage
│       ├── EvidenceFileResource.java
│       ├── EvidenceStorageService.java
│       └── StoredEvidenceFile.java
│
├── exception
│   ├── CaseNotFoundException.java
│   ├── DuplicateEmailException.java
│   ├── ErrorResponse.java
│   ├── EvidenceNotFoundException.java
│   ├── EvidenceStorageException.java
│   ├── GlobalExceptionHandler.java
│   ├── InvalidEvidenceFileException.java
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

---

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
EVIDENCE_STORAGE_ROOT=evidence-storage
```

Do not commit real passwords, JWT secrets, API keys, authentication tokens or uploaded evidence to GitHub.

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

`EVIDENCE_STORAGE_ROOT` is optional because the application defaults to:

```text
evidence-storage
```

The real environment-variable values remain in the local run configuration and are not stored in source control.

---

## Database Setup

Create the MySQL database before starting the application:

```sql
CREATE DATABASE IF NOT EXISTS tracelens_db
CHARACTER SET utf8mb4
COLLATE utf8mb4_unicode_ci;
```

Select the database:

```sql
USE tracelens_db;
```

Hibernate creates and updates the required tables using the JPA entity definitions.

The current database contains:

```text
users
investigation_cases
evidence_files
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
- Unique human-readable case number
- Case title
- Case description
- Case status
- Case priority
- Owner ID
- Creation timestamp
- Update timestamp

Every investigation case is connected to an existing user through the `owner_id` foreign key.

### `evidence_files` Table

Stores:

- Evidence ID
- Original uploaded filename
- Generated stored filename
- Storage-relative path
- Supported file type
- Content type
- Exact file size in bytes
- Optional investigator description
- Processing status
- Investigation case ID
- Upload timestamp
- Update timestamp

Every evidence record is connected to an investigation case through the `case_id` foreign key.

The physical evidence bytes are stored on the filesystem, not inside MySQL.

---

## Application Configuration

Database credentials and JWT configuration are loaded through environment variables:

```properties
spring.datasource.username=${DB_USERNAME:root}
spring.datasource.password=${DB_PASSWORD}

app.security.jwt.secret=${JWT_SECRET}
app.security.jwt.issuer=tracelens-backend
app.security.jwt.access-token-expiration-minutes=60
```

Evidence storage configuration:

```properties
spring.servlet.multipart.enabled=true
spring.servlet.multipart.max-file-size=10MB
spring.servlet.multipart.max-request-size=11MB

app.evidence.storage-root=${EVIDENCE_STORAGE_ROOT:evidence-storage}
app.evidence.max-file-size-bytes=10485760
```

Real credentials must never be written directly inside `application.properties`.

---

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
Evidence storage initialized at ...
Tomcat started on port 8080
Started TracelensBackendApplication
```

---

## Authentication

The application uses stateless JWT authentication.

Authentication flow:

```text
Register user
→ Password is hashed using BCrypt
→ Log in using email and password
→ Receive signed JWT access token
→ Send token in the Authorization header
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

---

## Available APIs

## System Status

### Check System Status

```http
GET /api/system/status
```

This public endpoint verifies that the Spring Boot application and MySQL database connection are working.

Example local URL:

```text
http://localhost:8080/api/system/status
```

Example response:

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

## Authentication APIs

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

### Get Current User

```http
GET /api/auth/me
```

Returns the currently authenticated user’s safe profile information.

Request header:

```http
Authorization: Bearer <access-token>
```

---

## Investigation Case APIs

All investigation-case endpoints require a valid JWT access token.

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

The backend automatically:

- Generates a unique case number
- Sets the initial status to `OPEN`
- Uses `MEDIUM` when priority is omitted
- Associates the case with the authenticated user
- Adds creation and update timestamps

Example case number:

```text
TL-20260715-YV3SBJQ4
```

### Retrieve One Investigation Case

```http
GET /api/cases/{caseId}
```

The case is returned only when it belongs to the authenticated user.

A missing or unowned case returns:

```text
404 Not Found
```

### Update Investigation Case

```http
PUT /api/cases/{caseId}
```

Example request:

```json
{
  "title": "Critical Invoice Manipulation Investigation",
  "description": "Investigate invoice manipulation, payment instructions and related communication records.",
  "priority": "CRITICAL"
}
```

This endpoint updates:

- Title
- Description
- Priority

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

Supported case-status values:

```text
OPEN
IN_PROGRESS
COMPLETED
ARCHIVED
```

### Delete Investigation Case

```http
DELETE /api/cases/{caseId}
```

Deletes a case only when it belongs to the authenticated user.

### List Investigation Cases

```http
GET /api/cases
```

Returns only the authenticated user’s cases.

Default values:

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

### Search Investigation Cases

```http
GET /api/cases?keyword=invoice
```

The keyword is searched in:

- Case number
- Case title
- Case description

### Filter by Status

```http
GET /api/cases?status=IN_PROGRESS
```

### Filter by Priority

```http
GET /api/cases?priority=HIGH
```

### Combined Search, Filters and Pagination

```http
GET /api/cases?keyword=invoice&status=IN_PROGRESS&priority=HIGH&page=0&size=10&sortBy=createdAt&sortDirection=desc
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

Supported sort directions:

```text
asc
desc
```

---

## Evidence APIs

All evidence endpoints require a valid JWT access token.

### Upload Evidence

```http
POST /api/cases/{caseId}/evidence
```

Request type:

```text
multipart/form-data
```

Multipart fields:

```text
file         Required
description  Optional
```

Supported file types:

```text
PDF
TXT
CSV
JSON
```

Example using `curl.exe`:

```powershell
curl.exe -i -X POST `
"http://localhost:8080/api/cases/$caseId/evidence" `
-H "Authorization: Bearer $token" `
-F "file=@$sampleFile;type=text/plain" `
-F "description=Invoice communication and payment evidence"
```

A successful upload returns:

```text
201 Created
```

Example response structure:

```json
{
  "success": true,
  "message": "Evidence uploaded successfully",
  "data": {
    "id": 1,
    "caseId": 5,
    "caseNumber": "TL-20260715-YV3SBJQ4",
    "originalFileName": "invoice-evidence.txt",
    "fileType": "TXT",
    "contentType": "text/plain",
    "fileSizeBytes": 213,
    "description": "Invoice communication and payment evidence",
    "status": "UPLOADED",
    "uploadedAt": "2026-07-16T11:58:40Z",
    "updatedAt": "2026-07-16T11:58:40Z"
  }
}
```

The response deliberately does not expose:

```text
storedFileName
storageRelativePath
absolute server path
```

### List Evidence for a Case

```http
GET /api/cases/{caseId}/evidence
```

Optional pagination parameters:

```text
page=0
size=10
```

Example:

```http
GET /api/cases/5/evidence?page=0&size=10
```

Evidence is sorted by upload time in descending order.

The response includes:

- Evidence metadata
- Current page number
- Page size
- Total evidence records
- Total pages
- Next-page availability
- Previous-page availability

### Retrieve Evidence Metadata

```http
GET /api/evidence/{evidenceId}
```

Returns safe evidence metadata only.

It does not return the physical file or internal storage information.

### Download Evidence

```http
GET /api/evidence/{evidenceId}/download
```

The endpoint:

- Requires JWT authentication
- Verifies evidence ownership
- Loads the physical file from secure storage
- Preserves the original uploaded filename
- Returns the stored content as an attachment

Example:

```powershell
curl.exe -L `
"http://localhost:8080/api/evidence/$evidenceId/download" `
-H "Authorization: Bearer $token" `
-o "$env:TEMP\downloaded-evidence.txt"
```

### Delete Evidence

```http
DELETE /api/evidence/{evidenceId}
```

The deletion workflow is:

```text
Verify authenticated ownership
→ Delete evidence metadata inside a database transaction
→ Wait for the database transaction to commit
→ Delete the physical evidence file
→ Remove the empty case directory when applicable
```

A successful response returns:

```text
Evidence deleted successfully
```

A deleted or unowned evidence ID returns:

```text
404 Not Found
```

---

## Evidence Storage Design

Evidence files are stored outside MySQL.

Default structure:

```text
evidence-storage
├── case-5
│   ├── 8ca31a28-3f74-4a43-9e35-b77aeae660cf.txt
│   └── f240eb74-c714-4781-914e-e4e49007188c.pdf
└── case-7
    └── cdafdd8a-9ab8-4209-aad9-bbc0fb4d71a7.json
```

The original uploaded filename is stored only as metadata.

The physical file receives a generated UUID filename.

Example:

```text
Original filename:
invoice-final.txt

Stored filename:
8ca31a28-3f74-4a43-9e35-b77aeae660cf.txt
```

### Why Files Are Stored Outside MySQL

This design:

- Keeps the relational database smaller
- Avoids storing large binary data in ordinary database rows
- Makes physical file access easier
- Allows the storage root to change during deployment
- Separates metadata persistence from file storage

---

## Evidence Validation Rules

### Supported Extensions

```text
.pdf
.txt
.csv
.json
```

Unsupported files such as these are rejected:

```text
.exe
.zip
.docx
.jpg
.php
.html
```

### Maximum File Size

Maximum individual evidence size:

```text
10 MB
```

Maximum multipart request size:

```text
11 MB
```

The larger request limit allows room for multipart headers, boundaries and the optional description.

### Original Filename

The original filename:

- Is required
- Cannot be blank
- Cannot exceed 255 characters
- Has path information removed
- Has control characters removed
- Is never used as the physical stored filename

### Description

The evidence description:

- Is optional
- Is trimmed before storage
- Cannot exceed 500 characters

### MIME-Type Validation

The uploaded content type must be compatible with the file extension.

For example, a `.pdf` file sent as `text/plain` is rejected.

MIME-type validation is an initial safeguard. Actual parsing and file-content validation will be added during evidence text extraction.

### Empty File Validation

Zero-byte files are rejected with:

```text
Evidence file cannot be empty
```

### Case Ownership

Evidence can be uploaded only when the selected case belongs to the authenticated investigator.

A nonexistent or unowned case returns:

```text
404 Not Found
```

---

## Evidence Security

### UUID-Based Stored Filenames

Generated filenames prevent:

- Filename collisions
- Accidental overwriting
- Direct use of unsafe user-controlled filenames
- Disclosure of meaningful original filenames in storage paths

### Path-Traversal Protection

Every generated or resolved storage path is:

1. Converted to a normalised Java `Path`
2. Resolved relative to the configured storage root
3. Checked to ensure it still begins inside the approved storage directory

This prevents paths such as:

```text
../../outside-file.txt
```

from accessing files outside `evidence-storage`.

### Ownership-Aware Queries

Evidence retrieval checks:

```text
Evidence ID matches
AND
Investigation case owner matches authenticated user
```

An evidence file belonging to another investigator is returned as:

```text
404 Not Found
```

This avoids revealing whether another investigator’s evidence ID exists.

### Internal Paths Are Hidden

API responses never contain:

```text
stored filename
relative storage path
absolute server path
developer-machine path
```

### Git Exclusion

The following directory must remain excluded through `.gitignore`:

```gitignore
evidence-storage/
```

Uploaded evidence must never be committed to GitHub.

---

## Current Security Rules

Public endpoints:

```text
GET  /api/system/status
POST /api/auth/register
POST /api/auth/login
```

Protected endpoints:

```text
GET    /api/auth/me

POST   /api/cases
GET    /api/cases
GET    /api/cases/{caseId}
PUT    /api/cases/{caseId}
PATCH  /api/cases/{caseId}/status
DELETE /api/cases/{caseId}

POST   /api/cases/{caseId}/evidence
GET    /api/cases/{caseId}/evidence
GET    /api/evidence/{evidenceId}
GET    /api/evidence/{evidenceId}/download
DELETE /api/evidence/{evidenceId}
```

The backend uses stateless authentication and does not store HTTP login sessions.

---

## Ownership Protection

### Investigation Cases

Every case is associated with the authenticated user.

The backend does not accept an owner ID from the client.

Ownership flow:

```text
Validated JWT subject
→ Authenticated email
→ Database user
→ Investigation case owner
```

### Evidence Files

Every evidence file belongs to an investigation case.

Evidence ownership is derived through:

```text
Evidence
→ Investigation case
→ Case owner
→ Authenticated user
```

The user cannot provide or change the evidence owner directly.

---

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
- Must use a supported value

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
- Must use a supported value

### Case Pagination

```text
page must be 0 or greater
size must be between 1 and 100
```

### Evidence Upload

File:

- Required
- Cannot be empty
- Maximum 10 MB
- Must use PDF, TXT, CSV or JSON
- Filename cannot exceed 255 characters
- MIME type must match the extension

Description:

- Optional
- Maximum 500 characters

### Evidence Pagination

```text
page must be 0 or greater
size must be between 1 and 100
```

---

## Error Handling

The API currently handles:

- Invalid request fields with `400 Bad Request`
- Malformed JSON with `400 Bad Request`
- Unsupported enum values with `400 Bad Request`
- Invalid pagination values with `400 Bad Request`
- Unsupported sorting fields with `400 Bad Request`
- Invalid request-parameter types with `400 Bad Request`
- Invalid evidence files with `400 Bad Request`
- Empty evidence files with `400 Bad Request`
- Unsupported evidence extensions with `400 Bad Request`
- File and MIME-type mismatches with `400 Bad Request`
- Duplicate email registrations with `409 Conflict`
- Invalid login credentials with `401 Unauthorized`
- Missing or invalid JWT tokens with `401 Unauthorized`
- Disabled user accounts with `403 Forbidden`
- Missing users with `404 Not Found`
- Missing or unowned investigation cases with `404 Not Found`
- Missing or unowned evidence files with `404 Not Found`
- Files exceeding the servlet upload limit with `413 Content Too Large`
- Database constraint conflicts with `409 Conflict`
- Evidence-storage failures with `500 Internal Server Error`
- Unexpected server errors with `500 Internal Server Error`

Example error response:

```json
{
  "success": false,
  "status": 400,
  "error": "Bad Request",
  "message": "Unsupported evidence file type. Supported types are PDF, TXT, CSV and JSON",
  "path": "/api/cases/5/evidence",
  "fieldErrors": {},
  "timestamp": "2026-07-16T12:00:00Z"
}
```

---

## Current Entity Relationships

```text
User
  │
  │ one user owns many cases
  ▼
InvestigationCase
  │
  │ one case contains many evidence files
  ▼
Evidence
```

Database foreign keys:

```text
investigation_cases.owner_id
→ users.id

evidence_files.case_id
→ investigation_cases.id
```

The application currently uses unidirectional entity relationships:

```text
InvestigationCase → User
Evidence → InvestigationCase
```

Cases and evidence are queried through their repositories instead of loading large child collections through parent entities.

---

## Current Evidence Status Values

```text
UPLOADED
```

The file has been stored successfully but has not yet been processed.

```text
PROCESSING
```

Text extraction or another processing operation is currently running.

```text
PROCESSED
```

The evidence processing operation completed successfully.

```text
FAILED
```

Evidence processing failed.

Day 4 uploads currently receive:

```text
UPLOADED
```

Text extraction and status transitions will be implemented in the upcoming development days.

---

## Planned Features

### Day 5

- SHA-256 evidence hashing
- Evidence-integrity verification
- Duplicate-file detection
- Hash metadata persistence
- Evidence audit information

### Day 6

- PDF text extraction
- TXT text extraction
- CSV parsing
- JSON parsing
- Strategy-based extractor architecture
- Evidence processing-status updates

### Day 7

- Spring AI integration
- Groq configuration
- `ChatClient`
- Prompt templates
- Structured AI responses
- AI error and timeout handling

### Day 8

- Evidence summarisation
- Risk-level classification
- Suspicious-finding detection
- Recommended investigative actions
- AI analysis persistence

### Day 9

- Person and organisation extraction
- Email, phone, URL and IP extraction
- Date and amount extraction
- Timeline-event generation
- Hybrid deterministic and AI extraction

### Day 10

- Investigator notes
- Case-level notes management
- Aggregated investigation reports
- Report-generation API

### Later Development

- Dashboard and analytics
- React frontend
- Automated testing
- Swagger/OpenAPI documentation
- Docker deployment
- Cloud deployment
- Resume and demonstration preparation

---

## Git Workflow

Development is committed incrementally using GitHub Desktop.

Daily workflow:

```text
Complete and test a feature
→ Review changed files
→ Confirm no secrets or evidence files are included
→ Commit to main
→ Push origin
```

Secrets, generated build output, uploaded evidence, logs and generated reports are excluded through `.gitignore`.

---

## Disclaimer

AI-generated findings are investigative aids and must be independently verified before being used for legal, disciplinary or security decisions.

TraceLens AI is being developed as an educational and portfolio project. Uploaded test evidence must not contain real confidential, personal or legally restricted information.