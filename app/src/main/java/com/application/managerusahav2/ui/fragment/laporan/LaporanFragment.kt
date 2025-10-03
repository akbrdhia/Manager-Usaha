package com.application.managerusahav2.ui.fragment.laporan

import android.graphics.Color
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.*
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout
import com.application.managerusahav2.R
import com.application.managerusahav2.data.network.RetrofitClient
import com.application.managerusahav2.data.repository.BarangRepository
import com.application.managerusahav2.helper.StatusBarHelper
import com.github.mikephil.charting.charts.BarChart
import com.github.mikephil.charting.components.XAxis
import com.github.mikephil.charting.data.BarData
import com.github.mikephil.charting.data.BarDataSet
import com.github.mikephil.charting.data.BarEntry
import com.github.mikephil.charting.formatter.IndexAxisValueFormatter
import com.github.mikephil.charting.formatter.ValueFormatter
import com.google.android.material.button.MaterialButton
import kotlinx.coroutines.launch
import java.text.NumberFormat
import java.util.*

class LaporanFragment : Fragment() {

    private val viewModel: LaporanViewModel by viewModels {
        LaporanViewModelFactory(
            BarangRepository(RetrofitClient.getInstance())
        )
    }

    // Views
    private lateinit var periodeSpinner: Spinner
    private lateinit var barChart: BarChart
    private lateinit var rvTop: RecyclerView
    private lateinit var progressBar: ProgressBar
    private lateinit var errorLayout: LinearLayout
    private lateinit var emptyState: LinearLayout

    // Adapters
    private lateinit var periodeAdapter: ArrayAdapter<String>
    private lateinit var topBarangAdapter: TopBarangAdapter

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        return inflater.inflate(R.layout.fragment_laporan, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        StatusBarHelper.setStatusBar(requireActivity(), isLight = true)

        initViews(view)
        setupSpinner()
        setupChart()
        setupRecyclerView()
        observeViewModel()
    }

    private fun initViews(view: View) {
        periodeSpinner = view.findViewById(R.id.periode_spinner)
        barChart = view.findViewById(R.id.barChart)
        rvTop = view.findViewById(R.id.rvTop)

        // New state views
        val swipeRefresh: SwipeRefreshLayout = view.findViewById(R.id.swipe_refresh)
        val loadingState: LinearLayout = view.findViewById(R.id.loading_state)
        val emptyState: LinearLayout = view.findViewById(R.id.empty_state)
        val errorState: LinearLayout = view.findViewById(R.id.error_state)
        val btnRetry: MaterialButton = view.findViewById(R.id.btn_retry)
        val tvErrorMessage: TextView = view.findViewById(R.id.tv_error_message)
        val chartLoading: LinearLayout = view.findViewById(R.id.chart_loading)

        // Setup SwipeRefreshLayout
        swipeRefresh.setColorSchemeResources(R.color.primary)
        swipeRefresh.setOnRefreshListener {
            viewModel.retryLoadData()
            swipeRefresh.isRefreshing = false
        }

        // Setup retry button
        btnRetry.setOnClickListener {
            viewModel.retryLoadData()
        }
    }


