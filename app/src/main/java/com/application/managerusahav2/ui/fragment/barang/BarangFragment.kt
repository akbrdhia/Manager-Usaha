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
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import androidx.lifecycle.lifecycleScope

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
    private val expandedCategories = mutableSetOf<String>()

    private var searchJob: Job? = null

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        return inflater.inflate(R.layout.fragment_barang, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        StatusBarHelper.setStatusBar(requireActivity(), isLight = true)

        // init views & adapter
        initViews(view)
        initializeAdapter()
        setupRecyclerView()
        setupSpinners()
        setupSearchFunctionality()
        observeViewModel()
        handle()

        viewModel.refreshData()
    }

    private fun handle() {
        btnAddBarang.setOnClickListener {
            findNavController().navigate(R.id.action_barangFragment_to_tambahBarangFragment)
        }
    }

    private fun initializeAdapter() {
        adapter = ExpandableBarangAdapter(
            onItemClick = { barang -> /* navigate or show detail */ },
            onEditClick = { barang -> /* edit */ },
            onDeleteClick = { barang -> /* delete */ },
            onHeaderClicked = { kategori ->
                toggleCategory(kategori)
            }
        )
    }

    private fun initViews(view: View) {
        recyclerView = view.findViewById(R.id.rv_barang)
        searchEditText = view.findViewById(R.id.search_edit_text)
        kategoriSpinner = view.findViewById(R.id.kategori_spinner)
        stokSpinner = view.findViewById(R.id.stok_spinner)
        btnAddBarang = view.findViewById(R.id.btn_add_barang)
        progressBar = view.findViewById(R.id.progress_bar)
        emptyState = view.findViewById(R.id.empty_state)
    }

    private fun setupRecyclerView() {
        recyclerView.layoutManager = LinearLayoutManager(requireContext())
        recyclerView.adapter = adapter
    }

    private fun setupSpinners() {
        // default options (will be updated by updateKategoriSpinner when data loaded)
        val stokList = listOf("Semua", "Tersedia", "Habis", "Stok Rendah")
        val stokAdapter = ArrayAdapter(requireContext(), android.R.layout.simple_spinner_dropdown_item, stokList)
        stokSpinner.adapter = stokAdapter

        kategoriSpinner.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(parent: AdapterView<*>?, view: View?, position: Int, id: Long) {
                val q = searchEditText.text?.toString().orEmpty()
                applyFiltersOnBackground(q, kategoriSpinner.selectedItem?.toString() ?: "Semua", stokSpinner.selectedItem?.toString() ?: "Semua")
            }
            override fun onNothingSelected(parent: AdapterView<*>?) {}
        }
        stokSpinner.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(parent: AdapterView<*>?, view: View?, position: Int, id: Long) {
                val q = searchEditText.text?.toString().orEmpty()
                applyFiltersOnBackground(q, kategoriSpinner.selectedItem?.toString() ?: "Semua", stokSpinner.selectedItem?.toString() ?: "Semua")
            }
            override fun onNothingSelected(parent: AdapterView<*>?) {}
        }
    }

    private fun toggleCategory(kategori: String) {
        if (expandedCategories.contains(kategori)) expandedCategories.remove(kategori)
        else expandedCategories.add(kategori)

        val display = rebuildDisplayList(filteredBarangList, expandedCategories)
        adapter.submitList(display)
    }

    // Run filtering + grouping in background thread
    private fun applyFiltersOnBackground(search: String, kategoriFilter: String, stokFilter: String) {
        lifecycleScope.launch {
            val result = withContext(Dispatchers.Default) {
                val q = search.lowercase().trim()
                val filtered = originalBarangList.filter { barang ->
                    val matchesSearch = q.isEmpty() || barang.nama.lowercase().contains(q) ||
                            barang.kategori.lowercase().contains(q) ||
                            (barang.barcode?.contains(q) == true)
                    val matchesKategori = kategoriFilter == "Semua" || barang.kategori == kategoriFilter
                    val matchesStok = when (stokFilter) {
                        "Semua" -> true
                        "Tersedia" -> barang.stok > 0
                        "Habis" -> barang.stok == 0
                        "Stok Rendah" -> barang.stok in 1..5
                        else -> true
                    }
                    matchesSearch && matchesKategori && matchesStok
                }.sortedBy { it.nama.lowercase() }

                val displayList = rebuildDisplayList(filtered, expandedCategories)
                Pair(filtered, displayList)
            }

            filteredBarangList = result.first
            adapter.submitList(result.second)

            if (filteredBarangList.isEmpty()) {
                recyclerView.visibility = View.GONE
                emptyState.visibility = View.VISIBLE
            } else {
                recyclerView.visibility = View.VISIBLE
                emptyState.visibility = View.GONE
            }
        }
    }

    private fun rebuildDisplayList(barangList: List<Barang>, expanded: Set<String>): List<DisplayItem> {
        val grouped = barangList.groupBy { it.kategori }
        val out = mutableListOf<DisplayItem>()
        grouped.forEach { (kategori, list) ->
            val isExpanded = expanded.contains(kategori)
            out.add(DisplayItem.Header(kategori, list.size, isExpanded))
            if (isExpanded) {
                list.forEach { out.add(DisplayItem.Child(it)) }
            }
        }
        return out
    }

    // call this when initial data fetched from API / viewModel
    private fun onDataLoadedFromApi(data: List<Barang>) {
        originalBarangList = data
        filteredBarangList = data

        // decide initial expanded state:
        expandedCategories.clear()
        expandedCategories.addAll(data.map { it.kategori }) // expand all initially, or remove if you want collapsed

        updateKategoriSpinner(data)

        val display = rebuildDisplayList(originalBarangList, expandedCategories)
        adapter.submitList(display)

        if (originalBarangList.isEmpty()) {
            recyclerView.visibility = View.GONE
            emptyState.visibility = View.VISIBLE
        } else {
            recyclerView.visibility = View.VISIBLE
            emptyState.visibility = View.GONE
        }
    }

    private fun setupSearchFunctionality() {
        searchEditText.doOnTextChanged { text, _, _, _ ->
            searchJob?.cancel()
            searchJob = lifecycleScope.launch {
                delay(250) // debounce
                val q = text?.toString().orEmpty()
                applyFiltersOnBackground(q, kategoriSpinner.selectedItem?.toString() ?: "Semua", stokSpinner.selectedItem?.toString() ?: "Semua")
            }
        }
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
                    }
                    else -> {
                        progressBar.visibility = View.GONE
                        onDataLoadedFromApi(state.barangList)
                    }
                }
            }
        }
    }

    private fun setupDummyData() {
        originalBarangList = listOf(
            Barang(1, "Teh Pucuk", "Minuman", 5, 2000.0, 1500.0, "123456789", null),
            Barang(2, "Aqua", "Minuman", 2, 2000.0, 1800.0, "987654321", null),
            Barang(3, "Le Minerale", "Minuman", 2, 2000.0, 1700.0, "456789123", null),
            Barang(4, "Teh Gelas", "Minuman", 2, 2000.0, 1600.0, "789123456", null),
            Barang(5, "Nasi Gudeg", "Makanan", 10, 15000.0, 12000.0, "321654987", null),
            Barang(6, "Ayam Geprek", "Makanan", 8, 18000.0, 15000.0, "654987321", null)
        )

        updateKategoriSpinner(originalBarangList)
        onDataLoadedFromApi(originalBarangList)
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

        val selectionIndex = categories.indexOf(currentSelection)
        if (selectionIndex >= 0) {
            kategoriSpinner.setSelection(selectionIndex)
        }
    }

    private fun showDeleteConfirmation(barang: Barang) {
        // implement jika perlu
    }
}
