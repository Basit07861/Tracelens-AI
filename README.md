# TraceLens AI

TraceLens AI is an AI-powered digital evidence analysis and investigation platform built with Spring Boot, React and MySQL.

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
- Create, edit, pin and delete investigator notes
- Generate aggregated investigation reports
- View owner-restricted dashboard analytics
- Access the platform through a responsive forensic investigation interface

> AI-generated output is investigative assistance only. It is not legal proof, a final conclusion, or a substitute for independent human review.

---

## Current Status

The project implementation is complete through **Day 12** of the development plan.

### Completion Status

```text
Backend MVP                         Complete
Dashboard and analytics backend     Complete
React frontend foundation           Complete
Authentication interface            Complete
Protected routing                   Complete
Live dashboard integration          Complete
Case-management interface           Complete
Evidence and AI workspace           Planned for Day 13
Automated testing and OpenAPI        Planned for Day 14
Deployment and presentation         Planned for Day 15
```

### Implemented So Far

#### Backend

- Spring Boot backend foundation
- MySQL database integration
- Registration and login
- BCrypt password hashing
- JWT authentication
- Stateless Spring Security
- Investigation-case CRUD
- Search, filtering, pagination and sorting
- Owner-restricted case access
- Secure digital-evidence storage
- SHA-256 hashing and integrity verification
- Duplicate evidence detection
- TXT, CSV, JSON and PDF extraction
- Temporary AI previews
- Persistent AI analyses
- AI-analysis history and regeneration
- Persistent evidence-intelligence runs
- Deterministic and AI-assisted entity extraction
- Structured timeline generation
- Investigator-note CRUD
- Aggregated final-case reports
- Dashboard analytics using database aggregation
- Configurable frontend CORS support

#### Frontend

- React frontend created with Vite
- Reusable Axios API client
- JWT request interceptor
- Local authentication-session handling
- Protected routes
- Investigator login page
- Investigator registration page
- Client-side registration validation
- Unique forensic dossier visual design
- Responsive application layout
- Sidebar and mobile navigation
- Owner-restricted live analytics dashboard
- Case-status and priority registers
- Recently updated case files
- Investigation-case listing
- Case search
- Status and priority filters
- Case pagination
- New-case intake form
- Case creation and redirect flow
- Case overview and metadata page
- Loading, empty, error and retry states
- Responsive desktop and mobile styling

---

## Main Features

### Authentication and Security

- User registration
- Email-and-password login
- BCrypt password hashing
- JWT access tokens
- Stateless Spring Security
- Protected backend endpoints
- Protected React routes
- Authenticated user lookup
- Disabled-account handling
- Local JWT-session persistence
- Automatic JWT attachment through Axios
- Automatic local-session cleanup on `401`
- Ownership enforcement for:
  - Investigation cases
  - Evidence
  - AI analyses
  - Intelligence runs
  - Entities
  - Timeline events
  - Investigator notes
  - Final reports
  - Dashboard analytics
- Missing and unowned resources return the same safe not-found response
- Internal filesystem paths and security-sensitive fields are excluded from API DTOs
- Environment-based secret management

### Investigation Cases

- Create, retrieve, update and delete cases
- Unique human-readable case numbers
- Case status management
- Priority management
- Keyword search
- Status filtering
- Priority filtering
- Pagination
- Controlled sorting
- JWT-derived case ownership
- Responsive case-file cards
- Case overview and metadata interface
- New-case intake workflow
- Server and client-side validation

### Digital Evidence

- Multipart evidence upload
- Supported formats:
  - TXT
  - CSV
  - JSON
  - PDF
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
- Integrity states:
  - `NOT_VERIFIED`
  - `VERIFIED`
  - `MISMATCH`
- Original baseline hash is never replaced
- Integrity rechecked before extraction, analysis and intelligence generation

### Evidence Text Extraction

