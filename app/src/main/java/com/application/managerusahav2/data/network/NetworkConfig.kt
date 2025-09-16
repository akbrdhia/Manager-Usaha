// File: com.application.managerusahav2.data.network.NetworkConfig.kt
package com.application.managerusahav2.data.network

import android.content.Context
import android.content.SharedPreferences

/**
 * NetworkConfig mengelola konfigurasi jaringan secara dinamis
 * Menggunakan SharedPreferences untuk menyimpan base URL
 */
object NetworkConfig {
    private const val PREFS_NAME = "network_config"
    private const val KEY_BASE_URL = "base_url"

    // Default base URL - akan digunakan jika belum ada yang disimpan
    private const val DEFAULT_BASE_URL = "http://192.168.1.7:8000/api/"

    private var sharedPreferences: SharedPreferences? = null

    /**
     * Inisialisasi dengan Context - panggil di Application class atau fragment pertama
     */
    fun init(context: Context) {
        sharedPreferences = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
    }

    /**
     * Mendapatkan base URL saat ini
     * Jika belum pernah diset, akan return default URL
     */
    fun getCurrentBaseUrl(): String {
        return sharedPreferences?.getString(KEY_BASE_URL, DEFAULT_BASE_URL)
            ?: DEFAULT_BASE_URL
    }

    /**
     * Mengubah base URL dan menyimpannya secara permanen
     * Setelah dipanggil, semua request selanjutnya akan menggunakan URL baru
     */
    fun updateBaseUrl(newBaseUrl: String) {
        // Pastikan URL diakhiri dengan "/"
        val formattedUrl = if (newBaseUrl.endsWith("/")) {
            newBaseUrl
        } else {
            "$newBaseUrl/"
        }

        sharedPreferences?.edit()?.putString(KEY_BASE_URL, formattedUrl)?.apply()

        // Rebuild retrofit instance dengan URL baru
        RetrofitClient.rebuildInstance()
    }

    /**
     * Reset ke default URL
     */
    fun resetToDefault() {
        updateBaseUrl(DEFAULT_BASE_URL)
    }

    /**
     * Mengecek apakah base URL sudah diubah dari default
     */
    fun isUsingCustomUrl(): Boolean {
        return getCurrentBaseUrl() != DEFAULT_BASE_URL
    }
}