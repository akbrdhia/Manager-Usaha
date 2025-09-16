package com.application.managerusahav2.ui.fragment.tambah_barang

import android.net.Uri
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel

class TambahBarangViewModel : ViewModel() {

    // Fields (string semua biar gampang bind ke EditText)
    private val _nama = MutableLiveData<String>("")
    val nama: LiveData<String> = _nama

    private val _kuantitas = MutableLiveData<String>("")
    val kuantitas: LiveData<String> = _kuantitas

    // kategorinya disimpan sebagai list + selected index
    private val _kategoriList = MutableLiveData<MutableList<String>>(mutableListOf("Umum"))
    val kategoriList: LiveData<MutableList<String>> = _kategoriList

    private val _kategoriIndex = MutableLiveData<Int>(0)
    val kategoriIndex: LiveData<Int> = _kategoriIndex

    private val _barcode = MutableLiveData<String>("")
    val barcode: LiveData<String> = _barcode

    private val _hargaModal = MutableLiveData<String>("")
    val hargaModal: LiveData<String> = _hargaModal

    private val _hargaJual = MutableLiveData<String>("")
    val hargaJual: LiveData<String> = _hargaJual

    // image uri as string (nullable)
    private val _imageUri = MutableLiveData<String?>(null)
    val imageUri: LiveData<String?> = _imageUri

    // ===== setters / helpers =====
    fun setNama(v: String) { _nama.value = v }
    fun setKuantitas(v: String) { _kuantitas.value = v }
    fun setBarcode(v: String) { _barcode.value = v }
    fun setHargaModal(v: String) { _hargaModal.value = v }
    fun setHargaJual(v: String) { _hargaJual.value = v }
    fun setImageUri(uriString: String?) { _imageUri.value = uriString }

    fun addKategori(k: String) {
        if (k.isBlank()) return
        val list = _kategoriList.value ?: mutableListOf()
        list.add(k)
        _kategoriList.value = list
        _kategoriIndex.value = list.size - 1
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
        _kategoriList.value = mutableListOf("Umum")
        _kategoriIndex.value = 0
    }

    // Build payload if needed (optional)
    fun buildPayload(): TambahBarangFragment.ItemPayload {
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

        return TambahBarangFragment.ItemPayload(
            nama = nama,
            kuantitas = qty,
            kategori = kategori,
            barcode = barcodeVal,
            hargaModal = parseRupiahToLongLocal(hargaModalStr),
            hargaJual = parseRupiahToLongLocal(hargaJualStr),
            imageUri = _imageUri.value
        )
    }
}
