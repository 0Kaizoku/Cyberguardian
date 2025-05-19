package com.cyberguardianapp.service

import android.content.Context
import android.content.pm.ApplicationInfo
import android.content.pm.PackageManager
import com.cyberguardianapp.api.ApiClient
import com.cyberguardianapp.api.AppScanRequest
import com.cyberguardianapp.model.AppInfo
import com.cyberguardianapp.util.PermissionChecker
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
//import java.util.jar.Manifest

/**
 * Service responsible for scanning installed applications and analyzing them for security risks
 */
class AppScannerService {
    
    private val permissionChecker = PermissionChecker()


//    fun getAllAndroidPermissions(): List<String> {
//        return Manifest.permission::class.java.fields.map { it.name }
//    }
    /**
     * Get all installed apps on the device with their permissions
     */
    suspend fun getInstalledApps(context: Context): List<AppInfo> = withContext(Dispatchers.IO) {
        val packageManager = context.packageManager
        val installedApps = packageManager.getInstalledApplications(PackageManager.GET_META_DATA)
        
        installedApps
            .filter { !isSystemApp(it) } // Exclude system apps
            .map { app ->
                val packageInfo = packageManager.getPackageInfo(
                    app.packageName,
                    PackageManager.GET_PERMISSIONS or PackageManager.GET_META_DATA
                )
                
                // Get app permissions
                val permissions = packageInfo.requestedPermissions?.toList() ?: emptyList()

                // Log the permissions for each app
                println("App: ${app.packageName}, Permissions: $permissions")

                AppInfo(
                    packageName = app.packageName,
                    appName = packageManager.getApplicationLabel(app).toString(),
                    versionCode = packageInfo.versionCode,
                    versionName = packageInfo.versionName ?: "",
                    appIcon = packageManager.getApplicationIcon(app.packageName),
                    installTime = packageInfo.firstInstallTime,
                    updateTime = packageInfo.lastUpdateTime,
                    permissions = permissions
                )
            }
    }
    
    /**
     * Check if app is a system app
     */
    private fun isSystemApp(appInfo: ApplicationInfo): Boolean {
        return (appInfo.flags and ApplicationInfo.FLAG_SYSTEM) != 0
    }
    
    /**
     * Analyze apps by checking permissions locally and sending to backend for ML-based detection
     */
    suspend fun analyzeApps(apps: List<AppInfo>): List<AppInfo> = withContext(Dispatchers.IO) {
        apps.map { app ->
            try {
                // First, perform local static analysis
                val staticRiskScore = calculateStaticRiskScore(app.permissions)
                
                // Then, call backend for ML-based prediction
                val scanRequest = AppScanRequest(
                    package_name = app.packageName,
                    app_name = app.appName,
                    permissions = app.permissions,
                    version_code = app.versionCode
                )
                
                val response = ApiClient.api.analyzeApp(scanRequest)
                app.riskScore = response.risk_score
                app.riskLevel = ApiClient.mapRiskLabelToRiskLevel(response.risk_label)
                app
            } catch (e: Exception) {
                // If API call fails, use only static analysis
                var staticRiskScore = 0.0
                app.riskScore = staticRiskScore
                app.riskLevel = getRiskLevelFromScore(staticRiskScore)
                app
            }
        }
    }
    
    /**
     * Calculate static risk score based on dangerous permissions
     */
    private fun calculateStaticRiskScore(permissions: List<String>): Double {
        val dangerousPermissions = permissions.count { permission ->
            permissionChecker.isDangerousPermission(permission)
        }
        
        // Simple heuristic: risk score between 0.0 - 1.0 based on dangerous permissions count
        return minOf(1.0, dangerousPermissions / 10.0)
    }
    
    /**
     * Map a risk score to a RiskLevel enum
     */
    private fun getRiskLevelFromScore(score: Double): com.cyberguardianapp.model.RiskLevel {
        return when {
            score < 0.3 -> com.cyberguardianapp.model.RiskLevel.BENIGN
            score < 0.7 -> com.cyberguardianapp.model.RiskLevel.SUSPICIOUS
            else -> com.cyberguardianapp.model.RiskLevel.MALICIOUS
        }
    }
}
