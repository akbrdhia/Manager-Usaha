package com.application.managerusahav2.data.model.request

import com.google.gson.annotations.SerializedName

data class TambahStokRequest(
    @SerializedName("id")
    val id: Int,

    @SerializedName("stok")
    val stok: Int
)