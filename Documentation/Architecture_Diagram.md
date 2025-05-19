# CyberGuardian Architecture Diagram

## System Architecture

```
┌─────────────────┐        ┌────────────────────┐        ┌───────────────────┐
│  CyberGuardian  │        │   Spring Boot      │        │  FastAPI          │
│  Android App    │───────▶│   Backend          │───────▶│  ML Service       │
└─────────────────┘        └────────────────────┘        └───────────────────┘
        │                           │                             │
        │                           ▼                             │
        │                  ┌────────────────────┐                │
        │                  │   PostgreSQL       │                │
        └─────────────────▶│   Database         │◀───────────────┘
                           └────────────────────┘


```

## Component Details

### 1. CyberGuardian Android App
- **Purpose**: Mobile application that scans installed apps and monitors behavior
- **Key Features**:
  - Permission scanner (static detection)
  - Accessibility service (dynamic behavior monitoring)
  - UI for displaying risk assessment
- **Technologies**: Kotlin, Android SDK 33, Retrofit for API communication

### 2. Spring Boot Backend
- **Purpose**: REST API service for app analysis and risk score persistence
- **Key Features**:
  - Analyze app data received from Android client
  - Communicate with ML service for risk prediction
  - Store analysis results in PostgreSQL
- **Technologies**: Java 21, Spring Boot 3.2+, Spring Data JPA

### 3. FastAPI ML Service
- **Purpose**: Machine learning microservice for risk prediction
- **Key Features**:
  - XGBoost model for app risk prediction
  - Feature engineering from app permissions
  - VirusTotal API integration (placeholder)
- **Technologies**: Python, FastAPI, XGBoost, scikit-learn

### 4. PostgreSQL Database
- **Purpose**: Persistent storage for app data and risk assessments
- **Key Features**:
  - Store app details, permissions, and risk predictions
  - Support for querying historical risk data
- **Tables**: applications, permissions, risk_predictions

## API Flow

1. **Android App → Spring Boot Backend**:
   - POST /api/analyzeApp (send app data including permissions)
   - GET /api/riskReport/{packageName} (retrieve risk assessment)

2. **Spring Boot Backend → FastAPI ML Service**:
   - POST /predict (forward app data for ML prediction)

3. **FastAPI → VirusTotal API** (Future enhancement):
   - Verify app reputation using SHA256 hash

## Detection Methods

1. **Static Detection**:
   - Permission analysis (dangerous permissions)
   - Package name verification
   - Feature engineering

2. **Dynamic Detection**:
   - Accessibility service monitoring
   - Runtime behavior analysis
   - Suspicious activity detection

## Extensions (Future)

1. **Real-time Alerts**:
   - Push notifications for suspicious activities

2. **Enhanced VirusTotal Integration**:
   - Pre-install app verification

3. **Machine Learning Enhancements**:
   - Retraining with user feedback
   - Additional model types (Isolation Forest, Deep Learning)
