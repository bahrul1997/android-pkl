package id.sch.smkn1kraksaan.pkl.models

import com.google.gson.annotations.SerializedName

// ==========================================
// LIST ABSENSI (GET /api/absensi)
// ==========================================
data class AbsensiListData(
    @SerializedName("bulan") val bulan: Int,
    @SerializedName("tahun") val tahun: Int,
    @SerializedName("riwayat") val riwayat: List<AbsenItem>,
    @SerializedName("statistik") val statistik: AbsensiStat
)

data class AbsenItem(
    @SerializedName("id") val id: Int,
    @SerializedName("tanggal") val tanggal: String,
    @SerializedName("tanggal_formatted") val tanggalFormatted: String,
    @SerializedName("jam_absen") val jamAbsen: String?,
    @SerializedName("status") val status: String,        // hadir | izin | sakit | alpa
    @SerializedName("keterangan") val keterangan: String?,
    @SerializedName("foto_selfie") val fotoSelfie: String?,
    @SerializedName("foto_surat") val fotoSurat: String?,
    @SerializedName("validasi_status") val validasiStatus: String,  // pending | approved | rejected
    @SerializedName("validasi_catatan") val validasiCatatan: String?
)

// ==========================================
// ABSEN HARI INI (GET /api/absensi/hari-ini)
// ==========================================
data class AbsenHariIniData(
    @SerializedName("tanggal") val tanggal: String,
    @SerializedName("tanggal_formatted") val tanggalFormatted: String,
    @SerializedName("sudah_absen") val sudahAbsen: Boolean,
    @SerializedName("can_absen") val canAbsen: Boolean,
    @SerializedName("absen") val absen: AbsenItem?
)