# TraceLens AI

TraceLens AI is an AI-powered digital evidence analysis and investigation platform built using Spring Boot.

The platform is designed to help authorised investigators organise digital evidence, extract useful information, identify suspicious patterns, generate chronological timelines, and prepare structured investigation reports.

## Current Development Status

### Day 1 — Backend Foundation

- Spring Boot project initialised
- MySQL database configured
- Spring Data JPA and Hibernate configured
- Environment-based database credentials added
- Reusable API response structure implemented
- System-status REST API created
- Live database connectivity verification implemented
- Global REST exception handling added

## Technology Stack

- Java 17 compatible
- Spring Boot 4.1.0
- Spring Web MVC
- Spring Data JPA
- Hibernate
- MySQL
- Maven

## Project Structure

```text
com.tracelens
├── common
│   └── ApiResponse.java
├── exception
│   ├── ErrorResponse.java
│   └── GlobalExceptionHandler.java
├── system
│   ├── controller
│   │   └── SystemStatusController.java
│   └── service
│       └── SystemStatusService.java
└── TracelensBackendApplication.java