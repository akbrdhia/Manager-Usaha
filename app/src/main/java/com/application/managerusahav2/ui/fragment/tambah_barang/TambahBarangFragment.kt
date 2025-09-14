package com.application.managerusahav2.ui.fragment.tambah_barang

import android.net.Uri
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.*
import androidx.activity.result.contract.ActivityResultContracts
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import com.application.managerusahav2.R
import com.bumptech.glide.Glide
import com.google.android.material.button.MaterialButton
import com.google.android.material.card.MaterialCardView
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.google.android.material.textfield.TextInputEditText
import com.google.android.material.textfield.TextInputLayout
import java.text.NumberFormat
import java.util.*

class TambahBarangFragment : Fragment() {

    private val viewModel: TambahBarangViewModel by viewModels()

    // Views
    private lateinit var btnBack: ImageView
    private lateinit var tvTitle: TextView
    private lateinit var etNamaBarang: TextInputEditText
    private lateinit var etKuantitas: TextInputEditText
    private lateinit var kategoriSpinner: Spinner
    private lateinit var btnAddKategori: MaterialButton
    private lateinit var etBarcode: TextInputEditText
    private lateinit var inputLayoutBarcode: TextInputLayout
    private lateinit var etHargaModal: TextInputEditText
    private lateinit var etHargaJual: TextInputEditText
    private lateinit var photoCard: MaterialCardView
    private lateinit var btnBatal: MaterialButton
    private lateinit var btnTambah: MaterialButton

    // Data
    private val categories = mutableListOf("Umum")
    private lateinit var kategoriAdapter: ArrayAdapter<String>

    // selected image uri (string)
    private var selectedImageUriString: String? = null

    companion object {
        private const val BARCODE_RESULT_KEY = "barcode_result"
    }

