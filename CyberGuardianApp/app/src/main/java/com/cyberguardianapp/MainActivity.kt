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
import com.cyberguardianapp.api.BackendApi
import com.cyberguardianapp.model.AnalyzeAppRequest
import com.cyberguardianapp.model.RiskResponse
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
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
    private val backendApi = Retrofit.Builder()
        .baseUrl("http://10.0.2.2:8081/")
        .addConverterFactory(GsonConverterFactory.create())
        .build()
        .create(BackendApi::class.java)
    
    private val appAnalyzer = AppAnalyzer(permissionChecker, backendApi)

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
        
        // Set up RecyclerView
        appListRecyclerView.layoutManager = LinearLayoutManager(this)
        appListRecyclerView.adapter = AppListAdapter(emptyList())
        
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

                // Launch all backend calls concurrently using async
                val backendResults = installedApps.map { app ->
                    async {
                        appAnalyzer.analyzeApp(
                            packageName = app.packageName,
                            appName = app.appName,
                            permissions = app.permissions,
                            versionCode = app.versionCode
                        ).getOrNull()
                    }
                }.awaitAll().filterNotNull()

                runOnUiThread {
                    statusText.text = "Scan completed. Found ${backendResults.size} apps analyzed."
                    updateAppListWithBackendResults(backendResults)
                }
            } catch (e: Exception) {
                progressBar.visibility = View.GONE
                statusText.text = "Error: ${e.message}"
                Toast.makeText(this@MainActivity, "Scan failed: ${e.message}", Toast.LENGTH_SHORT).show()
            }
        }
    }
    
    private fun updateAppList(apps: List<AppInfo>) {
        appListRecyclerView.adapter = AppListAdapter(apps)
    }
    
    private fun updateAppListWithBackendResults(backendResults: List<RiskResponse>) {
        // Map packageName to AppInfo for quick lookup
        val appInfoMap = lastScannedApps.associateBy { it.packageName }
        val appInfoList = backendResults.mapNotNull { riskResponse ->
            val appInfo = appInfoMap[riskResponse.package_name]
            if (appInfo != null) {
                AppInfo(
                    packageName = riskResponse.package_name,
                    appName = appInfo.appName,
                    versionCode = appInfo.versionCode,
                    versionName = appInfo.versionName,
                    appIcon = appInfo.appIcon,
                    installTime = appInfo.installTime,
                    updateTime = appInfo.updateTime,
                    permissions = appInfo.permissions,
                    riskLevel = try {
                        RiskLevel.valueOf(riskResponse.risk_label.uppercase())
                    } catch (e: Exception) {
                        RiskLevel.UNKNOWN // Fallback if label doesn't match enum
                    },
                    riskScore = riskResponse.risk_score
                )
            } else {
                null // Skip if not found
            }
        }
        appListRecyclerView.adapter = AppListAdapter(appInfoList)
    }
    
    private fun analyzeAppOnBackend() {
        val retrofit = Retrofit.Builder()
            .baseUrl("http://10.0.2.2:8081/") // Use your backend's IP if on a real device
            .addConverterFactory(GsonConverterFactory.create())
            .build()

        val backendApi = retrofit.create(BackendApi::class.java)

        val request = AnalyzeAppRequest(
            package_name = "com.example.app",
            app_name = "Example App",
            permissions = listOf("INTERNET", "READ_CONTACTS"),
            version_code = 1
        )

        backendApi.analyzeApp(request).enqueue(object : Callback<RiskResponse> {
            override fun onResponse(call: Call<RiskResponse>, response: Response<RiskResponse>) {
                if (response.isSuccessful && response.body() != null) {
                    val risk = response.body()!!
                    runOnUiThread {
                        statusText.text = "Risk: ${risk.risk_label}, Score: ${risk.risk_score}"
                    }
                } else {
                    runOnUiThread {
                        statusText.text = "Error: ${response.code()}"
                    }
                }
            }

            override fun onFailure(call: Call<RiskResponse>, t: Throwable) {
                runOnUiThread {
                    statusText.text = "Network error: ${t.message}"
                }
            }
        })
    }
    
    override fun onResume() {
        super.onResume()
        updatePermissionStatus()
    }
}
