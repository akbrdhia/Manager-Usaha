package com.application.managerusahav2.data.model

// RiwayatDataClasses.kt

import com.google.gson.annotations.SerializedName

// Display Item for RecyclerView
sealed class RiwayatDisplayItem {
    data class DateHeader(val dateString: String) : RiwayatDisplayItem()
    data class RiwayatData(val riwayatItem: RiwayatItem) : RiwayatDisplayItem()
}

// API Response Models
data class RiwayatResponse(
    val success: Boolean,
    val data: RiwayatData
)

data class RiwayatData(
    @SerializedName("current_page") val currentPage: Int,
    @SerializedName("last_page") val lastPage: Int,
    @SerializedName("first_page_url") val firstPageUrl: String?,
    @SerializedName("last_page_url") val lastPageUrl: String?,
    @SerializedName("next_page_url") val nextPageUrl: String?,
    @SerializedName("prev_page_url") val prevPageUrl: String?,
    @SerializedName("per_page") val perPage: Int,
    val from: Int?,
    val to: Int?,
    val total: Int,
    val path: String?,
    val links: List<PaginationLink>?,
    val data: List<RiwayatItem>
)

data class PaginationLink(
    val url: String?,
    val label: String,
    val active: Boolean
)

data class RiwayatItem(
    val id: Int,
    @SerializedName("barang_id") val barangId: Int,
    val tanggal: Long,
    val jumlah: Int,
    val tipe: String,
    @SerializedName("created_at") val createdAt: String,
    @SerializedName("updated_at") val updatedAt: String,
    val barang: Barang
)

data class Barang(
    val id: Int,
    val nama: String,
    val kategori: String,
    val stok: Int,
    val harga: Int,
    val modal: Int,
    val barcode: String?,
    @SerializedName("gambar_path") val gambarPath: String?,
    @SerializedName("created_at") val createdAt: String,
    @SerializedName("updated_at") val updatedAt: String
)