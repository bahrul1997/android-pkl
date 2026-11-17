package id.sch.smkn1kraksaan.pkl.models

import com.google.gson.annotations.SerializedName

// ==========================================
// JURNAL HARIAN
// ==========================================
data class JurnalHarianListData(
    @SerializedName("bulan") val bulan: Int,
    @SerializedName("tahun") val tahun: Int,
    @SerializedName("total") val total: Int,
    @SerializedName("jurnal") val jurnal: List<JurnalItem>
)

data class JurnalItem(
    @SerializedName("id") val id: Int,
    @SerializedName("tanggal") val tanggal: String,
    @SerializedName("tanggal_formatted") val tanggalFormatted: String?,
    @SerializedName("kegiatan") val kegiatan: String,
    @SerializedName("foto") val foto: String?,
    @SerializedName("created_at") val createdAt: String?,
    @SerializedName("validasi_status") val validasiStatus: String?,
    @SerializedName("catatan_guru") val catatanGuru: String?
)

// ==========================================
// JURNAL BULANAN (buat FASE 6 nanti)
// ==========================================
data class JurnalBulananListData(
    @SerializedName("total") val total: Int,
    @SerializedName("jurnal") val jurnal: List<JurnalBulananItem>
)

data class JurnalBulananItem(
    @SerializedName("id") val id: Int,
    @SerializedName("bulan") val bulan: Int,
    @SerializedName("tahun") val tahun: Int,
    @SerializedName("periode") val periode: String,
    @SerializedName("kegiatan") val kegiatan: String,
    @SerializedName("foto") val foto: String?,
    @SerializedName("created_at") val createdAt: String?
)

data class ExportUrlData(
    @SerializedName("url") val url: String,
    @SerializedName("filename") val filename: String,
    @SerializedName("periode") val periode: String?
)