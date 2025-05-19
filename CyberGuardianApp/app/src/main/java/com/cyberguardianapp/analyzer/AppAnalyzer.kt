package com.cyberguardianapp.analyzer

import com.cyberguardianapp.api.BackendApi
import com.cyberguardianapp.model.AnalyzeAppRequest
import com.cyberguardianapp.model.RiskResponse
import com.cyberguardianapp.util.PermissionChecker
import retrofit2.Response
import android.util.Log

class AppAnalyzer(
    private val permissionChecker: PermissionChecker,
    private val backendApi: BackendApi
) {

    suspend fun analyzeApp(
        packageName: String,
        appName: String,
        permissions: List<String>,
        versionCode: Int
    ): Result<RiskResponse> {
        return try {
            // 1. Prepare data for ML model
            val modelRequest = permissionChecker.prepareForModelAnalysis(
                packageName = packageName,
                appName = appName,
                permissions = permissions,
                versionCode = versionCode
            )

            // Log the outgoing request for debugging
            Log.d("AppAnalyzer", "Sending to backend: $modelRequest")

            // 2. Send to ML model and get results
            val response = backendApi.analyzeApp(modelRequest).execute()

            if (response.isSuccessful && response.body() != null) {
                Result.success(response.body()!!)
            } else {
                Result.failure(Exception("Analysis failed: ${response.code()}"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}