# TraceLens AI

TraceLens AI is an AI-powered digital evidence analysis and investigation platform built with Spring Boot.

It helps authorised investigators:

- Register and authenticate securely
- Create and manage investigation cases
- Upload and organise digital evidence
- Preserve evidence metadata
- Generate SHA-256 evidence fingerprints
- Detect duplicate evidence
- Verify whether stored evidence has been altered
- Search and filter investigation cases
- Download and securely delete stored evidence

Evidence text extraction and AI-powered analysis will be added in the upcoming development phases.

---

## Current Development Status

### Day 1 — Backend Foundation

- Initialised the Spring Boot Maven project
- Configured MySQL, Spring Data JPA and Hibernate
- Added environment-based database credentials
- Implemented a reusable API response structure
- Created a system-status REST endpoint
- Added live database-connectivity verification
- Added global REST exception handling
- Connected and published the project to GitHub

### Day 2 — Authentication and Security

- Created the `User` JPA entity and `Role` enum
- Added a Spring Data JPA user repository
- Implemented secure user registration
- Added field-level request validation
- Normalised email addresses and user names
- Implemented BCrypt password hashing
- Assigned the default `INVESTIGATOR` role
- Added duplicate-email protection
- Implemented email-and-password login
- Added JWT access-token generation and validation
- Configured stateless Spring Security
- Added a database-backed `UserDetailsService`
- Added a protected current-user endpoint
- Added handling for invalid credentials and disabled accounts

### Day 3 — Investigation Case Management

- Created the `InvestigationCase` JPA entity
- Added case status and priority enums
- Connected investigation cases to their user owners
- Added unique human-readable case numbers
- Added case lifecycle timestamps
- Added database indexes for common case queries
- Implemented authenticated case creation
- Implemented case retrieval, update, status update and deletion
- Enforced JWT-based case ownership
- Added case request validation and normalisation
- Added keyword search across case number, title and description
- Added status and priority filters
- Added pagination and sorting
- Restricted sorting to approved fields
- Added reusable paginated API responses
- Added validation for invalid pagination and sorting values
- Added safe handling for unsupported enum values

### Day 4 — Digital Evidence Management

- Created the `Evidence` JPA entity
- Added supported evidence file-type and processing-status enums
- Connected evidence files to investigation cases
- Added evidence metadata persistence
- Implemented secure multipart file upload
- Added PDF, TXT, CSV and JSON upload support
- Added configurable upload-size limits
- Added extension and MIME-type validation
- Added empty-file and unsafe-filename validation
- Generated UUID-based physical filenames
- Stored evidence inside case-specific directories
- Prevented path-traversal access
- Stored portable relative paths instead of machine-specific paths
- Removed uploaded files when database persistence failed
- Added paginated evidence listing
- Added evidence metadata retrieval
- Added authenticated physical-file download
- Preserved original filenames during downloads
- Enforced evidence ownership through case ownership
- Added transactional evidence deletion
- Removed physical files after successful database commits
- Removed empty case evidence directories
- Added evidence-specific exception handling

### Day 5 — Evidence Integrity and Duplicate Detection

- Added SHA-256 hashing for uploaded evidence
- Calculated hashes from the bytes stored on disk
- Stored SHA-256 values as 64-character lowercase hexadecimal strings
- Verified that uploaded and stored file sizes match
- Added evidence-integrity status tracking
- Added last integrity-verification timestamps
- Added database indexes for SHA-256 and integrity status
- Added per-case evidence-hash uniqueness
- Prevented the same evidence from being uploaded twice to one case
- Removed rejected duplicate candidate files from physical storage
- Allowed identical evidence to be associated with different cases
- Added SHA-256 metadata to evidence API responses
- Added authenticated integrity-verification API
- Recalculated hashes from current stored evidence bytes
- Compared current hashes with immutable original upload hashes
- Recorded `VERIFIED` when evidence remained unchanged
- Recorded `MISMATCH` when stored evidence was modified
- Preserved original hashes as integrity baselines
- Tested deliberate evidence modification and restoration
- Enforced ownership during integrity verification

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

### Evidence and File Management

- Spring multipart upload
- Java NIO filesystem APIs
- UUID-based stored filenames
- Case-specific evidence directories
- Secure relative-path handling
- SHA-256 hashing using Java `MessageDigest`
- Hexadecimal hash formatting using Java `HexFormat`

