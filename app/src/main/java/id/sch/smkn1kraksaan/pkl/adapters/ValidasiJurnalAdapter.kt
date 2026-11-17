package id.sch.smkn1kraksaan.pkl.adapters

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import id.sch.smkn1kraksaan.pkl.R
import id.sch.smkn1kraksaan.pkl.models.ValidasiJurnal

class ValidasiJurnalAdapter(
    private var list: List<ValidasiJurnal>,
    private val onApprove: (ValidasiJurnal) -> Unit,
    private val onReject: (ValidasiJurnal) -> Unit
) : RecyclerView.Adapter<ValidasiJurnalAdapter.ViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_validasi_jurnal, parent, false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.bind(list[position])
    }

    override fun getItemCount(): Int = list.size

    fun updateData(newList: List<ValidasiJurnal>) {
        list = newList
        notifyDataSetChanged()
    }

    inner class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val tvNamaSiswa: TextView = itemView.findViewById(R.id.tvNamaSiswa)
        val tvTanggal: TextView = itemView.findViewById(R.id.tvTanggal)
        val tvKegiatan: TextView = itemView.findViewById(R.id.tvKegiatan)
        val ivFoto: ImageView = itemView.findViewById(R.id.ivFoto)
        val btnApprove = itemView.findViewById<com.google.android.material.button.MaterialButton>(R.id.btnApprove)
        val btnReject = itemView.findViewById<com.google.android.material.button.MaterialButton>(R.id.btnReject)

        fun bind(item: ValidasiJurnal) {
            tvNamaSiswa.text = item.namaSiswa
            tvTanggal.text = item.tanggal
            tvKegiatan.text = item.kegiatan

            if (!item.foto.isNullOrEmpty()) {
                ivFoto.visibility = View.VISIBLE
                Glide.with(itemView.context).load(item.foto).into(ivFoto)
            } else {
                ivFoto.visibility = View.GONE
            }

            btnApprove.setOnClickListener { onApprove(item) }
            btnReject.setOnClickListener { onReject(item) }
        }
    }
}