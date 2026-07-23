# TraceLens AI

TraceLens AI is an AI-powered digital evidence analysis and investigation platform built with Spring Boot.

It enables authorised investigators to:

- Register and authenticate securely
- Create and manage investigation cases
- Upload, organise, download and securely delete digital evidence
- Preserve evidence integrity using SHA-256
- Detect duplicate files and verify whether stored evidence was modified
- Extract readable content from TXT, CSV, JSON and PDF files
- Generate temporary AI previews and persistent AI evidence analyses
- Classify preliminary risk levels and record evidence-supported findings
- Extract structured entities using deterministic and AI-assisted methods
- Build persistent investigation timelines from processed evidence
- Retrieve intelligence history and safely regenerate intelligence runs
- Create, edit, pin and delete investigator notes
- Generate one aggregated case report containing saved case, evidence, analysis, intelligence and note data
- View owner-restricted dashboard analytics for case, evidence, processing and risk summaries

> AI-generated output is investigative assistance only. It is not legal proof, a final conclusion, or a substitute for independent human review.

---

## Current Status

The backend implementation is complete through **Day 11** of the project plan.

**Backend MVP status: complete.**

**Dashboard analytics milestone: complete.**

Implemented so far:

- Spring Boot backend foundation and MySQL integration
- Registration, login, JWT authentication and BCrypt password hashing
- Investigation-case CRUD, search, filtering, pagination and ownership enforcement
- Secure evidence upload, listing, metadata retrieval, download and deletion
- TXT, CSV, JSON and PDF evidence support
- SHA-256 hashing, duplicate detection and integrity verification
- Persistent extracted-text storage with controlled processing states
- Structured AI previews using Spring AI and Groq
- Persistent AI evidence analyses with history, regeneration and concurrency protection
- Persistent evidence-intelligence runs
- Hybrid deterministic and AI-assisted entity extraction
- Structured timeline-event extraction with entity links
- Direct, latest and paginated intelligence-run retrieval
- Paginated entity and timeline retrieval with optional filters
- Intelligence regeneration with previous runs preserved
- Concurrent intelligence-run protection
- Investigator-note CRUD with pinning, validation and optimistic locking
- Secure aggregated case-report generation
- Report ordering, generation timestamp and mandatory AI-verification disclaimer
- Authenticated dashboard analytics using database aggregation queries
- Owner-restricted case totals, status counts and priority breakdowns
- Total and processed evidence metrics
- HIGH-risk analysis count
- Up to five recently updated case summaries
- Safe error handling, ownership enforcement and internal-field protection across the completed workflow

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
- Ownership enforcement for cases, evidence, analyses, intelligence runs, notes, reports and dashboard analytics
- Missing and unowned resources return the same safe not-found response
- Environment-based secret management
- Internal filesystem paths and security-sensitive fields are excluded from API DTOs

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
- Evidence integrity is rechecked before extraction, analysis and intelligence generation

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

### Evidence Intelligence and Timeline

- Persistent intelligence runs linked to evidence
- Lifecycle states: `PENDING`, `PROCESSING`, `COMPLETED`, `FAILED`
- Methods: `DETERMINISTIC`, `AI`, `HYBRID`
- Hybrid deterministic and AI-assisted extraction
- Entity types:
  - `PERSON`
  - `ORGANIZATION`
  - `EMAIL_ADDRESS`
  - `PHONE_NUMBER`
  - `URL`
  - `IP_ADDRESS`
  - `DATE`
  - `DATE_TIME`
  - `TIME`
  - `MONEY`
- Normalised entity values for deduplication
- Original display values and supporting context
- Confidence, occurrence count and character offsets
- Timeline titles, descriptions and source expressions
- Normalised date/time values
- Temporal precision and certainty
- Links between timeline events and involved entities
- Direct and latest-run retrieval
- Paginated run history
- Filtered entity retrieval
- Filtered timeline retrieval
- Intelligence regeneration with previous runs preserved
- Concurrent-run protection
- Evidence and case ownership enforcement

### Investigator Notes