### Development Tools

- Eclipse / Spring Tools
- MySQL Workbench
- Windows PowerShell
- Git
- GitHub
- GitHub Desktop

---

## Project Structure

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
│   │   ├── EvidenceIntegrityResponse.java
│   │   └── EvidenceResponse.java
│   ├── entity
│   │   ├── Evidence.java
│   │   ├── EvidenceFileType.java
│   │   ├── EvidenceIntegrityStatus.java
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
│   ├── DuplicateEvidenceException.java
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

The application requires:

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

Never commit real passwords, JWT secrets, API keys, access tokens or uploaded evidence.

### Eclipse Configuration

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

`EVIDENCE_STORAGE_ROOT` is optional because the default value is:

```text
evidence-storage
```

---

## Database Setup

Create the database:

```sql
CREATE DATABASE IF NOT EXISTS tracelens_db
CHARACTER SET utf8mb4
COLLATE utf8mb4_unicode_ci;
```

Select it:

```sql
USE tracelens_db;
```

Hibernate creates and updates the tables from the JPA entities.

Current tables:

```text
users
investigation_cases
evidence_files
```

### `users`

Stores:

- User ID
- Full name
- Email
- BCrypt password hash
- Role
- Active status
- Creation timestamp
- Update timestamp

### `investigation_cases`

Stores:

- Internal case ID
- Human-readable case number
- Title
- Description
- Status
- Priority
- Owner ID
- Creation timestamp
- Update timestamp

### `evidence_files`

Stores:

- Evidence ID
- Original filename
- Generated stored filename
- Relative storage path
- File type
- MIME content type
- File size
- Description
- Processing status
- SHA-256 baseline hash
- Integrity status
- Last integrity-verification timestamp
- Investigation case ID
- Upload timestamp
- Update timestamp

Physical evidence bytes are stored on the filesystem, not inside MySQL.

---

## Entity Relationships

```text
User
  │
  │ one user owns many investigation cases
  ▼
InvestigationCase
  │
  │ one case contains many evidence files
  ▼
Evidence
```

Foreign keys:

```text
investigation_cases.owner_id
→ users.id

evidence_files.case_id
→ investigation_cases.id
```

The project currently uses unidirectional entity relationships:

```text
InvestigationCase → User
Evidence → InvestigationCase
```

Child records are queried through repositories instead of loading large collections from parent entities.

---

## Application Configuration

Database and JWT configuration:

```properties
spring.datasource.username=${DB_USERNAME:root}
spring.datasource.password=${DB_PASSWORD}

app.security.jwt.secret=${JWT_SECRET}
app.security.jwt.issuer=tracelens-backend
app.security.jwt.access-token-expiration-minutes=60
```

Evidence upload configuration:

```properties
spring.servlet.multipart.enabled=true
spring.servlet.multipart.max-file-size=10MB
spring.servlet.multipart.max-request-size=11MB

app.evidence.storage-root=${EVIDENCE_STORAGE_ROOT:evidence-storage}
app.evidence.max-file-size-bytes=10485760
```

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

Successful startup should include:

```text
HikariPool-1 - Start completed
Evidence storage initialized at ...
Tomcat started on port 8080
Started TracelensBackendApplication
```

---

## Authentication Flow

```text
Register user
→ Hash password with BCrypt
→ Save user in MySQL
→ Log in with email and password
→ Generate signed JWT access token
→ Send JWT in Authorization header
→ Access protected APIs
```

Protected requests require:

```http
Authorization: Bearer <access-token>
```

Current JWT expiration:

```text
60 minutes
```

---

# API Endpoints

## System API

### System Status

```http
GET /api/system/status
```

Public endpoint used to verify the application and database connection.

---

## Authentication APIs

### Register

```http
POST /api/auth/register
```

Example:

```json
{
  "fullName": "Basit Mahmood",
  "email": "basit.test@example.com",
  "password": "TraceLens@123"
}
```

Successful result:

```text
201 Created
```

### Login

```http
POST /api/auth/login
```

Example:

```json
{
  "email": "basit.test@example.com",
  "password": "TraceLens@123"
}
```

A successful response contains:

- JWT access token
- Token type
- Expiration time
- Safe user details

### Current User

