package com.application.managerusahav2.data.service

import com.application.managerusahav2.data.model.RiwayatResponse
import com.application.managerusahav2.data.model.request.EditBarangRequest
import com.application.managerusahav2.data.model.request.TambahBarangRequest
import com.application.managerusahav2.data.model.request.TambahStokRequest
import com.application.managerusahav2.data.model.response.Barang
import com.application.managerusahav2.data.model.response.DeleteBarangResponse
import com.application.managerusahav2.data.model.response.EditBarangResponse
import com.application.managerusahav2.data.model.response.KategoriResponse
import com.application.managerusahav2.data.model.response.LaporanResponse
import com.application.managerusahav2.data.model.response.TambahBarangResponse
import com.application.managerusahav2.data.model.response.TambahStokResponse
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.DELETE
import retrofit2.http.GET
import retrofit2.http.POST
import retrofit2.http.PUT
import retrofit2.http.Path
import retrofit2.http.Query

interface ApiService {
    @GET("allbarang")
    suspend fun getAllBarang(): Response<List<Barang>>

    @GET("kategori")
    suspend fun getAllKategori(): Response<KategoriResponse>

    @POST("barang")
    suspend fun tambahBarang(@Body request: TambahBarangRequest): Response<TambahBarangResponse>

    @POST("tambahstok")
    suspend fun updateStok(@Body request: TambahStokRequest): Response<TambahStokResponse>

    @GET("riwayat")
    suspend fun getRiwayat(
        @Query("page") page: Int,
        @Query("search") search: String? = null,
        @Query("kategori") kategori: String? = null,
        @Query("tipe") tipe: String? = null
    ): RiwayatResponse

    @DELETE("barang/{id}")
    suspend fun deleteBarang(@Path("id") id: Int): Response<DeleteBarangResponse>

    @PUT("barang/{id}")
    suspend fun editBarang(@Path("id") id: Int, @Body request: EditBarangRequest): Response<EditBarangResponse>

    @GET("laporan/top-barang")
    suspend fun getTopBarang(
        @Query("start") startDate: String? = null,
        @Query("end") endDate: String? = null
    ): Response<LaporanResponse>

}