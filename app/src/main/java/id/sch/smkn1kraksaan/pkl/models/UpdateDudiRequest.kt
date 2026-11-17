package id.sch.smkn1kraksaan.pkl.models

import com.google.gson.annotations.SerializedName

data class UpdateDudiRequest(
    @SerializedName("nama_pimpinan") val namaPimpinan: String?,
    @SerializedName("nama_instruktur") val namaInstruktur: String,
    @SerializedName("jabatan_instruktur") val jabatanInstruktur: String?,
    @SerializedName("nohp_instruktur") val nohpInstruktur: String?
)