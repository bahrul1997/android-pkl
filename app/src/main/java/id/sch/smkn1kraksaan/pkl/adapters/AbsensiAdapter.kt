package id.sch.smkn1kraksaan.pkl.adapters

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import id.sch.smkn1kraksaan.pkl.R
import id.sch.smkn1kraksaan.pkl.models.AbsenItem

class AbsensiAdapter(
    private var list: List<AbsenItem>,
    private val onItemClick: (AbsenItem) -> Unit
) : RecyclerView.Adapter<AbsensiAdapter.ViewHolder>() {

    fun updateData(newList: List<AbsenItem>) {
        list = newList
        notifyDataSetChanged()
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_absensi, parent, false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.bind(list[position])
    }

    override fun getItemCount(): Int = list.size

    inner class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {

        private val tvTanggal: TextView = itemView.findViewById(R.id.tvTanggal)
        private val tvStatus: TextView = itemView.findViewById(R.id.tvStatus)
        private val tvJam: TextView = itemView.findViewById(R.id.tvJam)
        private val tvValidasi: TextView = itemView.findViewById(R.id.tvValidasi)
        private val tvKeterangan: TextView = itemView.findViewById(R.id.tvKeterangan)
        private val ivSelfie: ImageView = itemView.findViewById(R.id.ivSelfie)
        private val layoutSurat: LinearLayout = itemView.findViewById(R.id.layoutSurat)

        fun bind(item: AbsenItem) {
            // Tanggal
            tvTanggal.text = item.tanggalFormatted

            // Status + emoji
            val (emoji, statusText, color) = when (item.status) {
                "hadir" -> Triple("✅", "Hadir", "#4CAF50")
                "izin" -> Triple("📝", "Izin", "#FF9800")
                "sakit" -> Triple("🤒", "Sakit", "#2196F3")
                "alpa" -> Triple("❌", "Alpa", "#F44336")
                else -> Triple("❓", "-", "#888888")
            }
            tvStatus.text = "$emoji $statusText"
            tvStatus.setTextColor(android.graphics.Color.parseColor(color))

            // Jam
            tvJam.text = if (!item.jamAbsen.isNullOrEmpty() && item.jamAbsen.length >= 5) {
                "Jam ${item.jamAbsen.substring(0, 5)}"
            } else "-"

            // Validasi
            when (item.validasiStatus) {
                "approved" -> {
                    tvValidasi.text = "✅ Disetujui"
                    tvValidasi.setTextColor(android.graphics.Color.parseColor("#4CAF50"))
                }
                "rejected" -> {
                    tvValidasi.text = "❌ Ditolak"
                    tvValidasi.setTextColor(android.graphics.Color.parseColor("#F44336"))
                }
                else -> {
                    tvValidasi.text = "⏳ Menunggu"
                    tvValidasi.setTextColor(android.graphics.Color.parseColor("#FF9800"))
                }
            }

            // Keterangan
            if (!item.keterangan.isNullOrEmpty()) {
                tvKeterangan.visibility = View.VISIBLE
                tvKeterangan.text = item.keterangan
            } else {
                tvKeterangan.visibility = View.GONE
            }

            // Foto selfie
            if (!item.fotoSelfie.isNullOrEmpty()) {
                Glide.with(itemView.context)
                    .load(item.fotoSelfie)
                    .placeholder(R.mipmap.ic_launcher)
                    .into(ivSelfie)
            } else {
                ivSelfie.setImageResource(R.mipmap.ic_launcher)
            }

            // Surat (kalau ada)
            if (!item.fotoSurat.isNullOrEmpty()) {
                layoutSurat.visibility = View.VISIBLE
            } else {
                layoutSurat.visibility = View.GONE
            }

            // Klik item (kalau rejected, buat reupload)
            itemView.setOnClickListener {
                onItemClick(item)
            }
        }
    }
}