```http
GET /api/auth/me
```

Returns the authenticated user’s safe profile information.

---

## Investigation Case APIs

All case APIs require authentication.

### Create Case

```http
POST /api/cases
```

Example:

```json
{
  "title": "Suspicious Invoice Investigation",
  "description": "Investigate possible invoice manipulation and an unusual payment request.",
  "priority": "HIGH"
}
```

The backend automatically:

- Generates a unique case number
- Assigns the authenticated user as owner
- Sets status to `OPEN`
- Uses `MEDIUM` when priority is omitted
- Adds timestamps

### List Cases

```http
GET /api/cases
```

Default parameters:

```text
page=0
size=10
sortBy=createdAt
sortDirection=desc
```

### Retrieve Case

```http
GET /api/cases/{caseId}
```

### Update Case

```http
PUT /api/cases/{caseId}
```

### Update Case Status

```http
PATCH /api/cases/{caseId}/status
```

Example:

```json
{
  "status": "IN_PROGRESS"
}
```

Supported statuses:

```text
OPEN
IN_PROGRESS
COMPLETED
ARCHIVED
```

### Delete Case

```http
DELETE /api/cases/{caseId}
```

### Search and Filter Cases

```http
GET /api/cases?keyword=invoice
```

```http
GET /api/cases?status=IN_PROGRESS
```

```http
GET /api/cases?priority=HIGH
```

Combined example:

```http
GET /api/cases?keyword=invoice&status=IN_PROGRESS&priority=HIGH&page=0&size=10&sortBy=createdAt&sortDirection=desc
```

---

## Evidence APIs

All evidence APIs require authentication.

### Upload Evidence

```http
POST /api/cases/{caseId}/evidence
```

Request type:

```text
multipart/form-data
```

Parts:

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

Example:

```powershell
curl.exe -i -X POST `
"http://localhost:8080/api/cases/$caseId/evidence" `
-H "Authorization: Bearer $token" `
-F "file=@$sampleFile;type=text/plain" `
-F "description=Invoice communication evidence"
```

The backend:

1. Verifies case ownership
2. Validates file extension, MIME type and size
3. Generates a UUID filename
4. Stores the file in a case-specific directory
5. Verifies the stored byte count
6. Calculates SHA-256 from stored bytes
7. Checks for duplicates within the selected case
8. Stores evidence metadata and integrity information

Example response:

```json
{
  "success": true,
  "message": "Evidence uploaded successfully",
  "data": {
    "id": 2,
    "caseId": 5,
    "caseNumber": "TL-20260715-YV3SBJQ4",
    "originalFileName": "invoice-evidence.txt",
    "fileType": "TXT",
    "contentType": "text/plain",
    "fileSizeBytes": 213,
    "description": "Invoice communication evidence",
    "status": "UPLOADED",
    "sha256Hash": "0bc385d9ab9817a5b281f183d31efdb90984abbfd8e75d94a5ae8c08f1b4aee8",
    "integrityStatus": "VERIFIED",
    "lastIntegrityVerifiedAt": "2026-07-17T12:03:20Z",
    "uploadedAt": "2026-07-17T12:03:20Z",
    "updatedAt": "2026-07-17T12:03:20Z"
  }
}
```

Internal storage paths are never exposed.

### List Case Evidence

```http
GET /api/cases/{caseId}/evidence
```

Pagination:

```text
page=0
size=10
```

Example:

```http
GET /api/cases/5/evidence?page=0&size=10
```

### Retrieve Evidence Metadata

```http
GET /api/evidence/{evidenceId}
```

Returns safe metadata, SHA-256 information and integrity status.

### Download Evidence

```http
GET /api/evidence/{evidenceId}/download
```

The endpoint:

- Verifies authentication
- Verifies ownership
- Loads the physical file
- Preserves the original uploaded filename
- Returns the file as an attachment

### Delete Evidence

```http
DELETE /api/evidence/{evidenceId}
```

Workflow:

```text
Verify ownership
→ Delete database metadata transactionally
→ Commit database transaction
→ Delete physical file
→ Remove empty case directory
```

### Verify Evidence Integrity

```http
POST /api/evidence/{evidenceId}/verify-integrity
```

The endpoint:

