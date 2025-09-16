package com.application.managerusahav2.data.network

import com.application.managerusahav2.data.service.BarangService
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.util.concurrent.TimeUnit
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor

/**
 * RetrofitClient yang mendukung perubahan base URL secara dinamis
 * Instance akan di-rebuild setiap kali base URL berubah
 */
object RetrofitClient {

    @Volatile
    private var INSTANCE: BarangService? = null

    /**
     * Mendapatkan instance BarangService
     * Akan membuat instance baru jika belum ada atau perlu di-rebuild
     */

    // tambahan di RetrofitClient (opsional, biar compatible)

    fun getInstance(): BarangService {
        return INSTANCE ?: synchronized(this) {
            val instance = buildRetrofitInstance()
            INSTANCE = instance
            instance
        }
    }

    /**
     * Membangun ulang instance dengan base URL terbaru
     * Dipanggil secara otomatis ketika base URL berubah
     */
    internal fun rebuildInstance() {
        synchronized(this) {
            INSTANCE = null // Reset instance lama
        }
    }

    /**
     * Membangun instance Retrofit dengan konfigurasi lengkap
     */
    private fun buildRetrofitInstance(): BarangService {
        // Logging interceptor untuk debugging (opsional)
        val loggingInterceptor = HttpLoggingInterceptor().apply {
            level = HttpLoggingInterceptor.Level.BODY
        }

        // OkHttp client dengan timeout configuration
        val okHttpClient = OkHttpClient.Builder()
            .addInterceptor(loggingInterceptor)
            .connectTimeout(30, TimeUnit.SECONDS)
            .readTimeout(30, TimeUnit.SECONDS)
            .writeTimeout(30, TimeUnit.SECONDS)
            .build()

        // Build Retrofit dengan base URL dinamis
        val retrofit = Retrofit.Builder()
            .baseUrl(NetworkConfig.getCurrentBaseUrl())
            .client(okHttpClient)
            .addConverterFactory(GsonConverterFactory.create())
            .build()

        return retrofit.create(BarangService::class.java)
    }
}