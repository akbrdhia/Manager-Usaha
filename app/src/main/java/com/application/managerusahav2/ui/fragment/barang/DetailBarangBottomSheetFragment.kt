package com.application.managerusahav2.ui.fragment.barang

import android.app.Dialog
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.*
import androidx.core.content.ContextCompat
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import com.application.managerusahav2.R
import com.application.managerusahav2.data.model.response.Barang
import com.application.managerusahav2.data.network.RetrofitClient
import com.application.managerusahav2.data.repository.BarangRepository
import com.bumptech.glide.Glide
import com.google.android.material.bottomsheet.BottomSheetBehavior
import com.google.android.material.bottomsheet.BottomSheetDialog
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import com.google.android.material.button.MaterialButton
import com.google.android.material.chip.Chip
import com.google.android.material.textfield.TextInputEditText
import com.google.android.material.textfield.TextInputLayout
import kotlinx.coroutines.launch
import java.text.NumberFormat
import java.util.*

class DetailBarangBottomSheetFragment : BottomSheetDialogFragment() {

    private val viewModel: DetailBarangViewModel by viewModels {
        DetailBarangViewModelFactory(
            BarangRepository(RetrofitClient.getInstance())
        )
    }

    // Views
    private lateinit var ivBarang: ImageView
    private lateinit var tvNamaBarang: TextView
    private lateinit var tvKategori: TextView
    private lateinit var tvStok: TextView
    private lateinit var tvHargaModal: TextView
    private lateinit var tvHargaJual: TextView
    private lateinit var tvBarcode: TextView
    private lateinit var barcodeContainer: LinearLayout
    private lateinit var etTambahStok: TextInputEditText
    private lateinit var inputLayoutTambahStok: TextInputLayout
    private lateinit var previewContainer: LinearLayout
    private lateinit var tvPreviewStok: TextView
    private lateinit var progressBar: ProgressBar
    private lateinit var btnTambahStok: MaterialButton
    private lateinit var btnEdit: MaterialButton
    private lateinit var btnDelete: MaterialButton

    // Quick add chips
    private lateinit var chipAdd5: Chip
    private lateinit var chipAdd10: Chip
    private lateinit var chipAdd20: Chip
    private lateinit var chipAdd50: Chip

    private var barang: Barang? = null
    private var onStokUpdatedListener: ((Barang, Int) -> Unit)? = null

    companion object {
        private const val ARG_BARANG_ID = "arg_barang_id"
        private const val ARG_BARANG_NAMA = "arg_barang_nama"
        private const val ARG_BARANG_KATEGORI = "arg_barang_kategori"
        private const val ARG_BARANG_STOK = "arg_barang_stok"
        private const val ARG_BARANG_HARGA = "arg_barang_harga"
        private const val ARG_BARANG_MODAL = "arg_barang_modal"
        private const val ARG_BARANG_BARCODE = "arg_barang_barcode"
        private const val ARG_BARANG_GAMBAR = "arg_barang_gambar"

        fun newInstance(barang: Barang): DetailBarangBottomSheetFragment {
            return DetailBarangBottomSheetFragment().apply {
                arguments = Bundle().apply {
                    putInt(ARG_BARANG_ID, barang.id)
                    putString(ARG_BARANG_NAMA, barang.nama)
                    putString(ARG_BARANG_KATEGORI, barang.kategori)
                    putInt(ARG_BARANG_STOK, barang.stok)
                    putDouble(ARG_BARANG_HARGA, barang.harga)
                    putDouble(ARG_BARANG_MODAL, barang.modal)
                    putString(ARG_BARANG_BARCODE, barang.barcode)
                    putString(ARG_BARANG_GAMBAR, barang.gambarPath)
                }
            }
        }
    }

    fun setOnStokUpdatedListener(listener: (Barang, Int) -> Unit) {
        onStokUpdatedListener = listener
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        arguments?.let { args ->
            barang = Barang(
                id = args.getInt(ARG_BARANG_ID),
                nama = args.getString(ARG_BARANG_NAMA, ""),
                kategori = args.getString(ARG_BARANG_KATEGORI, ""),
                stok = args.getInt(ARG_BARANG_STOK),
                harga = args.getDouble(ARG_BARANG_HARGA),
                modal = args.getDouble(ARG_BARANG_MODAL),
                barcode = args.getString(ARG_BARANG_BARCODE),
                gambarPath = args.getString(ARG_BARANG_GAMBAR)
            )
        }
    }

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        val dialog = super.onCreateDialog(savedInstanceState) as BottomSheetDialog

