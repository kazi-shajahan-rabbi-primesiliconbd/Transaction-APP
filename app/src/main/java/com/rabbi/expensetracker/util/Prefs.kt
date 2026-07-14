package com.rabbi.expensetracker.util

import android.content.Context

class Prefs(context: Context) {
    private val sp = context.getSharedPreferences("taka_tracker_prefs", Context.MODE_PRIVATE)

    // null = follow system, true = force dark, false = force light
    var darkModeOverride: Boolean?
        get() = if (!sp.contains("dark_override")) null else sp.getBoolean("dark_override", false)
        set(value) {
            sp.edit().apply {
                if (value == null) remove("dark_override") else putBoolean("dark_override", value)
            }.apply()
        }

    var dynamicColorEnabled: Boolean
        get() = sp.getBoolean("dynamic_color", true)
        set(value) = sp.edit().putBoolean("dynamic_color", value).apply()

    var lastInboxScanMillis: Long
        get() = sp.getLong("last_scan", 0L)
        set(value) = sp.edit().putLong("last_scan", value).apply()

    var smsImportEnabled: Boolean
        get() = sp.getBoolean("sms_import_enabled", true)
        set(value) = sp.edit().putBoolean("sms_import_enabled", value).apply()
}
