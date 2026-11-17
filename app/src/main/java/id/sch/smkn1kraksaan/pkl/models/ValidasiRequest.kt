package id.sch.smkn1kraksaan.pkl.models

import com.google.gson.annotations.SerializedName

data class ValidasiRequest(
    @SerializedName("id") val id: Int,
    @SerializedName("status") val status: String, // approved | rejected
    @SerializedName("catatan") val catatan: String? = null
)