1. Verifies JWT ownership
2. Loads the current stored file
3. Recalculates SHA-256
4. Compares it with the original baseline hash
5. Records `VERIFIED` or `MISMATCH`
6. Updates the verification timestamp
7. Returns expected and current hashes

Example successful response:

```json
{
  "success": true,
  "message": "Evidence integrity verified successfully",
  "data": {
    "evidenceId": 2,
    "caseId": 5,
    "caseNumber": "TL-20260715-YV3SBJQ4",
    "originalFileName": "invoice-evidence.txt",
    "expectedSha256Hash": "0bc385d9ab9817a5b281f183d31efdb90984abbfd8e75d94a5ae8c08f1b4aee8",
    "currentSha256Hash": "0bc385d9ab9817a5b281f183d31efdb90984abbfd8e75d94a5ae8c08f1b4aee8",
    "matches": true,
    "integrityStatus": "VERIFIED",
    "verifiedAt": "2026-07-17T13:00:00Z"
  }
}
```

Example mismatch response:

```json
{
  "success": true,
  "message": "Evidence integrity mismatch detected",
  "data": {
    "expectedSha256Hash": "original-hash",
    "currentSha256Hash": "different-current-hash",
    "matches": false,
    "integrityStatus": "MISMATCH"
  }
}
```

---

## SHA-256 Evidence Integrity

SHA-256 produces a 256-bit digest represented as:

```text
64 lowercase hexadecimal characters
```

Example:

```text
0bc385d9ab9817a5b281f183d31efdb90984abbfd8e75d94a5ae8c08f1b4aee8
```

The hash is generated from file bytes, not from:

- Filename
- Description
- Case number
- Upload time
- Investigator name

Therefore:

```text
Same filename + same bytes
→ Same SHA-256

Same filename + changed bytes
→ Different SHA-256

Different filename + same bytes
→ Same SHA-256
```

SHA-256 is not encryption. It cannot be decrypted back into the original file.

---

## Duplicate Evidence Detection

Duplicate detection is applied within one investigation case.

```text
Same case + same SHA-256
→ Rejected with 409 Conflict
```

```text
Different case + same SHA-256
→ Allowed
```

This allows the same document to be legitimately associated with separate investigations.

Application-level duplicate detection gives a readable response.

The database also contains a combined unique constraint on:

```text
case_id
sha256_hash
```

This acts as a final safeguard against concurrent duplicate uploads.

---

## Integrity Status Values

### `NOT_VERIFIED`

No completed integrity comparison is currently available.

### `VERIFIED`

The current stored bytes generate the same SHA-256 value as the original upload baseline.

```text
expected hash == current hash
```

### `MISMATCH`

The current stored bytes generate a different SHA-256 value.

```text
expected hash != current hash
```

Possible causes include:

- File modification
- File corruption
- File replacement
- Truncation
- External storage changes

The system never replaces the original hash with the altered hash.

---

## Evidence Processing Status Values

### `UPLOADED`

The file is stored but text extraction has not started.

### `PROCESSING`

Text extraction or another processing task is running.

### `PROCESSED`

Processing completed successfully.

### `FAILED`

Processing failed.

Current uploads initially receive:

```text
UPLOADED
```

Text extraction and processing-state transitions will be added next.

---

## Evidence Storage Structure

Default local structure:

```text
evidence-storage
├── case-5
│   ├── 8ca31a28-3f74-4a43-9e35-b77aeae660cf.txt
│   └── f240eb74-c714-4781-914e-e4e49007188c.pdf
└── case-7
    └── cdafdd8a-9ab8-4209-aad9-bbc0fb4d71a7.json
```

Example mapping:

```text
Original filename:
invoice-final.txt

Stored filename:
8ca31a28-3f74-4a43-9e35-b77aeae660cf.txt
```

The original filename is retained as metadata but is not used as the physical filename.

---

## Evidence Validation

### Supported Extensions

```text
.pdf
.txt
.csv
.json
```

Unsupported examples:

```text
.exe
.zip
.docx
.jpg
.php
.html
```

### Size Limits

```text
Maximum file size: 10 MB
Maximum multipart request size: 11 MB
```

### Original Filename

The filename:

- Is required
- Cannot be blank
- Cannot exceed 255 characters
- Has path information removed
- Has control characters removed
- Is never used directly for physical storage

### Description

- Optional
- Trimmed before storage
- Maximum 500 characters

