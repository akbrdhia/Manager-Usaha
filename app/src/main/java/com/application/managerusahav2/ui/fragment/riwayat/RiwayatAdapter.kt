package com.application.managerusahav2.ui.fragment.riwayat

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.core.content.ContextCompat
import androidx.core.view.isVisible
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.application.managerusahav2.R
import com.application.managerusahav2.data.model.RiwayatDisplayItem
import com.application.managerusahav2.data.model.RiwayatItem
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

// RiwayatAdapter.kt
class RiwayatAdapter : ListAdapter<RiwayatDisplayItem, RecyclerView.ViewHolder>(RiwayatDiffCallback()) {

    companion object {
        private const val TYPE_DATE_HEADER = 0
        private const val TYPE_RIWAYAT_ITEM = 1
        private const val TYPE_LOADING = 2
    }

    private var isLoadingMore = false

    fun setLoadingMore(loading: Boolean) {
        val wasLoading = isLoadingMore
        isLoadingMore = loading

        if (wasLoading && !loading) {
            notifyItemRemoved(itemCount)
        } else if (!wasLoading && loading) {
            notifyItemInserted(itemCount - 1)
        }
    }

    override fun getItemViewType(position: Int): Int {
        return when {
            position == itemCount - 1 && isLoadingMore -> TYPE_LOADING
            position < currentList.size && getItem(position) is RiwayatDisplayItem.DateHeader -> TYPE_DATE_HEADER
            else -> TYPE_RIWAYAT_ITEM
        }
    }

    override fun getItemCount(): Int {
        return super.getItemCount() + if (isLoadingMore) 1 else 0
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        return when (viewType) {
            TYPE_DATE_HEADER -> {
                val view = LayoutInflater.from(parent.context)
                    .inflate(R.layout.item_date_header, parent, false)
                DateHeaderViewHolder(view)
            }
            TYPE_LOADING -> {
                val view = LayoutInflater.from(parent.context)
                    .inflate(R.layout.item_loading, parent, false)
                LoadingViewHolder(view)
            }
            else -> {
                val view = LayoutInflater.from(parent.context)
                    .inflate(R.layout.item_riwayat, parent, false)
                RiwayatViewHolder(view)
            }
        }
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        when (holder) {
            is DateHeaderViewHolder -> {
                if (position < currentList.size) {
                    val item = getItem(position) as RiwayatDisplayItem.DateHeader
                    holder.bind(item.dateString)
                }
            }
            is RiwayatViewHolder -> {
                if (position < currentList.size) {
                    val item = getItem(position) as RiwayatDisplayItem.RiwayatData
                    holder.bind(item.riwayatItem)
                }
            }
            // LoadingViewHolder doesn't need binding
        }
    }
}

// ViewHolders
class DateHeaderViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
    private val tvDate: TextView = itemView.findViewById(R.id.tv_date)

    fun bind(dateString: String) {
        tvDate.text = dateString
    }
}

class RiwayatViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
    private val ivIcon: ImageView = itemView.findViewById(R.id.iv_activity_icon)
    private val tvNamaBarang: TextView = itemView.findViewById(R.id.tv_nama_barang)
    private val tvKategori: TextView = itemView.findViewById(R.id.tv_kategori)
    private val tvAktivitas: TextView = itemView.findViewById(R.id.tv_aktivitas)
    private val tvJumlah: TextView = itemView.findViewById(R.id.tv_jumlah)
    private val tvWaktu: TextView = itemView.findViewById(R.id.tv_waktu)

    fun bind(item: RiwayatItem) {
        tvNamaBarang.text = item.barang.nama
        tvKategori.text = item.barang.kategori

        // Set activity type and icon
        when (item.tipe) {
            "create" -> {
                tvAktivitas.text = "Ditambahkan"
                ivIcon.setImageResource(R.drawable.ic_add_circle)
                ivIcon.setColorFilter(ContextCompat.getColor(itemView.context, R.color.green))
                tvJumlah.isVisible = false
            }
            "update" -> {
                tvAktivitas.text = "Diperbarui"
                ivIcon.setImageResource(R.drawable.ic_edit)
                ivIcon.setColorFilter(ContextCompat.getColor(itemView.context, R.color.blue))
                tvJumlah.isVisible = false
            }
            "delete" -> {
                tvAktivitas.text = "Dihapus"
                ivIcon.setImageResource(R.drawable.ic_delete)
                ivIcon.setColorFilter(ContextCompat.getColor(itemView.context, R.color.secondary))
                tvJumlah.isVisible = false
            }
            "tambah_stok", "plus" -> {
                tvAktivitas.text = "Restok"
                ivIcon.setImageResource(R.drawable.ic_trending_up)
                ivIcon.setColorFilter(ContextCompat.getColor(itemView.context, R.color.info))
                tvJumlah.text = "+${item.jumlah} pcs"
                tvJumlah.setTextColor(ContextCompat.getColor(itemView.context, R.color.info))
                tvJumlah.isVisible = true
            }
            "kurangi_stok", "min" -> {
                tvAktivitas.text = "Dibeli"
                ivIcon.setImageResource(R.drawable.ic_trending_down)
                ivIcon.setColorFilter(ContextCompat.getColor(itemView.context, R.color.red))
                tvJumlah.text = "-${item.jumlah} pcs"
                tvJumlah.setTextColor(ContextCompat.getColor(itemView.context, R.color.red))
                tvJumlah.isVisible = true
            }
            else -> {
                tvAktivitas.text = item.tipe.capitalize()
                ivIcon.setImageResource(R.drawable.ic_help_outline)
                ivIcon.setColorFilter(ContextCompat.getColor(itemView.context, R.color.text_secondary))
                tvJumlah.isVisible = item.jumlah > 0
                if (item.jumlah > 0) {
                    tvJumlah.text = "${item.jumlah} pcs"
                    tvJumlah.setTextColor(ContextCompat.getColor(itemView.context, R.color.text_secondary))
                }
            }
        }

        // Format time
        tvWaktu.text = getTimeString(item.tanggal)
    }

    private fun getTimeString(timestamp: Long): String {
        val format = SimpleDateFormat("HH:mm", Locale.getDefault())
        return format.format(Date(timestamp))
    }
}

class LoadingViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView)

// DiffCallback
class RiwayatDiffCallback : DiffUtil.ItemCallback<RiwayatDisplayItem>() {
    override fun areItemsTheSame(oldItem: RiwayatDisplayItem, newItem: RiwayatDisplayItem): Boolean {
        return when {
            oldItem is RiwayatDisplayItem.DateHeader && newItem is RiwayatDisplayItem.DateHeader ->
                oldItem.dateString == newItem.dateString
            oldItem is RiwayatDisplayItem.RiwayatData && newItem is RiwayatDisplayItem.RiwayatData ->
                oldItem.riwayatItem.id == newItem.riwayatItem.id
            else -> false
        }
    }

    override fun areContentsTheSame(oldItem: RiwayatDisplayItem, newItem: RiwayatDisplayItem): Boolean {
        return oldItem == newItem
    }
}