- Strict UTF-8 TXT processing
- CSV parsing with quoted-field support
- JSON flattening using Jackson
- PDF extraction using Apache PDFBox
- Page-by-page PDF processing
- Processing states:
  - `UPLOADED`
  - `PROCESSING`
  - `PROCESSED`
  - `FAILED`
- Character, row, page and nesting limits
- Safe extraction-error persistence
- Extracted-text retrieval endpoint

### AI Integration

- Spring AI `ChatClient`
- Groq through an OpenAI-compatible endpoint
- Configurable AI model
- Resource-based prompt templates
- Structured Java-record responses
- Protected AI-connectivity endpoint
- Prompt-injection resistance instructions
- Application-level output validation
- Provider retry handling
- Semantic correction attempts
- Safe `502` and `503` responses
- Mandatory human review

### Persistent AI Evidence Analysis

- Persistent AI-analysis records in MySQL
- Lifecycle states:
  - `PENDING`
  - `PROCESSING`
  - `COMPLETED`
  - `FAILED`
- Factual summaries
- Preliminary risk levels
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
- Intelligence lifecycle states:
  - `PENDING`
  - `PROCESSING`
  - `COMPLETED`
  - `FAILED`
- Extraction methods:
  - `DETERMINISTIC`
  - `AI`
  - `HYBRID`
- Hybrid deterministic and AI-assisted extraction
- Supported entity types:
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
- Entity normalisation and deduplication
- Original display values
- Supporting context
- Confidence values
- Occurrence counts
- Character offsets
- Timeline titles and descriptions
- Source temporal expressions
- Normalised date and time values
- Temporal precision
- Event certainty
- Links between events and involved entities
- Direct and latest-run retrieval
- Paginated run history
- Filtered entity retrieval
- Filtered timeline retrieval
- Intelligence regeneration
- Previous runs preserved
- Concurrent-run protection

### Investigator Notes

- Persistent notes linked to cases and authors
- Create and list notes
- Edit note content
- Pin and unpin notes
- Delete notes
- Blank-content rejection
- Maximum length of 5,000 characters
- Pinned notes displayed first
- Newest notes displayed first within each group
- Author resolved from the authenticated user
- Optimistic locking
- Safe not-found handling

### Aggregated Case Report

- Secure endpoint:

```http
GET /api/cases/{caseId}/report
```

- Case ownership verification
- Case summary
- Evidence metadata
- Latest completed AI analyses
- Latest completed intelligence output
- Aggregated extracted entities
- Chronologically ordered timeline
- Investigator notes
- Report-generation timestamp
- Mandatory verification disclaimer
- DTO-only output
- No storage paths, password hashes, JWT claims, access tokens or internal exception details

### Dashboard Analytics

- Secure endpoint:

```http
GET /api/dashboard
```

- JWT-authenticated statistics
- Owner-restricted database queries
- Total case count
- Case counts for:
  - `OPEN`
  - `IN_PROGRESS`
  - `COMPLETED`
  - `ARCHIVED`
- Priority breakdown for:
  - `LOW`
  - `MEDIUM`
  - `HIGH`
  - `CRITICAL`
- Total evidence count
- Processed evidence count
- Stored HIGH-risk analysis count
- Up to five recently updated cases
- Database-level aggregation
- No AI-provider request during dashboard retrieval

### React Investigation Interface

- Unique charcoal-and-amber forensic visual identity
- Dossier-inspired login and registration pages
- Investigation command-centre dashboard
- Reusable protected application layout
- Responsive sidebar and mobile menu
- Investigator identity display
- Session termination
- Live dashboard metrics
- Status-distribution register
- Priority-distribution register
- Recently updated case records
- Case register with search and filters
- New-case intake form
- Case-details overview
- Responsive loading, error and empty states

---

## Development Progress

### Day 1 — Backend Foundation

- Initialised the Spring Boot Maven project
- Configured MySQL and Spring Data JPA
- Added environment-based credentials
- Added reusable API-response models
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
- Added case-status update endpoint
- Added search, filtering, pagination and sorting
- Enforced case ownership

