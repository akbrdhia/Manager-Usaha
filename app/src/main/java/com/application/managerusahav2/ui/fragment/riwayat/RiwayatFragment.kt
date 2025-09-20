// RiwayatFragment.kt
package com.application.managerusahav2.ui.fragment.riwayat

import android.content.Context
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.inputmethod.EditorInfo
import android.view.inputmethod.InputMethodManager
import android.widget.AdapterView
import android.widget.ArrayAdapter
import android.widget.EditText
import android.widget.ProgressBar
import android.widget.Spinner
import android.widget.TextView
import androidx.core.view.isVisible
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.application.managerusahav2.R
import com.google.android.material.snackbar.Snackbar
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout
import com.application.managerusahav2.data.network.RetrofitClient
import com.application.managerusahav2.data.repository.RiwayatRepository
import com.application.managerusahav2.data.service.ApiService
// RiwayatFragment.kt
class RiwayatFragment : Fragment() {

    private val viewModel: RiwayatViewModel by viewModels {
        RiwayatViewModelFactory(
            RiwayatRepository(RetrofitClient.getInstance())
        )
    }
    private lateinit var riwayatAdapter: RiwayatAdapter

    private var searchJob: Job? = null

    // Views
    private lateinit var rvBarang: RecyclerView
    private lateinit var searchEditText: EditText
    private lateinit var kategoriSpinner: Spinner
    private lateinit var filterSpinner: Spinner
    private lateinit var swipeRefresh: SwipeRefreshLayout
    private lateinit var progressBar: ProgressBar
    private lateinit var emptyState: View

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        return inflater.inflate(R.layout.fragment_riwayat, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // Init views
        rvBarang = view.findViewById(R.id.rv_barang)
        searchEditText = view.findViewById(R.id.search_edit_text)
        kategoriSpinner = view.findViewById(R.id.kategori_spinner)
        filterSpinner = view.findViewById(R.id.filter_spinner)
        swipeRefresh = view.findViewById(R.id.swipe_refresh)
        progressBar = view.findViewById(R.id.progress_bar)
        emptyState = view.findViewById(R.id.empty_state)

        setupRecyclerView()
        setupSearch()
        setupFilters()
        setupSwipeRefresh()
        observeViewModel()

        // Load initial data
        viewModel.loadRiwayat()
    }

    private fun setupRecyclerView() {
        riwayatAdapter = RiwayatAdapter()

        rvBarang.apply {
            layoutManager = LinearLayoutManager(context)
            adapter = riwayatAdapter

            // Infinite scroll
            addOnScrollListener(object : RecyclerView.OnScrollListener() {
                override fun onScrolled(recyclerView: RecyclerView, dx: Int, dy: Int) {
                    super.onScrolled(recyclerView, dx, dy)

                    val layoutManager = recyclerView.layoutManager as LinearLayoutManager
                    val visibleItemCount = layoutManager.childCount
                    val totalItemCount = layoutManager.itemCount
                    val firstVisibleItemPosition = layoutManager.findFirstVisibleItemPosition()

                    if (viewModel.isLoading.value == false && viewModel.isLastPage.value == false) {
                        if ((visibleItemCount + firstVisibleItemPosition) >= totalItemCount
                            && firstVisibleItemPosition >= 0
                        ) {
                            viewModel.loadMoreRiwayat()
                        }
                    }
                }
            })
        }
    }

    private fun setupSearch() {
        searchEditText.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}