### MIME Type

The MIME content type must be compatible with the file extension.

### Empty Files

Zero-byte evidence files are rejected.

---

## Security Protections

### JWT Authentication

All case and evidence APIs require a valid JWT.

### Ownership Enforcement

Evidence ownership is derived through:

```text
Evidence
→ Investigation case
→ Case owner
→ Authenticated JWT user
```

The client cannot provide or change the owner directly.

### Path-Traversal Protection

Storage paths are:

1. Resolved relative to the configured storage root
2. Normalised
3. Verified to remain inside the approved root

Unsafe paths such as:

```text
../../outside-file.txt
```

cannot access files outside evidence storage.

### UUID Stored Filenames

UUID filenames prevent:

- Name collisions
- Accidental overwriting
- Use of unsafe uploaded filenames
- Exposure of descriptive filenames in storage

### Hidden Internal Paths

API responses do not expose:

```text
stored filename
relative storage path
absolute server path
developer machine path
```

### Git Exclusion

The storage directory must remain ignored:

```gitignore
evidence-storage/
```

Uploaded evidence must never be pushed to GitHub.

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
POST   /api/evidence/{evidenceId}/verify-integrity
DELETE /api/evidence/{evidenceId}
```

---

## Error Handling

The API currently handles:

- Invalid request fields with `400 Bad Request`
- Malformed JSON with `400 Bad Request`
- Unsupported enum values with `400 Bad Request`
- Invalid pagination with `400 Bad Request`
- Unsupported sorting with `400 Bad Request`
- Invalid request-parameter types with `400 Bad Request`
- Invalid evidence files with `400 Bad Request`
- Empty evidence files with `400 Bad Request`
- Unsupported evidence extensions with `400 Bad Request`
- File and MIME-type mismatches with `400 Bad Request`
- Missing original SHA-256 baseline with `400 Bad Request`
- Invalid login credentials with `401 Unauthorized`
- Missing or invalid JWT tokens with `401 Unauthorized`
- Disabled accounts with `403 Forbidden`
- Missing users with `404 Not Found`
- Missing or unowned cases with `404 Not Found`
- Missing or unowned evidence with `404 Not Found`
- Duplicate email registrations with `409 Conflict`
- Duplicate evidence uploads with `409 Conflict`
- Database constraint conflicts with `409 Conflict`
- Files exceeding limits with `413 Content Too Large`
- Evidence-storage failures with `500 Internal Server Error`
- Unexpected server failures with `500 Internal Server Error`

---

## Planned Features

### Day 6 — Evidence Text Extraction

- TXT text extraction
- PDF text extraction using Apache PDFBox
- CSV parsing
- JSON parsing
- File-content validation
- Extractor interface and strategy pattern
- Extracted-text persistence
- Processing-status transitions
- Extraction failure handling

### Day 7 — Spring AI Integration

- Spring AI dependencies
- Groq API configuration
- `ChatClient`
- Prompt templates
- Structured AI output
- API timeout and failure handling

### Day 8 — AI Evidence Analysis

- Evidence summarisation
- Risk classification
- Suspicious findings
- Recommended investigative actions
- Persistent AI analysis results

### Day 9 — Entity and Timeline Extraction

- People
- Organisations
- Email addresses
- Phone numbers
- URLs
- IP addresses
- Dates
- Money amounts
- Timeline events

### Day 10 — Notes and Reports

- Investigator notes
- Case-level notes CRUD
- Aggregated case reports
- Report-generation API

### Later Development

- Dashboard analytics
- React frontend
- Automated tests
- Swagger/OpenAPI
- Docker
- Cloud deployment
- Resume and demonstration preparation

---

## Git Workflow

Development is committed incrementally.

```text
Complete and test a checkpoint
→ Review changed files
→ Confirm no secrets or uploaded evidence are included
→ Commit to main
→ Push origin
```

Never commit:

```text
DB_PASSWORD
JWT_SECRET
JWT access tokens
API keys
evidence-storage/
target/
temporary integrity backup files
```

---

## Disclaimer

TraceLens AI is an educational and portfolio project.

AI-generated findings must be treated as investigative aids and independently verified before use in legal, disciplinary or security decisions.

Uploaded test evidence must not contain real confidential, personal, privileged or legally restricted information.