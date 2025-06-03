package com.cyberguardianapp.util

import android.accessibilityservice.AccessibilityService
import android.content.Context
import android.provider.Settings
import android.text.TextUtils

class PermissionChecker {
    fun isAccessibilityServiceEnabled(
        context: Context,
        serviceClass: Class<out AccessibilityService>
    ): Boolean {
        val expectedServiceName = "${context.packageName}/${serviceClass.name}"
        val enabledServicesSetting = Settings.Secure.getString(
            context.contentResolver,
            Settings.Secure.ENABLED_ACCESSIBILITY_SERVICES
        ) ?: return false

        return TextUtils.SimpleStringSplitter(':').apply {
            setString(enabledServicesSetting)
        }.any { it.equals(expectedServiceName, ignoreCase = true) }
    }
}

