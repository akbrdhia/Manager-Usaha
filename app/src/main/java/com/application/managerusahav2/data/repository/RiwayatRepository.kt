package com.application.managerusahav2.data.repository

// RiwayatRepository.kt
import com.application.managerusahav2.data.model.RiwayatResponse
import com.application.managerusahav2.data.service.ApiService
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class RiwayatRepository @Inject constructor(
    private val apiService: ApiService
) {

    suspend fun getRiwayat(
        page: Int,
        search: String? = null,
        kategori: String? = null,
        tipe: String? = null
    ): RiwayatResponse {
        return apiService.getRiwayat(
            page = page,
            search = search,
            kategori = kategori,
            tipe = tipe
        )
    }
}