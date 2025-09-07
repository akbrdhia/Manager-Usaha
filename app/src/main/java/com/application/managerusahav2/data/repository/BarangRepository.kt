package com.application.managerusahav2.data.repository

import com.application.managerusahav2.data.model.response.Barang
import com.application.managerusahav2.data.service.BarangService
import com.application.managerusahav2.helper.RepositoryException
import retrofit2.Response

class BarangRepository (private val barangService: BarangService) {
    suspend fun GetAllBarang(): Response<List<Barang>>{
        return try {
            barangService.getAllBarang()
        }catch(e: Exception){
            throw RepositoryException("Failed to fetch barang data: ${e.message}", e)
        }
    }


}
