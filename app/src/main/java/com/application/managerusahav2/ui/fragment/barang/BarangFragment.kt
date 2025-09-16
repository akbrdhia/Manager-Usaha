package com.application.managerusahav2.ui.fragment.barang

import androidx.fragment.app.viewModels
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import android.widget.Spinner
import android.widget.AdapterView
import android.widget.ProgressBar
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.application.managerusahav2.R
import com.application.managerusahav2.data.model.response.Barang
import com.application.managerusahav2.data.repository.BarangRepository
import com.google.android.material.button.MaterialButton
import com.google.android.material.textfield.TextInputEditText
import androidx.core.widget.doOnTextChanged
import androidx.navigation.fragment.findNavController
import com.application.managerusahav2.data.network.RetrofitClient
import com.application.managerusahav2.helper.StatusBarHelper
import kotlinx.coroutines.launch

class BarangFragment : Fragment() {

    private val viewModel: BarangViewModel by viewModels {
        BarangViewModelFactory(
            BarangRepository(RetrofitClient.getInstance())
        )
    }
    private lateinit var adapter: ExpandableBarangAdapter
    private lateinit var recyclerView: RecyclerView
    private lateinit var searchEditText: TextInputEditText
    private lateinit var kategoriSpinner: Spinner
    private lateinit var stokSpinner: Spinner
    private lateinit var btnAddBarang: MaterialButton
    private lateinit var progressBar: ProgressBar
    private lateinit var emptyState: View

    // Store original data for filtering
    private var originalBarangList: List<Barang> = emptyList()
    private var filteredBarangList: List<Barang> = emptyList()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        return inflater.inflate(R.layout.fragment_barang, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        StatusBarHelper.setStatusBar(requireActivity(), isLight = true)
        initializeViewModel()
        initViews(view)
        setupRecyclerView()
        setupSpinners()
        setupSearchFunctionality()
        observeViewModel()
        handle()
        viewModel.refreshData()

    }

    private fun handle() {
        btnAddBarang.setOnClickListener{
            findNavController().navigate(R.id.action_barangFragment_to_tambahBarangFragment)
        }
    }

    private fun initializeViewModel() {
        // Temporary solution: Create dummy repository
        // Replace this with proper dependency injection (Hilt/Dagger)

        // For now, you'll need to inject these dependencies properly
        // This is just to prevent the crash
        try {
            // Create the repository with your actual Retrofit instance
            // val retrofit = // Your retrofit instance
            // val barangService = retrofit.create(BarangService::class.java)
            // val repository = BarangRepository(barangService)
            // val factory = BarangViewModelFactory(repository)
            // viewModel = ViewModelProvider(this, factory)[BarangViewModel::class.java]

            // For testing purposes, create a mock viewModel
            // You need to replace this with proper DI
        } catch (e: Exception) {
            // Handle initialization error
        }
    }

    private fun initViews(view: View) {
        recyclerView = view.findViewById(R.id.rv_barang)
        searchEditText = view.findViewById(R.id.search_edit_text)
        kategoriSpinner = view.findViewById(R.id.kategori_spinner)
        stokSpinner = view.findViewById(R.id.stok_spinner)
        btnAddBarang = view.findViewById(R.id.btn_add_barang)
        progressBar = view.findViewById(R.id.progress_bar)
        emptyState = view.findViewById(R.id.empty_state)

        // Add button click listener
        btnAddBarang.setOnClickListener {
            // Navigate to add barang screen
            // findNavController().navigate(R.id.action_to_add_barang)
        }
    }

    private fun setupRecyclerView() {
        adapter = ExpandableBarangAdapter(
            onItemClick = { barang ->
                // Navigate to detail screen
                // val action = BarangFragmentDirections.actionToBarangDetail(barang.id)
                // findNavController().navigate(action)
            },
            onEditClick = { barang ->
                // Navigate to edit screen
                // val action = BarangFragmentDirections.actionToEditBarang(barang.id)
                // findNavController().navigate(action)
            },
            onDeleteClick = { barang ->
                // Show delete confirmation dialog
                showDeleteConfirmation(barang)
            }
        )

        recyclerView.layoutManager = LinearLayoutManager(requireContext())
        recyclerView.adapter = adapter
    }

    private fun setupSpinners() {
        // Kategori spinner
        val kategoriList = listOf("Semua", "Makanan", "Minuman", "ATK", "Lainnya")
        val kategoriAdapter = ArrayAdapter(
            requireContext(),
            android.R.layout.simple_spinner_dropdown_item,
            kategoriList
        )
        kategoriSpinner.adapter = kategoriAdapter

        // Stok spinner
        val stokList = listOf("Semua", "Tersedia", "Habis", "Stok Rendah")
        val stokAdapter = ArrayAdapter(
            requireContext(),
            android.R.layout.simple_spinner_dropdown_item,
            stokList
        )
        stokSpinner.adapter = stokAdapter

        // Kategori spinner listener
        kategoriSpinner.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(parent: AdapterView<*>?, view: View?, position: Int, id: Long) {
                applyFilters()
            }
            override fun onNothingSelected(parent: AdapterView<*>?) {}
        }

