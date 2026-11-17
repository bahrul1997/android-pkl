package id.sch.smkn1kraksaan.pkl.adapters

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import id.sch.smkn1kraksaan.pkl.R
import id.sch.smkn1kraksaan.pkl.models.JurnalBulananItem

class JurnalBulananAdapter(
    private var list: List<JurnalBulananItem>,
    private val onLihatClick: (JurnalBulananItem) -> Unit,
    private val onExportClick: (JurnalBulananItem) -> Unit
) : RecyclerView.Adapter<JurnalBulananAdapter.ViewHolder>() {

    fun updateData(newList: List<JurnalBulananItem>) {
        list = newList
        notifyDataSetChanged()
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_jurnal_bulanan, parent, false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.bind(list[position])
    }

    override fun getItemCount(): Int = list.size

    inner class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {

        private val tvPeriode: TextView = itemView.findViewById(R.id.tvPeriode)
        private val tvPreview: TextView = itemView.findViewById(R.id.tvPreview)
        private val ivFoto: ImageView = itemView.findViewById(R.id.ivFoto)
        private val btnLihat: TextView = itemView.findViewById(R.id.btnLihat)
        private val btnExport: TextView = itemView.findViewById(R.id.btnExport)

        fun bind(item: JurnalBulananItem) {
            tvPeriode.text = "📅 ${item.periode}"

            // Preview kegiatan (potong kalau panjang)
            val preview = if (item.kegiatan.length > 150) {
                item.kegiatan.substring(0, 150) + "..."
            } else {
                item.kegiatan
            }
            tvPreview.text = preview

            // Foto
            if (!item.foto.isNullOrEmpty()) {
                ivFoto.visibility = View.VISIBLE
                Glide.with(itemView.context)
                    .load(item.foto)
                    .placeholder(R.mipmap.ic_launcher)
                    .into(ivFoto)
            } else {
                ivFoto.visibility = View.GONE
            }

            // Buttons
            btnLihat.setOnClickListener { onLihatClick(item) }
            btnExport.setOnClickListener { onExportClick(item) }
        }
    }
}