- Persistent notes linked to cases and authors
- Create and list notes for owned cases
- Update note content and pinned state
- Delete owned notes
- Maximum content length of 5,000 characters
- Blank-content rejection
- Pinned notes displayed first
- Newest notes displayed first within each pinned group
- Author resolved from the authenticated user
- Optimistic locking through a version column
- Safe `404` handling for missing and unowned notes

### Aggregated Case Report

- Secure endpoint: `GET /api/cases/{caseId}/report`
- Ownership verified before aggregation
- Case summary
- Evidence metadata ordered by upload time
- Latest completed AI analyses
- Latest completed intelligence output for each evidence item
- Aggregated extracted entities
- Chronologically ordered timeline events
- Investigator notes with pinned notes first
- Report-generation timestamp
- Mandatory disclaimer:
  - `AI-generated findings are investigative aids and must be independently verified.`
- JSON response for the backend MVP
- Safe DTO-only output
- No storage paths, password hashes, JWT claims, access tokens or internal exception details

### Dashboard Analytics

- Secure endpoint: `GET /api/dashboard`
- JWT-authenticated and owner-restricted statistics
- Total investigation-case count
- Separate `OPEN`, `IN_PROGRESS`, `COMPLETED` and `ARCHIVED` case counts
- Complete case-status breakdown for charts
- Complete `LOW`, `MEDIUM`, `HIGH` and `CRITICAL` priority breakdown
- Total evidence count
- Successfully processed evidence count
- Stored AI-analysis count with `HIGH` risk
- Up to five recently updated case summaries
- Recent cases ordered by `updatedAt` descending and then `id` descending
- Database-level count queries instead of loading complete tables into Java
- Existing safe `CaseResponse` DTOs used for recent-case output
- No Groq request or new AI processing during dashboard retrieval
- Missing authentication returns `401 Unauthorized`

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

### Day 9 — Evidence Intelligence and Timeline

- Added persistent evidence-intelligence runs
- Added intelligence lifecycle and method enums
- Added deterministic entity extraction
- Added structured AI-assisted entity and timeline extraction
- Combined deterministic and AI results through a hybrid workflow
- Added entity normalisation and deduplication
- Added entity context, confidence, occurrence counts and character offsets
- Added timeline descriptions, temporal expressions and normalised dates
- Added timeline certainty and temporal-precision values
- Linked timeline events to involved entities
- Added full intelligence-run retrieval
- Added latest-run retrieval
- Added paginated intelligence history
- Added paginated and filtered entity retrieval
- Added paginated and filtered timeline retrieval
- Added intelligence regeneration
- Preserved previous intelligence runs
- Added source-analysis and source-hash metadata
- Added concurrent-run protection
- Enforced ownership for every intelligence operation

### Day 10 — Investigator Notes and Final Report

- Added investigator-note persistence
- Linked notes to investigation cases and authenticated authors
- Added note creation and listing
- Added note content and pin-state updates
- Added note deletion
- Added blank and 5,000-character validation
- Added pinned-first and newest-first ordering
- Added optimistic locking for note updates
- Added secure note repository queries
- Added centralised note-not-found handling
- Added the aggregated case-report response DTO
- Added the secure final-report service and endpoint
- Aggregated case, evidence, analyses, entities, timeline and notes
- Used the latest completed analysis and intelligence data
- Added deterministic report ordering
- Added report-generation timestamp
- Added the mandatory AI-verification disclaimer
- Prevented internal storage and security fields from leaking
- Completed the backend MVP through Day 10

### Day 11 — Dashboard and Analytics APIs