    private fun setupSpinner() {
        val periodeOptions = listOf(
            PeriodeType.HARI_INI.label,
            PeriodeType.TUJUH_HARI.label,
            PeriodeType.TIGA_PULUH_HARI.label,
            PeriodeType.ALL_TIME.label
        )

        periodeAdapter = ArrayAdapter(
            requireContext(),
            android.R.layout.simple_spinner_item,
            periodeOptions
        )
        periodeAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        periodeSpinner.adapter = periodeAdapter

        periodeSpinner.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(parent: AdapterView<*>?, view: View?, position: Int, id: Long) {
                val selectedPeriode = when (position) {
                    0 -> PeriodeType.HARI_INI
                    1 -> PeriodeType.TUJUH_HARI
                    2 -> PeriodeType.TIGA_PULUH_HARI
                    3 -> PeriodeType.ALL_TIME
                    else -> PeriodeType.HARI_INI
                }
                viewModel.loadTopBarang(selectedPeriode)
            }
            override fun onNothingSelected(parent: AdapterView<*>?) {}
        }
    }

    private fun setupChart() {
        barChart.apply {
            description.isEnabled = false
            legend.isEnabled = false

            // Disable interactions untuk cleaner look
            setTouchEnabled(false)
            setDragEnabled(false)
            setScaleEnabled(false)
            setPinchZoom(false)

            // X-axis configuration
            xAxis.apply {
                position = XAxis.XAxisPosition.BOTTOM
                setDrawGridLines(false)
                granularity = 1f
                textColor = ContextCompat.getColor(requireContext(), R.color.grey_text)
                textSize = 10f
            }

            // Left Y-axis configuration
            axisLeft.apply {
                setDrawGridLines(true)
                gridColor = ContextCompat.getColor(requireContext(), R.color.grey_light)
                textColor = ContextCompat.getColor(requireContext(), R.color.grey_text)
                textSize = 10f
                valueFormatter = object : ValueFormatter() {
                    override fun getFormattedValue(value: Float): String {
                        return if (value >= 1000000) {
                            "${(value / 1000000).toInt()}M"
                        } else if (value >= 1000) {
                            "${(value / 1000).toInt()}K"
                        } else {
                            value.toInt().toString()
                        }
                    }
                }
            }

            // Right Y-axis
            axisRight.isEnabled = false

            // Padding
            setExtraOffsets(10f, 10f, 10f, 20f)

            // Animation
            animateY(800)
        }
    }

    private fun setupRecyclerView() {
        topBarangAdapter = TopBarangAdapter()
        rvTop.apply {
            layoutManager = LinearLayoutManager(requireContext())
            adapter = topBarangAdapter
            isNestedScrollingEnabled = false
        }
    }

    private fun observeViewModel() {
        lifecycleScope.launch {
            viewModel.uiState.collect { state ->
                updateLoadingState(state.isLoading)

                if (state.errorMessage != null) {
                    showError(state.errorMessage)
                } else if (state.topBarangData.isEmpty() && !state.isLoading) {
                    showEmptyState()
                } else {
                    showData(state.topBarangData)
                }
            }
        }
    }

    private fun updateLoadingState(isLoading: Boolean) {
        val loadingState: LinearLayout = requireView().findViewById(R.id.loading_state)
        val chartLoading: LinearLayout = requireView().findViewById(R.id.chart_loading)

        if (isLoading) {
            loadingState.visibility = View.VISIBLE
            chartLoading.visibility = View.VISIBLE
            barChart.visibility = View.GONE
            rvTop.visibility = View.GONE
            requireView().findViewById<LinearLayout>(R.id.error_state).visibility = View.GONE
            requireView().findViewById<LinearLayout>(R.id.empty_state).visibility = View.GONE
        } else {
            loadingState.visibility = View.GONE
            chartLoading.visibility = View.GONE
        }
    }

    private fun showLoadingShimmer() {
        // Simple loading state - you can enhance this with shimmer effect
        barChart.clear()
        barChart.setNoDataText("Memuat data...")
        barChart.setNoDataTextColor(ContextCompat.getColor(requireContext(), R.color.grey_text))
        barChart.visibility = View.VISIBLE
    }

    private fun showError(errorMessage: String) {
        val errorState: LinearLayout = requireView().findViewById(R.id.error_state)
        val tvErrorMessage: TextView = requireView().findViewById(R.id.tv_error_message)

        barChart.visibility = View.GONE
        rvTop.visibility = View.GONE
        requireView().findViewById<LinearLayout>(R.id.empty_state).visibility = View.GONE

        errorState.visibility = View.VISIBLE
        tvErrorMessage.text = errorMessage
    }

    private fun showEmptyState() {
        val emptyState: LinearLayout = requireView().findViewById(R.id.empty_state)

        barChart.visibility = View.GONE
        rvTop.visibility = View.GONE
        requireView().findViewById<LinearLayout>(R.id.error_state).visibility = View.GONE

        emptyState.visibility = View.VISIBLE
    }
    private fun showData(data: List<com.application.managerusahav2.data.model.response.TopBarangData>) {
        requireView().findViewById<LinearLayout>(R.id.error_state).visibility = View.GONE
        requireView().findViewById<LinearLayout>(R.id.empty_state).visibility = View.GONE
        requireView().findViewById<LinearLayout>(R.id.loading_state).visibility = View.GONE
        requireView().findViewById<LinearLayout>(R.id.chart_loading).visibility = View.GONE

        barChart.visibility = View.VISIBLE
        rvTop.visibility = View.VISIBLE

        updateChart(data)
        topBarangAdapter.submitList(data)
    }

    private fun updateChart(data: List<com.application.managerusahav2.data.model.response.TopBarangData>) {
        if (data.isEmpty()) {
            barChart.clear()
            return
        }

        // Take top 5 for chart display
        val topData = data.take(5)

        // Create bar entries for different metrics
        val omsetEntries = mutableListOf<BarEntry>()
        val profitEntries = mutableListOf<BarEntry>()
        val labels = mutableListOf<String>()

        topData.forEachIndexed { index, item ->
            omsetEntries.add(BarEntry(index.toFloat(), item.omset.toFloat()))
            profitEntries.add(BarEntry(index.toFloat(), item.profit.toFloat()))

            // Truncate long names for chart labels
            val shortName = if (item.nama.length > 10) {
                "${item.nama.take(10)}..."
            } else {
                item.nama
            }
            labels.add(shortName)
        }

        // Create datasets
        val omsetDataSet = BarDataSet(omsetEntries, "Omset").apply {
            color = ContextCompat.getColor(requireContext(), R.color.primary)
            valueTextColor = ContextCompat.getColor(requireContext(), R.color.grey_text)
            valueTextSize = 8f
            valueFormatter = object : ValueFormatter() {
                override fun getFormattedValue(value: Float): String {
                    val nf = NumberFormat.getInstance(Locale("in", "ID"))
                    return when {
                        value >= 1000000 -> "${nf.format((value / 1000000).toInt())}M"
                        value >= 1000 -> "${nf.format((value / 1000).toInt())}K"
                        else -> nf.format(value.toInt())
                    }
                }
            }
        }

        val profitDataSet = BarDataSet(profitEntries, "Profit").apply {
            color = ContextCompat.getColor(requireContext(), R.color.blue)
            valueTextColor = ContextCompat.getColor(requireContext(), R.color.grey_text)
            valueTextSize = 8f
            valueFormatter = object : ValueFormatter() {
                override fun getFormattedValue(value: Float): String {
                    val nf = NumberFormat.getInstance(Locale("in", "ID"))
                    return when {
                        value >= 1000000 -> "${nf.format((value / 1000000).toInt())}M"
                        value >= 1000 -> "${nf.format((value / 1000).toInt())}K"
                        else -> nf.format(value.toInt())
                    }
                }
            }
        }

        // Create bar data with grouped bars
        val barData = BarData(omsetDataSet, profitDataSet).apply {
            barWidth = 0.35f
        }

        // Set data to chart
        barChart.data = barData
        barChart.groupBars(-0.5f, 0.3f, 0.03f) // Group bars with spacing

        // Update X-axis labels
        barChart.xAxis.apply {
            valueFormatter = IndexAxisValueFormatter(labels)
            labelCount = labels.size
            setCenterAxisLabels(true)
            granularity = 1f
        }

        // Refresh chart
        barChart.invalidate()
    }
}