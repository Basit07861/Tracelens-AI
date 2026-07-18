# TraceLens AI

TraceLens AI is an AI-powered digital evidence analysis and investigation platform built using Spring Boot.

The application enables authorised investigators to:

- Register and authenticate securely
- Create and manage investigation cases
- Upload and organise digital evidence
- Generate SHA-256 evidence fingerprints
- Detect duplicate evidence
- Verify whether stored evidence has been altered
- Extract readable content from TXT, CSV, JSON and PDF files
- Preserve evidence-processing metadata
- Search, filter and manage investigation cases
- Download and securely delete evidence files

The extracted evidence text will be used for AI-powered investigation analysis in the next development phase.

---

# Current Development Status

## Day 1 — Backend Foundation

- Initialised the Spring Boot Maven project
- Configured MySQL, Spring Data JPA and Hibernate
- Added environment-based database credentials
- Implemented a reusable API response structure
- Created a system-status REST endpoint
- Added live database-connectivity verification
- Added global REST exception handling
- Connected and published the project to GitHub

## Day 2 — Authentication and Security

- Created the `User` JPA entity and `Role` enum
- Added a Spring Data JPA user repository
- Implemented secure user registration
- Added request validation
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

## Day 3 — Investigation Case Management

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
- Added safe handling for invalid pagination, sorting and enum values

## Day 4 — Digital Evidence Management

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
- Stored portable relative paths
- Removed physical files when database persistence failed
- Added paginated evidence listing
- Added evidence metadata retrieval
- Added authenticated file download
- Preserved original filenames during downloads
- Enforced evidence ownership through case ownership
- Added transactional evidence deletion
- Removed physical files only after successful database commits
- Removed empty case evidence directories
- Added evidence-specific exception handling

## Day 5 — Evidence Integrity and Duplicate Detection

- Added SHA-256 hashing for uploaded evidence
- Calculated hashes from the bytes stored on disk
- Stored SHA-256 values as 64-character lowercase hexadecimal strings
- Verified uploaded and persisted file sizes
- Added evidence-integrity status tracking
- Added last integrity-verification timestamps
- Added database indexes for SHA-256 and integrity status
- Added per-case evidence-hash uniqueness
- Prevented duplicate evidence inside the same case
- Removed rejected duplicate candidate files
- Allowed identical evidence across separate cases
- Added SHA-256 metadata to evidence API responses
- Added an authenticated integrity-verification endpoint
- Recalculated hashes from current stored file bytes
- Compared current hashes with immutable upload hashes
- Recorded `VERIFIED` when evidence remained unchanged
- Recorded `MISMATCH` when evidence was modified
- Tested deliberate modification and restoration
- Enforced ownership during integrity verification

## Day 6 — Evidence Text Extraction

- Added extracted-text persistence to evidence records
- Added extracted character-count metadata
- Added safe extraction-error storage
- Added processing-completion timestamps
- Added configurable extraction safety limits
- Implemented evidence processing status transitions
- Added a common `EvidenceTextExtractor` interface
- Implemented the Strategy design pattern
- Added an extractor registry
- Added strict UTF-8 text decoding
- Added shared text-normalisation utilities
- Implemented TXT evidence extraction
- Implemented CSV parsing with quoted-field support
- Implemented JSON flattening using Jackson
- Added Apache PDFBox
- Implemented PDF text extraction
- Extracted PDF content page by page
- Added page headings to PDF output
- Added PDF page limits
- Rejected encrypted or restricted PDFs
- Handled malformed and image-only PDFs safely
- Verified evidence integrity before extraction
- Added transactional `PROCESSING`, `PROCESSED` and `FAILED` state handling
- Added evidence text-extraction API
- Added extracted-text retrieval API
- Returned `422` for unprocessable evidence content
- Preserved failure metadata in separate transactions
- Kept original files and SHA-256 values unchanged
- Tested TXT, CSV, JSON and PDF processing

---

# Technology Stack

## Backend

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

## Evidence Processing

- Spring multipart file upload
- Java NIO filesystem APIs
- Java `MessageDigest`
- SHA-256 evidence hashing
- UUID-based stored filenames
- Case-specific evidence directories
- Strict UTF-8 text decoding
- Jackson JSON processing
- Apache PDFBox 3.0.8
- Strategy design pattern
- Transactional processing-state persistence

