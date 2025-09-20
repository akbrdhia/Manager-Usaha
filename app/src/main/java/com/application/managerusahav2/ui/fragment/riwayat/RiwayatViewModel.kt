package com.application.managerusahav2.ui.fragment.riwayat

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.application.managerusahav2.R
import com.application.managerusahav2.data.model.RiwayatDisplayItem
import com.application.managerusahav2.data.model.RiwayatItem
import com.application.managerusahav2.data.repository.RiwayatRepository
import kotlinx.coroutines.launch
import java.util.concurrent.TimeUnit

// RiwayatViewModel.kt
class RiwayatViewModel(private val repository: RiwayatRepository) : ViewModel() {

    private val _riwayatItems = MutableLiveData<List<RiwayatDisplayItem>>()
    val riwayatItems: LiveData<List<RiwayatDisplayItem>> = _riwayatItems

    private val _isLoading = MutableLiveData<Boolean>()
    val isLoading: LiveData<Boolean> = _isLoading

    private val _error = MutableLiveData<String?>()
    val error: LiveData<String?> = _error

    private val _isLastPage = MutableLiveData<Boolean>()
    val isLastPage: LiveData<Boolean> = _isLastPage

    private val _availableKategori = MutableLiveData<Set<String>>()
    val availableKategori: LiveData<Set<String>> = _availableKategori

    private var currentPage = 1
    private var searchQuery = ""
    private var selectedKategori: String? = null
    private var selectedTipe: String? = null

    private val allRiwayatData = mutableListOf<RiwayatItem>()

    fun loadRiwayat() {
        currentPage = 1
        allRiwayatData.clear()
        _isLastPage.value = false
        loadRiwayatPage()
    }

    fun loadMoreRiwayat() {
        if (_isLoading.value != true && _isLastPage.value != true) {
            currentPage++
            loadRiwayatPage()
        }
    }

    fun refreshRiwayat() {
        loadRiwayat()
    }

    fun searchRiwayat(query: String) {
        searchQuery = query
        applyFilters()
    }

    fun filterByKategori(kategori: String?) {
        selectedKategori = kategori
        applyFilters()
    }

    fun filterByTipe(tipe: String?) {
        selectedTipe = tipe
        applyFilters()
    }

    private fun loadRiwayatPage() {
        viewModelScope.launch {
            try {
                _isLoading.value = true
                _error.value = null

                val response = repository.getRiwayat(
                    page = currentPage,
                    search = searchQuery.takeIf { it.isNotEmpty() },
                    kategori = selectedKategori,
                    tipe = selectedTipe
                )

                if (response.success) {
                    val newItems = response.data.data

                    if (currentPage == 1) {
                        allRiwayatData.clear()
                    }
                    allRiwayatData.addAll(newItems)

                    // Update available kategori
                    val kategoriSet = allRiwayatData.mapNotNull { it.barang.kategori }.toSet()
                    _availableKategori.value = kategoriSet

                    // Check if last page
                    _isLastPage.value = response.data.currentPage >= response.data.lastPage

                    // Group and display
                    val displayItems = groupRiwayatByDate(allRiwayatData)
                    _riwayatItems.value = displayItems

                } else {
                    _error.value = "Gagal memuat data riwayat"
                }

            } catch (e: Exception) {
                _error.value = "Terjadi kesalahan: ${e.message}"
            } finally {
                _isLoading.value = false
            }
        }
    }

    private fun applyFilters() {
        val filteredData = allRiwayatData.filter { item ->
            val matchesSearch = if (searchQuery.isEmpty()) true else {
                item.barang.nama.contains(searchQuery, ignoreCase = true) ||
                        item.barang.kategori.contains(searchQuery, ignoreCase = true) ||
                        getTipeDisplayName(item.tipe).contains(searchQuery, ignoreCase = true)
            }

            val matchesKategori = selectedKategori == null || item.barang.kategori == selectedKategori
            val matchesTipe = selectedTipe == null || item.tipe == selectedTipe

            matchesSearch && matchesKategori && matchesTipe
        }

        val displayItems = groupRiwayatByDate(filteredData)
        _riwayatItems.value = displayItems
    }

    private fun groupRiwayatByDate(items: List<RiwayatItem>): List<RiwayatDisplayItem> {
        val displayItems = mutableListOf<RiwayatDisplayItem>()

        // Sort items by timestamp descending (newest first)
        val sortedItems = items.sortedByDescending { it.tanggal }

        val groupedItems = sortedItems.groupBy { item ->
            getRelativeDateString(item.tanggal)
        }

        // Maintain order by processing dates in chronological order
        val orderedDates = groupedItems.keys.sortedBy { dateString ->
            when (dateString) {
                "Hari ini" -> 0
                "Kemarin" -> 1
                else -> {
                    when {
                        dateString.contains("hari yang lalu") -> {
                            val days = dateString.split(" ")[0].toIntOrNull() ?: 999
                            days + 1
                        }
                        dateString.contains("minggu yang lalu") -> {
                            val weeks = dateString.split(" ")[0].toIntOrNull() ?: 999
                            (weeks * 7) + 100
                        }
                        dateString.contains("bulan yang lalu") -> {
                            val months = dateString.split(" ")[0].toIntOrNull() ?: 999
                            (months * 30) + 1000
                        }
                        dateString.contains("tahun yang lalu") -> {
                            val years = dateString.split(" ")[0].toIntOrNull() ?: 999
                            (years * 365) + 10000
                        }
                        else -> 99999
                    }
                }
            }
        }

        orderedDates.forEach { dateString ->
            val itemsForDate = groupedItems[dateString] ?: emptyList()

            // Add date header
            displayItems.add(RiwayatDisplayItem.DateHeader(dateString))

            // Add items for this date (already sorted by time descending)
            itemsForDate.forEach { item ->
                displayItems.add(RiwayatDisplayItem.RiwayatData(item))
            }
        }

        return displayItems
    }

    private fun getRelativeDateString(timestamp: Long): String {
        val now = System.currentTimeMillis()
        val diff = now - timestamp
        val days = TimeUnit.MILLISECONDS.toDays(diff)

        return when {
            days == 0L -> "Hari ini"
            days == 1L -> "Kemarin"
            days < 7L -> "$days hari yang lalu"
            days < 30L -> "${days / 7} minggu yang lalu"
            days < 365L -> "${days / 30} bulan yang lalu"
            else -> "${days / 365} tahun yang lalu"
        }
    }

    private fun getTipeDisplayName(tipe: String): String {
        return when (tipe) {
            "create" -> "Ditambahkan"
            "update" -> "Diperbarui"
            "delete" -> "Dihapus"
            "tambah_stok", "add" -> "Restok"
            "kurangi_stok", "min" -> "Pengurangan Stok"
            else -> tipe.capitalize()
        }
    }

    fun getTipeColor(tipe: String): Int {
        return when (tipe) {
            "create", "tambah_stok", "add" -> R.color.green
            "update" -> R.color.blue
            "delete", "kurangi_stok", "min" -> R.color.red
            else -> R.color.text_secondary
        }
    }

    fun getTipeIcon(tipe: String): Int {
        return when (tipe) {
            "create" -> R.drawable.ic_add_circle
            "update" -> R.drawable.ic_edit
            "delete" -> R.drawable.ic_delete
            "tambah_stok", "add" -> R.drawable.ic_trending_up
            "kurangi_stok", "min" -> R.drawable.ic_trending_down
            else -> R.drawable.ic_help_outline
        }
    }
}