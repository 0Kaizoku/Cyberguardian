package com.cyberguardianapp.service

import android.accessibilityservice.AccessibilityService
import android.content.Intent
import android.util.Log
import android.view.accessibility.AccessibilityEvent
import android.view.accessibility.AccessibilityNodeInfo
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch
import java.util.concurrent.ConcurrentHashMap

/**
 * Accessibility service to monitor app behavior for dynamic detection
 */
class AppMonitorAccessibilityService : AccessibilityService() {
    
    private val TAG = "AppMonitorService"
    private val serviceScope = CoroutineScope(SupervisorJob() + Dispatchers.Default)
    
    // Track suspicious behavior patterns
    private val suspiciousActionCounter = ConcurrentHashMap<String, Int>()
    private val windowOpenCounter = ConcurrentHashMap<String, Int>()
    
    override fun onServiceConnected() {
        Log.d(TAG, "Accessibility service connected")
    }
    
    override fun onAccessibilityEvent(event: AccessibilityEvent) {
        val packageName = event.packageName?.toString() ?: return
        
        // Skip monitoring system UI and own app
        if (packageName == "com.android.systemui" || packageName == "com.cyberguardianapp") {
            return
        }
        
        when (event.eventType) {
            AccessibilityEvent.TYPE_WINDOW_STATE_CHANGED -> {
                handleWindowStateChange(packageName, event)
            }
            
            AccessibilityEvent.TYPE_VIEW_CLICKED -> {
                // Monitor user interactions
                Log.d(TAG, "User interaction with: $packageName")
            }
            
            AccessibilityEvent.TYPE_VIEW_TEXT_CHANGED -> {
                // Monitor text changes (could be sensitive data entry)
                val inputSource = event.source
                checkForSensitiveInputs(packageName, inputSource)
            }
        }
    }
    
    private fun handleWindowStateChange(packageName: String, event: AccessibilityEvent) {
        // Track rapid window opening (potential ad spam or phishing)
        val currentCount = windowOpenCounter.getOrDefault(packageName, 0)
        windowOpenCounter[packageName] = currentCount + 1
        
        // If many windows are opened in short succession, flag as suspicious
        if (currentCount > 5) {
            Log.w(TAG, "Suspicious rapid window opening detected in $packageName")
            incrementSuspiciousCounter(packageName)
            
            // Reset counter after reporting
            windowOpenCounter[packageName] = 0
        }
        
        // Check for potential phishing (apps mimicking login screens)
        val windowTitle = event.className?.toString() ?: ""
        if (windowTitle.contains("login", ignoreCase = true) || 
            windowTitle.contains("sign in", ignoreCase = true)) {
            
            // Potential phishing - real banking/payment apps have been checked
            if (!isTrustedFinancialApp(packageName)) {
                Log.w(TAG, "Potential phishing screen detected in $packageName")
                incrementSuspiciousCounter(packageName)
            }
        }
    }
    
    private fun checkForSensitiveInputs(packageName: String, source: AccessibilityNodeInfo?) {
        // This would check for password fields, credit card entry fields, etc.
        // For simplicity, we're just logging in this example
        source?.let {
            if (it.text != null) {
                Log.d(TAG, "Text input detected in $packageName")
            }
        }
    }
    
    private fun incrementSuspiciousCounter(packageName: String) {
        val currentCount = suspiciousActionCounter.getOrDefault(packageName, 0)
        suspiciousActionCounter[packageName] = currentCount + 1
        
        // When multiple suspicious actions are detected, report to backend
        if (currentCount + 1 >= 3) {
            reportSuspiciousBehavior(packageName)
            suspiciousActionCounter[packageName] = 0
        }
    }
    
    private fun reportSuspiciousBehavior(packageName: String) {
        serviceScope.launch {
            // In a real implementation, this would send data to the backend
            Log.w(TAG, "Reporting suspicious behavior for $packageName to backend")
            
            // Here you would call backend API to report behavior
            // For now, we're just logging
        }
    }
    
    private fun isTrustedFinancialApp(packageName: String): Boolean {
        // List of known legitimate financial/payment apps
        val trustedApps = listOf(
            "com.paypal.android.p2pmobile",
            "com.venmo",
            "com.squareup.cash",
            "com.google.android.apps.walletnfcrel",
            "com.bankofamerica.cashpromobile",
            "com.chase.mobile"
        )
        
        return trustedApps.contains(packageName)
    }
    
    override fun onInterrupt() {
        Log.d(TAG, "Accessibility service interrupted")
    }
    
    override fun onDestroy() {
        super.onDestroy()
        Log.d(TAG, "Accessibility service destroyed")
    }
}
