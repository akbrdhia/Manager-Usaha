package com.application.managerusahav2.data

import com.application.managerusahav2.data.service.BarangService
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

private const val BASE_URL = "http://127.0.0.1:8000/api/"

object RetrofitClient {
    val instance: BarangService by lazy {
        val retrofit = Retrofit.Builder()
            .baseUrl(BASE_URL)
            .addConverterFactory(GsonConverterFactory.create())
            .build()

        retrofit.create(BarangService::class.java)
    }
}