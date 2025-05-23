package com.cyberguardianapp.api

import com.cyberguardianapp.model.AppInfo
import com.cyberguardianapp.model.RiskLevel
import com.google.gson.Gson
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.POST
import retrofit2.http.Path
import java.util.concurrent.TimeUnit

/**
 * Data class for App scan request payload
 */
data class AppScanRequest(
    val package_name: String,
    val app_name: String,
    val permissions: List<String>,
    val version_code: Int
)

/**
 * Example JSON body sent to the backend for app analysis:
 * {
 *   "package_name": "com.example.app",
 *   "app_name": "Example App",
 *   "permissions": [
 *     "android.permission.CAMERA",
 *     "android.permission.READ_CONTACTS"
 *   ],
 *   "version_code": 42
 * }
 */

/**
 * Data class for risk prediction response from the server
 */
data class RiskPredictionResponse(
    val package_name: String,
    val risk_score: Double,
    val risk_label: String,
    val timestamp: String
)

/**
 * Retrofit API interface for backend communication
 */
interface CyberGuardianApi {
    @POST("api/analyzeApp")
    suspend fun analyzeApp(@Body request: AppScanRequest): RiskPredictionResponse
    
    @GET("api/riskReport/{packageName}")
    suspend fun getRiskReport(@Path("packageName") packageName: String): RiskPredictionResponse
    
    @GET("api/apps/risky")
    suspend fun getRiskyApps(): List<RiskPredictionResponse>
}

/**
 * Singleton to manage the API client
 */
/*
* i modified http://10.0.2.2:8080
* */
object ApiClient {
    private const val BASE_URL = "http://10.0.2.2:8081/" // Use this for Android emulator to reach host machine
    
    private val okHttpClient by lazy {
        val logging = HttpLoggingInterceptor().apply {
            level = HttpLoggingInterceptor.Level.BODY
        }
        
        OkHttpClient.Builder()
            .addInterceptor(logging)
            .connectTimeout(30, TimeUnit.SECONDS)
            .readTimeout(30, TimeUnit.SECONDS)
            .writeTimeout(30, TimeUnit.SECONDS)
            .build()
    }
    
    val api: CyberGuardianApi by lazy {
        Retrofit.Builder()
            .baseUrl(BASE_URL)
            .client(okHttpClient)
            .addConverterFactory(GsonConverterFactory.create())
            .build()
            .create(CyberGuardianApi::class.java)
    }
    
    /**
     * Helper function to convert risk label string to RiskLevel enum
     */
    fun mapRiskLabelToRiskLevel(riskLabel: String): RiskLevel {
        return when(riskLabel.lowercase()) {
            "benign" -> RiskLevel.BENIGN
            "suspicious" -> RiskLevel.SUSPICIOUS
            "malicious" -> RiskLevel.MALICIOUS
            else -> RiskLevel.UNKNOWN
        }
    }
}

