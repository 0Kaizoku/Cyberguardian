package com.cyberguardianapp.model

data class AnalyzeAppRequest(
    val package_name: String,
    val app_name: String,
    val permissions: List<String>,
    val version_code: Int
) 