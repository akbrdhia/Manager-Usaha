package com.application.managerusahav2.data.model.request

import com.google.gson.annotations.SerializedName

data class EditBarangRequest(
    @SerializedName("nama")
    val nama: String,

    @SerializedName("kategori")
    val kategori: String,

    @SerializedName("stok")
    val stok: Int,

    @SerializedName("harga")
    val harga: Long,

    @SerializedName("modal")
    val modal: Long,

    @SerializedName("barcode")
    val barcode: String? = null,

    @SerializedName("gambar_path")
    val gambarPath: String? = null
)