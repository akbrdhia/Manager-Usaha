package com.application.managerusahav2.data.model.response

import com.google.gson.annotations.SerializedName

data class TambahBarangResponse(
    @SerializedName("success")
    val success: Boolean,

    @SerializedName("message")
    val message: String,

    @SerializedName("data")
    val data: BarangData? = null
)

data class BarangData(
    @SerializedName("id")
    val id: Int,

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
    val barcode: String?,

    @SerializedName("gambar_path")
    val gambarPath: String?,

    @SerializedName("created_at")
    val createdAt: String,

    @SerializedName("updated_at")
    val updatedAt: String
)

// Error response model
data class ErrorResponse(
    @SerializedName("message")
    val message: String,

    @SerializedName("errors")
    val errors: Map<String, List<String>>? = null
)