### Day 4 — Evidence Management

- Added `Evidence`
- Added upload, listing, metadata, download and delete APIs
- Added TXT, CSV, JSON and PDF upload support
- Added safe file validation and storage
- Added UUID stored filenames
- Added case-specific evidence directories
- Added path-traversal protection
- Added transaction-safe cleanup

### Day 5 — Evidence Integrity

- Added SHA-256 hashing
- Added per-case duplicate detection
- Added integrity state and verification timestamps
- Added integrity-verification endpoint
- Tested evidence modification and restoration
- Preserved the original hash baseline

### Day 6 — Evidence Text Extraction

- Added extracted-text persistence
- Added processing states and timestamps
- Added extractor-strategy architecture
- Added TXT, CSV, JSON and PDF extractors
- Added extraction safety limits
- Added `422` handling for unprocessable content
- Added integrity verification before extraction

### Day 7 — Spring AI and Groq

- Added Spring AI
- Connected Groq through the OpenAI-compatible starter
- Added configurable provider and model settings
- Created a reusable `ChatClient`
- Added a protected AI-connectivity endpoint
- Added resource-based prompt templates
- Added structured AI-preview responses
- Added risk classification
- Added prompt-injection resistance
- Added response validation and semantic retry
- Added safe provider-error handling
- Kept preview results non-persistent

### Day 8 — Persistent AI Analysis

- Added persistent AI-analysis tables
- Added analysis lifecycle states
- Added summaries, risks, findings, actions and limitations
- Added provider, model, prompt and schema metadata
- Added source hashes
- Added token usage when available
- Added safe failure persistence
- Added initial analysis generation
- Added direct and latest-analysis retrieval
- Added paginated history
- Added regeneration
- Preserved previous attempts
- Added pessimistic locking
- Prevented simultaneous analysis generation

### Day 9 — Evidence Intelligence and Timeline

- Added persistent intelligence runs
- Added intelligence lifecycle and method enums
- Added deterministic entity extraction
- Added structured AI-assisted extraction
- Combined results using a hybrid workflow
- Added entity normalisation and deduplication
- Added context, confidence, occurrence counts and offsets
- Added timeline descriptions and normalised dates
- Added certainty and temporal precision
- Linked events to entities
- Added direct, latest and history retrieval
- Added paginated entity and timeline APIs
- Added regeneration
- Preserved earlier runs
- Added concurrent-run protection
- Enforced ownership throughout the workflow

### Day 10 — Investigator Notes and Final Report

- Added note persistence
- Linked notes to cases and authenticated authors
- Added note creation, listing, updating and deletion
- Added pinning
- Added validation
- Added optimistic locking
- Added secure repository queries
- Added final-report DTOs
- Added final-report service and endpoint
- Aggregated case, evidence, analyses, entities, timeline and notes
- Added deterministic report ordering
- Added generation timestamp
- Added mandatory verification disclaimer
- Completed the backend MVP

### Day 11 — Dashboard and Analytics APIs

- Added dashboard response DTOs
- Added owner-restricted count queries
- Added case-status counts
- Added case-priority counts
- Added total and processed evidence counts
- Added HIGH-risk analysis count
- Added recently updated case retrieval
- Added `DashboardService`
- Added `DashboardController`
- Added authenticated `GET /api/dashboard`
- Used the JWT subject as owner identity
- Returned zero-count categories
- Limited recent cases to five
- Used MySQL aggregation queries
- Verified unauthenticated access returns `401`

### Day 12 — React Frontend Foundation

