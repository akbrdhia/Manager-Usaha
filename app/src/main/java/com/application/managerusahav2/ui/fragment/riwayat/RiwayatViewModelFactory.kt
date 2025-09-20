package com.application.managerusahav2.ui.fragment.riwayat

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.application.managerusahav2.data.repository.RiwayatRepository
import javax.inject.Inject

class RiwayatViewModelFactory @Inject constructor(
    private val repository: RiwayatRepository
) : ViewModelProvider.Factory {

    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(RiwayatViewModel::class.java)) {
            return RiwayatViewModel(repository) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}