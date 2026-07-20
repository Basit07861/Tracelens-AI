# TraceLens AI

TraceLens AI is an AI-powered digital evidence analysis and investigation platform built with Spring Boot.

It enables authorised investigators to:

- Register and authenticate securely
- Create and manage investigation cases
- Upload and organise digital evidence
- Preserve evidence integrity using SHA-256
- Detect duplicate files
- Verify whether stored evidence was modified
- Extract readable content from TXT, CSV, JSON and PDF files
- Generate structured AI evidence previews
- Classify preliminary risk levels
- Identify evidence-supported indicators and limitations
- Search, filter, download and securely delete evidence

> AI-generated output is investigative assistance only. It is not legal proof, a final conclusion, or a substitute for independent human review.

---

## Current Status

The backend implementation is complete through **Day 8** of the project plan.

Implemented so far:

- Spring Boot backend foundation
- MySQL database integration
- Registration, login, JWT authentication and BCrypt password hashing
- Investigation-case CRUD, search, filtering, pagination and ownership enforcement
- Secure evidence upload, download, listing and deletion
- TXT, CSV, JSON and PDF support
- SHA-256 evidence hashing, duplicate detection and integrity verification
- Persistent extracted-text storage
- Structured AI previews using Spring AI and Groq
- Persistent AI evidence analyses
- AI analysis history, regeneration and concurrency protection
- Safe provider-error handling and mandatory human review

---

## Main Features

### Authentication and Security

- User registration
- Email-and-password login
- BCrypt password hashing
- JWT access tokens
- Stateless Spring Security
- Protected endpoints
- Authenticated user lookup
- Disabled-account handling
- Ownership enforcement for cases, evidence and AI analysis

### Investigation Cases

- Create, read, update and delete cases
- Unique human-readable case numbers
- Case status management
- Priority management
- Keyword search
- Status and priority filtering
- Pagination
- Controlled sorting
- Owner-restricted access

### Digital Evidence

- Upload evidence using multipart requests
- Supported formats: TXT, CSV, JSON and PDF
- Extension and MIME-type validation
- Maximum file-size validation
- Empty-file rejection
- Filename sanitisation
- UUID-based physical filenames
- Case-specific storage folders
- Path-traversal protection
- Evidence metadata retrieval
- Secure evidence download
- Transaction-safe deletion

### Evidence Integrity

- SHA-256 hash generation
- Hash calculated from persisted file bytes
- Per-case duplicate detection
- Same file allowed in separate cases
- Integrity verification endpoint
- `VERIFIED`, `MISMATCH` and `NOT_VERIFIED` states
- Original baseline hash is never replaced
- Evidence integrity is rechecked before AI processing

### Evidence Text Extraction

- Strict UTF-8 TXT processing
- CSV parsing with quoted-field support
- JSON flattening with Jackson
- PDF extraction using Apache PDFBox
- Page-by-page PDF extraction
- Processing lifecycle: `UPLOADED`, `PROCESSING`, `PROCESSED`, `FAILED`
- Character, row, page and nesting limits
- Safe extraction-error persistence
- Extracted-text retrieval endpoint

### AI Integration

- Spring AI `ChatClient`
- Groq through an OpenAI-compatible endpoint
- Configurable AI model
- Resource-based prompt templates
- Structured Java-record output
- AI connectivity endpoint
- Prompt-injection resistance instructions
- Application-level validation
- Provider retry handling
- Semantic correction attempts
- Safe `502` and `503` responses
- Human review always required

### Persistent AI Evidence Analysis

- Persistent analysis records in MySQL
- Lifecycle states: `PENDING`, `PROCESSING`, `COMPLETED`, `FAILED`
- Factual summary
- Preliminary risk level
- Suspicious findings
- Recommended investigative actions
- Information-sufficiency result
- Limitations
- Provider and model metadata
- Prompt and schema versioning
- Physical-evidence SHA-256
- Extracted-text SHA-256
- Token usage when available
- Direct analysis retrieval
- Latest-analysis retrieval
- Paginated analysis history
- Analysis regeneration
- Previous results preserved
- Pessimistic concurrency protection

