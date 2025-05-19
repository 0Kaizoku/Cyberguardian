package com.cyberguardianapp.model

data class RiskResponse(
    val package_name: String,
    val risk_score: Double,
    val risk_label: String,
    val timestamp: String
)
