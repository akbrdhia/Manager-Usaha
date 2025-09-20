package com.application.managerusahav2.data.model.response

import com.google.gson.annotations.SerializedName

data class Barang(
    val id: Int,
    val nama: String,
    val kategori: String,
    val stok: Int,
    val harga: Double,
    val modal: Double,
    val barcode: String?,
    @SerializedName("gambar_path")
    val gambarPath: String?,
)
