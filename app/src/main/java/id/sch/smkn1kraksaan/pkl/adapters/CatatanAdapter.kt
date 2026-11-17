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
import id.sch.smkn1kraksaan.pkl.models.CatatanItem

class CatatanAdapter(
    private var list: List<CatatanItem>,
    private val onEditClick: (CatatanItem) -> Unit
) : RecyclerView.Adapter<CatatanAdapter.ViewHolder>() {

    fun updateData(newList: List<CatatanItem>) {
        list = newList
        notifyDataSetChanged()
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_catatan, parent, false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.bind(list[position])
    }

    override fun getItemCount(): Int = list.size

    inner class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val tvNamaPekerjaan: TextView = itemView.findViewById(R.id.tvNamaPekerjaan)
        private val tvPerencanaan: TextView = itemView.findViewById(R.id.tvPerencanaan)
        private val tvStatus: TextView = itemView.findViewById(R.id.tvStatus)
        private val tvCatatanGuru: TextView = itemView.findViewById(R.id.tvCatatanGuru)
        private val ivFoto: ImageView = itemView.findViewById(R.id.ivFoto)
        private val btnEdit: ImageView = itemView.findViewById(R.id.btnEdit) // Pastikan ID ini ada di layout

        fun bind(item: CatatanItem) {
            tvNamaPekerjaan.text = item.namaPekerjaan
            tvPerencanaan.text = if (!item.perencanaan.isNullOrEmpty()) "Rencana: ${item.perencanaan}" else ""

            when (item.validasiStatus) {
                "approved" -> {
                    tvStatus.text = "✓ Disetujui"
                    tvStatus.setTextColor(Color.parseColor("#4CAF50"))
                }
                "rejected" -> {
                    tvStatus.text = "✗ Revisi"
                    tvStatus.setTextColor(Color.parseColor("#F44336"))
                }
                else -> {
                    tvStatus.text = "⏳ Menunggu Validasi"
                    tvStatus.setTextColor(Color.parseColor("#FF9800"))
                }
            }

            if (!item.catatanGuru.isNullOrEmpty()) {
                tvCatatanGuru.visibility = View.VISIBLE
                tvCatatanGuru.text = "Catatan Guru: ${item.catatanGuru}"
            } else {
                tvCatatanGuru.visibility = View.GONE
            }

            if (!item.foto.isNullOrEmpty()) {
                ivFoto.visibility = View.VISIBLE
                Glide.with(itemView.context).load(item.foto).into(ivFoto)
            } else {
                ivFoto.visibility = View.GONE
            }

            // Logika Tombol Edit
            if (item.validasiStatus == "approved") {
                btnEdit.visibility = View.GONE
            } else {
                btnEdit.visibility = View.VISIBLE
                btnEdit.setOnClickListener { onEditClick(item) }
            }
        }
    }
}