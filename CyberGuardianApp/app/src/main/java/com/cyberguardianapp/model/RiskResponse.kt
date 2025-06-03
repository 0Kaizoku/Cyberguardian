package com.cyberguardianapp.model

data class RiskResponse(
    val package_name: String,
    val risk_label: String,
    val risk_score: Double,
    val timestamp: String,
    val backend_risk_label: String? = null,
    val backend_risk_score: Double? = null
)