        dialog.setOnShowListener { dialogInterface ->
            val bottomSheetDialog = dialogInterface as BottomSheetDialog
            val bottomSheet = bottomSheetDialog.findViewById<View>(com.google.android.material.R.id.design_bottom_sheet)

            bottomSheet?.let {
                // Remove default background
                it.background = null

                // Set behavior
                val behavior = BottomSheetBehavior.from(it)
                behavior.state = BottomSheetBehavior.STATE_EXPANDED
            }
        }

        return dialog
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.layout_detail_barang_bottom_sheet, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)


        initViews(view)
        setupUI()
        setupListeners()
        observeViewModel()

        barang?.let { populateData(it) }
    }

    private fun initViews(view: View) {
        ivBarang = view.findViewById(R.id.iv_barang)
        tvNamaBarang = view.findViewById(R.id.tv_nama_barang)
        tvKategori = view.findViewById(R.id.tv_kategori)
        tvStok = view.findViewById(R.id.tv_stok)
        tvHargaModal = view.findViewById(R.id.tv_harga_modal)
        tvHargaJual = view.findViewById(R.id.tv_harga_jual)
        tvBarcode = view.findViewById(R.id.tv_barcode)
        barcodeContainer = view.findViewById(R.id.barcode_container)
        etTambahStok = view.findViewById(R.id.et_tambah_stok)
        inputLayoutTambahStok = view.findViewById(R.id.input_layout_tambah_stok)
        previewContainer = view.findViewById(R.id.preview_container)
        tvPreviewStok = view.findViewById(R.id.tv_preview_stok)
        progressBar = view.findViewById(R.id.progress_bar)
        btnTambahStok = view.findViewById(R.id.btn_tambah_stok)
        btnEdit = view.findViewById(R.id.btn_edit)
        btnDelete = view.findViewById(R.id.btn_delete)

        // Chips
        chipAdd5 = view.findViewById(R.id.chip_add_5)
        chipAdd10 = view.findViewById(R.id.chip_add_10)
        chipAdd20 = view.findViewById(R.id.chip_add_20)
        chipAdd50 = view.findViewById(R.id.chip_add_50)
    }

    private fun setupUI() {
        // Setup text watcher untuk preview
        etTambahStok.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}
            override fun afterTextChanged(s: Editable?) {
                updatePreview()
            }
        })
    }

    private fun setupListeners() {
        // Quick add chips
        chipAdd5.setOnClickListener { addToCurrentInput(5) }
        chipAdd10.setOnClickListener { addToCurrentInput(10) }
        chipAdd20.setOnClickListener { addToCurrentInput(20) }
        chipAdd50.setOnClickListener { addToCurrentInput(50) }

        // Buttons
        btnTambahStok.setOnClickListener { submitTambahStok() }
        btnEdit.setOnClickListener { handleEdit() }
        btnDelete.setOnClickListener { handleDelete() }
    }

    private fun addToCurrentInput(amount: Int) {
        val currentText = etTambahStok.text?.toString() ?: ""
        val currentValue = currentText.toIntOrNull() ?: 0
        val newValue = currentValue + amount
        etTambahStok.setText(newValue.toString())
        etTambahStok.setSelection(newValue.toString().length)
    }

    private fun updatePreview() {
        val inputText = etTambahStok.text?.toString() ?: ""
        val tambahStok = inputText.toIntOrNull()

        if (tambahStok != null && tambahStok > 0) {
            val stokBaru = (barang?.stok ?: 0) + tambahStok
            tvPreviewStok.text = stokBaru.toString()
            previewContainer.visibility = View.VISIBLE
        } else {
            previewContainer.visibility = View.GONE
        }
    }

    private fun populateData(barang: Barang) {
        tvNamaBarang.text = barang.nama
        tvKategori.text = barang.kategori
        tvStok.text = barang.stok.toString()

        // Format currency
        val nf = NumberFormat.getCurrencyInstance(Locale("in", "ID"))
        tvHargaModal.text = nf.format(barang.modal)
        tvHargaJual.text = nf.format(barang.harga)

        // Stok color based on availability
        val stokColor = when {
            barang.stok == 0 -> ContextCompat.getColor(requireContext(), R.color.red)
            barang.stok <= 5 -> ContextCompat.getColor(requireContext(), R.color.warning)
            else -> ContextCompat.getColor(requireContext(), R.color.green)
        }
        tvStok.setTextColor(stokColor)

        // Barcode
        if (!barang.barcode.isNullOrEmpty()) {
            tvBarcode.text = barang.barcode
            barcodeContainer.visibility = View.VISIBLE
        } else {
            barcodeContainer.visibility = View.GONE
        }

        // Image
        if (!barang.gambarPath.isNullOrEmpty()) {
            Glide.with(this)
                .load(barang.gambarPath)
                .placeholder(R.drawable.ic_placeholder_image)
                .error(R.drawable.ic_placeholder_image)
                .into(ivBarang)
        }
    }

    private fun submitTambahStok() {
        val inputText = etTambahStok.text?.toString() ?: ""
        val tambahStok = inputText.toIntOrNull()

        // Validasi
        if (tambahStok == null || tambahStok <= 0) {
            inputLayoutTambahStok.error = "Masukkan jumlah stok yang valid (> 0)"
            return
        }

        if (tambahStok > 1000) {
            inputLayoutTambahStok.error = "Jumlah stok maksimal 1000"
            return
        }

        inputLayoutTambahStok.error = null

        barang?.let { barang ->
            viewModel.tambahStok(barang.id, tambahStok)
        }
    }

    private fun handleEdit() {
        // TODO: Navigate to edit fragment atau show edit dialog
        Toast.makeText(requireContext(), "Edit barang - Coming soon", Toast.LENGTH_SHORT).show()
    }

    private fun handleDelete() {
        // TODO: Show delete confirmation dialog
        Toast.makeText(requireContext(), "Delete barang - Coming soon", Toast.LENGTH_SHORT).show()
    }

    private fun observeViewModel() {
        lifecycleScope.launch {
            viewModel.uiState.collect { state ->
                updateLoadingState(state.isLoading)

                if (state.isSuccess) {
                    handleSuccess(state.successMessage, state.newStokValue)
                }

                state.errorMessage?.let { errorMessage ->
                    handleError(errorMessage)
                }
            }
        }
    }

    private fun updateLoadingState(isLoading: Boolean) {
        if (isLoading) {
            progressBar.visibility = View.VISIBLE
            btnTambahStok.isEnabled = false
            btnTambahStok.text = "Menyimpan..."
            etTambahStok.isEnabled = false
            chipAdd5.isEnabled = false
            chipAdd10.isEnabled = false
            chipAdd20.isEnabled = false
            chipAdd50.isEnabled = false
        } else {
            progressBar.visibility = View.GONE
            btnTambahStok.isEnabled = true
            btnTambahStok.text = "Tambah Stok"
            etTambahStok.isEnabled = true
            chipAdd5.isEnabled = true
            chipAdd10.isEnabled = true
            chipAdd20.isEnabled = true
            chipAdd50.isEnabled = true
        }
    }

    private fun handleSuccess(message: String?, newStokValue: Int?) {
        Toast.makeText(requireContext(), message ?: "Stok berhasil ditambahkan", Toast.LENGTH_SHORT).show()

        // Update UI
        newStokValue?.let { newStok ->
            tvStok.text = newStok.toString()

            // Update stok color
            val stokColor = when {
                newStok == 0 -> ContextCompat.getColor(requireContext(), R.color.red)
                newStok <= 5 -> ContextCompat.getColor(requireContext(), R.color.warning)
                else -> ContextCompat.getColor(requireContext(), R.color.green)
            }
            tvStok.setTextColor(stokColor)

            // Update barang object
            barang = barang?.copy(stok = newStok)

            // Notify parent fragment
            barang?.let { updatedBarang ->
                onStokUpdatedListener?.invoke(updatedBarang, newStok)
            }
        }

        // Clear input
        etTambahStok.setText("")
        previewContainer.visibility = View.GONE

        // Clear states
        viewModel.clearStates()
    }

    private fun handleError(errorMessage: String) {
        Toast.makeText(requireContext(), "Error: $errorMessage", Toast.LENGTH_LONG).show()
        viewModel.clearStates()
    }
}