- Created the React frontend using Vite
- Added Axios and React Router
- Added `.env.example`
- Ignored local frontend environment files
- Added a reusable Axios API client
- Added JWT request interception
- Added unauthorised-session cleanup
- Added authentication context
- Added local user and token storage
- Added protected routes
- Added investigator login
- Added investigator registration
- Added client-side password validation
- Added registration success and API-error messages
- Added show/hide password controls
- Added a unique forensic dossier design
- Added the reusable TraceLens mark
- Added responsive authentication pages
- Added protected application layout
- Added responsive sidebar navigation
- Added mobile-menu handling
- Added session termination
- Connected the dashboard to `GET /api/dashboard`
- Added live case and evidence metrics
- Added status and priority registers
- Added recently updated case records
- Added dashboard loading, error and retry states
- Added Spring Security CORS configuration
- Allowed the local Vite development origin
- Added configurable production frontend origins
- Added browser preflight handling
- Added case listing through `GET /api/cases`
- Added keyword search
- Added status and priority filters
- Added pagination
- Added responsive case-file cards
- Added new-case intake
- Added case creation through `POST /api/cases`
- Added case-details overview through `GET /api/cases/{caseId}`
- Added metadata and future Day 13 tabs
- Verified login and dashboard integration
- Verified case creation from the browser
- Verified ESLint
- Verified the frontend production build

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

### Frontend

- React
- Vite
- React Router
- Axios
- JavaScript
- JSX
- CSS
- Browser `localStorage`
- Responsive forensic interface

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
- Visual Studio Code
- MySQL Workbench
- Windows PowerShell
- Git
- GitHub
- GitHub Desktop

---

## Architecture

```text
React Frontend
    │
    │ HTTPS / JSON / Multipart
    │ Authorization: Bearer JWT
    ▼
Spring Boot REST API
    │
    ├── Authentication and Security
    ├── Investigation Cases
    ├── Evidence Management
    ├── Integrity Verification
    ├── Text Extraction
    ├── AI Analysis
    ├── Entity and Timeline Intelligence
    ├── Investigator Notes
    ├── Reports
    └── Dashboard Analytics
          │
          ├── MySQL
          ├── Evidence Storage
          └── Groq through Spring AI
```

---

## Project Structure

```text
TraceLens-AI
├── src
│   └── main
│       ├── java
│       │   └── com
│       │       └── tracelens
│       │           ├── ai
│       │           ├── auth
│       │           ├── common
│       │           ├── dashboard
│       │           ├── evidence
│       │           ├── exception
│       │           ├── intelligence
│       │           ├── investigation
│       │           ├── note
│       │           ├── report
│       │           ├── security
│       │           ├── system
│       │           ├── user
│       │           └── TracelensBackendApplication.java
│       │
│       └── resources
│           ├── prompts
│           │   ├── evidence-analysis-user.st
│           │   ├── evidence-intelligence-user.st
│           │   └── evidence-preview-user.st
│           └── application.properties
│
├── frontend
│   ├── public
│   ├── src
│   │   ├── api
│   │   │   └── client.js
│   │   ├── components
│   │   │   ├── Layout.jsx
│   │   │   ├── ProtectedRoute.jsx
│   │   │   ├── TraceLensMark.jsx
│   │   │   ├── case
│   │   │   └── common
│   │   ├── context
│   │   │   ├── AuthContext.jsx
│   │   │   ├── auth-context.js
│   │   │   └── useAuth.js
│   │   ├── pages
│   │   │   ├── CaseDetailsPage.jsx
│   │   │   ├── CasesPage.css
│   │   │   ├── CasesPage.jsx
│   │   │   ├── DashboardPage.css
│   │   │   ├── DashboardPage.jsx
│   │   │   ├── LoginPage.jsx
│   │   │   ├── NewCasePage.jsx
│   │   │   └── RegisterPage.jsx
│   │   ├── App.jsx
│   │   ├── index.css
│   │   └── main.jsx
│   ├── .env.example
│   ├── eslint.config.js
│   ├── index.html
│   ├── package.json
│   └── vite.config.js
│
├── evidence-storage
├── pom.xml
├── README.md
└── LICENSE
```

The following directories and files must remain excluded from Git:

