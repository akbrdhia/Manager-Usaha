package com.application.managerusahav2.ui.fragment.barang

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.application.managerusahav2.data.model.response.Barang
import com.application.managerusahav2.data.repository.BarangRepository
import com.application.managerusahav2.helper.RepositoryException
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class BarangViewModel(private val repository: BarangRepository) : ViewModel() {

    private val _uiState = MutableStateFlow(BarangUiState())
    val uiState: StateFlow<BarangUiState> = _uiState.asStateFlow()

    init {
        loadBarangData()
    }

    fun loadBarangData() {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true, error = null)

            try {
                val response = repository.GetAllBarang()
                if (response.isSuccessful) {
                    val barangList = response.body() ?: emptyList()
                    // Sort by category for better grouping
                    val sortedList = barangList.sortedBy { it.kategori }
                    _uiState.value = _uiState.value.copy(
                        barangList = sortedList,
                        isLoading = false,
                        error = null
                    )
                } else {
                    _uiState.value = _uiState.value.copy(
                        isLoading = false,
                        error = "Failed to load data: ${response.message()}"
                    )
                }
            } catch (e: RepositoryException) {
                _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    error = e.message
                )
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    error = "Unexpected error: ${e.message}"
                )
            }
        }
    }

    fun refreshData() {
        loadBarangData()
    }

    fun clearError() {
        _uiState.value = _uiState.value.copy(error = null)
    }
}

data class BarangUiState(
    val barangList: List<Barang> = emptyList(),
    val isLoading: Boolean = false,
    val error: String? = null
)

class BarangViewModelFactory(
    private val repository: BarangRepository
) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(BarangViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return BarangViewModel(repository) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}