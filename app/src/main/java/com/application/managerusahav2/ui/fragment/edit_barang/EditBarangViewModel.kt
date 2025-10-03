package com.application.managerusahav2.ui.fragment.edit_barang

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.application.managerusahav2.data.model.request.EditBarangRequest
import com.application.managerusahav2.data.model.response.Barang
import com.application.managerusahav2.data.model.response.ErrorResponse
import com.application.managerusahav2.data.repository.BarangRepository
import com.google.gson.Gson
import kotlinx.coroutines.launch

class EditBarangViewModel(private val barangRepository: BarangRepository) : ViewModel() {

    // Original barang data
    private var originalBarang: Barang? = null

    // Form fields
    private val _nama = MutableLiveData<String>("")
    val nama: LiveData<String> = _nama

    private val _kuantitas = MutableLiveData<String>("")
    val kuantitas: LiveData<String> = _kuantitas

    private val _kategoriList = MutableLiveData<MutableList<String>>(mutableListOf())
    val kategoriList: LiveData<MutableList<String>> = _kategoriList

    private val _kategoriIndex = MutableLiveData<Int>(0)
    val kategoriIndex: LiveData<Int> = _kategoriIndex

    // Loading state untuk kategori
    private val _kategoriLoading = MutableLiveData<Boolean>(false)
    val kategoriLoading: LiveData<Boolean> = _kategoriLoading

    private val _kategoriError = MutableLiveData<String?>(null)
    val kategoriError: LiveData<String?> = _kategoriError

    private val _barcode = MutableLiveData<String>("")
    val barcode: LiveData<String> = _barcode

    private val _hargaModal = MutableLiveData<String>("")
    val hargaModal: LiveData<String> = _hargaModal

    private val _hargaJual = MutableLiveData<String>("")
    val hargaJual: LiveData<String> = _hargaJual

    // Image handling - keep track of original vs new
    private val _imageUri = MutableLiveData<String?>(null)
    val imageUri: LiveData<String?> = _imageUri
    private var isImageChanged = false

    // Submit states
    private val _submitLoading = MutableLiveData<Boolean>(false)
    val submitLoading: LiveData<Boolean> = _submitLoading

    private val _submitSuccess = MutableLiveData<Boolean>(false)
    val submitSuccess: LiveData<Boolean> = _submitSuccess

    private val _submitError = MutableLiveData<String?>(null)
    val submitError: LiveData<String?> = _submitError

    // ===== Initialization =====
    fun initializeWithBarang(barang: Barang) {
        originalBarang = barang
        populateFormData(barang)
        fetchKategoriFromServer()
    }

    private fun populateFormData(barang: Barang) {
        _nama.value = barang.nama
        _kuantitas.value = barang.stok.toString()
        _barcode.value = barang.barcode ?: ""

        // Fix: Safe conversion from Any to Long
        val hargaModalLong = when (val modal = barang.modal) {
            is Long -> modal
            else -> 0L
        }

        val hargaJualLong = when (val harga = barang.harga) {
            is Long -> harga

            else -> 0L
        }

        _hargaModal.value = formatCurrencyForDisplay(hargaModalLong)
        _hargaJual.value = formatCurrencyForDisplay(hargaJualLong)

        _imageUri.value = barang.gambarPath
        isImageChanged = false
    }

    private fun formatCurrencyForDisplay(amount: Long): String {
        val nf = java.text.NumberFormat.getInstance(java.util.Locale("in", "ID"))
        return nf.format(amount)
    }

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

                        if (serverKategori.isEmpty()) {
                            serverKategori.add("Umum")
                        }

                        _kategoriList.value = serverKategori