---

## Development Progress

### Day 1 — Backend Foundation

- Initialised the Spring Boot Maven project
- Configured MySQL and Spring Data JPA
- Added environment-based credentials
- Added reusable API response models
- Added global exception handling
- Added system and database status endpoint
- Connected the project to GitHub

### Day 2 — Authentication and Security

- Added `User` and `Role`
- Added registration and login
- Added request validation
- Added BCrypt password hashing
- Added JWT generation and validation
- Configured stateless Spring Security
- Added `/api/auth/me`
- Added invalid-credential and disabled-account handling

### Day 3 — Investigation Case Management

- Added `InvestigationCase`
- Added case status and priority enums
- Connected cases to users
- Added unique case numbers
- Added case CRUD
- Added status update endpoint
- Added search, filtering, pagination and sorting
- Enforced case ownership

### Day 4 — Evidence Management

- Added `Evidence`
- Added upload, list, metadata, download and delete APIs
- Added TXT, CSV, JSON and PDF upload support
- Added safe file validation and storage
- Added UUID stored filenames
- Added case-specific evidence directories
- Added path-traversal protection
- Added transaction-safe file cleanup

### Day 5 — Evidence Integrity

- Added SHA-256 hashing
- Added per-case duplicate detection
- Added integrity status and verification timestamps
- Added integrity verification endpoint
- Tested modification and restoration
- Preserved the original hash baseline

### Day 6 — Evidence Text Extraction

- Added extracted-text persistence
- Added processing states and timestamps
- Added extractor strategy architecture
- Added TXT, CSV, JSON and PDF extractors
- Added extraction safety limits
- Added `422` handling for unprocessable content
- Verified integrity before extraction

### Day 7 — Spring AI and Groq

- Added Spring AI
- Connected Groq using the OpenAI-compatible starter
- Added configurable provider and model settings
- Created a reusable `ChatClient`
- Added a protected AI connectivity endpoint
- Added resource-based prompt templates
- Added structured AI preview responses
- Added risk classification
- Added prompt-injection resistance instructions
- Added response validation and semantic retry
- Added safe AI error handling
- Kept previews non-persistent

### Day 8 — Persistent AI Analysis

- Added persistent AI-analysis tables
- Added analysis lifecycle states
- Added summaries, risk, findings, actions and limitations
- Added provider, model, prompt and schema metadata
- Added physical-evidence and extracted-text SHA-256 values
- Added token usage when available
- Added safe failure persistence
- Added persistent analysis generation
- Added direct analysis retrieval
- Added latest-analysis retrieval
- Added paginated history
- Added analysis regeneration
- Preserved previous analysis attempts
- Added pessimistic locking
- Prevented simultaneous analysis generation for the same evidence

---

## Technology Stack

### Backend

- Java 17
- Spring Boot 4.1.0
- Spring Web MVC
- Spring Data JPA
- Spring Security
- OAuth2 Resource Server
- JWT
- BCrypt
- Hibernate
- MySQL
- Maven

### AI

- Spring AI 2.0.0
- Spring AI `ChatClient`
- Groq API
- OpenAI-compatible model integration
- Configured model: `llama-3.3-70b-versatile`
- Structured Java-record conversion
- Prompt templates
- Semantic validation
- Provider and application-level retry

### Evidence Processing