            override fun afterTextChanged(s: Editable?) {
                searchJob?.cancel()
                searchJob = lifecycleScope.launch {
                    delay(300) // Debounce
                    val query = s?.toString()?.trim() ?: ""
                    viewModel.searchRiwayat(query)

                    if (query.isEmpty()) {
                        kategoriSpinner.setSelection(0)
                        filterSpinner.setSelection(0)
                    }
                }
            }
        })

        searchEditText.setOnEditorActionListener { _, actionId, _ ->
            if (actionId == EditorInfo.IME_ACTION_SEARCH) {
                hideKeyboard()
                true
            } else false
        }
    }

    private fun setupFilters() {
        val kategoriList = mutableListOf("Semua Kategori")
        val kategoriAdapter = ArrayAdapter(
            requireContext(),
            android.R.layout.simple_spinner_item,
            kategoriList
        ).apply {
            setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        }

        kategoriSpinner.adapter = kategoriAdapter
        kategoriSpinner.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(
                parent: AdapterView<*>?, view: View?, position: Int, id: Long
            ) {
                val selectedKategori = if (position == 0) null else kategoriList[position]
                viewModel.filterByKategori(selectedKategori)
            }

            override fun onNothingSelected(parent: AdapterView<*>?) {}
        }

        val tipeList = listOf(
            "Semua Aktivitas",
            "Ditambahkan",
            "Diperbarui",
            "Dihapus",
            "Restok",
            "Pengurangan Stok"
        )
        val tipeAdapter = ArrayAdapter(
            requireContext(),
            android.R.layout.simple_spinner_item,
            tipeList
        ).apply {
            setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        }

        filterSpinner.adapter = tipeAdapter
        filterSpinner.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(
                parent: AdapterView<*>?, view: View?, position: Int, id: Long
            ) {
                val selectedTipe = when (position) {
                    0 -> null
                    1 -> "create"
                    2 -> "update"
                    3 -> "delete"
                    4 -> "add" // atau "tambah_stok"
                    5 -> "min" // atau "kurangi_stok"
                    else -> null
                }
                viewModel.filterByTipe(selectedTipe)
            }

            override fun onNothingSelected(parent: AdapterView<*>?) {}
        }

        viewModel.availableKategori.observe(viewLifecycleOwner) { kategoriSet ->
            kategoriList.clear()
            kategoriList.add("Semua Kategori")
            kategoriList.addAll(kategoriSet.sorted())
            kategoriAdapter.notifyDataSetChanged()
        }
    }

    private fun setupSwipeRefresh() {
        swipeRefresh.setOnRefreshListener {
            viewModel.refreshRiwayat()
        }
        swipeRefresh.setColorSchemeResources(R.color.primary)
    }

    private fun observeViewModel() {
        viewModel.riwayatItems.observe(viewLifecycleOwner) { items ->
            riwayatAdapter.submitList(items)
            updateEmptyState(items.isEmpty())
        }

        viewModel.isLoading.observe(viewLifecycleOwner) { isLoading ->
            progressBar.isVisible = isLoading && riwayatAdapter.itemCount == 0
            swipeRefresh.isRefreshing = isLoading && riwayatAdapter.itemCount > 0
        }

        viewModel.error.observe(viewLifecycleOwner) { error ->
            error?.let { showError(it) }
        }

        viewModel.isLastPage.observe(viewLifecycleOwner) { isLastPage ->
            riwayatAdapter.setLoadingMore(!isLastPage && (viewModel.isLoading.value == false))
        }
    }

    private fun updateEmptyState(isEmpty: Boolean) {
        emptyState.isVisible = isEmpty && viewModel.isLoading.value == false
        rvBarang.isVisible = !isEmpty

        val searchQuery = searchEditText.text.toString().trim()
        val hasFilters =
            kategoriSpinner.selectedItemPosition > 0 || filterSpinner.selectedItemPosition > 0

        if (isEmpty) {
            val emptyTitle: String
            val emptyMessage: String

            if (searchQuery.isNotEmpty() || hasFilters) {
                emptyTitle = "Tidak ada hasil"
                emptyMessage = "Coba ubah kata kunci atau filter"
            } else {
                emptyTitle = "Belum ada riwayat"
                emptyMessage = "Aktivitas akan muncul di sini"
            }

            emptyState.findViewById<TextView>(R.id.tv_empty_title)?.text = emptyTitle
            emptyState.findViewById<TextView>(R.id.tv_empty_message)?.text = emptyMessage
        }
    }

    private fun showError(message: String) {
        Snackbar.make(requireView(), message, Snackbar.LENGTH_LONG)
            .setAction("Coba Lagi") {
                viewModel.loadRiwayat()
            }
            .show()
    }

    private fun hideKeyboard() {
        val imm =
            requireContext().getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
        imm.hideSoftInputFromWindow(searchEditText.windowToken, 0)
    }

    override fun onDestroyView() {
        super.onDestroyView()
        searchJob?.cancel()
    }
}
