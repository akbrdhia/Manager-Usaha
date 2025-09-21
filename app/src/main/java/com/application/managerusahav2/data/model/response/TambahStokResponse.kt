package com.application.managerusahav2.data.model.response

import com.google.gson.annotations.SerializedName

data class TambahStokResponse(
    @SerializedName("success")
    val success: Boolean,

    @SerializedName("message")
    val message: String,

    @SerializedName("data")
    val data: StokData
)

data class StokData(
    @SerializedName("barang_id")
    val barangId: Int,

    @SerializedName("stok_lama")
    val stokLama: Int,

    @SerializedName("stok_baru")
    val stokBaru: Int,

    @SerializedName("penambahan")
    val penambahan: Int
)