## Development Tools

- Eclipse / Spring Tools
- MySQL Workbench
- Windows PowerShell
- Git
- GitHub
- GitHub Desktop

---

# Project Structure

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
│   │   ├── EvidenceExtractionProperties.java
│   │   └── EvidenceProperties.java
│   ├── controller
│   │   └── EvidenceController.java
│   ├── dto
│   │   ├── EvidenceExtractionResponse.java
│   │   ├── EvidenceIntegrityResponse.java
│   │   └── EvidenceResponse.java
│   ├── entity
│   │   ├── Evidence.java
│   │   ├── EvidenceFileType.java
│   │   ├── EvidenceIntegrityStatus.java
│   │   └── EvidenceStatus.java
│   ├── extraction
│   │   ├── CsvEvidenceTextExtractor.java
│   │   ├── EvidenceTextExtractor.java
│   │   ├── EvidenceTextExtractorRegistry.java
│   │   ├── JsonEvidenceTextExtractor.java
│   │   ├── PdfEvidenceTextExtractor.java
│   │   ├── TextExtractionSupport.java
│   │   └── TxtEvidenceTextExtractor.java
│   ├── repository
│   │   └── EvidenceRepository.java
│   ├── service
│   │   ├── EvidenceFileValidator.java
│   │   ├── EvidenceProcessingService.java
│   │   ├── EvidenceProcessingStateService.java
│   │   ├── EvidenceProcessingTarget.java
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
│   ├── EvidenceTextExtractionException.java
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

# Environment Variables

Required variables:

```text
DB_PASSWORD=your_mysql_password
JWT_SECRET=your_base64_encoded_jwt_secret
```

Optional variables:

```text
DB_USERNAME=root
DB_URL=jdbc:mysql://localhost:3306/tracelens_db
EVIDENCE_STORAGE_ROOT=evidence-storage
```

Never commit:

```text
Database passwords
JWT secrets
JWT access tokens
AI API keys
Uploaded evidence
Temporary backup files
```

## Eclipse Configuration

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

The evidence storage root is optional because the application defaults to:

```text
evidence-storage
```

---

# Application Configuration

```properties
spring.application.name=tracelens-backend

server.port=8080

spring.datasource.url=${DB_URL:jdbc:mysql://localhost:3306/tracelens_db?useSSL=false&allowPublicKeyRetrieval=true&serverTimezone=Asia/Kolkata}
spring.datasource.username=${DB_USERNAME:root}
spring.datasource.password=${DB_PASSWORD}

spring.jpa.hibernate.ddl-auto=update
spring.jpa.show-sql=true
spring.jpa.properties.hibernate.format_sql=true
spring.jpa.open-in-view=false

app.security.jwt.secret=${JWT_SECRET}
app.security.jwt.issuer=tracelens-backend
app.security.jwt.access-token-expiration-minutes=60

spring.servlet.multipart.enabled=true
spring.servlet.multipart.max-file-size=10MB
spring.servlet.multipart.max-request-size=11MB

app.evidence.storage-root=${EVIDENCE_STORAGE_ROOT:evidence-storage}
app.evidence.max-file-size-bytes=10485760

app.evidence.extraction.max-characters=100000
app.evidence.extraction.max-pdf-pages=100
app.evidence.extraction.max-csv-rows=5000
app.evidence.extraction.max-json-depth=50
app.evidence.extraction.max-error-message-length=1000

server.error.include-message=always
server.error.include-binding-errors=always
```

---

# Database Setup

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

Current tables:

```text
users
investigation_cases
evidence_files
```

## `users`

Stores:

- User ID
- Full name
- Email address
- BCrypt password hash
- Role
- Active status
- Creation timestamp
- Update timestamp

## `investigation_cases`

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

## `evidence_files`

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
- Extracted text
- Extracted character count
- Safe extraction-error message
- Processing-completion timestamp
- Investigation case ID
- Upload timestamp
- Update timestamp

The physical evidence bytes are stored on the filesystem, not inside MySQL.

---

# Entity Relationships