- Added `StatusCount`, `PriorityCount` and `DashboardResponse` DTOs
- Added owner-restricted case-count repository queries
- Added case counts by `CaseStatus`
- Added case counts by `CasePriority`
- Added total and processed evidence count queries
- Added HIGH-risk AI-analysis count query
- Preserved Day 10 ordered evidence retrieval for final reports
- Added `DashboardService`
- Added `DashboardController`
- Added authenticated `GET /api/dashboard`
- Used the JWT subject as the owner identity
- Returned all status and priority categories, including zero-count categories
- Returned up to five recently updated cases
- Ordered recent cases by `updatedAt` descending and `id` descending
- Kept dashboard aggregation inside MySQL through Spring Data count queries
- Verified that case-status totals equal the overall case count
- Verified that priority totals equal the overall case count
- Verified total and processed evidence metrics
- Verified recent-case retrieval and the five-item limit
- Verified unauthenticated dashboard access returns `401`
- Completed the backend implementation through Day 11

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
├── dashboard
│   ├── controller
│   │   └── DashboardController.java
│   ├── dto
│   │   ├── DashboardResponse.java
│   │   ├── PriorityCount.java
│   │   └── StatusCount.java
│   └── service
│       └── DashboardService.java
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
│   ├── NoteNotFoundException.java
│   └── UserNotFoundException.java
│
├── intelligence
│   ├── config
│   │   └── IntelligenceExtractionProperties.java
│   ├── controller
│   │   └── IntelligenceController.java
│   ├── dto
│   │   ├── EvidenceIntelligenceContent.java
│   │   ├── EvidenceIntelligenceRunHistoryResponse.java
│   │   ├── EvidenceIntelligenceRunResponse.java
│   │   ├── EvidenceIntelligenceRunSummaryResponse.java
│   │   ├── ExtractedEntityPageResponse.java
│   │   ├── ExtractedEntityResponse.java
│   │   ├── IntelligenceEntityContent.java
│   │   ├── IntelligenceEntityReferenceContent.java
│   │   ├── IntelligenceTimelineEventContent.java
│   │   ├── TimelineEventPageResponse.java
│   │   └── TimelineEventResponse.java
│   ├── entity
│   │   ├── EvidenceIntelligenceRun.java
│   │   ├── ExtractedEntity.java
│   │   ├── ExtractedEntityType.java
│   │   ├── IntelligenceMethod.java
│   │   ├── IntelligenceRunStatus.java
│   │   ├── TimelineEvent.java
│   │   ├── TimelineEventCertainty.java
│   │   └── TimelineTemporalPrecision.java
│   ├── repository
│   │   ├── EvidenceIntelligenceRunRepository.java
│   │   ├── ExtractedEntityRepository.java
│   │   └── TimelineEventRepository.java
│   └── service
│       ├── DeterministicEntityExtractor.java
│       ├── EvidenceIntelligenceRunStartService.java
│       ├── EvidenceIntelligenceService.java
│       ├── EvidenceIntelligenceStateService.java
│       ├── EvidenceIntelligenceTarget.java
│       ├── EvidenceIntelligenceValidator.java
│       ├── IntelligenceEntityCandidate.java
│       ├── IntelligenceEntityNormalizationService.java
│       └── IntelligenceTimelineCandidate.java
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
├── note
│   ├── controller
│   │   └── InvestigatorNoteController.java
│   ├── dto
│   │   ├── CreateNoteRequest.java
│   │   ├── NoteResponse.java
│   │   └── UpdateNoteRequest.java
│   ├── entity
│   │   └── InvestigatorNote.java
│   ├── repository
│   │   └── InvestigatorNoteRepository.java
│   └── service
│       └── InvestigatorNoteService.java
│
├── report
│   ├── controller
│   │   └── CaseReportController.java
│   ├── dto
│   │   └── CaseReportResponse.java
│   └── service
│       └── CaseReportService.java
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
│   ├── evidence-intelligence-user.st
│   └── evidence-preview-user.st
└── application.properties
```

---

## Database Structure

Current tables include:

```text
users
investigation_cases
evidence_files

ai_evidence_analyses
ai_analysis_findings
ai_analysis_actions
ai_analysis_limitations

evidence_intelligence_runs
extracted_entities
timeline_events
timeline-event/entity association table

investigator_notes
```

### Relationships

```text
User
  ├── owns InvestigationCase
  └── authors InvestigatorNote

InvestigationCase
  ├── contains Evidence
  └── contains InvestigatorNote

Evidence
  ├── has many AiEvidenceAnalysis attempts
  └── has many EvidenceIntelligenceRun attempts