- Java NIO
- Java `MessageDigest`
- SHA-256
- Jackson
- Apache PDFBox 3.0.8
- Strict UTF-8 decoding
- Multipart upload
- UUID filenames
- Strategy design pattern

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
├── ai
│   ├── config
│   │   ├── AiAnalysisProperties.java
│   │   ├── AiPreviewProperties.java
│   │   └── TraceLensAiConfig.java
│   ├── controller
│   │   └── AiController.java
│   ├── dto
│   │   ├── AiEvidenceAnalysisContent.java
│   │   ├── AiEvidenceAnalysisHistoryResponse.java
│   │   ├── AiEvidenceAnalysisResponse.java
│   │   ├── AiEvidencePreviewContent.java
│   │   ├── AiEvidencePreviewResponse.java
│   │   └── AiStatusResponse.java
│   ├── entity
│   │   ├── AiAnalysisRequestType.java
│   │   ├── AiAnalysisStatus.java
│   │   ├── AiConnectionStatus.java
│   │   ├── AiEvidenceAnalysis.java
│   │   └── AiPreviewRiskLevel.java
│   ├── repository
│   │   ├── AiEvidenceAnalysisLockRepository.java
│   │   └── AiEvidenceAnalysisRepository.java
│   └── service
│       ├── AiEvidenceAnalysisService.java
│       ├── AiEvidenceAnalysisStateService.java
│       ├── AiEvidenceAnalysisTarget.java
│       ├── AiEvidenceAnalysisValidator.java
│       ├── AiEvidencePreviewService.java
│       ├── AiEvidencePreviewValidator.java
│       └── AiStatusService.java
│
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
│   ├── AiResponseValidationException.java
│   ├── AiServiceUnavailableException.java
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

Resources:

```text
src/main/resources
├── prompts
│   ├── evidence-analysis-user.st
│   └── evidence-preview-user.st
└── application.properties
```

---

## Database Structure

Current tables:

```text
users
investigation_cases
evidence_files
ai_evidence_analyses
ai_analysis_findings
ai_analysis_actions
ai_analysis_limitations
```

### Relationships

```text
User
  │
  │ owns
  ▼
InvestigationCase
  │
  │ contains
  ▼
Evidence
  │
  │ has many analysis attempts
  ▼
AiEvidenceAnalysis
  │
  ├── Suspicious findings
  ├── Recommended actions
  └── Limitations
```

Foreign keys:

```text
investigation_cases.owner_id
→ users.id

evidence_files.case_id
→ investigation_cases.id

ai_evidence_analyses.evidence_id
→ evidence_files.id

ai_analysis_findings.analysis_id
→ ai_evidence_analyses.id

ai_analysis_actions.analysis_id
→ ai_evidence_analyses.id

ai_analysis_limitations.analysis_id
→ ai_evidence_analyses.id
```

### Main Tables

`users` stores user identity, BCrypt password hashes, roles, active status and timestamps.

`investigation_cases` stores case details, priority, status, owner and timestamps.

`evidence_files` stores evidence metadata, integrity values, extracted text, processing status and timestamps.

`ai_evidence_analyses` stores persistent AI analysis output, lifecycle status, provider metadata, source hashes, token usage and timestamps.

Collection tables store ordered analysis items:

```text
ai_analysis_findings
- analysis_id
- finding_order
- finding_text

ai_analysis_actions
- analysis_id
- action_order
- action_text

ai_analysis_limitations
- analysis_id
- limitation_order
- limitation_text
```

---

## Prerequisites

Install:

- Java 17 or a compatible newer runtime
- Maven
- MySQL 8
- Eclipse or Spring Tools
- Git
- A Groq API key

---

## Database Setup

```sql
CREATE DATABASE IF NOT EXISTS tracelens_db
CHARACTER SET utf8mb4
COLLATE utf8mb4_unicode_ci;
```

```sql
USE tracelens_db;
```

Hibernate creates and updates the required tables when the application starts.

---

## Environment Variables

Required:

```text
DB_PASSWORD=your_mysql_password
JWT_SECRET=your_base64_encoded_jwt_secret
GROQ_API_KEY=your_groq_api_key
```

Optional:

