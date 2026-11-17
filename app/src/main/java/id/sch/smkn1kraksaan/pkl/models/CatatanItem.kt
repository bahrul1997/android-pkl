package id.sch.smkn1kraksaan.pkl.models

import com.google.gson.annotations.SerializedName

data class CatatanItem(
    @SerializedName("id") val id: Int,
    @SerializedName("nama_pekerjaan") val namaPekerjaan: String,
    @SerializedName("perencanaan") val perencanaan: String?,
    @SerializedName("pelaksanaan") val pelaksanaan: String?,
    @SerializedName("foto") val foto: String?,
    @SerializedName("catatan_instruktur") val catatanInstruktur: String?,
    @SerializedName("validasi_status") val validasiStatus: String?,
    @SerializedName("catatan_guru") val catatanGuru: String?
)