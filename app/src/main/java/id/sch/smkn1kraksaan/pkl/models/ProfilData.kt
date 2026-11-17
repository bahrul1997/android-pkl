package id.sch.smkn1kraksaan.pkl.models

import com.google.gson.annotations.SerializedName

data class ProfilData(
    @SerializedName("id") val id: Int,
    @SerializedName("username") val username: String,
    @SerializedName("nama") val nama: String,
    @SerializedName("role") val role: String,
    @SerializedName("foto") val foto: String?,
    @SerializedName("is_active") val isActive: Int,
    @SerializedName("created_at") val createdAt: String?,
    @SerializedName("nisn") val nisn: String?,
    @SerializedName("kelas") val kelas: String?,
    @SerializedName("jurusan") val jurusan: String?,
    @SerializedName("no_hp") val noHp: String?
)

data class UpdateProfilRequest(
    @SerializedName("nama") val nama: String,
    @SerializedName("username") val username: String
)

data class GantiPasswordRequest(
    @SerializedName("password_lama") val passwordLama: String,
    @SerializedName("password_baru") val passwordBaru: String,
    @SerializedName("konfirmasi") val konfirmasi: String
)

data class FotoResponse(
    @SerializedName("foto") val foto: String
)