```text
DB_USERNAME=root
DB_URL=jdbc:mysql://localhost:3306/tracelens_db
EVIDENCE_STORAGE_ROOT=evidence-storage
GROQ_BASE_URL=https://api.groq.com/openai/v1
GROQ_MODEL=llama-3.3-70b-versatile
```

Never commit:

```text
Database passwords
JWT secrets
JWT access tokens
Groq API keys
Uploaded evidence
Temporary evidence backups
Provider requests or responses containing evidence
```

### Eclipse Environment Setup

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
GROQ_API_KEY
```

Then select:

```text
Apply
→ Run
```

---

## Application Configuration

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

spring.ai.model.chat=openai
spring.ai.openai.api-key=${GROQ_API_KEY}
spring.ai.openai.base-url=${GROQ_BASE_URL:https://api.groq.com/openai/v1}
spring.ai.openai.chat.model=${GROQ_MODEL:llama-3.3-70b-versatile}
spring.ai.openai.chat.temperature=0.1
spring.ai.openai.chat.max-tokens=1400

spring.ai.retry.max-attempts=3
spring.ai.retry.backoff.initial-interval=1s
spring.ai.retry.backoff.multiplier=2
spring.ai.retry.backoff.max-interval=5s
spring.ai.retry.on-client-errors=false

app.ai.provider=Groq

app.ai.preview.max-input-characters=30000
app.ai.preview.max-summary-characters=1200
app.ai.preview.max-indicators=6
app.ai.preview.max-limitations=5
app.ai.preview.max-list-item-characters=300
app.ai.preview.validation-attempts=2

app.ai.analysis.max-input-characters=30000
app.ai.analysis.max-summary-characters=2000
app.ai.analysis.max-findings=8
app.ai.analysis.max-actions=8
app.ai.analysis.max-limitations=6
app.ai.analysis.max-item-characters=500
app.ai.analysis.validation-attempts=2
app.ai.analysis.max-failure-message-characters=1000
app.ai.analysis.prompt-version=evidence-analysis-v1
app.ai.analysis.response-schema-version=ai-analysis-v1

server.error.include-message=always
server.error.include-binding-errors=always
```

---

## Running the Application

Run `TracelensBackendApplication.java` as a Spring Boot application.

