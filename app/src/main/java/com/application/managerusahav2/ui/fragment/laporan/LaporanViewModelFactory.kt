package com.application.managerusahav2.ui.fragment.laporan

import com.application.managerusahav2.data.repository.BarangRepository

class LaporanViewModelFactory(
    private val barangRepository: BarangRepository
) : androidx.lifecycle.ViewModelProvider.Factory {

    @Suppress("UNCHECKED_CAST")
    override fun <T : androidx.lifecycle.ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(LaporanViewModel::class.java)) {
            return LaporanViewModel(barangRepository) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}