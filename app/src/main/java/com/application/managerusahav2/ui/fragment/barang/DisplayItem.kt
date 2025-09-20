package com.application.managerusahav2.ui.fragment.barang

import com.application.managerusahav2.data.model.response.Barang

sealed class DisplayItem {
    data class Header(val kategori: String, val itemCount: Int, val isExpanded: Boolean) : DisplayItem()
    data class Child(val barang: Barang) : DisplayItem()
}

