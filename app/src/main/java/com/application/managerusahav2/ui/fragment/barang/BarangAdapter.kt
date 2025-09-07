package com.application.managerusahav2.ui.fragment.barang

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.application.managerusahav2.R
import com.application.managerusahav2.data.model.response.Barang
import com.bumptech.glide.Glide
import java.text.NumberFormat
import java.util.*

sealed class ExpandableItem {
    data class Header(
        val kategori: String,
        val isExpanded: Boolean = true,
        val itemCount: Int = 0
    ) : ExpandableItem()

    data class Child(val barang: Barang) : ExpandableItem()
}

class ExpandableBarangAdapter(
    private val onItemClick: (Barang) -> Unit = {},
    private val onEditClick: (Barang) -> Unit = {},
    private val onDeleteClick: (Barang) -> Unit = {}
) : RecyclerView.Adapter<RecyclerView.ViewHolder>() {

    companion object {
        private const val TYPE_HEADER = 0
        private const val TYPE_CHILD = 1
    }

    private val items = mutableListOf<ExpandableItem>()
    private val expandedCategories = mutableSetOf<String>()
    private var originalBarangList = listOf<Barang>()

    fun submitList(barangList: List<Barang>) {
        originalBarangList = barangList
        val groupedBarang = barangList.groupBy { it.kategori }
        items.clear()

        groupedBarang.forEach { (kategori, barangInCategory) ->
            val isExpanded = expandedCategories.contains(kategori)
            items.add(ExpandableItem.Header(kategori, isExpanded, barangInCategory.size))

            if (isExpanded) {
                barangInCategory.forEach { barang ->
                    items.add(ExpandableItem.Child(barang))
                }
            }
        }
        notifyDataSetChanged()
    }

    override fun getItemViewType(position: Int): Int {
        return when (items[position]) {
            is ExpandableItem.Header -> TYPE_HEADER
            is ExpandableItem.Child -> TYPE_CHILD
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        return when (viewType) {
            TYPE_HEADER -> {
                val view = LayoutInflater.from(parent.context)
                    .inflate(R.layout.item_category, parent, false)
                HeaderViewHolder(view)
            }
            TYPE_CHILD -> {
                val view = LayoutInflater.from(parent.context)
                    .inflate(R.layout.item_barang_child, parent, false)
                ChildViewHolder(view)
            }
            else -> throw IllegalArgumentException("Invalid view type")
        }
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        when (holder) {
            is HeaderViewHolder -> {
                val header = items[position] as ExpandableItem.Header
                holder.bind(header)
            }
            is ChildViewHolder -> {
                val child = items[position] as ExpandableItem.Child
                holder.bind(child.barang)
            }
        }
    }

    override fun getItemCount(): Int = items.size

    inner class HeaderViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val tvKategori: TextView = itemView.findViewById(R.id.tv_kategori_header)
        private val tvItemCount: TextView = itemView.findViewById(R.id.tv_item_count)
        private val ivExpandIcon: ImageView = itemView.findViewById(R.id.iv_expand_icon)

        fun bind(header: ExpandableItem.Header) {
            tvKategori.text = header.kategori
            tvItemCount.text = "${header.itemCount} item"

            // Rotate icon based on expanded state with animation
            val targetRotation = if (header.isExpanded) 180f else 0f
            ivExpandIcon.animate()
                .rotation(targetRotation)
                .setDuration(200)
                .start()

            itemView.setOnClickListener {
                toggleCategory(header.kategori)
            }
        }
    }

    inner class ChildViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val tvNama: TextView = itemView.findViewById(R.id.tv_nama_barang)
        private val tvStok: TextView = itemView.findViewById(R.id.tv_stok)
        private val tvHarga: TextView = itemView.findViewById(R.id.tv_harga)
        private val ivGambar: ImageView = itemView.findViewById(R.id.iv_gambar_barang)

        fun bind(barang: Barang) {
            tvNama.text = barang.nama
            tvStok.text = "Stok: ${barang.stok} pcs"

            // Format currency for Indonesian Rupiah
            val currencyFormat = NumberFormat.getCurrencyInstance(Locale("in", "ID"))
            tvHarga.text = currencyFormat.format(barang.harga)

            // Load image using Glide
            if (!barang.gambarPath.isNullOrEmpty()) {
                Glide.with(itemView.context)
                    .load(barang.gambarPath)
                    .placeholder(R.drawable.ic_placeholder_image)
                    .error(R.drawable.ic_placeholder_image)
                    .into(ivGambar)
            } else {
                ivGambar.setImageResource(R.drawable.ic_placeholder_image)
            }

            // Set click listeners
            itemView.setOnClickListener { onItemClick(barang) }

            // Add visual feedback for low stock
            if (barang.stok <= 5) {
                tvStok.setTextColor(itemView.context.getColor(R.color.red))
            } else {
                tvStok.setTextColor(itemView.context.getColor(R.color.text_secondary))
            }
        }
    }

    private fun toggleCategory(kategori: String) {
        android.util.Log.d("ExpandableAdapter", "Toggle category: $kategori")

        if (expandedCategories.contains(kategori)) {
            expandedCategories.remove(kategori)
            android.util.Log.d("ExpandableAdapter", "Collapsed: $kategori")
        } else {
            expandedCategories.add(kategori)
            android.util.Log.d("ExpandableAdapter", "Expanded: $kategori")
        }

        android.util.Log.d("ExpandableAdapter", "Expanded categories: $expandedCategories")

        // Rebuild items list dengan data asli
        submitList(originalBarangList)
    }

    // Method to get original barang list for refresh
    fun refreshWithData(barangList: List<Barang>) {
        submitList(barangList)
    }
}