EvidenceIntelligenceRun
  ├── may reference a completed AiEvidenceAnalysis
  ├── contains ExtractedEntity records
  └── contains TimelineEvent records

TimelineEvent
  └── may reference multiple involved ExtractedEntity records
```

### Important Foreign Keys

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

evidence_intelligence_runs.evidence_id
→ evidence_files.id

evidence_intelligence_runs.source_analysis_id
→ ai_evidence_analyses.id

extracted_entities.intelligence_run_id
→ evidence_intelligence_runs.id

timeline_events.intelligence_run_id
→ evidence_intelligence_runs.id

investigator_notes.case_id
→ investigation_cases.id

investigator_notes.author_id
→ users.id
```

### Main Tables

`users` stores user identity, BCrypt password hashes, roles, active status and timestamps.

`investigation_cases` stores case details, priority, status, owner and timestamps.

`evidence_files` stores evidence metadata, integrity values, extracted text, processing status and timestamps.

`ai_evidence_analyses` stores persistent AI analysis output, lifecycle status, provider metadata, source hashes, token usage and timestamps.

`evidence_intelligence_runs` stores intelligence lifecycle state, generation method, source-analysis reference, provider metadata, source hashes, result counts and timestamps.

`extracted_entities` stores normalised entity values, supporting context, confidence, occurrence counts and character offsets for a specific intelligence run.

`timeline_events` stores ordered event descriptions, temporal expressions, normalised date/time values, temporal precision, certainty, context and entity links.

`investigator_notes` stores case notes, authenticated authors, pinned state, optimistic-lock version and timestamps.

Analysis collection tables preserve ordered findings, actions and limitations. Intelligence and note records remain independently queryable and are aggregated only when the final report is requested.

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

Day 9 intelligence limits, prompt/schema versions and validation settings are bound through `IntelligenceExtractionProperties`. Keep these settings in `application.properties` under the intelligence configuration prefix used by the current project.

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

### AI Preview and Persistent Analysis

```http
GET  /api/ai/status
POST /api/ai/evidence/{evidenceId}/preview
POST /api/ai/evidence/{evidenceId}/analyses
POST /api/ai/evidence/{evidenceId}/analyses/regenerate
GET  /api/ai/analyses/{analysisId}
GET  /api/ai/evidence/{evidenceId}/analyses/latest
GET  /api/ai/evidence/{evidenceId}/analyses
```

### Evidence Intelligence

```http
POST /api/intelligence/evidence/{evidenceId}/runs
POST /api/intelligence/evidence/{evidenceId}/runs/regenerate

GET  /api/intelligence/runs/{runId}
GET  /api/intelligence/evidence/{evidenceId}/runs/latest
GET  /api/intelligence/evidence/{evidenceId}/runs
GET  /api/intelligence/runs/{runId}/entities
GET  /api/intelligence/runs/{runId}/timeline
```

Supported intelligence query parameters include:

```text
Run history:
page
size

Entities:
entityType
page
size

Timeline:
certainty
temporalPrecision
page
size
```

### Investigator Notes

```http
POST   /api/cases/{caseId}/notes
GET    /api/cases/{caseId}/notes
PUT    /api/notes/{noteId}
DELETE /api/notes/{noteId}
```

### Final Case Report

```http
GET /api/cases/{caseId}/report
```

### Dashboard Analytics