    // Launcher: pick image from gallery
    private val  pickImageLauncher = registerForActivityResult(
        ActivityResultContracts.GetContent()
    ) { uri: Uri? ->
        if (uri != null) {
            selectedImageUriString = uri.toString()
            showImagePreview(uri)
        }
    }


    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?,
    ): View {
        return inflater.inflate(R.layout.fragment_tambah_barang, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        inisialisasi(view)
        setupKategoriSpinner()
        setupListeners()
        setupTextWatchers()
        restorePlaceholderImage()

        setupBarcodeResultListener()
    }

    private fun inisialisasi(view: View) {
        btnBack = view.findViewById(R.id.btn_back)
        tvTitle = view.findViewById(R.id.tv_title)
        etNamaBarang = view.findViewById(R.id.et_nama_barang)
        etKuantitas = view.findViewById(R.id.et_kuantitas)
        kategoriSpinner = view.findViewById(R.id.kategori_spinner)
        btnAddKategori = view.findViewById(R.id.btn_add_kategori)
        etBarcode = view.findViewById(R.id.et_barcode)
        inputLayoutBarcode = view.findViewById(R.id.input_layout_barcode)
        etHargaModal = view.findViewById(R.id.et_harga_modal)
        etHargaJual = view.findViewById(R.id.et_harga_jual)
        photoCard = view.findViewById(R.id.photo_card_1)
        btnBatal = view.findViewById(R.id.btn_batal)
        btnTambah = view.findViewById(R.id.btn_tambah)
    }

    private fun setupKategoriSpinner() {
        kategoriAdapter = ArrayAdapter(requireContext(), android.R.layout.simple_spinner_item, categories)
        kategoriAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        kategoriSpinner.adapter = kategoriAdapter
    }

    private fun setupListeners() {
        btnBack.setOnClickListener { findNavController().popBackStack()}

        btnAddKategori.setOnClickListener { showAddKategoriDialog() }

        // Update bagian ini - end icon pada TextInputLayout (barcode)
        inputLayoutBarcode.setEndIconOnClickListener {
            // Navigate ke scanner fragment
            findNavController().navigate(R.id.action_tambahBarangFragment_to_barcodeScannerFragment)
        }

        // buka gallery untuk pilih gambar
        photoCard.setOnClickListener {
            pickImageLauncher.launch("image/*")
        }

        btnBatal.setOnClickListener {
            clearForm()
        }

        btnTambah.setOnClickListener {
            submitForm()
        }
    }

    private fun setupBarcodeResultListener() {
        parentFragmentManager.setFragmentResultListener(
            BARCODE_RESULT_KEY,
            viewLifecycleOwner
        ) { _, result ->
            val barcodeValue = result.getString("barcode_value")
            if (!barcodeValue.isNullOrEmpty()) {
                etBarcode.setText(barcodeValue)
                Toast.makeText(requireContext(), "Barcode berhasil dipindai", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun setupTextWatchers() {
        etHargaModal.addTextChangedListener(RupiahTextWatcher(etHargaModal))
        etHargaJual.addTextChangedListener(RupiahTextWatcher(etHargaJual))
        // kuantitas udah diatur inputType=number di XML
    }

    private fun showAddKategoriDialog() {
        val edit = EditText(requireContext()).apply {
            hint = "Nama kategori"
        }
        MaterialAlertDialogBuilder(requireContext())
            .setTitle("Tambah Kategori")
            .setView(edit)
            .setPositiveButton("Tambah") { dialog, _ ->
                val text = edit.text.toString().trim()
                if (text.isNotEmpty()) {
                    categories.add(text)
                    kategoriAdapter.notifyDataSetChanged()
                    kategoriSpinner.setSelection(categories.size - 1)
                    Toast.makeText(requireContext(), "Kategori '$text' ditambahkan", Toast.LENGTH_SHORT).show()
                } else {
                    Toast.makeText(requireContext(), "Nama kategori kosong", Toast.LENGTH_SHORT).show()
                }
                dialog.dismiss()
            }
            .setNegativeButton("Batal") { d, _ -> d.dismiss() }
            .show()
    }

    private fun showImagePreview(uri: Uri) {
        photoCard.removeAllViews()
        val iv = ImageView(requireContext()).apply {
            layoutParams = FrameLayout.LayoutParams(
                FrameLayout.LayoutParams.MATCH_PARENT,
                FrameLayout.LayoutParams.MATCH_PARENT
            )
            // Change this to FIT_CENTER for proper fitting
            scaleType = ImageView.ScaleType.FIT_CENTER
            // Optional: add padding if you want some space around the image
            // setPadding(8, 8, 8, 8)
        }
        photoCard.addView(iv)

        Glide.with(this)
            .load(uri)
            // Remove fitCenter() since scaleType handles it now
            .placeholder(R.drawable.ic_placeholder_image)
            .error(R.drawable.ic_placeholder_image)
            .into(iv)
    }

    private fun restorePlaceholderImage() {
        photoCard.removeAllViews()
        val iv = ImageView(requireContext()).apply {
            layoutParams = FrameLayout.LayoutParams(
                FrameLayout.LayoutParams.MATCH_PARENT,
                FrameLayout.LayoutParams.MATCH_PARENT
            )
            scaleType = ImageView.ScaleType.CENTER_INSIDE
            alpha = 0.5f
        }
        photoCard.addView(iv)

        Glide.with(this)
            .load(R.drawable.ic_placeholder_image)
            .into(iv)
    }

    private fun clearForm() {
        etNamaBarang.text?.clear()
        etKuantitas.text?.clear()
        etBarcode.text?.clear()
        etHargaModal.text?.clear()
        etHargaJual.text?.clear()
        selectedImageUriString = null
        kategoriSpinner.setSelection(0)
        restorePlaceholderImage()
    }

    private fun submitForm() {
        val nama = etNamaBarang.text?.toString()?.trim().orEmpty()
        val qtyStr = etKuantitas.text?.toString()?.trim().orEmpty()
        val kategori = kategoriSpinner.selectedItem?.toString().orEmpty()
        val barcode = etBarcode.text?.toString()?.trim().orEmpty()
        val hargaModalStr = etHargaModal.text?.toString()?.trim().orEmpty()
        val hargaJualStr = etHargaJual.text?.toString()?.trim().orEmpty()

        // Validasi
        if (nama.isEmpty()) {
            etNamaBarang.error = "Nama barang wajib diisi"
            etNamaBarang.requestFocus()
            return
        }
        if (qtyStr.isEmpty()) {
            etKuantitas.error = "Kuantitas wajib diisi"
            etKuantitas.requestFocus()
            return
        }
        val qty = qtyStr.toIntOrNull()
        if (qty == null || qty <= 0) {
            etKuantitas.error = "Masukkan kuantitas valid (> 0)"
            etKuantitas.requestFocus()
            return
        }
        val hargaModal = parseRupiahToLong(hargaModalStr)
        val hargaJual = parseRupiahToLong(hargaJualStr)
        if (hargaModal == null) {
            etHargaModal.error = "Harga modal tidak valid"
            etHargaModal.requestFocus()
            return
        }
        if (hargaJual == null) {
            etHargaJual.error = "Harga jual tidak valid"
            etHargaJual.requestFocus()
            return
        }
        if (hargaJual < hargaModal) {
            Toast.makeText(requireContext(), "Peringatan: Harga jual lebih kecil dari harga modal", Toast.LENGTH_LONG).show()
            // tidak return — kita izinkan, cuma kasih peringatan. Ubah sesuai aturan lo.
        }

        val payload = ItemPayload(
            nama = nama,
            kuantitas = qty,
            kategori = kategori,
            barcode = if (barcode.isEmpty()) null else barcode,
            hargaModal = hargaModal,
            hargaJual = hargaJual,
            imageUri = selectedImageUriString
        )

        // Kirim ke ViewModel -> implementasi upload di ViewModel/Repository
        // viewModel.uploadItem(payload)

        Toast.makeText(requireContext(), "Data siap dikirim. Cek ViewModel untuk proses upload.", Toast.LENGTH_SHORT).show()
    }

    // Helper: parse formatted number -> Long
    private fun parseRupiahToLong(s: String?): Long? {
        if (s.isNullOrEmpty()) return 0L
        val onlyDigits = s.replace("[^0-9]".toRegex(), "")
        return onlyDigits.takeIf { it.isNotEmpty() }?.toLong()
    }

    // Data class payload
    data class ItemPayload(
        val nama: String,
        val kuantitas: Int,
        val kategori: String,
        val barcode: String?,
        val hargaModal: Long,
        val hargaJual: Long,
        val imageUri: String?
    )

    // Rupiah TextWatcher (thousand separator)
    inner class RupiahTextWatcher(private val editText: TextInputEditText) : TextWatcher {
        private var current = ""
        override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
        override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}
        override fun afterTextChanged(s: Editable?) {
            if (s == null) return
            if (s.toString() == current) return

            editText.removeTextChangedListener(this)
            val digits = s.toString().replace("[^0-9]".toRegex(), "")
            val parsed = if (digits.isEmpty()) 0L else digits.toLong()
            val nf = NumberFormat.getInstance(Locale("in", "ID"))
            val formatted = nf.format(parsed)

            current = formatted
            editText.setText(formatted)
            editText.setSelection(formatted.length)
            editText.addTextChangedListener(this)
        }
    }
}
