package com.application.managerusahav2.ui.fragment.laporan

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.application.managerusahav2.data.model.response.TopBarangData
import com.application.managerusahav2.data.repository.BarangRepository

import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.*

data class LaporanUiState(
    val isLoading: Boolean = false,
    val topBarangData: List<TopBarangData> = emptyList(),
    val errorMessage: String? = null,
    val currentPeriode: String = "Hari Ini"
)

enum class PeriodeType(val label: String, val days: Int?) {
    HARI_INI("Hari Ini", 0),
    TUJUH_HARI("7 Hari Terakhir", 7),
    TIGA_PULUH_HARI("30 Hari Terakhir", 30),
    ALL_TIME("Sepanjang Waktu", null)
}

class LaporanViewModel(
    private val barangRepository: BarangRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(LaporanUiState())
    val uiState: StateFlow<LaporanUiState> = _uiState.asStateFlow()

    private val dateFormat = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())

    init {
        loadTopBarang(PeriodeType.HARI_INI)
    }

    fun loadTopBarang(periode: PeriodeType) {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(
                isLoading = true,
                errorMessage = null,
                currentPeriode = periode.label
            )

            try {
                val (startDate, endDate) = calculateDateRange(periode)
                val response = barangRepository.getTopBarang(startDate, endDate)

                if (response.isSuccessful) {
                    val data = response.body()?.data ?: emptyList()
                    _uiState.value = _uiState.value.copy(
                        isLoading = false,
                        topBarangData = data,
                        errorMessage = null
                    )
                } else {
                    _uiState.value = _uiState.value.copy(
                        isLoading = false,
                        errorMessage = "Gagal memuat data laporan (HTTP ${response.code()})"
                    )
                }
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    errorMessage = "Koneksi bermasalah: ${e.message}"
                )
            }
        }
    }

    private fun calculateDateRange(periode: PeriodeType): Pair<String?, String?> {
        val calendar = Calendar.getInstance()

        return when (periode) {
            PeriodeType.HARI_INI -> {
                val today = dateFormat.format(calendar.time)
                Pair(today, today)
            }
            PeriodeType.TUJUH_HARI -> {
                val endDate = dateFormat.format(calendar.time)
                calendar.add(Calendar.DAY_OF_MONTH, -7)
                val startDate = dateFormat.format(calendar.time)
                Pair(startDate, endDate)
            }
            PeriodeType.TIGA_PULUH_HARI -> {
                val endDate = dateFormat.format(calendar.time)
                calendar.add(Calendar.DAY_OF_MONTH, -30)
                val startDate = dateFormat.format(calendar.time)
                Pair(startDate, endDate)
            }
            PeriodeType.ALL_TIME -> {
                Pair(null, null) // No date filter
            }
        }
    }

    fun retryLoadData() {
        val currentPeriode = when (_uiState.value.currentPeriode) {
            "Hari Ini" -> PeriodeType.HARI_INI
            "7 Hari Terakhir" -> PeriodeType.TUJUH_HARI
            "30 Hari Terakhir" -> PeriodeType.TIGA_PULUH_HARI
            "Sepanjang Waktu" -> PeriodeType.ALL_TIME
            else -> PeriodeType.HARI_INI
        }
        loadTopBarang(currentPeriode)
    }

    fun clearError() {
        _uiState.value = _uiState.value.copy(errorMessage = null)
    }
}