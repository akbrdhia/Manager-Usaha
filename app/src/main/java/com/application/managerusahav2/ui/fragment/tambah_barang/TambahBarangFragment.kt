package com.application.managerusahav2.ui.fragment.tambah_barang

import androidx.fragment.app.viewModels
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.application.managerusahav2.R

class TambahBarangFragment : Fragment() {

    private val viewModel: TambahBarangViewModel by viewModels()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        return inflater.inflate(R.layout.fragment_tambah_barang, container, false)
    }
}