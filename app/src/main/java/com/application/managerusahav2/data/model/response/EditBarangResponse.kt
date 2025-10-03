package com.application.managerusahav2.data.model.response

import com.google.gson.annotations.SerializedName

data class EditBarangResponse(
    @SerializedName("success")
    val success: Boolean,

    @SerializedName("message")
    val message: String,

    @SerializedName("data")
    val data: EditedBarangData
)

data class EditedBarangData(
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