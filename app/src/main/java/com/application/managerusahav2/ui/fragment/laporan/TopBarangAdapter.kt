package com.application.managerusahav2.ui.fragment.laporan

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.application.managerusahav2.R
import com.application.managerusahav2.data.model.response.TopBarangData
import com.bumptech.glide.Glide
import java.text.NumberFormat
import java.util.*

class TopBarangAdapter : ListAdapter<TopBarangData, TopBarangAdapter.TopBarangViewHolder>(TopBarangDiffCallback()) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): TopBarangViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_top_barang, parent, false)
        return TopBarangViewHolder(view)
    }

    override fun onBindViewHolder(holder: TopBarangViewHolder, position: Int) {
        holder.bind(getItem(position), position + 1)
    }

    class TopBarangViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val tvRanking: TextView = itemView.findViewById(R.id.tv_ranking)
        private val ivProduct: ImageView = itemView.findViewById(R.id.iv_product)
        private val tvNamaBarang: TextView = itemView.findViewById(R.id.tv_nama_barang)
        private val tvKategori: TextView = itemView.findViewById(R.id.tv_kategori)
        private val tvTerjual: TextView = itemView.findViewById(R.id.tv_terjual)
        private val tvOmset: TextView = itemView.findViewById(R.id.tv_omset)
        private val tvProfit: TextView = itemView.findViewById(R.id.tv_profit)

        fun bind(item: TopBarangData, ranking: Int) {
            val nf = NumberFormat.getCurrencyInstance(Locale("in", "ID"))

            tvRanking.text = ranking.toString()
            tvNamaBarang.text = item.nama
            tvKategori.text = item.kategori
            tvTerjual.text = "${item.terjual} pcs"
            tvOmset.text = nf.format(item.omset.toLong())
            tvProfit.text = nf.format(item.profit.toLong())

            // Load image
            if (!item.gambarPath.isNullOrEmpty()) {
                Glide.with(itemView.context)
                    .load(item.gambarPath)
                    .placeholder(R.drawable.ic_placeholder_image)
                    .error(R.drawable.ic_placeholder_image)
                    .into(ivProduct)
            } else {
                ivProduct.setImageResource(R.drawable.ic_placeholder_image)
            }
        }
    }

    class TopBarangDiffCallback : DiffUtil.ItemCallback<TopBarangData>() {
        override fun areItemsTheSame(oldItem: TopBarangData, newItem: TopBarangData): Boolean {
            return oldItem.id == newItem.id
        }

        override fun areContentsTheSame(oldItem: TopBarangData, newItem: TopBarangData): Boolean {
            return oldItem == newItem
        }
    }
}