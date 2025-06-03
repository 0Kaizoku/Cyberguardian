package com.cyberguardianapp

import android.os.Bundle
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity

class AppRiskDetailActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_app_risk_detail)

        val appName = intent.getStringExtra("app_name") ?: "Unknown"
        val riskLabel = intent.getStringExtra("risk_label") ?: "Unknown"
        val riskScore = intent.getDoubleExtra("risk_score", 0.0)
        val backendRiskLabel = intent.getStringExtra("backend_risk_label") ?: "Unknown"
        val backendRiskScore = intent.getDoubleExtra("backend_risk_score", 0.0)

        findViewById<TextView>(R.id.detailAppName).text = appName
        findViewById<TextView>(R.id.detailRiskLabel).text = "Risk: $riskLabel"
        findViewById<TextView>(R.id.detailRiskScore).text = "Score: $riskScore"
        findViewById<TextView>(R.id.detailBackendRiskLabel).text = "Backend Risk: $backendRiskLabel"
        findViewById<TextView>(R.id.detailBackendRiskScore).text = "Backend Score: $backendRiskScore"
    }
} 