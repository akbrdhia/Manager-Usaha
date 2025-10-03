package com.application.managerusahav2.ui.fragment.barang

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.application.managerusahav2.data.model.request.TambahStokRequest
import com.application.managerusahav2.data.model.response.ErrorResponse
import com.application.managerusahav2.data.repository.BarangRepository
import com.google.gson.Gson
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

data class DetailBarangUiState(
    val isLoading: Boolean = false,
    val isSuccess: Boolean = false,
    val successMessage: String? = null,
    val newStokValue: Int? = null,
    val errorMessage: String? = null,
    val isDeleting: Boolean = false,
    val deleteSuccess: Boolean = false,
    val deleteMessage: String? = null
)

class DetailBarangViewModel(
    private val repository: BarangRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(DetailBarangUiState())
    val uiState: StateFlow<DetailBarangUiState> = _uiState.asStateFlow()

    fun tambahStok(barangId: Int, tambahStok: Int) {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(
                isLoading = true,
                isSuccess = false,
                errorMessage = null
            )

            try {
                val request = TambahStokRequest(
                    id = barangId,
                    stok = tambahStok
                )

                val response = repository.UpdateStok(request)

                if (response.isSuccessful) {
                    val body = response.body()
                    if (body?.success == true) {
                        _uiState.value = _uiState.value.copy(
                            isLoading = false,
                            isSuccess = true,
                            successMessage = body.message,
                            newStokValue = body.data.stokBaru
                        )
                    } else {
                        _uiState.value = _uiState.value.copy(
                            isLoading = false,
                            errorMessage = body?.message ?: "Gagal menambah stok"
                        )
                    }
                } else {
                    handleApiError(response.errorBody()?.string(), response.code())
                }
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    errorMessage = "Koneksi bermasalah: ${e.message}"
                )
            }
        }
    }

    fun deleteBarang(barangId: Int) {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(
                isDeleting = true,
                deleteSuccess = false,
                errorMessage = null
            )

            try {
                val response = repository.DeleteBarang(barangId)

                if (response.isSuccessful) {
                    val body = response.body()
                    if (body?.success == true) {
                        _uiState.value = _uiState.value.copy(
                            isDeleting = false,
                            deleteSuccess = true,
                            deleteMessage = body.message
                        )
                    } else {
                        _uiState.value = _uiState.value.copy(
                            isDeleting = false,
                            errorMessage = body?.message ?: "Gagal menghapus barang"
                        )
                    }
                } else {
                    // Handle specific HTTP errors for delete
                    val errorMessage = when (response.code()) {
                        403 -> "Tidak memiliki izin untuk menghapus barang ini"
                        404 -> "Barang tidak ditemukan"
                        409 -> "Barang tidak dapat dihapus karena masih terkait dengan transaksi lain"
                        else -> parseErrorBody(response.errorBody()?.string(), response.code())
                    }

                    _uiState.value = _uiState.value.copy(
                        isDeleting = false,
                        errorMessage = errorMessage
                    )
                }
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(
                    isDeleting = false,
                    errorMessage = "Koneksi bermasalah: ${e.message}"
                )
            }
        }
    }

    private fun handleApiError(errorBody: String?, code: Int) {
        val errorMessage = parseErrorBody(errorBody, code)
        _uiState.value = _uiState.value.copy(
            isLoading = false,
            errorMessage = errorMessage
        )
    }

    private fun parseErrorBody(errorBody: String?, code: Int): String {
        return if (!errorBody.isNullOrEmpty()) {
            try {
                val gson = Gson()
                val errorResponse = gson.fromJson(errorBody, ErrorResponse::class.java)
                if (errorResponse.errors != null) {
                    errorResponse.errors.values.flatten().joinToString("\n")
                } else {
                    errorResponse.message
                }
            } catch (e: Exception) {
                "Server error (HTTP $code)"
            }
        } else {
            "Server error (HTTP $code)"
        }
    }

    fun clearStates() {
        _uiState.value = _uiState.value.copy(
            isSuccess = false,
            errorMessage = null,
            successMessage = null,
            newStokValue = null,
            deleteSuccess = false,
            deleteMessage = null
        )
    }
}