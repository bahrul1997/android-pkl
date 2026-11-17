package id.sch.smkn1kraksaan.pkl.utils

import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.MultipartBody
import okhttp3.RequestBody
import okhttp3.RequestBody.Companion.asRequestBody
import okhttp3.RequestBody.Companion.toRequestBody
import java.io.File

object MultipartUtils {

    /**
     * Convert File jadi MultipartBody.Part (buat upload)
     */
    fun fileToPart(fieldName: String, file: File): MultipartBody.Part {
        val requestFile = file.asRequestBody("image/jpeg".toMediaTypeOrNull())
        return MultipartBody.Part.createFormData(fieldName, file.name, requestFile)
    }

    /**
     * Convert String jadi RequestBody
     */
    fun stringToBody(value: String): RequestBody {
        return value.toRequestBody("text/plain".toMediaTypeOrNull())
    }

    /**
     * Convert String nullable jadi RequestBody (atau null)
     */
    fun nullableStringToBody(value: String?): RequestBody? {
        return value?.toRequestBody("text/plain".toMediaTypeOrNull())
    }
}