```text
User
  │
  │ one user owns many cases
  ▼
InvestigationCase
  │
  │ one case contains many evidence records
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

The project uses unidirectional JPA relationships:

```text
InvestigationCase → User
Evidence → InvestigationCase
```

Child records are queried through repositories instead of storing large collections inside parent entities.

---

# Running the Application

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

# Authentication Flow

```text
Register user
→ Hash password with BCrypt
→ Store user in MySQL
→ Log in using email and password
→ Generate signed JWT
→ Send JWT in Authorization header
→ Access protected APIs
```

Protected requests require:

```http
Authorization: Bearer <access-token>
```

Current access-token expiration:

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

Public endpoint for checking application and database availability.

---

# Authentication APIs

## Register

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

## Login

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

## Current User

```http
GET /api/auth/me
```

Returns the authenticated user’s safe profile details.

---

# Investigation Case APIs

All case APIs require authentication.

## Create Case

```http
POST /api/cases
```

Example:

```json
{
  "title": "Suspicious Invoice Investigation",
  "description": "Investigate possible invoice manipulation.",
  "priority": "HIGH"
}
```

The backend automatically:

- Generates a unique case number
- Assigns the authenticated user as owner
- Sets the initial status to `OPEN`
- Uses `MEDIUM` when priority is omitted
- Adds lifecycle timestamps

## List and Search Cases

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

Search examples:

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

## Retrieve Case

```http
GET /api/cases/{caseId}
```

## Update Case

```http
PUT /api/cases/{caseId}
```

## Update Case Status

```http
PATCH /api/cases/{caseId}/status
```

Supported values:

```text
OPEN
IN_PROGRESS
COMPLETED
ARCHIVED
```

## Delete Case

```http
DELETE /api/cases/{caseId}
```

---

# Evidence APIs

All evidence APIs require authentication.

## Upload Evidence

```http
POST /api/cases/{caseId}/evidence
```

Content type:

```text
multipart/form-data
```

Request parts:

```text
file         Required
description  Optional
```

Supported formats:

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

Upload workflow:

```text
Verify case ownership
→ Validate extension, MIME type and size
→ Generate UUID filename
→ Store the physical file
→ Verify persisted byte count
→ Calculate SHA-256
→ Check for duplicates inside the case
→ Save metadata and integrity state
```

New uploads receive:

```text
status = UPLOADED
integrityStatus = VERIFIED
```

## List Case Evidence

```http
GET /api/cases/{caseId}/evidence
```

Example:

```http
GET /api/cases/5/evidence?page=0&size=10
```

## Retrieve Evidence Metadata

```http
GET /api/evidence/{evidenceId}
```

Returns safe evidence metadata without exposing internal filesystem paths.

## Download Evidence

```http
GET /api/evidence/{evidenceId}/download
```

The endpoint:

- Verifies authentication
- Verifies evidence ownership
- Loads the physical file
- Preserves the original filename
- Returns the file as an attachment

## Delete Evidence

```http
DELETE /api/evidence/{evidenceId}
```

Deletion workflow:

```text
Verify ownership
→ Delete database metadata
→ Commit database transaction
→ Delete physical file
→ Remove empty case directory
```

## Verify Evidence Integrity

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
7. Returns the expected and current hashes

Example result:

```json
{
  "success": true,
  "message": "Evidence integrity verified successfully",
  "data": {
    "evidenceId": 2,
    "caseId": 5,
    "originalFileName": "invoice-evidence.txt",
    "expectedSha256Hash": "original-hash",
    "currentSha256Hash": "original-hash",
    "matches": true,
    "integrityStatus": "VERIFIED",
    "verifiedAt": "2026-07-18T10:30:00Z"
  }
}
```

---

# Evidence Text-Extraction APIs

## Extract Evidence Text

```http
POST /api/evidence/{evidenceId}/extract-text
```

Processing workflow:

```text
Verify ownership
→ Select extractor by file type
→ Verify SHA-256 integrity
→ Set status to PROCESSING
→ Load the physical file
→ Extract and normalise text
→ Save extracted text
→ Save character count
→ Set status to PROCESSED
```

When extraction fails:

```text
PROCESSING
→ Save safe error message
→ Set status to FAILED
→ Return 422 response
```

Example successful response:

```json
{
  "success": true,
  "message": "Evidence text extracted successfully",
  "data": {
    "evidenceId": 2,
    "caseId": 5,
    "caseNumber": "TL-20260718-ABCD1234",
    "originalFileName": "invoice.txt",
    "fileType": "TXT",
    "status": "PROCESSED",
    "extractedCharacterCount": 215,
    "extractedText": "Extracted evidence content...",
    "extractionError": null,
    "processedAt": "2026-07-18T10:45:00Z",
    "updatedAt": "2026-07-18T10:45:00Z"
  }
}
```

## Retrieve Extracted Text

```http
GET /api/evidence/{evidenceId}/extracted-text
```

Returns:

- Evidence identity
- File type
- Processing status
- Extracted character count
- Extracted text
- Safe failure message
- Processing timestamp

The normal evidence-listing endpoint does not include large extracted text.

---

# Extraction Architecture

TraceLens uses the Strategy design pattern.

```text
EvidenceProcessingService
          │
          ▼
