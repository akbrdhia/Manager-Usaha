package com.application.managerusahav2.data.service

import retrofit2.Response
import retrofit2.http.GET

interface BarangService {
    @GET("/barang")
    suspend fun getAllBarang(): Response<List<Barang>>

}