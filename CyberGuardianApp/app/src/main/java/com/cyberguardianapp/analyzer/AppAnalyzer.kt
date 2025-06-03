package com.cyberguardianapp.analyzer

import com.cyberguardianapp.api.CyberGuardianApi
import com.cyberguardianapp.model.AnalyzeAppRequest
import com.cyberguardianapp.model.RiskResponse
import android.util.Log

class AppAnalyzer(
    private val api: CyberGuardianApi // Make sure to pass ApiClient.api here!
) {
    // Call this from a coroutine scope!
    suspend fun analyzeApp(
        packageName: String,
        appName: String,
        permissions: List<String>,
        versionCode: Int
    ): Result<RiskResponse> {
        return try {
            val modelRequest = AnalyzeAppRequest(
                package_name = packageName,
                app_name = appName,
                permissions = permissions,
                version_code = versionCode
            )
            Log.d("AppAnalyzer", "Sending to backend: $modelRequest")
            val response = api.analyzeApp(modelRequest) // suspend function, returns RiskResponse
            Log.d("AppAnalyzer", "Data sent successfully: $response")
            Result.success(response)
        } catch (e: Exception) {
            Log.e("AppAnalyzer", "Failed to send data: ${e.message}", e)
            Result.failure(e)
        }
    }
}

// Usage example (in Activity/Fragment/ViewModel):
// val appAnalyzer = AppAnalyzer(ApiClient.api)
// lifecycleScope.launch { appAnalyzer.analyzeApp(...) }