```text
target/
evidence-storage/
frontend/node_modules/
frontend/dist/
frontend/.env
frontend/.env.local
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

### Main Relationships

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
  ├── may reference an AiEvidenceAnalysis
  ├── contains ExtractedEntity records
  └── contains TimelineEvent records

TimelineEvent
  └── may reference multiple ExtractedEntity records
```

---

## Prerequisites

Install:

- Java 17 or a compatible newer runtime
- Maven
- MySQL 8
- Node.js and npm
- Eclipse or Spring Tools
- Visual Studio Code
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

Hibernate creates and updates the required tables when the backend starts.

---

## Backend Environment Variables

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
FRONTEND_URL=http://localhost:5173
```

Multiple allowed frontend origins may be provided as comma-separated values:

```text
FRONTEND_URL=http://localhost:5173,https://your-frontend-domain.example
```

Do not add a trailing slash to an allowed frontend origin.

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

## Frontend Environment

The committed example file is:

```text
frontend/.env.example
```

Contents:

```env
VITE_API_BASE_URL=http://localhost:8080
```

Create a local file:

```text
frontend/.env
```

Contents:

```env
VITE_API_BASE_URL=http://localhost:8080
```

The real `.env` file must not be committed.

---

## Running the Backend

Start MySQL first.

Run:

```text
TracelensBackendApplication.java
→ Run As
→ Spring Boot App
```

Backend URL:

```text
http://localhost:8080
```

Public status endpoint:

```text
http://localhost:8080/api/system/status
```

Successful startup should contain:

```text
HikariPool-1 - Start completed
Evidence storage initialized at ...
Tomcat started on port 8080
Started TracelensBackendApplication
```

---

## Running the Frontend

Open a terminal from the repository root:

```powershell
cd frontend
npm install
npm run dev
```

Frontend URL:

```text
http://localhost:5173
```

Main routes:

```text
/login
/register
/dashboard
/cases
/cases/new
/cases/{caseId}
```

The frontend and backend must both remain running during local development.

---

## Frontend Quality Checks

From the `frontend` directory:

```powershell
npm run lint
npm run build
```

The production build is created in:

```text
frontend/dist
```

The `dist` directory must not be committed.

---

## Authentication Flow

```text
Register investigator
→ Password hashed using BCrypt
→ User stored in MySQL
→ Investigator logs in
→ Backend generates JWT
→ Frontend stores token and safe user data locally
→ Axios adds Authorization header
→ Protected routes become available
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

## Frontend Route Protection

Public routes:

```text
/login
/register
```

Protected routes:

```text
/dashboard
/cases
/cases/new
/cases/{caseId}
```

When an unauthenticated user opens a protected route, the application redirects to:

```text
/login
```

When the backend returns `401`, the frontend removes:

```text
tracelens_token
tracelens_user
```

---

## CORS Configuration

The backend allows the configured frontend origin to call `/api/**`.

Development origin:

```text
http://localhost:5173
```

Allowed methods:

```text
GET
POST
PUT
PATCH
DELETE
OPTIONS
```

Allowed headers:

```text
Authorization
Content-Type
```

Authentication uses a Bearer token rather than browser cookies.

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

## Case Search Parameters

Example:

```http
GET /api/cases?keyword=invoice&status=IN_PROGRESS&priority=HIGH&page=0&size=10&sortBy=updatedAt&sortDirection=desc
```

Supported filters include:

```text
keyword
status
priority
page
size
sortBy
sortDirection
```

---

## Frontend Case Workflow

```text
Login
→ Open investigation case register
→ Search or filter cases
→ Create a new case
→ Receive generated case number
→ Redirect to case details
→ Review case overview and metadata
```

The frontend sends only:

```json
{
  "title": "Suspicious Invoice Investigation",
  "description": "Investigate possible invoice manipulation.",
  "priority": "HIGH"
}
```

The backend derives ownership from the JWT subject.

---

## Evidence Storage

Default structure:

```text
evidence-storage
├── case-5
│   ├── generated-uuid.txt
│   ├── generated-uuid.pdf
│   └── generated-uuid.json
└── case-7
    └── generated-uuid.csv
```

