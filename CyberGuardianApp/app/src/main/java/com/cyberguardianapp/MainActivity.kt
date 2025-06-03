package com.cyberguardianapp

import android.app.AppOpsManager
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Bundle
import android.os.Process
import android.provider.Settings
import android.view.View
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import com.cyberguardianapp.api.ApiClient
import com.cyberguardianapp.model.AppInfo
import com.cyberguardianapp.model.RiskLevel
import com.cyberguardianapp.service.AppMonitorAccessibilityService
import com.cyberguardianapp.service.AppScannerService
import com.cyberguardianapp.util.PermissionChecker
import kotlinx.coroutines.launch
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import android.widget.Button
import android.widget.ProgressBar
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.cyberguardianapp.model.AnalyzeAppRequest
import com.cyberguardianapp.model.RiskResponse
import android.util.Log
import com.cyberguardianapp.analyzer.AppAnalyzer

class MainActivity : AppCompatActivity() {
    
    private lateinit var scanButton: Button
    private lateinit var accessibilityButton: Button
    private lateinit var usageStatsButton: Button
    private lateinit var progressBar: ProgressBar
    private lateinit var statusText: TextView
    private lateinit var appListRecyclerView: RecyclerView
    
    private val appScannerService = AppScannerService()
    private val permissionChecker = PermissionChecker()
    private val appAnalyzer = AppAnalyzer(ApiClient.api)

    // Store the last scanned installed apps for lookup
    private var lastScannedApps: List<AppInfo> = emptyList()
    
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        
        // Initialize views
        scanButton = findViewById(R.id.scanButton)
        accessibilityButton = findViewById(R.id.accessibilityButton)
        usageStatsButton = findViewById(R.id.usageStatsButton)
        progressBar = findViewById(R.id.progressBar)
        statusText = findViewById(R.id.statusText)
        appListRecyclerView = findViewById(R.id.appListRecyclerView)
        
        // Set up RecyclerView with empty list and empty click lambda
        appListRecyclerView.layoutManager = LinearLayoutManager(this)
        appListRecyclerView.adapter = AppListAdapter(emptyList()) { }
        
        // Set up button click listeners
        setupButtonListeners()


        // Check required permissions
        updatePermissionStatus()
    }
    
    private fun setupButtonListeners() {
        scanButton.setOnClickListener {
            if (!hasRequiredPermissions()) {
                Toast.makeText(this, "Please enable all required permissions first", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }
            
            scanInstalledApps()
        }
        
        accessibilityButton.setOnClickListener {
            val intent = Intent(Settings.ACTION_ACCESSIBILITY_SETTINGS)
            startActivity(intent)
        }
        
        usageStatsButton.setOnClickListener {
            val intent = Intent(Settings.ACTION_USAGE_ACCESS_SETTINGS)
            startActivity(intent)
        }
    }
    
    private fun updatePermissionStatus() {
        val accessibilityEnabled = permissionChecker.isAccessibilityServiceEnabled(
            this, 
            AppMonitorAccessibilityService::class.java
        )
        val usageStatsEnabled = hasUsageStatsPermission()
        
        accessibilityButton.isEnabled = !accessibilityEnabled
        usageStatsButton.isEnabled = !usageStatsEnabled
        
        val allPermissionsGranted = accessibilityEnabled && usageStatsEnabled
        scanButton.isEnabled = allPermissionsGranted
        
        statusText.text = if (allPermissionsGranted) {
            "All permissions granted. Ready to scan."
        } else {
            "Please enable required permissions"
        }
    }
    
    private fun hasRequiredPermissions(): Boolean {
        val accessibilityEnabled = permissionChecker.isAccessibilityServiceEnabled(
            this, 
            AppMonitorAccessibilityService::class.java
        )
        val usageStatsEnabled = hasUsageStatsPermission()
        
        return accessibilityEnabled && usageStatsEnabled
    }
    
    private fun hasUsageStatsPermission(): Boolean {
        val appOps = getSystemService(Context.APP_OPS_SERVICE) as AppOpsManager
        val mode = if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.Q) {
            appOps.unsafeCheckOpNoThrow(
                AppOpsManager.OPSTR_GET_USAGE_STATS,
                Process.myUid(), packageName
            )
        } else {
            appOps.checkOpNoThrow(
                AppOpsManager.OPSTR_GET_USAGE_STATS,
                Process.myUid(), packageName
            )
        }
        return mode == AppOpsManager.MODE_ALLOWED
    }
    
    private fun scanInstalledApps() {
        progressBar.visibility = View.VISIBLE
        statusText.text = "Scanning installed applications..."

        lifecycleScope.launch {
            try {
                val installedApps = appScannerService.getInstalledApps(applicationContext)
                lastScannedApps = installedApps // Save for later lookup
                runOnUiThread {
                    statusText.text = "Scan completed. Found ${installedApps.size} apps."
                    updateAppList(installedApps)
                }
                progressBar.visibility = View.GONE
            } catch (e: Exception) {
                progressBar.visibility = View.GONE
                statusText.text = "Error: ${e.message}"
                Toast.makeText(this@MainActivity, "Scan failed: ${e.message}", Toast.LENGTH_SHORT).show()
            }
        }
    }
    
    private fun updateAppList(apps: List<AppInfo>) {
        appListRecyclerView.adapter = AppListAdapter(apps) { appInfo ->
            progressBar.visibility = View.VISIBLE
            lifecycleScope.launch {
                val result = appAnalyzer.analyzeApp(
                    packageName = appInfo.packageName,
                    appName = appInfo.appName,
                    permissions = appInfo.permissions,
                    versionCode = appInfo.versionCode
                )
                progressBar.visibility = View.GONE
                val riskResponse = result.getOrNull()
                if (riskResponse != null) {
                    val intent = Intent(this@MainActivity, AppRiskDetailActivity::class.java)
                    intent.putExtra("app_name", appInfo.appName)
                    intent.putExtra("risk_label", riskResponse.risk_label)
                    intent.putExtra("risk_score", riskResponse.risk_score)
                    intent.putExtra("backend_risk_label", riskResponse.backend_risk_label)
                    intent.putExtra("backend_risk_score", riskResponse.backend_risk_score ?: 0.0)
                    startActivity(intent)
                } else {
                    Toast.makeText(this@MainActivity, "Failed to analyze app", Toast.LENGTH_SHORT).show()
                }
            }
        }
    }
    
    private fun analyzeAppOnBackend() {
        lifecycleScope.launch {
            try {
                val request = AnalyzeAppRequest(
                    package_name = "com.example.app",
                    app_name = "Example App",
                    permissions = listOf("INTERNET", "READ_CONTACTS"),
                    version_code = 1
                )
                val risk = ApiClient.api.analyzeApp(request)
                runOnUiThread {
                    statusText.text = "Risk: ${risk.risk_label}, Score: ${risk.risk_score}"
                }
            } catch (e: Exception) {
                runOnUiThread {
                    statusText.text = "Error: ${e.message}"
                }
            }
        }
    }
    
    override fun onResume() {
        super.onResume()
        updatePermissionStatus()
    }
}