EvidenceTextExtractorRegistry
          │
          ├── TXT  → TxtEvidenceTextExtractor
          ├── CSV  → CsvEvidenceTextExtractor
          ├── JSON → JsonEvidenceTextExtractor
          └── PDF  → PdfEvidenceTextExtractor
```

Common interface:

```java
public interface EvidenceTextExtractor {

    EvidenceFileType supportedFileType();

    String extract(Resource resource);
}
```

Adding another file format later requires a new extractor implementation rather than modifying one large service.

---

# TXT Extraction

TXT evidence processing:

```text
Read bytes
→ Strict UTF-8 decoding
→ Remove optional UTF-8 BOM
→ Reject malformed UTF-8
→ Reject binary-looking content
→ Normalise line endings
→ Remove surrounding whitespace
→ Enforce character limit
```

TXT files containing null bytes or excessive control characters are rejected.

---

# CSV Extraction

CSV processing supports:

- Header rows
- Quoted fields
- Commas inside quoted values
- Escaped quotation marks
- Empty field values
- Consistent row validation
- Duplicate-header detection
- Blank-header detection
- Configurable row limits

Example input:

```csv
date,sender,description,amount
2026-07-10,finance@example.com,"Invoice, urgent payment",80000
```

Example extracted output:

```text
CSV Headers:
- date
- sender
- description
- amount

Row 1:
date: 2026-07-10
sender: finance@example.com
description: Invoice, urgent payment
amount: 80000
```

The implementation does not use a simple comma split because quoted CSV fields may contain commas.

---

# JSON Extraction

JSON evidence is:

1. Parsed using Jackson
2. Validated for correct syntax
3. Traversed recursively
4. Flattened into searchable key paths
5. Limited by configured nesting depth
6. Limited by configured character count

Example JSON:

```json
{
  "transactionId": "TX-1042",
  "amount": 80000,
  "recipient": {
    "name": "Unknown Vendor"
  }
}
```

Extracted output:

```text
transactionId: TX-1042
amount: 80000
recipient.name: Unknown Vendor
```

Array example:

```text
reviewFlags[0]: account changed
reviewFlags[1]: urgent payment request
```

Malformed JSON returns:

```text
422 Unprocessable Content
```

and the evidence status becomes:

```text
FAILED
```

---

# PDF Extraction

PDF processing uses Apache PDFBox.

Workflow:

```text
Read PDF bytes
→ Load document with PDFBox
→ Verify document validity
→ Validate page count
→ Check extraction permissions
→ Extract each page
→ Add page headings
→ Normalise text
→ Enforce character limit
```

Example result:

```text
--- Page 1 ---

