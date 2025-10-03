package com.application.managerusahav2.ui.fragment.edit_barang

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.application.managerusahav2.data.repository.BarangRepository

class EditBarangViewModelFactory(
    private val barangRepository: BarangRepository
) : ViewModelProvider.Factory {

    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(EditBarangViewModel::class.java)) {
            return EditBarangViewModel(barangRepository) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}