Base URL:

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
→ Store user in MySQL
→ Log in
→ Generate JWT
→ Send Bearer token
→ Access protected APIs
```

Protected request header:

```http
Authorization: Bearer <access-token>
```

Configured JWT lifetime:

```text
60 minutes
```

---

## API Endpoints

### Public Endpoints

```http
GET  /api/system/status
POST /api/auth/register
POST /api/auth/login
```

### Authentication

```http
GET /api/auth/me
```

### Investigation Cases

```http
POST   /api/cases
GET    /api/cases
GET    /api/cases/{caseId}
PUT    /api/cases/{caseId}
PATCH  /api/cases/{caseId}/status
DELETE /api/cases/{caseId}
```

### Evidence

```http
POST   /api/cases/{caseId}/evidence
GET    /api/cases/{caseId}/evidence
GET    /api/evidence/{evidenceId}
GET    /api/evidence/{evidenceId}/download
POST   /api/evidence/{evidenceId}/verify-integrity
POST   /api/evidence/{evidenceId}/extract-text
GET    /api/evidence/{evidenceId}/extracted-text
DELETE /api/evidence/{evidenceId}
```

### AI

```http
GET  /api/ai/status
POST /api/ai/evidence/{evidenceId}/preview
POST /api/ai/evidence/{evidenceId}/analyses
POST /api/ai/evidence/{evidenceId}/analyses/regenerate
GET  /api/ai/analyses/{analysisId}
GET  /api/ai/evidence/{evidenceId}/analyses/latest
GET  /api/ai/evidence/{evidenceId}/analyses
```

All endpoints except the explicitly public routes require authentication.

---

## API Examples

### Register

```http
POST /api/auth/register
Content-Type: application/json
```

```json
{
  "fullName": "Example Investigator",
  "email": "investigator@example.com",
  "password": "StrongPassword@123"
}
```

### Login

```http
POST /api/auth/login
Content-Type: application/json
```

```json
{
  "email": "investigator@example.com",
  "password": "StrongPassword@123"
}
```

### Create Case

```http
POST /api/cases
Authorization: Bearer <token>
Content-Type: application/json
```

```json
{
  "title": "Suspicious Invoice Investigation",
  "description": "Investigate possible invoice manipulation.",
  "priority": "HIGH"
}
```

### Upload Evidence

```powershell
curl.exe -X POST `
"http://localhost:8080/api/cases/$caseId/evidence" `
-H "Authorization: Bearer $token" `
-F "file=@$sampleFile;type=text/plain" `
-F "description=Invoice communication evidence"
```

### Verify Evidence Integrity

```http
POST /api/evidence/{evidenceId}/verify-integrity
Authorization: Bearer <token>
```

### Extract Text

```http
POST /api/evidence/{evidenceId}/extract-text
Authorization: Bearer <token>
```

### Generate Temporary AI Preview

```http
POST /api/ai/evidence/{evidenceId}/preview
Authorization: Bearer <token>
```

### Generate First Persistent Analysis

```http
POST /api/ai/evidence/{evidenceId}/analyses
Authorization: Bearer <token>
```

Allowed only when no previous analysis exists for that evidence.

### Regenerate Analysis

```http
POST /api/ai/evidence/{evidenceId}/analyses/regenerate
Authorization: Bearer <token>
```

Creates a new analysis record and preserves previous attempts.

### Retrieve One Analysis

```http
GET /api/ai/analyses/{analysisId}
Authorization: Bearer <token>
```

### Retrieve Latest Analysis

```http
GET /api/ai/evidence/{evidenceId}/analyses/latest
Authorization: Bearer <token>
```

### Retrieve Analysis History

```http
GET /api/ai/evidence/{evidenceId}/analyses?page=0&size=10
Authorization: Bearer <token>
```

Maximum history page size: `50`.

---

## Evidence Storage

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

Original filenames are stored as metadata but are not used as physical filenames.

The storage directory must remain excluded from Git.

---

## Supported Evidence Formats

### TXT

- Strict UTF-8 decoding
- Optional BOM removal
- Binary-content detection
- Line-ending normalisation
- Character-limit enforcement

### CSV

- Header validation
- Quoted values
- Commas inside quoted values
- Escaped quotes
- Consistent row width
- Row-limit enforcement

### JSON

- Syntax validation
- Recursive traversal
- Flattened key paths
- Array indexes
- Nesting-depth enforcement

### PDF

- PDFBox extraction
- Page-by-page processing
- Page headings
- Page-count limit
- Password and permission checks
- Invalid-PDF handling
- Image-only PDF detection

OCR is not currently implemented.

---

## Evidence Processing Lifecycle

```text
UPLOADED
→ PROCESSING
→ PROCESSED
```

On failure:

```text
UPLOADED
→ PROCESSING
→ FAILED
```

Failed evidence may be processed again.

---

## Evidence Integrity Lifecycle

### `NOT_VERIFIED`

No completed integrity comparison is available.

### `VERIFIED`

```text
Current file hash == original baseline hash
```

### `MISMATCH`

```text
Current file hash != original baseline hash
```

Text extraction and AI analysis are blocked when an integrity mismatch is detected.

---

## AI Risk Levels

```text
LOW
MEDIUM
HIGH
CRITICAL
UNKNOWN
```

Risk values are preliminary AI classifications, not factual or legal conclusions.

---

## AI Prompt Architecture

```text
Default system prompt
+
Resource-based user prompt
+
Structured Java-record conversion
+
Semantic validator
+
Mandatory human review
```

Prompt resources:

```text
src/main/resources/prompts/evidence-preview-user.st
src/main/resources/prompts/evidence-analysis-user.st
```

---

## Prompt-Injection Protection

Uploaded evidence is treated as untrusted data.

Protection includes:

- Fixed system instructions
- Explicit evidence boundaries
- Instructions to ignore embedded commands
- Structured response mapping
- Java semantic validation
- Input and output limits
- Mandatory human review

Prompt controls reduce risk but cannot guarantee complete immunity from adversarial model inputs.

---

## AI Preview Limits

```properties
app.ai.preview.max-input-characters=30000
app.ai.preview.max-summary-characters=1200
app.ai.preview.max-indicators=6
app.ai.preview.max-limitations=5
app.ai.preview.max-list-item-characters=300
app.ai.preview.validation-attempts=2
```

---

## Persistent AI Analysis Limits

```properties
app.ai.analysis.max-input-characters=30000
app.ai.analysis.max-summary-characters=2000
app.ai.analysis.max-findings=8
app.ai.analysis.max-actions=8
app.ai.analysis.max-limitations=6
app.ai.analysis.max-item-characters=500
app.ai.analysis.validation-attempts=2
app.ai.analysis.max-failure-message-characters=1000
```

Validation requires:

- Non-empty summary
- Valid risk level
- Information-sufficiency value
- At most eight findings
- At most eight actions
- At least one recommended action
- At most six limitations
- Items within 500 characters
- A limitation when information is insufficient
- At least one finding for `HIGH` or `CRITICAL`
- Human review always enabled

---

## Persistent AI Analysis Lifecycle

### `PENDING`

The database record has been created.

### `PROCESSING`

The provider request is being prepared or executed.

### `COMPLETED`

A valid structured response was generated and stored.

### `FAILED`

The analysis attempt failed due to provider, conversion, validation or unexpected processing failure.

Only a safe application-level failure message is stored. API keys, stack traces and raw provider error bodies are not persisted.

---

## Source Hashes

Every persistent analysis stores two SHA-256 values.

### Physical Evidence Hash

```text
sourceEvidenceSha256
```

Represents the original physical evidence bytes.

### Extracted Text Hash

```text
sourceTextSha256
```

Represents the exact UTF-8 extracted text supplied to the AI provider.

This allows TraceLens to distinguish physical-file changes from extraction-output changes.

---

## Initial Analysis and Regeneration Rules

### Initial Generation

```http
POST /api/ai/evidence/{evidenceId}/analyses
```

Allowed only when no previous analysis exists.

### Regeneration

```http
POST /api/ai/evidence/{evidenceId}/analyses/regenerate
```

Allowed only when at least one previous analysis exists.

Regeneration:

- Creates a new analysis ID
- Preserves previous completed and failed records
- Generates new timestamps
- Stores the new structured response independently

---

## Concurrency Protection

TraceLens uses a pessimistic write lock on the evidence row during analysis-record creation.

```text
Request A
→ Locks evidence
→ Checks active analyses
→ Creates PENDING row
→ Commits
→ Releases lock