Invoice Number: INV-2048
Amount: Rs. 80,000
Recipient: Unknown Vendor
```

The application rejects or reports:

- Empty PDFs
- Corrupted PDFs
- Password-protected PDFs
- PDFs that prohibit text extraction
- PDFs exceeding the configured page limit
- Image-only or scanned PDFs without extractable text

OCR is not currently implemented.

---

# Extraction Safety Limits

Configured limits:

```properties
app.evidence.extraction.max-characters=100000
app.evidence.extraction.max-pdf-pages=100
app.evidence.extraction.max-csv-rows=5000
app.evidence.extraction.max-json-depth=50
app.evidence.extraction.max-error-message-length=1000
```

## Maximum Extracted Characters

```text
100000
```

Prevents excessively large database content and oversized future AI prompts.

## Maximum PDF Pages

```text
100
```

Prevents very large PDFs from consuming excessive processing time and memory.

## Maximum CSV Data Rows

```text
5000
```

Limits CSV processing workload.

## Maximum JSON Depth

```text
50
```

Prevents excessively nested structures.

## Maximum Stored Error Length

```text
1000 characters
```

Prevents large internal exception content from being persisted.

---

# Evidence Processing Status

## `UPLOADED`

The physical file has been stored, but extraction has not started.

## `PROCESSING`

Text extraction is currently running.

## `PROCESSED`

Extraction completed successfully.

The following values are available:

```text
extracted_text
extracted_character_count
processed_at
```

## `FAILED`

Extraction failed.

The following values are stored:

```text
extraction_error
processed_at
```

Failed evidence may be processed again:

```text
FAILED
→ PROCESSING
→ PROCESSED
```

---

# Integrity Status

## `NOT_VERIFIED`

No completed integrity comparison is available.

## `VERIFIED`

The current file bytes match the original SHA-256 baseline.

```text
expected hash == current hash
```

## `MISMATCH`

The current file bytes differ from the original SHA-256 baseline.

```text
expected hash != current hash
```

Text extraction is blocked when an integrity mismatch is detected.

The original SHA-256 value is never replaced with the altered file’s hash.

---

# SHA-256 Duplicate Detection

SHA-256 produces:

```text
64 lowercase hexadecimal characters
```

The hash is generated from physical file bytes, not from:

- Filename
- Description
- Upload time
- Case number
- Investigator name

Therefore:

```text
Same bytes + same case
→ Duplicate rejected

Same bytes + different case
→ Allowed

Same filename + changed bytes
→ New evidence accepted
```

The database contains a combined unique constraint on:

```text
case_id
sha256_hash
```

---

# Evidence Storage

Default structure:

```text
evidence-storage
├── case-5
│   ├── 8ca31a28-3f74-4a43-9e35-b77aeae660cf.txt
│   ├── f240eb74-c714-4781-914e-e4e49007188c.pdf
│   └── 645cc939-815a-46ce-aab7-30bd83fea5b0.json
└── case-7
    └── cdafdd8a-9ab8-4209-aad9-bbc0fb4d71a7.csv
```

Example mapping:

```text
Original filename:
invoice-final.txt

Stored filename:
8ca31a28-3f74-4a43-9e35-b77aeae660cf.txt
```

The original filename is stored as metadata but is never used directly as the physical filename.

---

# Evidence Validation

## Supported Extensions

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

## Upload Size

```text
Maximum file size: 10 MB
Maximum multipart request size: 11 MB
```

## Original Filename

The original filename:

- Is required
- Cannot be blank
- Cannot exceed 255 characters
- Has path information removed
- Has control characters removed
- Is never used directly for physical storage

## Description

- Optional
- Trimmed before storage
- Maximum 500 characters

## MIME Type

The MIME type must be compatible with the file extension.

## Empty Files

Zero-byte evidence files are rejected.

---

# Security Protections

## JWT Authentication

All case and evidence APIs require a valid JWT.

## Ownership Enforcement

Evidence ownership is derived through:

```text
Evidence
→ Investigation case
→ Case owner
→ Authenticated JWT user
```

The client cannot assign or modify evidence ownership directly.

## Integrity Verification Before Processing

Extraction verifies the current physical file against its original SHA-256 baseline.

```text
VERIFIED
→ Extraction allowed

MISMATCH
→ Extraction blocked
```

## Path-Traversal Protection

Storage paths are:

1. Resolved relative to the configured root
2. Normalised
3. Verified to remain inside the root

Paths such as:

```text
../../outside-file.txt
```

cannot escape the evidence-storage directory.

## UUID Stored Filenames

UUID names prevent:

- Filename collisions
- Accidental overwrites
- Unsafe uploaded names
- Exposure of descriptive filenames

## Hidden Internal Paths

API responses do not expose:

```text
Stored filename
Relative storage path
Absolute server path
Developer-machine path
```

## Safe Error Persistence

Extraction errors do not store:

```text
Stack traces
Absolute paths
Database credentials
JWT values
API keys
Internal implementation details
```

## Git Exclusion

The storage directory remains ignored:

```gitignore
evidence-storage/
```

Uploaded evidence must never be committed to GitHub.

---

# Current Security Rules

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
POST   /api/evidence/{evidenceId}/extract-text
GET    /api/evidence/{evidenceId}/extracted-text
DELETE /api/evidence/{evidenceId}
```

