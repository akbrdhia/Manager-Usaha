package com.application.managerusahav2.ui.fragment.edit_barang

import android.content.ContentValues
import android.content.Context
import android.net.Uri
import android.os.Bundle
import android.os.Environment
import android.provider.MediaStore
import android.text.Editable
import android.text.TextWatcher
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.inputmethod.InputMethodManager
import android.widget.*
import androidx.activity.result.contract.ActivityResultContracts
import androidx.fragment.app.Fragment
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.fragment.findNavController
import com.application.managerusahav2.R
import com.application.managerusahav2.data.model.response.Barang
import com.application.managerusahav2.data.network.RetrofitClient
import com.application.managerusahav2.data.repository.BarangRepository
import com.bumptech.glide.Glide
import com.google.android.material.button.MaterialButton
import com.google.android.material.card.MaterialCardView
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.google.android.material.textfield.TextInputEditText
import com.google.android.material.textfield.TextInputLayout
import java.text.NumberFormat
import java.util.*

class EditBarangFragment : Fragment() {

    private lateinit var barangRepository: BarangRepository
    private lateinit var viewModel: EditBarangViewModel

    // Views
    private lateinit var btnBack: ImageView
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
    private lateinit var btnSimpan: MaterialButton

    // Loading views untuk kategori
    private lateinit var kategoriProgressBar: ProgressBar

    // Adapter
    private lateinit var kategoriAdapter: ArrayAdapter<String>

    // Barang data
    private var barang: Barang? = null

    companion object {
        private const val ARG_BARANG = "arg_barang"
        // Fix: Define BARCODE_RESULT_KEY
        private const val BARCODE_RESULT_KEY = "barcode_result"

        fun newInstance(barang: Barang): EditBarangFragment {
            return EditBarangFragment().apply {
                arguments = Bundle().apply {
                    putSerializable(ARG_BARANG, barang)
                }
            }
        }
    }