                        // Set spinner to current barang kategori
                        originalBarang?.let { barang ->
                            val kategoriIndex = serverKategori.indexOf(barang.kategori)
                            _kategoriIndex.value = if (kategoriIndex >= 0) kategoriIndex else 0
                        }
                    } else {
                        handleKategoriFallback("Server response tidak valid")
                    }
                } else {
                    handleKategoriFallback("Gagal mengambil data kategori (HTTP ${response.code()})")
                }
            } catch (e: Exception) {
                handleKategoriFallback("Koneksi bermasalah: ${e.message}")
            }

            _kategoriLoading.value = false
        }
    }

    private fun handleKategoriFallback(errorMessage: String) {
        _kategoriError.value = errorMessage
        val fallbackList = mutableListOf("Umum")
        originalBarang?.kategori?.let { currentKategori ->
            if (!fallbackList.contains(currentKategori)) {
                fallbackList.add(currentKategori)
            }
        }
        _kategoriList.value = fallbackList
        _kategoriIndex.value = fallbackList.indexOf(originalBarang?.kategori ?: "Umum").takeIf { it >= 0 } ?: 0
    }

    fun retryFetchKategori() {
        fetchKategoriFromServer()
    }

    fun clearKategoriError() {
        _kategoriError.value = null
    }

    // ===== Form Setters =====
    fun setNama(v: String) { _nama.value = v }
    fun setKuantitas(v: String) { _kuantitas.value = v }
    fun setBarcode(v: String) { _barcode.value = v }
    fun setHargaModal(v: String) { _hargaModal.value = v }
    fun setHargaJual(v: String) { _hargaJual.value = v }

    fun setImageUri(uriString: String?) {
        _imageUri.value = uriString
        isImageChanged = true // Mark as changed when user selects new image
    }

    fun addKategori(k: String) {
        if (k.isBlank()) return

        val mainList = _kategoriList.value ?: mutableListOf()
        mainList.add(k)
        _kategoriList.value = mainList
        _kategoriIndex.value = mainList.size - 1
    }

    fun setKategoriIndex(index: Int) {
        val list = _kategoriList.value ?: return
        if (index in 0 until list.size) {
            _kategoriIndex.value = index
        }
    }

    // ===== Submit Functions =====
    fun submitEdit() {
        originalBarang?.let { barang ->
            viewModelScope.launch {
                _submitLoading.value = true
                _submitError.value = null
                _submitSuccess.value = false

                try {
                    val request = buildEditRequest()
                    val response = barangRepository.EditBarang(barang.id, request)

                    if (response.isSuccessful) {
                        val body = response.body()
                        if (body?.success == true) {
                            _submitSuccess.value = true
                        } else {
                            _submitError.value = body?.message ?: "Gagal mengupdate barang"
                        }
                    } else {
                        val errorBody = response.errorBody()?.string()
                        val errorMessage = if (!errorBody.isNullOrEmpty()) {
                            try {
                                val gson = Gson()
                                val errorResponse = gson.fromJson(errorBody, ErrorResponse::class.java)
                                if (errorResponse.errors != null) {
                                    errorResponse.errors.values.flatten().joinToString("\n")
                                } else {
                                    errorResponse.message
                                }
                            } catch (e: Exception) {
                                "Server error (HTTP ${response.code()})"
                            }
                        } else {
                            "Server error (HTTP ${response.code()})"
                        }
                        _submitError.value = errorMessage
                    }
                } catch (e: Exception) {
                    _submitError.value = "Koneksi bermasalah: ${e.message}"
                }

                _submitLoading.value = false
            }
        }
    }

    private fun buildEditRequest(): EditBarangRequest {
        val nama = _nama.value.orEmpty()
        val qty = _kuantitas.value?.toIntOrNull() ?: 0
        val kategori = _kategoriList.value?.getOrNull(_kategoriIndex.value ?: 0).orEmpty()
        val barcodeVal = _barcode.value.orEmpty().ifEmpty { null }
        val hargaModalStr = _hargaModal.value.orEmpty()
        val hargaJualStr = _hargaJual.value.orEmpty()

        // For image, use new image if changed, otherwise keep original
        val gambarPath = if (isImageChanged) {
            _imageUri.value
        } else {
            originalBarang?.gambarPath
        }

        fun parseRupiahToLong(s: String): Long {
            val onlyDigits = s.replace("[^0-9]".toRegex(), "")
            return onlyDigits.takeIf { it.isNotEmpty() }?.toLong() ?: 0L
        }

        return EditBarangRequest(
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

    // ===== Validation Payload =====
    fun buildPayloadForValidation(): EditPayload {
        val nama = _nama.value.orEmpty()
        val qty = _kuantitas.value?.toIntOrNull() ?: 0
        val kategori = _kategoriList.value?.getOrNull(_kategoriIndex.value ?: 0).orEmpty()
        val barcodeVal = _barcode.value.orEmpty().ifEmpty { null }
        val hargaModalStr = _hargaModal.value.orEmpty()
        val hargaJualStr = _hargaJual.value.orEmpty()

        fun parseRupiahToLongLocal(s: String): Long {
            val onlyDigits = s.replace("[^0-9]".toRegex(), "")
            return onlyDigits.takeIf { it.isNotEmpty() }?.toLong() ?: 0L
        }

        return EditPayload(
            nama = nama,
            kuantitas = qty,
            kategori = kategori,
            barcode = barcodeVal,
            hargaModal = parseRupiahToLongLocal(hargaModalStr),
            hargaJual = parseRupiahToLongLocal(hargaJualStr),
            imageUri = _imageUri.value
        )
    }

    data class EditPayload(
        val nama: String,
        val kuantitas: Int,
        val kategori: String,
        val barcode: String?,
        val hargaModal: Long,
        val hargaJual: Long,
        val imageUri: String?
    )
}