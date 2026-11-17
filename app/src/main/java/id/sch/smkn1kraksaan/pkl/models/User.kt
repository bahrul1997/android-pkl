package id.sch.smkn1kraksaan.pkl.models

import com.google.gson.annotations.SerializedName

data class User(
    @SerializedName("id") val id: Int,
    @SerializedName("username") val username: String,
    @SerializedName("nama") val nama: String,
    @SerializedName("role") val role: String,
    @SerializedName("foto") val foto: String?,
    @SerializedName("nisn") val nisn: String?,
    @SerializedName("kelas") val kelas: String?,
    @SerializedName("jurusan") val jurusan: String?,
    @SerializedName("nama_instruktur") val namaInstruktur: String?,
    @SerializedName("jabatan_instruktur") val jabatanInstruktur: String?
)