package com.application.managerusahav2.ui.fragment.tambah_barang

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.application.managerusahav2.data.repository.BarangRepository

class TambahBarangViewModelFactory(
    private val barangRepository: BarangRepository
) : ViewModelProvider.Factory {

    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(TambahBarangViewModel::class.java)) {
            return TambahBarangViewModel(barangRepository) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}