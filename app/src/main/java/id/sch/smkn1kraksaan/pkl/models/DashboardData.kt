package id.sch.smkn1kraksaan.pkl.models

import com.google.gson.annotations.SerializedName

data class DashboardData(
    @SerializedName("user") val user: User,
    @SerializedName("penempatan") val penempatan: Penempatan?,
    @SerializedName("periode") val periode: Periode?,
    @SerializedName("statistik") val statistik: Statistik?,
    @SerializedName("absen_hari_ini") val absenHariIni: AbsenHariIni?

)

data class Penempatan(
    @SerializedName("id") val id: Int,
    @SerializedName("nama_dudi") val namaDudi: String,
    @SerializedName("alamat_dudi") val alamatDudi: String?,
    @SerializedName("logo_dudi") val logoDudi: String?,
    @SerializedName("nama_pimpinan") val namaPimpinan: String?,
    @SerializedName("nohp_dudi") val nohpDudi: String?,
    @SerializedName("nama_guru") val namaGuru: String,
    @SerializedName("nip") val nip: String?,
    @SerializedName("tanggal_mulai") val tanggalMulai: String?,
    @SerializedName("tanggal_selesai") val tanggalSelesai: String?,
    @SerializedName("status") val status: String
)

data class Periode(
    @SerializedName("status") val status: String,        // aktif | belum | selesai | batal
    @SerializedName("message") val message: String,
    @SerializedName("can_absen") val canAbsen: Boolean,
    @SerializedName("can_jurnal") val canJurnal: Boolean
)

data class Statistik(
    @SerializedName("jurnal_harian") val jurnalHarian: JurnalHarianStat?,
    @SerializedName("jurnal_bulanan") val jurnalBulanan: JurnalBulananStat?,
    @SerializedName("absensi") val absensi: AbsensiStat?
)

data class JurnalHarianStat(
    @SerializedName("total") val total: Int,
    @SerializedName("bulan_ini") val bulanIni: Int
)

data class JurnalBulananStat(
    @SerializedName("total") val total: Int
)

data class AbsensiStat(
    @SerializedName("total") val total: Int,
    @SerializedName("hadir") val hadir: Int,
    @SerializedName("izin") val izin: Int,
    @SerializedName("sakit") val sakit: Int,
    @SerializedName("alpa") val alpa: Int,
    @SerializedName("pending") val pending: Int
)

data class AbsenHariIni(
    @SerializedName("id") val id: Int,
    @SerializedName("status") val status: String,
    @SerializedName("jam_absen") val jamAbsen: String?,
    @SerializedName("foto_selfie") val fotoSelfie: String?,
    @SerializedName("keterangan") val keterangan: String?,
    @SerializedName("validasi_status") val validasiStatus: String,
    @SerializedName("validasi_catatan") val validasiCatatan: String?
)