        // Stok spinner listener
        stokSpinner.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(parent: AdapterView<*>?, view: View?, position: Int, id: Long) {
                applyFilters()
            }
            override fun onNothingSelected(parent: AdapterView<*>?) {}
        }
    }

    private fun setupSearchFunctionality() {
        searchEditText.doOnTextChanged { text, _, _, _ ->
            applyFilters()
        }
    }

    private fun applyFilters() {
        val searchQuery = searchEditText.text.toString().lowercase().trim()
        val selectedKategori = kategoriSpinner.selectedItem.toString()
        val selectedStok = stokSpinner.selectedItem.toString()

        filteredBarangList = originalBarangList.filter { barang ->
            // Search filter
            val matchesSearch = if (searchQuery.isEmpty()) {
                true
            } else {
                barang.nama.lowercase().contains(searchQuery) ||
                        barang.kategori.lowercase().contains(searchQuery) ||
                        barang.barcode?.contains(searchQuery) == true
            }

            // Category filter
            val matchesKategori = selectedKategori == "Semua" || barang.kategori == selectedKategori

            // Stock filter
            val matchesStok = when (selectedStok) {
                "Semua" -> true
                "Tersedia" -> barang.stok > 0
                "Habis" -> barang.stok == 0
                "Stok Rendah" -> barang.stok in 1..5
                else -> true
            }

            matchesSearch && matchesKategori && matchesStok
        }

        adapter.refreshWithData(filteredBarangList)
    }

    private fun observeViewModel() {
        lifecycleScope.launch {
            viewModel.uiState.collect { state ->
                when {
                    state.isLoading -> {
                        progressBar.visibility = View.VISIBLE
                        recyclerView.visibility = View.GONE
                        emptyState.visibility = View.GONE
                    }
                    state.error != null -> {
                        progressBar.visibility = View.GONE
                        recyclerView.visibility = View.GONE
                        emptyState.visibility = View.VISIBLE
                        // Bisa tambahin dialog error di sini
                    }
                    else -> {
                        progressBar.visibility = View.GONE
                        originalBarangList = state.barangList

                        if (originalBarangList.isEmpty()) {
                            recyclerView.visibility = View.GONE
                            emptyState.visibility = View.VISIBLE
                        } else {
                            recyclerView.visibility = View.VISIBLE
                            emptyState.visibility = View.GONE

                            // Update kategori spinner dengan data API
                            updateKategoriSpinner(state.barangList)

                            // Apply filters
                            adapter.expandAllInitially(originalBarangList)
                        }
                    }
                }
            }
        }
    }


    private fun setupDummyData() {
        // Dummy data untuk testing UI
        originalBarangList = listOf(
            Barang(1, "Teh Pucuk", "Minuman", 5, 2000.0, 1500.0, "123456789", null),
            Barang(2, "Aqua", "Minuman", 2, 2000.0, 1800.0, "987654321", null),
            Barang(3, "Le Minerale", "Minuman", 2, 2000.0, 1700.0, "456789123", null),
            Barang(4, "Teh Gelas", "Minuman", 2, 2000.0, 1600.0, "789123456", null),
            Barang(5, "Nasi Gudeg", "Makanan", 10, 15000.0, 12000.0, "321654987", null),
            Barang(6, "Ayam Geprek", "Makanan", 8, 18000.0, 15000.0, "654987321", null)
        )

        // Debug log
        android.util.Log.d("BarangFragment", "Dummy data loaded: ${originalBarangList.size} items")

        updateKategoriSpinner(originalBarangList)
        applyFilters()

        // Show RecyclerView and hide empty state
        recyclerView.visibility = View.VISIBLE
        emptyState.visibility = View.GONE
        progressBar.visibility = View.GONE
    }

    private fun updateKategoriSpinner(barangList: List<Barang>) {
        val categories = mutableListOf("Semua")
        categories.addAll(barangList.map { it.kategori }.distinct().sorted())

        val currentSelection = kategoriSpinner.selectedItem?.toString() ?: "Semua"

        val kategoriAdapter = ArrayAdapter(
            requireContext(),
            android.R.layout.simple_spinner_dropdown_item,
            categories
        )
        kategoriSpinner.adapter = kategoriAdapter

        // Restore selection if still exists
        val selectionIndex = categories.indexOf(currentSelection)
        if (selectionIndex >= 0) {
            kategoriSpinner.setSelection(selectionIndex)
        }
    }

    private fun showDeleteConfirmation(barang: Barang) {
        // Implement delete confirmation dialog
        /*
        AlertDialog.Builder(requireContext())
            .setTitle("Hapus Barang")
            .setMessage("Yakin ingin menghapus ${barang.nama}?")
            .setPositiveButton("Hapus") { _, _ ->
                // Call delete API
                viewModel.deleteBarang(barang.id)
            }
            .setNegativeButton("Batal", null)
            .show()
        */
    }
}