package com.collabmatch.mobile.data.repository

import android.content.Context

class SessionManager(context: Context) {
    private val prefs = context.getSharedPreferences("collabmatch_session", Context.MODE_PRIVATE)

    fun saveToken(token: String) {
        prefs.edit().putString(KEY_TOKEN, token).apply()
    }

    fun getToken(): String? = prefs.getString(KEY_TOKEN, null)

    fun clearToken() {
        prefs.edit().remove(KEY_TOKEN).apply()
    }

    companion object {
        private const val KEY_TOKEN = "collabmatch_token"
    }
}
