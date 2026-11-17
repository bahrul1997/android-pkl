package id.sch.smkn1kraksaan.pkl.models

import com.google.gson.annotations.SerializedName

data class GuruDashboardData(
    @SerializedName("nama_guru") val namaGuru: String,
    @SerializedName("total_siswa") val totalSiswa: Int,
    @SerializedName("total_dudi") val totalDudi: Int,
    @SerializedName("total_pending") val totalPending: Int
)