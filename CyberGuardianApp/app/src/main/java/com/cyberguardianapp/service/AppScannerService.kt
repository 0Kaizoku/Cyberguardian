package com.cyberguardianapp.service

import android.content.Context
import android.content.pm.ApplicationInfo
import android.content.pm.PackageManager
import com.cyberguardianapp.api.ApiClient
//import com.cyberguardianapp.api.AppScanRequest
import com.cyberguardianapp.model.AppInfo
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

/**
 * Service responsible for scanning installed applications and analyzing them for security risks
 */
class AppScannerService {
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
}
