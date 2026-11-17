package id.sch.smkn1kraksaan.pkl.models

import com.google.gson.annotations.SerializedName

data class GuruValidasiResponse(
    @SerializedName("jurnal") val jurnal: List<ValidasiJurnal>,
    @SerializedName("catatan") val catatan: List<ValidasiCatatan>,
    @SerializedName("absensi") val absensi: List<ValidasiAbsensi>
)

data class ValidasiJurnal(
    @SerializedName("id") val id: Int,
    @SerializedName("penempatan_id") val penempatanId: Int,
    @SerializedName("nama_siswa") val namaSiswa: String,
    @SerializedName("tanggal") val tanggal: String,
    @SerializedName("kegiatan") val kegiatan: String,
    @SerializedName("foto") val foto: String?
)

data class ValidasiCatatan(
    @SerializedName("id") val id: Int,
    @SerializedName("penempatan_id") val penempatanId: Int,
    @SerializedName("nama_siswa") val namaSiswa: String,
    @SerializedName("nama_pekerjaan") val namaPekerjaan: String,
    @SerializedName("foto") val foto: String?
)

data class ValidasiAbsensi(
    @SerializedName("id") val id: Int,
    @SerializedName("penempatan_id") val penempatanId: Int,
    @SerializedName("nama_siswa") val namaSiswa: String,
    @SerializedName("tanggal") val tanggal: String,
    @SerializedName("jam_absen") val jamAbsen: String?,
    @SerializedName("status") val status: String,
    @SerializedName("foto_selfie") val fotoSelfie: String?
)