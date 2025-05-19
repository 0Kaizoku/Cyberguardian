# CyberGuardian - Mobile App Security Scanner

CyberGuardian is a security monitoring solution for Android that analyzes installed applications for suspicious permissions and behaviors, protecting your personal data and privacy.

## Project Components

This project consists of three main components:

1. **Spring Boot Backend**: REST API for app analysis and risk persistence (located in `/cyberguardian-backend`)
2. **FastAPI ML Service**: Machine learning microservice for risk prediction
3. **Android App**: Scans installed apps and monitors behavior

## Important Note About Project Structure

> **IMPORTANT**: This project uses a microservices architecture. The main backend implementation is in the `/cyberguardian-backend` directory. The root `/src` directory contains legacy code that is being phased out. For all development, use the `/cyberguardian-backend` directory.

## Setup Instructions

### Prerequisites

- JDK 21
- Maven 3.6+
- Python 3.9+
- PostgreSQL 14+
- Android Studio (Arctic Fox or newer)

### 1. Database Setup

1. Install PostgreSQL and create a database named `cyberguardian`.
2. Run the schema creation script:
   ```
   psql -U your_username -d cyberguardian -f schema.sql
   ```

### 2. FastAPI ML Service Setup

1. Navigate to the ML service directory:
   ```
   cd cyberguardian-ml-service
   ```

2. Create a virtual environment and install dependencies:
   ```
   python -m venv venv
   source venv/bin/activate  # On Windows: venv\Scripts\activate
   pip install -r requirements.txt
   ```

3. Start the ML service:
   ```
   uvicorn main:app --reload --host 0.0.0.0 --port 8000
   ```

### 3. Spring Boot Backend Setup

1. Navigate to the backend directory:
   ```
   cd cyberguardian-backend
   ```

2. Configure database connection:
   - Edit `src/main/resources/application.properties` with your PostgreSQL credentials

3. Build and run the Spring Boot app:
   ```
   ./mvnw spring-boot:run
   ```

### 4. Android App Setup

1. Open the CyberGuardianApp directory in Android Studio

2. Update API endpoint:
   - Navigate to `app/src/main/java/com/cyberguardianapp/api/ApiClient.kt`
   - Update the `BASE_URL` to point to your Spring Boot backend

3. Build and run the app on an emulator or physical device
   - Make sure to grant all required permissions when prompted

## Testing the API

A Postman collection is included for testing the API endpoints:

1. Import `CyberGuardian.postman_collection.json` into Postman
2. Use the included collection to test the following endpoints:
   - POST /api/analyzeApp - Analyze an app for security risks
   - GET /api/riskReport/{packageName} - Get risk report for a specific app
   - GET /api/apps/risky - Get all risky applications

## Architecture

For a visual representation of the system architecture, see `Documentation/Architecture_Diagram.md`.

## API Documentation

For detailed API documentation, see `Documentation/API_Documentation.md`.

## Model Details

CyberGuardian uses XGBoost for risk prediction based on app permissions and behavior. The model is trained to detect:

1. Permission-based risks (static analysis)
2. Behavioral anomalies (dynamic analysis)

The pretrained model is included in the project as `cyberguardian-ml-service/model.pkl`.