---

# Error Handling

The API currently handles:

- Request validation errors with `400 Bad Request`
- Malformed JSON bodies with `400 Bad Request`
- Unsupported enum values with `400 Bad Request`
- Invalid pagination with `400 Bad Request`
- Unsupported sorting with `400 Bad Request`
- Invalid request-parameter types with `400 Bad Request`
- Unsupported evidence extensions with `400 Bad Request`
- Invalid evidence MIME types with `400 Bad Request`
- Empty evidence files with `400 Bad Request`
- Missing evidence hash baselines with `400 Bad Request`
- Evidence-integrity mismatches with `400 Bad Request`
- Invalid credentials with `401 Unauthorized`
- Missing or invalid JWT tokens with `401 Unauthorized`
- Disabled accounts with `403 Forbidden`
- Missing or unowned users, cases or evidence with `404 Not Found`
- Duplicate email registrations with `409 Conflict`
- Duplicate evidence uploads with `409 Conflict`
- Database constraint conflicts with `409 Conflict`
- Evidence files exceeding upload limits with `413 Content Too Large`
- Unprocessable TXT, CSV, JSON and PDF content with `422`
- Evidence-storage failures with `500 Internal Server Error`
- Unexpected server failures with `500 Internal Server Error`

---

# Planned Features

## Day 7 — Spring AI and Groq Integration

- Add Spring AI dependencies
- Configure Groq API access
- Add environment-based AI credentials
- Configure `ChatClient`
- Add prompt templates
- Add structured response models
- Add timeout and failure handling
- Add AI connectivity testing

## Day 8 — AI Evidence Analysis

- Evidence summarisation
- Risk classification
- Suspicious findings
- Recommended investigative actions
- Structured AI response validation
- Persistent AI-analysis results

## Day 9 — Entity and Timeline Extraction

- People
- Organisations
- Email addresses
- Phone numbers
- URLs
- IP addresses
- Dates
- Money values
- Timeline events

## Day 10 — Investigator Notes and Reports

- Investigator notes
- Notes CRUD
- Case-level report aggregation
- Report-generation API
- Evidence and AI result linking

## Day 11 — Dashboard Backend

- Case statistics
- Evidence statistics
- Risk distribution
- Recent activity
- Processing summaries
- Dashboard API

## Day 12 — React Frontend Foundation

- React and Vite setup
- Authentication pages
- JWT session handling
- Protected routes
- Responsive application layout
- Navbar and sidebar
- API client
- Theme foundation

## Day 13 — Investigation Interface

- Dashboard
- Case management screens
- Case-detail page
- Evidence upload interface
- Integrity controls
- Extracted-text viewer
- AI analysis display
- Entities and timeline
- Investigator notes
- Report interface

## Day 14 — Testing and Documentation

- Automated service tests
- Controller tests
- Security tests
- OpenAPI documentation
- Actuator
- Validation review
- Frontend testing
- UI and error-state polish

## Day 15 — Deployment and Presentation

- Backend deployment
- Frontend deployment
- Production environment configuration
- Database deployment
- Demonstration workflow
- Resume project description
- LinkedIn project description
- Interview questions and explanations

---

# Git Workflow

Development is committed checkpoint by checkpoint.

```text
Complete implementation
→ Test functionality
→ Review changed files
→ Confirm no secrets or evidence are included
→ Commit to main
→ Push origin
```

Never commit:

```text
DB_PASSWORD
JWT_SECRET
JWT access tokens
AI API keys
evidence-storage/
target/
temporary integrity backups
uploaded test evidence
```

---

# Disclaimer

TraceLens AI is an educational and portfolio project.

AI-generated findings must be treated as investigative assistance and independently verified before use in legal, disciplinary, financial or security decisions.

Uploaded test evidence must not contain real confidential, personal, privileged or legally restricted information.