Original filenames are stored as metadata but are not used as physical filenames.

The storage directory must remain excluded from Git.

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

Text extraction, AI analysis and intelligence generation are blocked when an integrity mismatch is detected.

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
Fixed system instructions
+
Resource-based user prompt
+
Untrusted evidence boundaries
+
Structured Java-record conversion
+
Semantic validation
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
- Mandatory independent review

Prompt controls reduce risk but cannot guarantee complete immunity from adversarial model inputs.

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
→ Intelligence
→ Notes
→ Report
→ Dashboard
```

Ownership is derived from the JWT subject. The client never submits an owner ID.

### Secret Management

Secrets are stored through environment variables and must never be committed.

### Filesystem Safety

- UUID stored filenames
- Normalised paths
- Storage-root boundary checks
- Case-specific directories
- Internal paths hidden from API responses

### Safe DTO Output

Controllers return response DTOs rather than JPA entities.

### Safe Logging

Logs do not intentionally include:

- JWT values
- Groq API keys
- Database passwords
- Complete evidence text
- Provider authorisation headers
- Complete provider error bodies

### Mandatory Human Review

Every AI preview, persistent analysis and intelligence run requires independent human review.

---

## Error Handling

TraceLens handles:

- Validation failures with `400`
- Malformed JSON with `400`
- Invalid enums and query values with `400`
- Invalid pagination and sorting with `400`
- Invalid evidence files with `400`
- Integrity mismatches with `400`
- Unprocessed evidence operations with `400`
- Duplicate generation requests with `400`
- Concurrent analysis or intelligence requests with `400`
- Blank and oversized notes with `400`
- Invalid credentials with `401`
- Missing or invalid JWT with `401`
- Disabled accounts with `403`
- Missing and unowned resources with `404`
- Duplicate data with `409`
- Optimistic-lock and database conflicts with `409`
- Oversized uploads with `413`
- Unprocessable evidence with `422`
- Invalid structured AI output with `502`
- Unavailable AI provider with `503`
- Storage failures with `500`
- Unexpected application failures with `500`

The React frontend displays safe backend messages and provides loading, retry and empty states.

---

## Verified Day 12 Browser Flow

```text
Start MySQL
→ Start Spring Boot backend
→ Start Vite frontend
→ Open /login
→ Sign in
→ Open protected dashboard
→ Load live owner-restricted analytics
→ Open investigation cases
→ Search and filter case records
→ Create a new investigation case
→ Redirect to the generated case-details URL
→ Confirm status, priority, owner and timestamps
→ Return to case register
→ Terminate session
→ Confirm protected routes redirect to login
```

Frontend checks completed:

```text
npm run lint
npm run build
```

---

## Planned Features

### Day 13 — Evidence and AI Investigation Interface

- Activate case-details tabs
- Evidence upload interface
- Evidence register
- Download and delete controls
- Integrity verification
- Text extraction
- Extracted-text viewer
- AI-analysis generation
- AI-analysis history and regeneration
- Risk and findings interface
- Entity interface
- Timeline interface
- Investigator-note interface
- Final-report screen
- Browser printing with print CSS
- Complete full workflow without PowerShell

### Day 14 — Testing, OpenAPI and Hardening

- Automated service tests
- Security integration tests
- Evidence validation tests
- SHA-256 tests
- OpenAPI documentation
- Swagger UI
- Actuator health monitoring
- Security audit
- Ownership audit
- Backend production package verification
- Frontend production-build verification

### Day 15 — Deployment and Presentation

- Backend deployment
- Frontend deployment
- Cloud MySQL configuration
- Persistent evidence storage
- Production CORS
- SPA route rewriting
- Live demonstration workflow
- GitHub repository polish
- Architecture and database diagrams
- Project screenshots
- Resume description
- LinkedIn description
- Interview questions and explanations

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
frontend/.env
frontend/.env.local
frontend/node_modules/
frontend/dist/
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