package com.application.managerusahav2.data.repository

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
import com.application.managerusahav2.data.service.ApiService
import com.application.managerusahav2.helper.RepositoryException
import retrofit2.Response

class BarangRepository(private val apiService: ApiService) {

    suspend fun GetAllBarang(): Response<List<Barang>> {
        return try {
            val resp = apiService.getAllBarang()
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
            val resp = apiService.getAllKategori()
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
            val resp = apiService.tambahBarang(request)
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

    suspend fun UpdateStok(request: TambahStokRequest): Response<TambahStokResponse> {
        return try {
            val resp = apiService.updateStok(request)
            android.util.Log.d("Repo", "Update stok response code: ${resp.code()}")
            if (!resp.isSuccessful) {
                android.util.Log.e("Repo", "Update stok error body: ${resp.errorBody()?.string()}")
            }
            resp
        } catch (e: Exception) {
            android.util.Log.e("Repo", "Exception updating stok", e)
            throw RepositoryException("Failed to update stok: ${e.message}", e)
        }
    }

    suspend fun DeleteBarang(id: Int): Response<DeleteBarangResponse> {
        return try {
            val resp = apiService.deleteBarang(id)
            android.util.Log.d("Repo", "Delete barang response code: ${resp.code()}")
            if (!resp.isSuccessful) {
                android.util.Log.e("Repo", "Delete barang error body: ${resp.errorBody()?.string()}")
            }
            resp
        } catch (e: Exception) {
            android.util.Log.e("Repo", "Exception deleting barang", e)
            throw RepositoryException("Failed to delete barang: ${e.message}", e)
        }
    }

    suspend fun EditBarang(id: Int, request: EditBarangRequest): Response<EditBarangResponse> {
        return try {
            val resp = apiService.editBarang(id, request)
            android.util.Log.d("Repo", "Edit barang response code: ${resp.code()}")
            if (!resp.isSuccessful) {
                android.util.Log.e("Repo", "Edit barang error body: ${resp.errorBody()?.string()}")
            }
            resp
        } catch (e: Exception) {
            android.util.Log.e("Repo", "Exception editing barang", e)
            throw RepositoryException("Failed to edit barang: ${e.message}", e)
        }
    }

    suspend fun getTopBarang(startDate: String? = null, endDate: String? = null): Response<LaporanResponse> {
        return try {
            val resp = apiService.getTopBarang(startDate, endDate)
            android.util.Log.d("LaporanRepo", "Response code: ${resp.code()}")
            if (!resp.isSuccessful) {
                android.util.Log.e("LaporanRepo", "Error body: ${resp.errorBody()?.string()}")
            }
            resp
        } catch (e: Exception) {
            android.util.Log.e("LaporanRepo", "Exception fetching laporan", e)
            throw RepositoryException("Failed to fetch laporan data: ${e.message}", e)
        }
    }
}