Request B
→ Waits
→ Finds PENDING or PROCESSING analysis
→ Is rejected
```

The Groq network call occurs after the lock transaction finishes.

---

## Transaction Design

```text
Transaction 1
→ Create PENDING

Transaction 2
→ Mark PROCESSING

No database transaction
→ Call provider and validate output

Transaction 3
→ Mark COMPLETED

or

Transaction 3
→ Mark FAILED
```

This avoids holding a database transaction during a network request and preserves failure history.

---

## Security Protections

### Authentication

Protected APIs require a valid JWT.

### Ownership

```text
Analysis
→ Evidence
→ Investigation case
→ Authenticated owner
```

### Secret Management

Secrets are stored as environment variables and must never be committed.

### Filesystem Safety

- UUID stored filenames
- Normalised paths
- Storage-root boundary checks
- Case-specific directories
- Internal paths hidden from API responses

### Safe Logging

Logs do not intentionally include JWT values, Groq API keys, database passwords, evidence text, provider authorisation headers or complete provider error bodies.

### Mandatory Human Review

Every AI preview and persistent AI analysis has:

```text
humanReviewRequired = true
```

The model cannot disable human review.

---

## Error Handling

TraceLens currently handles:

- Request validation failures with `400`
- Malformed JSON with `400`
- Invalid enum or query values with `400`
- Invalid pagination and sorting with `400`
- Invalid evidence files with `400`
- Evidence-integrity mismatches with `400`
- Unprocessed evidence analysis with `400`
- Oversized AI input with `400`
- Duplicate initial analysis with `400`
- Regeneration without history with `400`
- Concurrent analysis request with `400`
- Invalid credentials with `401`
- Missing or invalid JWT with `401`
- Disabled account with `403`
- Missing or unowned resources with `404`
- Duplicate data with `409`
- Oversized uploads with `413`
- Unprocessable evidence content with `422`
- Invalid structured AI output with `502`
- Unavailable AI provider with `503`
- Evidence-storage failure with `500`
- Unexpected application failure with `500`

---

## Recommended Test Flow

```text
1. Register or log in
2. Create an investigation case
3. Upload TXT, CSV, JSON or PDF evidence
4. Verify evidence integrity
5. Extract text
6. Generate a temporary AI preview
7. Generate the first persistent AI analysis
8. Retrieve the analysis by ID
9. Retrieve the latest analysis
10. Retrieve paginated history
11. Regenerate the analysis
12. Confirm the previous analysis remains unchanged
13. Confirm only one concurrent request can start
```

---

## Planned Features

### Day 9 — Entity and Timeline Extraction

- Persistent extracted-entity records
- People and organisations
- Email addresses
- Phone numbers
- URLs
- IP addresses
- Dates and times
- Monetary values
- Entity confidence and source support
- Timeline-event extraction
- Event descriptions and involved entities
- Entity and timeline retrieval APIs
- Deduplication and ownership enforcement

### Day 10 — Investigator Notes and Reports

- Investigator notes
- Notes CRUD
- Case-level report aggregation
- Evidence and AI-result linking
- Report-generation API

### Day 11 — Dashboard Backend

- Case statistics
- Evidence statistics
- Risk distribution
- Recent activity
- Processing summaries
- Dashboard API

### Day 12 — React Frontend Foundation

- React and Vite
- Authentication pages
- JWT session handling
- Protected routes
- Responsive layout
- Navbar and sidebar
- API client
- Theme foundation

### Day 13 — Investigation Interface

- Dashboard
- Case management screens
- Case-detail screen
- Evidence upload interface
- Integrity controls
- Extracted-text viewer
- AI-analysis viewer
- Entity and timeline interface
- Notes and reports

### Day 14 — Testing and Documentation

- Service tests
- Controller tests
- Security tests
- OpenAPI documentation
- Actuator
- Frontend tests
- Validation and error-state polish

### Day 15 — Deployment and Presentation

- Backend deployment
- Frontend deployment
- Production environment configuration
- Database deployment
- Demonstration workflow
- Resume project description
- LinkedIn project description
- Interview preparation

---

## Git Workflow

```text
Implement checkpoint
→ Test checkpoint
→ Review changed files
→ Confirm no secrets or evidence are included
→ Commit to main
→ Push origin
```

Never commit:

```text
DB_PASSWORD
JWT_SECRET
GROQ_API_KEY
JWT access tokens
evidence-storage/
target/
Uploaded evidence
Temporary evidence backups
Sensitive provider logs
```

---

## Disclaimer

TraceLens AI is an educational and portfolio project.

AI-generated summaries, classifications, findings and recommendations must be treated as investigative assistance only.

They must be independently reviewed and verified before use in legal, disciplinary, financial, employment, compliance or security decisions.

Uploaded test evidence must not contain real confidential, privileged, personal or legally restricted information.

---

## License

This project is licensed under the MIT License.