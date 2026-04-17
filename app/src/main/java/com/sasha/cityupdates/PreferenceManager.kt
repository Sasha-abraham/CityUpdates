package com.sasha.cityupdates

import android.content.Context
import android.content.SharedPreferences

class PreferenceManager(context: Context) {

    private val prefs: SharedPreferences = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE)

    companion object {
        private const val PREF_NAME = "CityUpdatesPrefs"
        private const val KEY_USER_NAME = "user_name"
        private const val KEY_USER_EMAIL = "user_email"
        private const val KEY_USER_AREA = "user_area"
        private const val KEY_USER_ID = "user_id"
        private const val KEY_IS_LOGGED_IN = "is_logged_in"
        private const val KEY_LAST_CATEGORY = "last_category"
    }

    // Save user details on login/register
    fun saveUser(name: String, email: String, area: String, userId: String) {
        prefs.edit()
            .putString(KEY_USER_NAME, name)
            .putString(KEY_USER_EMAIL, email)
            .putString(KEY_USER_AREA, area)
            .putString(KEY_USER_ID, userId)
            .putBoolean(KEY_IS_LOGGED_IN, true)
            .apply()
    }

    fun getUserName(): String = prefs.getString(KEY_USER_NAME, "") ?: ""
    fun getUserEmail(): String = prefs.getString(KEY_USER_EMAIL, "") ?: ""
    fun getUserArea(): String = prefs.getString(KEY_USER_AREA, "") ?: ""
    fun getUserId(): String = prefs.getString(KEY_USER_ID, "") ?: ""
    fun isLoggedIn(): Boolean = prefs.getBoolean(KEY_IS_LOGGED_IN, false)

    // Save last selected category filter
    fun saveLastCategory(category: String) {
        prefs.edit().putString(KEY_LAST_CATEGORY, category).apply()
    }
    fun getLastCategory(): String = prefs.getString(KEY_LAST_CATEGORY, "All") ?: "All"

    // Save area preference
    fun saveUserArea(area: String) {
        prefs.edit().putString(KEY_USER_AREA, area).apply()
    }

    // Clear everything on logout
    fun clearUser() {
        prefs.edit().clear().apply()
    }
}