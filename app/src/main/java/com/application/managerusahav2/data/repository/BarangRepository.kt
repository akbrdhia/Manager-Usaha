package com.application.managerusahav2.data.repository

import com.application.managerusahav2.data.model.request.TambahBarangRequest
import com.application.managerusahav2.data.model.response.Barang
import com.application.managerusahav2.data.model.response.KategoriResponse
import com.application.managerusahav2.data.model.response.TambahBarangResponse
import com.application.managerusahav2.data.service.BarangService
import com.application.managerusahav2.helper.RepositoryException
import retrofit2.Response

class BarangRepository(private val barangService: BarangService) {

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

    suspend fun GetAllKategori(): Response<KategoriResponse> {
        return try {
            val resp = barangService.getAllKategori()
            android.util.Log.d("Repo", "Kategori response code: ${resp.code()}")
            if (!resp.isSuccessful) {
                android.util.Log.e("Repo", "Kategori error body: ${resp.errorBody()?.string()}")
            }
            resp
        } catch (e: Exception) {
            android.util.Log.e("Repo", "Exception fetching kategori", e)
            throw RepositoryException("Failed to fetch kategori data: ${e.message}", e)
        }
    }

    suspend fun TambahBarang(request: TambahBarangRequest): Response<TambahBarangResponse> {
        return try {
            val resp = barangService.tambahBarang(request)
            android.util.Log.d("Repo", "Tambah barang response code: ${resp.code()}")
            if (!resp.isSuccessful) {
                android.util.Log.e("Repo", "Tambah barang error body: ${resp.errorBody()?.string()}")
            }
            resp
        } catch (e: Exception) {
            android.util.Log.e("Repo", "Exception adding barang", e)
            throw RepositoryException("Failed to add barang: ${e.message}", e)
        }
    }
}