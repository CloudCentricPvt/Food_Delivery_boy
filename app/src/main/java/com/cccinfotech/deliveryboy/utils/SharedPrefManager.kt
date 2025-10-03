package com.cccinfotech.deliveryboy.utils

import android.content.Context
import android.content.SharedPreferences

object SharedPrefManager {
    private const val PREF_NAME = "my_prefs"
    private var sharedPreferences: SharedPreferences? = null

    fun init(context: Context) {
        if (sharedPreferences == null) {
            sharedPreferences = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE)
        }
    }

    fun putString(key: String, value: String) {
        sharedPreferences?.edit()?.putString(key, value)?.apply()
    }

    fun getString(key: String, default: String = ""): String {
        return sharedPreferences?.getString(key, default) ?: default
    }

    fun putInt(key: String, value: Int) {
        sharedPreferences?.edit()?.putInt(key, value)?.apply()
    }

    fun getInt(key: String, default: Int = 0): Int {
        return sharedPreferences?.getInt(key, default) ?: default
    }

    fun putBoolean(key: String, value: Boolean) {
        sharedPreferences?.edit()?.putBoolean(key, value)?.apply()
    }

    fun getBoolean(key: String, default: Boolean = false): Boolean {
        return sharedPreferences?.getBoolean(key, default) ?: default
    }

    fun remove(key: String) {
        sharedPreferences?.edit()?.remove(key)?.apply()
    }

    fun clear() {
        sharedPreferences?.edit()?.clear()?.apply()
    }
}
