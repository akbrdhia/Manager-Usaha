package com.application.managerusahav2.data.repository

import com.application.managerusahav2.data.model.response.Barang
import com.application.managerusahav2.data.service.BarangService
import com.application.managerusahav2.helper.RepositoryException
import retrofit2.Response

class BarangRepository (private val barangService: BarangService) {
    suspend fun GetAllBarang(): Response<List<Barang>> {
        return try {
            val resp = barangService.getAllBarang()
            android.util.Log.d("Repo", "Response code: ${resp.code()}")
            if (!resp.isSuccessful) {
                android.util.Log.e("Repo", "Error body: ${resp.errorBody()?.string()}")
            }
            resp
        } catch (e: Exception) {
            android.util.Log.e("Repo", "Exception fetching barang", e)
            throw RepositoryException("Failed to fetch barang data: ${e.message}", e)
        }
    }



}