```http
GET /api/dashboard
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

Maximum analysis-history page size: `50`.

### Generate First Intelligence Run

```http
POST /api/intelligence/evidence/{evidenceId}/runs
Authorization: Bearer <token>
```

The evidence must be owned, integrity-verified and successfully processed.

### Regenerate Intelligence

```http
POST /api/intelligence/evidence/{evidenceId}/runs/regenerate
Authorization: Bearer <token>
```

Creates a new run and preserves previous completed and failed runs.

### Retrieve One Intelligence Run

```http
GET /api/intelligence/runs/{runId}
Authorization: Bearer <token>
```

Returns run metadata, extracted entities and timeline events.

### Retrieve Latest Intelligence Run

```http
GET /api/intelligence/evidence/{evidenceId}/runs/latest
Authorization: Bearer <token>
```

### Retrieve Intelligence History

```http
GET /api/intelligence/evidence/{evidenceId}/runs?page=0&size=10
Authorization: Bearer <token>
```

### Filter Run Entities

```http
GET /api/intelligence/runs/{runId}/entities?entityType=EMAIL_ADDRESS&page=0&size=20
Authorization: Bearer <token>
```

### Filter Timeline Events

```http
GET /api/intelligence/runs/{runId}/timeline?certainty=OBSERVED&temporalPrecision=DATE_TIME&page=0&size=20
Authorization: Bearer <token>
```

### Create Investigator Note

```http
POST /api/cases/{caseId}/notes
Authorization: Bearer <token>
Content-Type: application/json
```

```json
{
  "content": "Priority finding requires independent verification.",
  "pinned": true
}
```

### Update Investigator Note

```http
PUT /api/notes/{noteId}
Authorization: Bearer <token>
Content-Type: application/json
```

```json
{
  "content": "Updated investigator note after document review.",
  "pinned": false
}
```

### List Investigator Notes

```http
GET /api/cases/{caseId}/notes
Authorization: Bearer <token>
```

Pinned notes are returned before unpinned notes.

### Delete Investigator Note

```http
DELETE /api/notes/{noteId}
Authorization: Bearer <token>
```

### Generate Final Case Report

```http
GET /api/cases/{caseId}/report
Authorization: Bearer <token>
```

The report contains:

```text
investigationCase
evidence
analyses
entities
timeline
notes
generatedAt
disclaimer
```

The report is assembled from saved database data and does not invoke the AI provider.

### Retrieve Dashboard Analytics

```http
GET /api/dashboard
Authorization: Bearer <token>
```

Example response:

```json
{
  "success": true,
  "message": "Dashboard analytics retrieved successfully",
  "data": {
    "totalCases": 6,
    "openCases": 2,
    "inProgressCases": 2,
    "completedCases": 1,
    "archivedCases": 1,
    "totalEvidence": 12,
    "processedEvidence": 9,
    "highRiskAnalyses": 3,
    "casesByStatus": [
      {
        "status": "OPEN",
        "count": 2
      },
      {
        "status": "IN_PROGRESS",
        "count": 2
      },
      {
        "status": "COMPLETED",
        "count": 1
      },
      {
        "status": "ARCHIVED",
        "count": 1
      }
    ],
    "casesByPriority": [
      {
        "priority": "LOW",
        "count": 1
      },
      {
        "priority": "MEDIUM",
        "count": 2
      },
      {
        "priority": "HIGH",
        "count": 2
      },
      {
        "priority": "CRITICAL",
        "count": 1
      }
    ],
    "recentlyUpdatedCases": []
  },
  "timestamp": "2026-07-23T11:00:00Z"
}
```

Dashboard rules:

- Every metric is restricted to the authenticated investigator.
- The client does not supply an owner ID.
- The service uses the JWT subject as the normalised owner email.
- The service executes database count queries.
- At most five recently updated cases are returned.
- The endpoint does not invoke Groq or generate new AI output.

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
src/main/resources/prompts/evidence-intelligence-user.st
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

## Evidence Intelligence Lifecycle

### `PENDING`

The intelligence-run record has been created before the longer extraction workflow begins.

### `PROCESSING`

Deterministic extraction, prompt construction, provider processing and validation are in progress.

### `COMPLETED`

Validated entities and timeline events were stored successfully.

### `FAILED`

The attempt failed due to provider, conversion, validation or unexpected processing failure.

Only a safe application-level failure message is stored. Previous runs are preserved.

### Generation Rules

Initial generation:

```http
POST /api/intelligence/evidence/{evidenceId}/runs
```

- Allowed only when no previous intelligence run exists for that evidence.
- Requires owned, processed evidence with a SHA-256 baseline.
- Rechecks integrity before generation.

Regeneration:

```http
POST /api/intelligence/evidence/{evidenceId}/runs/regenerate
```

- Allowed after at least one previous run exists.
- Creates a new run ID.
- Preserves previous completed and failed runs.
- Rejects a second active request while a run is `PENDING` or `PROCESSING`.

---

## Entity and Timeline Model

### Entity Data

Each extracted entity may include:

```text
entityId
entityType
displayValue
normalizedValue
contextSnippet
confidence
occurrenceCount
firstCharacterOffset
lastCharacterOffset
```

Deterministic and AI-assisted candidates are combined using normalised keys so duplicate values are not repeatedly stored within the same run.

### Timeline Data

Each timeline event may include:

```text
eventId
sequenceNumber
title
description
temporalExpression
normalizedDateTime
temporalPrecision
certainty
contextSnippet
involvedEntities
```

Timeline events are returned chronologically where a normalised date/time exists. Undated or unresolved events are placed after dated events in the final report.

---

## Investigator Notes

Notes are manual investigator-authored records and are not generated by the AI provider.

Rules:

- The case must belong to the authenticated investigator.
- The author is resolved from the JWT subject and database user.
- Content must not be blank.
- Content must not exceed 5,000 characters.
- Pinned notes are returned first.
- Newer notes appear first within pinned and unpinned groups.
- Updates use optimistic locking.
- Missing and unowned notes return a safe `404`.

---

## Final Case Report

Endpoint:

```http
GET /api/cases/{caseId}/report
```

The service first verifies case ownership and then aggregates safe response DTOs.

Ordering:

```text
Evidence  → uploadedAt ascending
Analyses  → higher risk first
Entities  → entity type and normalised value
Timeline  → chronological, undated last
Notes     → pinned first, then newest
```

Mandatory disclaimer:

```text
AI-generated findings are investigative aids and must be independently verified.
```

The Day 10 backend report is JSON. Browser printing and presentation belong to the frontend phase; direct PDF generation is optional after the MVP.

The report never exposes:

```text
storagePath
storageRelativePath
passwordHash
JWT claims
accessToken
raw provider credentials
internal exception details
```

---

## Dashboard Analytics Design

Endpoint:

```http
GET /api/dashboard
```

The dashboard is a read-only backend aggregation endpoint. It does not create or modify cases, evidence, analyses or intelligence records.

### Response Fields

```text
totalCases
openCases
inProgressCases
completedCases
archivedCases
totalEvidence
processedEvidence
highRiskAnalyses
casesByStatus
casesByPriority
recentlyUpdatedCases
```

### Aggregation Rules

```text
Case totals
→ Count only cases owned by the authenticated investigator

