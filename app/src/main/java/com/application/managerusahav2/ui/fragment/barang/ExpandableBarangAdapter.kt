package com.application.managerusahav2.ui.fragment.barang

import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.application.managerusahav2.R
import com.application.managerusahav2.data.model.response.Barang
import com.bumptech.glide.Glide
import java.text.NumberFormat
import java.util.Locale

class ExpandableBarangAdapter(
    private val onItemClick: (Barang) -> Unit = {},
    private val onEditClick: (Barang) -> Unit = {},
    private val onDeleteClick: (Barang) -> Unit = {},
    private val onHeaderClicked: (String) -> Unit = {} // callback ke fragment untuk toggle
) : ListAdapter<DisplayItem, RecyclerView.ViewHolder>(DisplayDiffCallback()) {

    companion object {
        private const val TYPE_HEADER = 0
        private const val TYPE_CHILD = 1
    }

    init { setHasStableIds(true) }

    override fun getItemId(position: Int): Long {
        return when (val item = getItem(position)) {
            is DisplayItem.Header -> ("H:" + item.kategori).hashCode().toLong()
            is DisplayItem.Child -> item.barang.id.toLong()
        }
    }

    override fun getItemViewType(position: Int): Int {
        return when (getItem(position)) {
            is DisplayItem.Header -> TYPE_HEADER
            is DisplayItem.Child -> TYPE_CHILD
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
            else -> throw IllegalArgumentException("Invalid viewType")
        }
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        val item = getItem(position)
        when {
            holder is HeaderViewHolder && item is DisplayItem.Header -> holder.bind(item)
            holder is ChildViewHolder && item is DisplayItem.Child -> holder.bind(item.barang)
        }
    }

    inner class HeaderViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val tvKategori: TextView = itemView.findViewById(R.id.tv_kategori_header)
        private val tvItemCount: TextView = itemView.findViewById(R.id.tv_item_count)
        private val ivExpandIcon: ImageView = itemView.findViewById(R.id.iv_expand_icon)

        fun bind(header: DisplayItem.Header) {
            tvKategori.text = header.kategori
            tvItemCount.text = "${header.itemCount} item"

            // rotate icon sesuai state
            val targetRotation = if (header.isExpanded) 180f else 0f
            ivExpandIcon.animate().rotation(targetRotation).setDuration(200).start()

            itemView.setOnClickListener {
                onHeaderClicked(header.kategori)
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

            val currencyFormat = NumberFormat.getCurrencyInstance(Locale("in", "ID"))
            tvHarga.text = currencyFormat.format(barang.harga)

            if (!barang.gambarPath.isNullOrEmpty()) {
                Log.d("GlideTest", "Gambar path: ${barang.gambarPath}")
                Glide.with(itemView.context)
                    .load(barang.gambarPath)
                    .placeholder(R.drawable.ic_placeholder_image)
                    .error(R.drawable.ic_placeholder_image)
                    .into(ivGambar)
            } else {
                Log.d("GlideTest", "Gambar path: ${barang.gambarPath}")
                ivGambar.setImageResource(R.drawable.ic_placeholder_image)
            }

            // low stock feedback
            if (barang.stok <= 5) {
                tvStok.setTextColor(itemView.context.getColor(R.color.red))
            } else {
                tvStok.setTextColor(itemView.context.getColor(R.color.text_secondary))
            }

            itemView.setOnClickListener { onItemClick(barang) }
            // you can add edit/delete buttons and their listeners via onEditClick/onDeleteClick
        }
    }
}

class DisplayDiffCallback : DiffUtil.ItemCallback<DisplayItem>() {
    override fun areItemsTheSame(oldItem: DisplayItem, newItem: DisplayItem): Boolean {
        return when {
            oldItem is DisplayItem.Header && newItem is DisplayItem.Header ->
                oldItem.kategori == newItem.kategori
            oldItem is DisplayItem.Child && newItem is DisplayItem.Child ->
                oldItem.barang.id == newItem.barang.id
            else -> false
        }
    }

    override fun areContentsTheSame(oldItem: DisplayItem, newItem: DisplayItem): Boolean {
        return oldItem == newItem
    }
}
