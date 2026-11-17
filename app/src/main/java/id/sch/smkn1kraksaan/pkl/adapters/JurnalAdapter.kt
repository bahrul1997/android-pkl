package id.sch.smkn1kraksaan.pkl.adapters

import android.graphics.Color
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import id.sch.smkn1kraksaan.pkl.R
import id.sch.smkn1kraksaan.pkl.models.JurnalItem

class JurnalAdapter(
    private var list: List<JurnalItem>,
    private val onEditClick: (JurnalItem) -> Unit,
    private val onDeleteClick: (JurnalItem) -> Unit
) : RecyclerView.Adapter<JurnalAdapter.ViewHolder>() {

    fun updateData(newList: List<JurnalItem>) {
        list = newList
        notifyDataSetChanged()
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_jurnal, parent, false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.bind(list[position])
    }

    override fun getItemCount(): Int = list.size

    inner class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {

        private val tvTanggal: TextView = itemView.findViewById(R.id.tvTanggal)
        private val tvKegiatan: TextView = itemView.findViewById(R.id.tvKegiatan)
        private val ivFoto: ImageView = itemView.findViewById(R.id.ivFoto)
        private val btnEdit: ImageView = itemView.findViewById(R.id.btnEdit)
        private val btnDelete: ImageView = itemView.findViewById(R.id.btnDelete)
        private val tvStatusValidasi: TextView = itemView.findViewById(R.id.tvStatusValidasi)
        private val tvCatatanGuru: TextView = itemView.findViewById(R.id.tvCatatanGuru)

        fun bind(item: JurnalItem) {
            tvTanggal.text = item.tanggalFormatted ?: item.tanggal
            tvKegiatan.text = item.kegiatan

            // 1. Tampilkan Foto
            if (!item.foto.isNullOrEmpty()) {
                ivFoto.visibility = View.VISIBLE
                Glide.with(itemView.context)
                    .load(item.foto)
                    .placeholder(R.mipmap.ic_launcher)
                    .into(ivFoto)
            } else {
                ivFoto.visibility = View.GONE
            }

            // 2. Tampilkan Status Validasi & Catatan Guru
            when (item.validasiStatus) {
                "approved" -> {
                    tvStatusValidasi.text = "✓ Disetujui"
                    tvStatusValidasi.setTextColor(Color.parseColor("#4CAF50")) // Hijau
                }
                "rejected" -> {
                    tvStatusValidasi.text = "✗ Revisi"
                    tvStatusValidasi.setTextColor(Color.parseColor("#F44336")) // Merah
                }
                else -> {
                    tvStatusValidasi.text = "⏳ Menunggu Validasi"
                    tvStatusValidasi.setTextColor(Color.parseColor("#FF9800")) // Orange
                }
            }

            if (!item.catatanGuru.isNullOrEmpty()) {
                tvCatatanGuru.visibility = View.VISIBLE
                tvCatatanGuru.text = "Catatan Guru: ${item.catatanGuru}"
            } else {
                tvCatatanGuru.visibility = View.GONE
            }

            // 3. Logika Tombol Aksi (Sembunyikan Edit/Hapus kalau udah Approved)
            if (item.validasiStatus == "approved") {
                btnEdit.visibility = View.GONE
                btnDelete.visibility = View.GONE
            } else {
                btnEdit.visibility = View.VISIBLE
                btnDelete.visibility = View.VISIBLE

                btnEdit.setOnClickListener { onEditClick(item) }
                btnDelete.setOnClickListener { onDeleteClick(item) }
            }
        }
    }
}