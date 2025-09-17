package com.application.managerusahav2.data.service

import com.application.managerusahav2.data.model.request.TambahBarangRequest
import com.application.managerusahav2.data.model.response.Barang
import com.application.managerusahav2.data.model.response.KategoriResponse
import com.application.managerusahav2.data.model.response.TambahBarangResponse
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.POST

interface BarangService {
    @GET("allbarang")
    suspend fun getAllBarang(): Response<List<Barang>>

    @GET("kategori")
    suspend fun getAllKategori(): Response<KategoriResponse>

    @POST("barang")
    suspend fun tambahBarang(@Body request: TambahBarangRequest): Response<TambahBarangResponse>
}