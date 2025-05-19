package com.cyberguardianapp.model

/**
 * Represents the risk level of an application
 */
enum class RiskLevel(val displayName: String, val colorResourceId: Int) {
    BENIGN("Safe", android.R.color.holo_green_light),
    SUSPICIOUS("Suspicious", android.R.color.holo_orange_light),
    MALICIOUS("Dangerous", android.R.color.holo_red_light),
    UNKNOWN("Unknown", android.R.color.darker_gray)
}