    // Image picker launcher
    private val pickImageLauncher = registerForActivityResult(
        ActivityResultContracts.GetContent()
    ) { uri: Uri? ->
        if (uri != null) {
            viewModel.setImageUri(uri.toString())
            showImagePreview(uri)
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        barang = arguments?.getSerializable(ARG_BARANG) as? Barang
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?,
    ): View {
        return inflater.inflate(R.layout.fragment_edit_barang, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        initializeViewModel()
        inisialisasi(view)
        setupKategoriSpinner()
        setupListeners()
        setupTextWatchers()
        setupObservers()
        setupBarcodeResultListener()

        // Initialize dengan data barang
        barang?.let {
            viewModel.initializeWithBarang(it)
            showInitialImage(it.gambarPath)
        }
    }

    private fun initializeViewModel() {
        val barangService = RetrofitClient.getInstance()
        barangRepository = BarangRepository(barangService)
        val factory = EditBarangViewModelFactory(barangRepository)
        viewModel = ViewModelProvider(this, factory)[EditBarangViewModel::class.java]
    }

    private fun inisialisasi(view: View) {
        btnBack = view.findViewById(R.id.btn_back)
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
        btnSimpan = view.findViewById(R.id.btn_simpan)

        // Kategori loading
        kategoriProgressBar = view.findViewById(R.id.kategori_progress_bar)
    }

    private fun setupKategoriSpinner() {
        kategoriAdapter = ArrayAdapter(requireContext(), android.R.layout.simple_spinner_item, mutableListOf<String>())
        kategoriAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        kategoriSpinner.adapter = kategoriAdapter

        kategoriSpinner.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(parent: AdapterView<*>?, v: View?, pos: Int, id: Long) {
                viewModel.setKategoriIndex(pos)
            }
            override fun onNothingSelected(parent: AdapterView<*>?) {}
        }
    }

    private fun setupListeners() {
        btnBack.setOnClickListener {
            findNavController().popBackStack()
        }

        btnAddKategori.setOnClickListener {
            showAddKategoriDialog()
        }

        // Fix: Use generic barcode navigation or disable for now
        inputLayoutBarcode.setEndIconOnClickListener {
            try {

               findNavController().navigate(R.id.action_editBarangFragment_to_barcodeScannerFragment)

                // Option 2: Temporary fallback
                Toast.makeText(requireContext(), "Barcode scanner - Coming soon", Toast.LENGTH_SHORT).show()
            } catch (e: Exception) {
                Toast.makeText(requireContext(), "Barcode scanner tidak tersedia", Toast.LENGTH_SHORT).show()
            }
        }

        photoCard.setOnClickListener {
            pickImageLauncher.launch("image/*")
        }

        btnBatal.setOnClickListener {
            showResetConfirmation()
        }

        btnSimpan.setOnClickListener {
            submitForm()
        }
    }

    private fun setupBarcodeResultListener() {
        parentFragmentManager.setFragmentResultListener(
            BARCODE_RESULT_KEY,  // Now properly defined
            viewLifecycleOwner
        ) { _, result ->
            val barcodeValue = result.getString("barcode_value")
            if (!barcodeValue.isNullOrEmpty()) {
                viewModel.setBarcode(barcodeValue)
                Toast.makeText(requireContext(), "Barcode berhasil dipindai", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun showInitialImage(gambarPath: String?) {
        if (!gambarPath.isNullOrEmpty()) {
            photoCard.removeAllViews()
            val iv = ImageView(requireContext()).apply {
                layoutParams = FrameLayout.LayoutParams(
                    FrameLayout.LayoutParams.MATCH_PARENT,
                    FrameLayout.LayoutParams.MATCH_PARENT
                )
                scaleType = ImageView.ScaleType.CENTER_CROP
            }
            photoCard.addView(iv)

            Glide.with(this)
                .load(gambarPath)
                .placeholder(R.drawable.ic_placeholder_image)
                .error(R.drawable.ic_placeholder_image)
                .into(iv)
        } else {
            restorePlaceholderImage()
        }
    }

    private fun setupTextWatchers() {
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
        // Kategori loading state
        viewModel.kategoriLoading.observe(viewLifecycleOwner, Observer { isLoading ->
            updateKategoriLoadingState(isLoading)
        })

        // Kategori error state
        viewModel.kategoriError.observe(viewLifecycleOwner, Observer { errorMessage ->
            if (!errorMessage.isNullOrEmpty()) {
                Toast.makeText(requireContext(), "Gagal memuat kategori: $errorMessage", Toast.LENGTH_LONG).show()
            }
        })

        // Submit loading state
        viewModel.submitLoading.observe(viewLifecycleOwner, Observer { isLoading ->
            updateSubmitLoadingState(isLoading)
        })

        // Submit success
        viewModel.submitSuccess.observe(viewLifecycleOwner, Observer { success ->
            if (success) {
                handleSubmitSuccess()
            }
        })

        // Submit error
        viewModel.submitError.observe(viewLifecycleOwner, Observer { errorMessage ->
            if (!errorMessage.isNullOrEmpty()) {
                handleSubmitError(errorMessage)
            }
        })

        // Form fields
        viewModel.nama.observe(viewLifecycleOwner, Observer { value ->
            if (etNamaBarang.text?.toString() != value) {
                etNamaBarang.setText(value)
                etNamaBarang.setSelection(value.length)
            }
        })

        viewModel.kuantitas.observe(viewLifecycleOwner, Observer { value ->
            if (etKuantitas.text?.toString() != value) {
                etKuantitas.setText(value)
                etKuantitas.setSelection(value.length)
            }
        })

        viewModel.barcode.observe(viewLifecycleOwner, Observer { value ->
            if (etBarcode.text?.toString() != value) {
                etBarcode.setText(value)
                etBarcode.setSelection(value.length)
            }
        })

        viewModel.hargaModal.observe(viewLifecycleOwner, Observer { value ->
            if (etHargaModal.text?.toString() != value) {
                etHargaModal.setText(value)
                etHargaModal.setSelection(value.length)
            }
        })

        viewModel.hargaJual.observe(viewLifecycleOwner, Observer { value ->
            if (etHargaJual.text?.toString() != value) {
                etHargaJual.setText(value)
                etHargaJual.setSelection(value.length)
            }
        })

        // Kategori list
        viewModel.kategoriList.observe(viewLifecycleOwner, Observer { list ->
            kategoriAdapter.clear()
            kategoriAdapter.addAll(list)
            kategoriAdapter.notifyDataSetChanged()

            val idx = viewModel.kategoriIndex.value ?: 0
            if (idx in 0 until kategoriAdapter.count) {
                kategoriSpinner.setSelection(idx)
            }
        })

        // Kategori index
        viewModel.kategoriIndex.observe(viewLifecycleOwner, Observer { idx ->
            if (idx in 0 until kategoriAdapter.count && kategoriSpinner.selectedItemPosition != idx) {
                kategoriSpinner.setSelection(idx)
            }
        })

        // Image URI
        viewModel.imageUri.observe(viewLifecycleOwner, Observer { uriString ->
            if (!uriString.isNullOrEmpty() && uriString != barang?.gambarPath) {
                // Only update preview if it's a new image (not the original)
                try {
                    showImagePreview(Uri.parse(uriString))
                } catch (e: Exception) {
                    // If parsing fails, it might be a server path, try loading directly
                    showImageFromPath(uriString)
                }
            }
        })
    }

    private fun updateKategoriLoadingState(isLoading: Boolean) {
        if (isLoading) {
            kategoriProgressBar.visibility = View.VISIBLE
            kategoriSpinner.visibility = View.GONE
            btnAddKategori.isEnabled = false
        } else {
            kategoriProgressBar.visibility = View.GONE
            kategoriSpinner.visibility = View.VISIBLE
            btnAddKategori.isEnabled = true
        }
    }

    private fun updateSubmitLoadingState(isLoading: Boolean) {
        // Disable all inputs during loading
        etNamaBarang.isEnabled = !isLoading
        etKuantitas.isEnabled = !isLoading
        kategoriSpinner.isEnabled = !isLoading
        btnAddKategori.isEnabled = !isLoading
        etBarcode.isEnabled = !isLoading
        inputLayoutBarcode.isEnabled = !isLoading
        etHargaModal.isEnabled = !isLoading
        etHargaJual.isEnabled = !isLoading
        photoCard.isEnabled = !isLoading
        btnBatal.isEnabled = !isLoading

        if (isLoading) {
            btnSimpan.text = "Menyimpan..."
            btnSimpan.isEnabled = false
        } else {
            btnSimpan.text = "Simpan"
            btnSimpan.isEnabled = true
        }
    }

    private fun handleSubmitSuccess() {
        Toast.makeText(requireContext(), "Barang berhasil diupdate!", Toast.LENGTH_SHORT).show()
        viewModel.clearSubmitStates()
        findNavController().popBackStack()
    }

    private fun handleSubmitError(errorMessage: String) {
        Toast.makeText(requireContext(), "Gagal mengupdate barang: $errorMessage", Toast.LENGTH_LONG).show()
        viewModel.clearSubmitStates()
    }

    private fun showAddKategoriDialog() {
        val dialogView = layoutInflater.inflate(R.layout.dialog_tambah_kategori, null)
        val etKategori = dialogView.findViewById<TextInputEditText>(R.id.et_kategori_name)
        val btnBatal = dialogView.findViewById<MaterialButton>(R.id.btn_batal_dialog)
        val btnTambah = dialogView.findViewById<MaterialButton>(R.id.btn_tambah_dialog)

        val dialog = MaterialAlertDialogBuilder(requireContext())
            .setView(dialogView)
            .setCancelable(true)
            .create()
        dialog.window?.setBackgroundDrawableResource(android.R.color.transparent)
        dialog.show()

        // Fokus & buka keyboard
        etKategori.requestFocus()
        val imm = requireContext().getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
        imm.showSoftInput(etKategori, InputMethodManager.SHOW_IMPLICIT)

        // Listener tombol
        btnBatal.setOnClickListener {
            imm.hideSoftInputFromWindow(etKategori.windowToken, 0)
            dialog.dismiss()
        }

        btnTambah.setOnClickListener {
            val text = etKategori.text?.toString()?.trim().orEmpty()
            if (text.isEmpty()) {
                etKategori.error = "Nama kategori kosong"
                etKategori.requestFocus()
                return@setOnClickListener
            }

            // Tambah kategori ke viewmodel (sesuai implementasimu)
            viewModel.addKategori(text)

            imm.hideSoftInputFromWindow(etKategori.windowToken, 0)
            dialog.dismiss()
        }
    }

    private fun showResetConfirmation() {
        MaterialAlertDialogBuilder(requireContext())
            .setTitle("Reset Form")
            .setMessage("Apakah Anda yakin ingin mengembalikan semua data ke nilai awal?")
            .setPositiveButton("Reset") { _, _ ->
                barang?.let { viewModel.initializeWithBarang(it) }
                Toast.makeText(requireContext(), "Form dikembalikan ke data awal", Toast.LENGTH_SHORT).show()
            }
            .setNegativeButton("Batal", null)
            .show()
    }

    private fun showImagePreview(uri: Uri) {
        photoCard.removeAllViews()
        val iv = ImageView(requireContext()).apply {
            layoutParams = FrameLayout.LayoutParams(
                FrameLayout.LayoutParams.MATCH_PARENT,
                FrameLayout.LayoutParams.MATCH_PARENT
            )
            scaleType = ImageView.ScaleType.CENTER_CROP
        }
        photoCard.addView(iv)

        Glide.with(this)
            .load(uri)
            .placeholder(R.drawable.ic_placeholder_image)
            .error(R.drawable.ic_placeholder_image)
            .into(iv)
    }

    private fun showImageFromPath(imagePath: String) {
        photoCard.removeAllViews()
        val iv = ImageView(requireContext()).apply {
            layoutParams = FrameLayout.LayoutParams(
                FrameLayout.LayoutParams.MATCH_PARENT,
                FrameLayout.LayoutParams.MATCH_PARENT
            )
            scaleType = ImageView.ScaleType.CENTER_CROP
        }
        photoCard.addView(iv)

        Glide.with(this)
            .load(imagePath)
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

    private fun submitForm() {
        val payload = viewModel.buildPayloadForValidation()

        // Validation (same as add barang)
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

        // Warning untuk harga jual lebih kecil dari modal
        if (payload.hargaJual < payload.hargaModal) {
            MaterialAlertDialogBuilder(requireContext())
                .setTitle("Peringatan")
                .setMessage("Harga jual lebih kecil dari harga modal. Apakah Anda yakin ingin melanjutkan?")
                .setPositiveButton("Ya, Lanjutkan") { _, _ ->
                    viewModel.submitEdit()
                }
                .setNegativeButton("Batal", null)
                .show()
            return
        }

        // Submit to server
        viewModel.submitEdit()
    }

    // Simple TextWatcher util
    inner class SimpleTextWatcher(val onChange: (String) -> Unit) : TextWatcher {
        override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
        override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}
        override fun afterTextChanged(s: Editable?) {
            onChange(s?.toString().orEmpty())
        }
    }

    // Rupiah TextWatcher
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
}