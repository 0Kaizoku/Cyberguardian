# CyberGuardian API Documentation

## 1. Backend API - Analyze App
- URL: `POST /api/analyzeApp`
- Description: Accepts app scan data from Android App.
- Request Body Example:
```json
{
  "packageName": "com.example.app",
  "appName": "Example App",
  "permissions": [
    "INTERNET",
    "READ_CONTACTS"
  ]
}
