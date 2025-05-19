package com.cyberguardianapp.model

import android.graphics.drawable.Drawable

/**
 * Represents information about an installed Android application
 */
data class AppInfo(
    val packageName: String,
    val appName: String,
    val versionCode: Int,
    val versionName: String,
    val appIcon: Drawable?,
    val installTime: Long,
    val updateTime: Long,
    val permissions: List<String>,
    var riskLevel: RiskLevel = RiskLevel.UNKNOWN,
    var riskScore: Double = 0.0
)
