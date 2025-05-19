package com.cyberguardianapp.util

object PermissionMapper {

    // Mapping between Android permissions and dataset permissions
    private val permissionMapping = mapOf(
        "android.permission.ACCESS_FINE_LOCATION" to "Your location : fine (GPS) location (D)",
        "android.permission.ACCESS_COARSE_LOCATION" to "Your location : coarse (network-based) location (D)",
        "android.permission.CAMERA" to "Hardware controls : take pictures and videos (D)",
        "android.permission.RECORD_AUDIO" to "Hardware controls : record audio (D)",
        "android.permission.READ_CONTACTS" to "Your personal information : read contact data (D)",
        "android.permission.WRITE_CONTACTS" to "Your personal information : write contact data (D)",
        "android.permission.INTERNET" to "Network communication : full Internet access (D)",
        "android.permission.SEND_SMS" to "Services that cost you money : send SMS messages (D)",
        "android.permission.RECEIVE_SMS" to "Your messages : receive SMS (D)",
        "android.permission.READ_SMS" to "Your messages : read SMS or MMS (D)",
        "android.permission.READ_EXTERNAL_STORAGE" to "Storage : modify/delete USB storage contents modify/delete SD card contents (D)",
        "android.permission.WRITE_EXTERNAL_STORAGE" to "Storage : modify/delete USB storage contents modify/delete SD card contents (D)"
        // Add more mappings as needed
    )

    /**
     * Maps Android permissions to dataset permissions.
     * Excludes permissions without a match.
     */
    fun mapPermissions(androidPermissions: List<String>): List<String> {
        return androidPermissions.mapNotNull { permissionMapping[it] }
    }
}