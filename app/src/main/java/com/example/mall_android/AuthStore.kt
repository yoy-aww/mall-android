package com.example.mall_android

import android.content.Context
import com.example.mall_android.model.User

class AuthStore(context: Context) {
    private val prefs = context.getSharedPreferences("auth_prefs", Context.MODE_PRIVATE)
    private val gson = com.google.gson.Gson()

    var token: String?
        get() = prefs.getString("token", null)
        set(v) = prefs.edit().putString("token", v).apply()

    var user: User?
        get() {
            val json = prefs.getString("user", null) ?: return null
            return runCatching { gson.fromJson(json, User::class.java) }.getOrNull()
        }
        set(v) {
            val editor = prefs.edit()
            if (v == null) editor.remove("user") else editor.putString("user", gson.toJson(v))
            editor.apply()
        }

    fun isLoggedIn() = !token.isNullOrBlank()

    fun logout() {
        prefs.edit().clear().apply()
    }
}
