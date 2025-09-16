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
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.Observer
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

    private val viewModel: TambahBarangViewModel by activityViewModels()

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

    // adapter
    private lateinit var kategoriAdapter: ArrayAdapter<String>

    companion object {
        private const val BARCODE_RESULT_KEY = "barcode_result"
    }

    // Launcher: pick image from gallery
    private val pickImageLauncher = registerForActivityResult(
        ActivityResultContracts.GetContent()
    ) { uri: Uri? ->
        if (uri != null) {
            viewModel.setImageUri(uri.toString())
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
        setupObservers()
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
        // initial adapter from ViewModel's kategori list
        val initial = viewModel.kategoriList.value ?: mutableListOf("Umum")
        kategoriAdapter = ArrayAdapter(requireContext(), android.R.layout.simple_spinner_item, initial)
        kategoriAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        kategoriSpinner.adapter = kategoriAdapter

        kategoriSpinner.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(parent: AdapterView<*>?, v: View?, pos: Int, id: Long) {
                // update viewmodel
                viewModel.setKategoriIndex(pos)
            }
            override fun onNothingSelected(parent: AdapterView<*>?) {}
        }
    }

    private fun setupListeners() {
        btnBack.setOnClickListener { findNavController().popBackStack() }

        btnAddKategori.setOnClickListener { showAddKategoriDialog() }

        inputLayoutBarcode.setEndIconOnClickListener {
            findNavController().navigate(R.id.action_tambahBarangFragment_to_barcodeScannerFragment)
        }

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
                viewModel.setBarcode(barcodeValue)
                Toast.makeText(requireContext(), "Barcode berhasil dipindai", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun setupTextWatchers() {
        // nama
        etNamaBarang.addTextChangedListener(SimpleTextWatcher { viewModel.setNama(it) })
        etKuantitas.addTextChangedListener(SimpleTextWatcher { viewModel.setKuantitas(it) })
        etBarcode.addTextChangedListener(SimpleTextWatcher { viewModel.setBarcode(it) })

        etHargaModal.addTextChangedListener(RupiahTextWatcher(etHargaModal) { formatted ->
            viewModel.setHargaModal(formatted)
        })

        etHargaJual.addTextChangedListener(RupiahTextWatcher(etHargaJual) { formatted ->
            viewModel.setHargaJual(formatted)
        })
    }

    private fun setupObservers() {
        // restore nama
        viewModel.nama.observe(viewLifecycleOwner, Observer { value ->
            if (etNamaBarang.text?.toString() != value) {
                etNamaBarang.setText(value)
                etNamaBarang.setSelection(value.length)
            }
        })

        // kuantitas
        viewModel.kuantitas.observe(viewLifecycleOwner, Observer { value ->
            if (etKuantitas.text?.toString() != value) {
                etKuantitas.setText(value)
                etKuantitas.setSelection(value.length)
            }
        })

        // barcode
        viewModel.barcode.observe(viewLifecycleOwner, Observer { value ->
            if (etBarcode.text?.toString() != value) {
                etBarcode.setText(value)
                etBarcode.setSelection(value.length)
            }
        })

        // harga modal
        viewModel.hargaModal.observe(viewLifecycleOwner, Observer { value ->
            if (etHargaModal.text?.toString() != value) {
                etHargaModal.setText(value)
                etHargaModal.setSelection(value.length)
            }
        })

        // harga jual
        viewModel.hargaJual.observe(viewLifecycleOwner, Observer { value ->
            if (etHargaJual.text?.toString() != value) {
                etHargaJual.setText(value)
                etHargaJual.setSelection(value.length)
            }
        })

        // kategori list observer
        viewModel.kategoriList.observe(viewLifecycleOwner, Observer { list ->
            kategoriAdapter.clear()
            kategoriAdapter.addAll(list)
            kategoriAdapter.notifyDataSetChanged()

            // restore selection
            val idx = viewModel.kategoriIndex.value ?: 0
            if (idx in 0 until kategoriAdapter.count) {
                kategoriSpinner.setSelection(idx)
            }
        })

        // kategori index observer (in case changed programmatically)
        viewModel.kategoriIndex.observe(viewLifecycleOwner, Observer { idx ->
            if (idx in 0 until kategoriAdapter.count && kategoriSpinner.selectedItemPosition != idx) {
                kategoriSpinner.setSelection(idx)
            }
        })

        // image uri observer
        viewModel.imageUri.observe(viewLifecycleOwner, Observer { uriString ->
            if (!uriString.isNullOrEmpty()) {
                try {
                    showImagePreview(Uri.parse(uriString))
                } catch (e: Exception) {
                    restorePlaceholderImage()
                }
            } else {
                restorePlaceholderImage()
            }
        })
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
                    viewModel.addKategori(text)
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
            scaleType = ImageView.ScaleType.FIT_CENTER
        }
        photoCard.addView(iv)

        Glide.with(this)
            .load(uri)
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
        viewModel.clearAll()
        Toast.makeText(requireContext(), "Form dibersihkan", Toast.LENGTH_SHORT).show()
    }

    private fun submitForm() {
        // Gunakan viewModel.buildPayload() untuk data terakhir
        val payload = viewModel.buildPayload()

        // Validasi sederhana (mirip sebelumnya)
        if (payload.nama.isEmpty()) {
            etNamaBarang.error = "Nama barang wajib diisi"
            etNamaBarang.requestFocus()
            return
        }

        if (payload.kuantitas <= 0) {
            etKuantitas.error = "Masukkan kuantitas valid (> 0)"
            etKuantitas.requestFocus()
            return
        }

        if (payload.hargaModal <= 0L) {
            etHargaModal.error = "Harga modal tidak valid"
            etHargaModal.requestFocus()
            return
        }

        if (payload.hargaJual <= 0L) {
            etHargaJual.error = "Harga jual tidak valid"
            etHargaJual.requestFocus()
            return
        }

        if (payload.hargaJual < payload.hargaModal) {
            Toast.makeText(requireContext(), "Peringatan: Harga jual lebih kecil dari harga modal", Toast.LENGTH_LONG).show()
        }

        // TODO: kirim payload ke ViewModel/Repository untuk upload
        Toast.makeText(requireContext(), "Data siap dikirim. Cek ViewModel untuk proses upload.", Toast.LENGTH_SHORT).show()
    }

    // Helper: parse formatted number -> Long (tidak berubah)
    private fun parseRupiahToLong(s: String?): Long? {
        if (s.isNullOrEmpty()) return 0L
        val onlyDigits = s.replace("[^0-9]".toRegex(), "")
        return onlyDigits.takeIf { it.isNotEmpty() }?.toLong()
    }

    // Simple TextWatcher util
    inner class SimpleTextWatcher(val onChange: (String) -> Unit) : TextWatcher {
        override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
        override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}
        override fun afterTextChanged(s: Editable?) {
            onChange(s?.toString().orEmpty())
        }
    }

    // Rupiah TextWatcher (thousand separator) with callback
    inner class RupiahTextWatcher(
        private val editText: TextInputEditText,
        private val onFormatted: (String) -> Unit
    ) : TextWatcher {
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
            onFormatted(formatted)
            editText.addTextChangedListener(this)
        }
    }
    data class ItemPayload( val nama: String, val kuantitas: Int, val kategori: String, val barcode: String?, val hargaModal: Long, val hargaJual: Long, val imageUri: String? )
}


