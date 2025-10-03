package com.application.managerusahav2.data.model.response

import com.google.gson.annotations.SerializedName

data class LaporanResponse(
    @SerializedName("data")
    val data: List<TopBarangData>
)

data class TopBarangData(
    @SerializedName("id")
    val id: Int,

    @SerializedName("nama")
    val nama: String,

    @SerializedName("kategori")
    val kategori: String,

    @SerializedName("stok")
    val stok: Int,

    @SerializedName("harga")
    val harga: Double,

    @SerializedName("modal")
    val modal: Double,

    @SerializedName("barcode")
    val barcode: String?,

    @SerializedName("gambar_path")
    val gambarPath: String?,

    @SerializedName("created_at")
    val createdAt: String,

    @SerializedName("updated_at")
    val updatedAt: String,

    @SerializedName("terjual")
    val terjual: Int,

    @SerializedName("omset")
    val omset: Double,

    @SerializedName("hpp")
    val hpp: Double,

    @SerializedName("profit")
    val profit: Double
)