package com.cyberguardianapp.service

import android.content.Context
import android.content.pm.ApplicationInfo
import android.content.pm.PackageManager
import com.cyberguardianapp.api.ApiClient
import com.cyberguardianapp.api.AppScanRequest
import com.cyberguardianapp.model.AppInfo
import com.cyberguardianapp.util.PermissionChecker
import com.google.gson.Gson
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

/**
 * Service responsible for scanning installed applications and analyzing them for security risks
 */
class AppScannerService {
    
    private val permissionChecker = PermissionChecker()

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
                // First, perform local static analysis using PermissionChecker
                val staticRiskScore = permissionChecker.calculatePermissionBasedRiskScore(app.permissions)
                
                // Then, call backend for ML-based prediction
                val scanRequest = AppScanRequest(
                    package_name = app.packageName,
                    app_name = app.appName,
                    permissions = app.permissions,
                    version_code = app.versionCode
                )
                
                // Log the JSON body being sent
                val jsonBody = Gson().toJson(scanRequest)
                println("Sending scan request JSON: $jsonBody")

                // Send request and log response
                val response = ApiClient.api.analyzeApp(scanRequest)
                println("Received backend response: $response")
                app.riskScore = response.risk_score
                app.riskLevel = ApiClient.mapRiskLabelToRiskLevel(response.risk_label)
                app
            } catch (e: Exception) {
                // Log the error
                println("Error sending scan request for ${app.packageName}: ${e.message}")
                // If API call fails, use only static analysis
                val staticRiskScore = permissionChecker.calculatePermissionBasedRiskScore(app.permissions)
                app.riskScore = staticRiskScore
                app.riskLevel = permissionChecker.determineRiskLevel(staticRiskScore)
                app
            }
        }
    }
}