Evidence totals
→ Count only evidence belonging to the investigator's cases

Processed evidence
→ Count evidence where status = PROCESSED

High-risk analyses
→ Count stored analysis records where riskLevel = HIGH
  through the analysis → evidence → case → owner path

Recently updated cases
→ Maximum 5
→ updatedAt descending
→ id descending as the tie-breaker
```

All status categories and all priority categories are returned even when their count is zero. This provides a stable response shape for frontend cards and charts.

### Efficiency

The dashboard service uses Spring Data count queries and one small paginated recent-case query. It does not retrieve every case, evidence record or analysis record and count them in Java.

### Security

- Authentication is mandatory.
- Ownership comes from the JWT subject.
- The client never sends an owner ID.
- Another investigator's data is excluded at the repository-query level.
- Recent cases are returned through `CaseResponse`, not raw JPA entities.
- The endpoint does not expose evidence text, file paths, password hashes, tokens or AI-provider credentials.

---

## Security Protections

### Authentication

Protected APIs require a valid JWT.

### Ownership

```text
Authenticated user
→ Investigation case
→ Evidence
→ AI analysis
→ Intelligence run
→ Extracted entities and timeline

Authenticated user
→ Investigation case
→ Investigator notes
→ Final report

Authenticated user
→ Dashboard analytics
→ Owned case, evidence and analysis metrics only
```

Repository and service lookups include the ownership path wherever data is retrieved through an external identifier. An unowned identifier is treated the same as a missing identifier.

### Secret Management

Secrets are stored as environment variables and must never be committed.

### Filesystem Safety

- UUID stored filenames
- Normalised paths
- Storage-root boundary checks
- Case-specific directories
- Internal paths hidden from API responses

### Safe DTO Aggregation

The report and dashboard return response DTOs rather than exposing JPA entities directly. This prevents lazy relationships and internal persistence fields from being serialised accidentally.

### Safe Logging

Logs do not intentionally include JWT values, Groq API keys, database passwords, evidence text, provider authorisation headers or complete provider error bodies.

### Mandatory Human Review

Every AI preview, persistent AI analysis and intelligence run keeps human review enabled.

The final report also includes the mandatory independent-verification disclaimer.

---

## Error Handling

TraceLens currently handles:

- Request validation failures with `400`
- Malformed JSON with `400`
- Invalid enum or query values with `400`
- Invalid pagination and sorting with `400`
- Invalid evidence files with `400`
- Evidence-integrity mismatches with `400`
- Unprocessed evidence analysis or intelligence generation with `400`
- Oversized AI or intelligence input with `400`
- Duplicate initial analysis with `400`
- Analysis regeneration without history with `400`
- Concurrent analysis request with `400`
- Duplicate initial intelligence generation with `400`
- Intelligence regeneration without history with `400`
- Concurrent intelligence-run request with `400`
- Blank note content with `400`
- Note content over 5,000 characters with `400`
- Invalid credentials with `401`
- Missing or invalid JWT with `401`
- Disabled account with `403`
- Missing or unowned cases, evidence, analyses, intelligence runs, notes and reports with `404`
- Duplicate data with `409`
- Optimistic or database conflicts with `409`
- Oversized uploads with `413`
- Unprocessable evidence content with `422`
- Invalid structured AI or intelligence output with `502`
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
10. Retrieve paginated analysis history
11. Regenerate the analysis
12. Confirm the previous analysis remains unchanged
13. Confirm only one concurrent analysis request can start
14. Generate the first intelligence run
15. Retrieve the intelligence run by ID
16. Retrieve the latest intelligence run
17. Retrieve paginated intelligence history
18. Filter entities by entity type
19. Filter timeline events by certainty and temporal precision
20. Regenerate intelligence
21. Confirm previous intelligence runs remain unchanged
22. Confirm only one concurrent intelligence request can start
23. Create an unpinned investigator note
24. Create a pinned investigator note
25. Confirm pinned-first ordering
26. Update note content and pin state
27. Confirm blank and oversized notes are rejected
28. Delete a note and confirm a second deletion returns 404
29. Generate the final case report
30. Verify case, evidence, analysis, entity, timeline and note counts
31. Verify timeline order and pinned-note order
32. Verify generatedAt and the exact disclaimer
33. Confirm no storage path, password hash, token or internal field is present
34. Confirm missing and unowned report access returns 404
35. Confirm an unauthenticated report request returns 401
36. Retrieve the authenticated dashboard
37. Verify totalCases equals the sum of all case-status counts
38. Verify totalCases equals the sum of all case-priority counts
39. Verify totalEvidence and processedEvidence against the database
40. Verify recentlyUpdatedCases contains at most five owned cases
41. Verify recentlyUpdatedCases is ordered by updatedAt descending
42. Confirm dashboard retrieval does not call the AI provider
43. Confirm an unauthenticated dashboard request returns 401
```

All protected operations in a complete flow must use the same authenticated owner.

---

## Planned Features

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
- Investigator notes
- Final report screen
- Browser printing with print CSS

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

AI-generated previews, summaries, classifications, findings, recommendations, extracted contextual entities and timeline interpretations must be treated as investigative assistance only.

The exact report warning is:

```text
AI-generated findings are investigative aids and must be independently verified.
```

All AI-assisted output must be independently reviewed and verified before use in legal, disciplinary, financial, employment, compliance or security decisions.

Uploaded test evidence must not contain real confidential, privileged, personal or legally restricted information.

---

## License

This project is licensed under the MIT License.