package com.cyberguardianapp.util

import android.accessibilityservice.AccessibilityService
import android.content.Context
import android.os.Build
import android.provider.Settings
import android.text.TextUtils
import androidx.annotation.RequiresApi
import com.cyberguardianapp.model.AnalyzeAppRequest
import com.cyberguardianapp.model.RiskLevel
import com.cyberguardianapp.model.RiskResponse
import java.time.Instant

class PermissionChecker {

    private val permissionMapper = PermissionMapper

    // Your existing dangerous permissions list
    private val dangerousPermissions = listOf(
        "android.permission.READ_SMS",
        "android.permission.SEND_SMS",
        "android.permission.RECEIVE_SMS",
        "android.permission.CALL_PHONE",
        "android.permission.READ_CONTACTS",
        "android.permission.WRITE_CONTACTS",
        "android.permission.RECORD_AUDIO",
        "android.permission.CAMERA",
        "android.permission.ACCESS_FINE_LOCATION",
        "android.permission.ACCESS_COARSE_LOCATION",
        "android.permission.READ_PHONE_STATE",
        "android.permission.READ_CALL_LOG",
        "android.permission.WRITE_CALL_LOG",
        "android.permission.READ_EXTERNAL_STORAGE",
        "android.permission.WRITE_EXTERNAL_STORAGE",
        "android.permission.BLUETOOTH_CONNECT" // <-- Added new dangerous permission
    )

    /**
     * Prepares permissions for ML model analysis
     */
    fun prepareForModelAnalysis(
        packageName: String,
        appName: String,
        permissions: List<String>,
        versionCode: Int
    ): AnalyzeAppRequest {
        val mappedPermissions = permissionMapper.mapPermissions(permissions)
        return AnalyzeAppRequest(
            package_name = packageName,
            app_name = appName,
            permissions = mappedPermissions, // Using mapped permissions for ML model
            version_code = versionCode
        )
    }

    /**
     * Creates risk response from analysis results
     */
    @RequiresApi(Build.VERSION_CODES.O)
    fun createRiskResponse(
        packageName: String,
        permissions: List<String>
    ): RiskResponse {
        val riskScore = calculatePermissionBasedRiskScore(permissions)
        val riskLevel = determineRiskLevel(riskScore)

        return RiskResponse(
            package_name = packageName,
            risk_score = riskScore,
            risk_label = riskLevel.displayName,
            timestamp = Instant.now().toString()
        )
    }

    /**
     * Determine risk level based on risk score
     */
    fun determineRiskLevel(riskScore: Double): RiskLevel {
        return when {
            riskScore < 0.3 -> RiskLevel.BENIGN
            riskScore < 0.6 -> RiskLevel.SUSPICIOUS
            riskScore <= 1.0 -> RiskLevel.MALICIOUS
            else -> RiskLevel.UNKNOWN
        }
    }

    // Your existing methods remain the same
    fun isDangerousPermission(permission: String): Boolean =
        dangerousPermissions.contains(permission)

    fun isAccessibilityServiceEnabled(
        context: Context,
        serviceClass: Class<out AccessibilityService>
    ): Boolean {
        val expectedServiceName = "${context.packageName}/${serviceClass.name}"
        val enabledServicesSetting = Settings.Secure.getString(
            context.contentResolver,
            Settings.Secure.ENABLED_ACCESSIBILITY_SERVICES
        ) ?: return false

        return TextUtils.SimpleStringSplitter(':').apply {
            setString(enabledServicesSetting)
        }.any { it.equals(expectedServiceName, ignoreCase = true) }
    }

    fun calculatePermissionBasedRiskScore(permissions: List<String>): Double {
        if (permissions.isEmpty()) return 0.0

        val dangerousCount = permissions.count { isDangerousPermission(it) }
        val baseScore = dangerousCount.toDouble() / permissions.size.toDouble()

        val permissionCategories = permissions.groupBy { permission ->
            when {
                permission.contains("INTERNET") -> "INTERNET"
                permission.contains("SMS") -> "SMS"
                permission.contains("LOCATION") -> "LOCATION"
                permission.contains("CAMERA") -> "CAMERA"
                permission.contains("CONTACTS") -> "CONTACTS"
                else -> "OTHER"
            }
        }
        var combinationBonus = 0.0

        // Check for risky combinations
        if (permissionCategories.containsKey("INTERNET")) {
            if (permissionCategories.containsKey("SMS")) combinationBonus += 0.2
            if (permissionCategories.containsKey("LOCATION")) combinationBonus += 0.1
            if (permissionCategories.containsKey("CAMERA")) combinationBonus += 0.15
            if (permissionCategories.containsKey("CONTACTS")) combinationBonus += 0.15
        }

        return (baseScore + combinationBonus).coerceAtMost(1.0)
    }
}
    /**
     * Data class to hold permission analysis results
     */
//    data class AnalyzedPermissions(
//        val originalPermissions: List<String>,
//        val mappedPermissions: List<String>,
//        val riskScore: Double,
//        val riskLevel: RiskLevel
//    )

