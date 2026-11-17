package id.sch.smkn1kraksaan.pkl.models

import com.google.gson.annotations.SerializedName

data class LoginData(
    @SerializedName("token") val token: String,
    @SerializedName("token_type") val tokenType: String,
    @SerializedName("expires_in") val expiresIn: Long,
    @SerializedName("user") val user: User
)