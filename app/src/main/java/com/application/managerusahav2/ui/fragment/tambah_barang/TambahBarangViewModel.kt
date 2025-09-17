package com.application.managerusahav2.ui.fragment.tambah_barang

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.application.managerusahav2.data.model.request.TambahBarangRequest
import com.application.managerusahav2.data.model.response.ErrorResponse
import com.application.managerusahav2.data.repository.BarangRepository
import com.google.gson.Gson
import kotlinx.coroutines.launch

class TambahBarangViewModel(private val barangRepository: BarangRepository) : ViewModel() {

    // Fields (string semua biar gampang bind ke EditText)
    private val _nama = MutableLiveData<String>("")
    val nama: LiveData<String> = _nama

    private val _kuantitas = MutableLiveData<String>("")
    val kuantitas: LiveData<String> = _kuantitas

    // kategorinya disimpan sebagai list + selected index
    private val _kategoriList = MutableLiveData<MutableList<String>>(mutableListOf())
    val kategoriList: LiveData<MutableList<String>> = _kategoriList

    private val _kategoriIndex = MutableLiveData<Int>(0)
    val kategoriIndex: LiveData<Int> = _kategoriIndex

    // Loading state untuk kategori
    private val _kategoriLoading = MutableLiveData<Boolean>(false)
    val kategoriLoading: LiveData<Boolean> = _kategoriLoading

    // Error state untuk kategori
    private val _kategoriError = MutableLiveData<String?>(null)
    val kategoriError: LiveData<String?> = _kategoriError

    // List untuk kategori baru yang ditambahkan user (temporary)
    private val _newKategoriList = MutableLiveData<MutableList<String>>(mutableListOf())
    val newKategoriList: LiveData<MutableList<String>> = _newKategoriList

    private val _barcode = MutableLiveData<String>("")
    val barcode: LiveData<String> = _barcode

    private val _hargaModal = MutableLiveData<String>("")
    val hargaModal: LiveData<String> = _hargaModal

    private val _hargaJual = MutableLiveData<String>("")
    val hargaJual: LiveData<String> = _hargaJual

    // image uri as string (nullable)
    private val _imageUri = MutableLiveData<String?>(null)
    val imageUri: LiveData<String?> = _imageUri

    // Submit states
    private val _submitLoading = MutableLiveData<Boolean>(false)
    val submitLoading: LiveData<Boolean> = _submitLoading

    private val _submitSuccess = MutableLiveData<Boolean>(false)
    val submitSuccess: LiveData<Boolean> = _submitSuccess

    private val _submitError = MutableLiveData<String?>(null)
    val submitError: LiveData<String?> = _submitError

    // ===== Kategori Functions =====
    fun fetchKategoriFromServer() {
        viewModelScope.launch {
            _kategoriLoading.value = true
            _kategoriError.value = null

            try {
                val response = barangRepository.GetAllKategori()
                if (response.isSuccessful) {
                    val kategoriResponse = response.body()
                    if (kategoriResponse?.success == true) {
                        val serverKategori = kategoriResponse.data.toMutableList()

                        // Jika server return empty, fallback ke "Umum"
                        if (serverKategori.isEmpty()) {
                            serverKategori.add("Umum")
                        }

                        _kategoriList.value = serverKategori
                        _kategoriIndex.value = 0
                    } else {
                        // Response tidak success
                        handleKategoriFallback("Server response tidak valid")
                    }
                } else {
                    // HTTP error
                    handleKategoriFallback("Gagal mengambil data kategori (HTTP ${response.code()})")
                }
            } catch (e: Exception) {
                // Network error atau exception lain
                handleKategoriFallback("Koneksi bermasalah: ${e.message}")
            }

            _kategoriLoading.value = false
        }
    }

    private fun handleKategoriFallback(errorMessage: String) {
        _kategoriError.value = errorMessage
        _kategoriList.value = mutableListOf("Umum")
        _kategoriIndex.value = 0
    }

    fun retryFetchKategori() {
        fetchKategoriFromServer()
    }

    fun clearKategoriError() {
        _kategoriError.value = null
    }

    // ===== Submit Functions =====
    fun submitBarang() {
        viewModelScope.launch {
            _submitLoading.value = true
            _submitError.value = null
            _submitSuccess.value = false

            try {
                // Build request dari current form data
                val request = buildTambahBarangRequest()

                val response = barangRepository.TambahBarang(request)

                if (response.isSuccessful) {
                    val body = response.body()
                    if (body?.success == true) {
                        _submitSuccess.value = true
                        // Clear form setelah success
                        clearAll()
                    } else {
                        _submitError.value = body?.message ?: "Gagal menyimpan barang"
                    }
                } else {
                    // Parse error response
                    val errorBody = response.errorBody()?.string()
                    if (!errorBody.isNullOrEmpty()) {
                        try {
                            val gson = Gson()
                            val errorResponse = gson.fromJson(errorBody, ErrorResponse::class.java)
                            val errorMessage = if (errorResponse.errors != null) {
                                // Format error validation messages
                                errorResponse.errors.values.flatten().joinToString("\n")
                            } else {
                                errorResponse.message
                            }
                            _submitError.value = errorMessage
                        } catch (e: Exception) {
                            _submitError.value = "Server error (HTTP ${response.code()})"
                        }
                    } else {
                        _submitError.value = "Server error (HTTP ${response.code()})"
                    }
                }
            } catch (e: Exception) {
                _submitError.value = "Koneksi bermasalah: ${e.message}"
            }

            _submitLoading.value = false
        }
    }

