package com.application.managerusahav2.data.model.response

import com.google.gson.annotations.SerializedName

data class DeleteBarangResponse(
    @SerializedName("success")
    val success: Boolean,

    @SerializedName("message")
    val message: String
)