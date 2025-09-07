package com.application.managerusahav2.data.model.response

data class Barang(
    val id: Int,
    val nama: String,
    val kategori: String,
    val stok: Int,
    val harga: Double,
    val modal: Double,
    val barcode: String?,
    val gambarPath: String?,
)