    private fun buildTambahBarangRequest(): TambahBarangRequest {
        val nama = _nama.value.orEmpty()
        val qty = _kuantitas.value?.toIntOrNull() ?: 0
        val kategori = _kategoriList.value?.getOrNull(_kategoriIndex.value ?: 0).orEmpty()
        val barcodeVal = _barcode.value.orEmpty().ifEmpty { null }
        val hargaModalStr = _hargaModal.value.orEmpty()
        val hargaJualStr = _hargaJual.value.orEmpty()
        val gambarPath = _imageUri.value // Use imageUri as gambar_path

        fun parseRupiahToLong(s: String): Long {
            val onlyDigits = s.replace("[^0-9]".toRegex(), "")
            return onlyDigits.takeIf { it.isNotEmpty() }?.toLong() ?: 0L
        }

        return TambahBarangRequest(
            nama = nama,
            kategori = kategori,
            stok = qty,
            harga = parseRupiahToLong(hargaJualStr),
            modal = parseRupiahToLong(hargaModalStr),
            barcode = barcodeVal,
            gambarPath = gambarPath
        )
    }

    fun clearSubmitStates() {
        _submitSuccess.value = false
        _submitError.value = null
    }

    // ===== setters / helpers =====
    fun setNama(v: String) { _nama.value = v }
    fun setKuantitas(v: String) { _kuantitas.value = v }
    fun setBarcode(v: String) { _barcode.value = v }
    fun setHargaModal(v: String) { _hargaModal.value = v }
    fun setHargaJual(v: String) { _hargaJual.value = v }
    fun setImageUri(uriString: String?) { _imageUri.value = uriString }

    fun addKategori(k: String) {
        if (k.isBlank()) return

        // Tambah ke list kategori utama
        val mainList = _kategoriList.value ?: mutableListOf()
        mainList.add(k)
        _kategoriList.value = mainList

        // Simpan juga ke list kategori baru (untuk reference)
        val newList = _newKategoriList.value ?: mutableListOf()
        newList.add(k)
        _newKategoriList.value = newList

        // Set selection ke kategori yang baru ditambahkan
        _kategoriIndex.value = mainList.size - 1
    }

    fun setKategoriIndex(index: Int) {
        val list = _kategoriList.value ?: return
        if (index in 0 until list.size) {
            _kategoriIndex.value = index
        }
    }

    fun clearAll() {
        _nama.value = ""
        _kuantitas.value = ""
        _barcode.value = ""
        _hargaModal.value = ""
        _hargaJual.value = ""
        _imageUri.value = null
        _newKategoriList.value = mutableListOf()

        // Reset kategori ke default atau fetch ulang dari server
        fetchKategoriFromServer()

        // Clear submit states
        clearSubmitStates()
    }

    // Build payload for validation (keep for compatibility)
    fun buildPayload(): ItemPayload {
        val nama = _nama.value.orEmpty()
        val qty = _kuantitas.value?.toIntOrNull() ?: 0
        val kategori = _kategoriList.value?.getOrNull(_kategoriIndex.value ?: 0).orEmpty()
        val barcodeVal = _barcode.value.orEmpty().ifEmpty { null }
        val hargaModalStr = _hargaModal.value.orEmpty()
        val hargaJualStr = _hargaJual.value.orEmpty()
        val newKategories = _newKategoriList.value ?: mutableListOf()

        fun parseRupiahToLongLocal(s: String): Long {
            val onlyDigits = s.replace("[^0-9]".toRegex(), "")
            return onlyDigits.takeIf { it.isNotEmpty() }?.toLong() ?: 0L
        }

        return ItemPayload(
            nama = nama,
            kuantitas = qty,
            kategori = kategori,
            barcode = barcodeVal,
            hargaModal = parseRupiahToLongLocal(hargaModalStr),
            hargaJual = parseRupiahToLongLocal(hargaJualStr),
            imageUri = _imageUri.value,
            newKategories = newKategories
        )
    }

    data class ItemPayload(
        val nama: String,
        val kuantitas: Int,
        val kategori: String,
        val barcode: String?,
        val hargaModal: Long,
        val hargaJual: Long,
        val imageUri: String?,
        val newKategories: List<String> = emptyList()
    )
}