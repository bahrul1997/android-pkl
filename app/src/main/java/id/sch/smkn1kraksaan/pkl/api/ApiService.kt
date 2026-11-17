package id.sch.smkn1kraksaan.pkl.api

import id.sch.smkn1kraksaan.pkl.models.ApiResponse
import id.sch.smkn1kraksaan.pkl.models.DashboardData
import id.sch.smkn1kraksaan.pkl.models.LoginData
import id.sch.smkn1kraksaan.pkl.models.LoginRequest
import id.sch.smkn1kraksaan.pkl.models.User
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.Header
import retrofit2.http.Headers
import retrofit2.http.POST
import id.sch.smkn1kraksaan.pkl.models.*
import okhttp3.MultipartBody
import okhttp3.RequestBody
import retrofit2.http.*

interface ApiService {

    @POST("api/auth/login")
    @Headers("Content-Type: application/json")
    suspend fun login(@Body request: LoginRequest): Response<ApiResponse<LoginData>>

    @GET("api/auth/me")
    suspend fun me(): Response<ApiResponse<User>>

    @POST("api/auth/logout")
    suspend fun logout(): Response<ApiResponse<Any>>

    // ==========================================
    // DASHBOARD
    // ==========================================
    @GET("api/dashboard")
    suspend fun getDashboard(): Response<ApiResponse<DashboardData>>

    // ==========================================
    // ABSENSI
    // ==========================================
    @GET("api/absensi")
    suspend fun getAbsensi(
        @Query("bulan") bulan: Int? = null,
        @Query("tahun") tahun: Int? = null
    ): Response<ApiResponse<AbsensiListData>>

    @GET("api/absensi/hari-ini")
    suspend fun getAbsenHariIni(): Response<ApiResponse<AbsenHariIniData>>

    @Multipart
    @POST("api/absensi/simpan")
    suspend fun simpanAbsen(
        @Part("status") status: RequestBody,
        @Part("keterangan") keterangan: RequestBody?,
        @Part fotoSelfie: MultipartBody.Part,
        @Part fotoSurat: MultipartBody.Part? = null
    ): Response<okhttp3.ResponseBody> // ★ UBAH JADI ResponseBody ★

    @Multipart
    @POST("api/absensi/reupload/{id}")
    suspend fun reuploadAbsen(
        @Path("id") id: Int,
        @Part("keterangan") keterangan: RequestBody?,
        @Part fotoSelfie: MultipartBody.Part? = null,
        @Part fotoSurat: MultipartBody.Part? = null
    ): Response<okhttp3.ResponseBody> // ★ UBAH JADI ResponseBody ★

    // ==========================================
    // JURNAL HARIAN
    // ==========================================
    @GET("api/jurnal/harian")
    suspend fun getJurnalHarian(
        @Query("bulan") bulan: Int? = null,
        @Query("tahun") tahun: Int? = null
    ): Response<ApiResponse<JurnalHarianListData>>

    @GET("api/jurnal/harian/{id}")
    suspend fun getJurnalHarianDetail(
        @Path("id") id: Int
    ): Response<ApiResponse<JurnalItem>>

    @Multipart
    @POST("api/jurnal/harian/simpan")
    suspend fun simpanJurnalHarian(
        @Part("tanggal") tanggal: RequestBody,
        @Part("kegiatan") kegiatan: RequestBody,
        @Part foto: MultipartBody.Part? = null
    ): Response<ApiResponse<JurnalItem>>

    @Multipart
    @POST("api/jurnal/harian/{id}/update")
    suspend fun updateJurnalHarian(
        @Path("id") id: Int,
        @Part("tanggal") tanggal: RequestBody,
        @Part("kegiatan") kegiatan: RequestBody,
        @Part foto: MultipartBody.Part? = null
    ): Response<ApiResponse<JurnalItem>>

    @POST("api/jurnal/harian/{id}/hapus")
    suspend fun hapusJurnalHarian(
        @Path("id") id: Int
    ): Response<ApiResponse<Any>>

    // ==========================================
// JURNAL BULANAN
// ==========================================
    @GET("api/jurnal/bulanan")
    suspend fun getJurnalBulanan(): Response<ApiResponse<JurnalBulananListData>>

    @GET("api/jurnal/bulanan/{id}")
    suspend fun getJurnalBulananDetail(
        @Path("id") id: Int
    ): Response<ApiResponse<JurnalBulananItem>>

    // ==========================================
// EXPORT PDF
// ==========================================
    @GET("api/export/jurnal-harian")
    suspend fun getUrlExportHarian(
        @Query("bulan") bulan: Int,
        @Query("tahun") tahun: Int
    ): Response<ApiResponse<ExportUrlData>>

    @GET("api/export/jurnal-bulanan/{id}")
    suspend fun getUrlExportBulanan(
        @Path("id") id: Int
    ): Response<ApiResponse<ExportUrlData>>

    // ==========================================
// PROFIL
// ==========================================
    @GET("api/profil")
    suspend fun getProfil(): Response<ApiResponse<ProfilData>>

    @POST("api/profil/update")
    @Headers("Content-Type: application/json")
    suspend fun updateProfil(@Body request: UpdateProfilRequest): Response<ApiResponse<ProfilData>>

    @Multipart
    @POST("api/profil/foto")
    suspend fun uploadFotoProfil(
        @Part foto: MultipartBody.Part
    ): Response<ApiResponse<FotoResponse>>

    @POST("api/profil/password")
    @Headers("Content-Type: application/json")
    suspend fun gantiPassword(@Body request: GantiPasswordRequest): Response<ApiResponse<Any>>

    @GET("api/jurnal/catatan")
    suspend fun getCatatanKegiatan(): Response<ApiResponse<List<CatatanItem>>>

    @Multipart
    @POST("api/jurnal/catatan_simpan")
    suspend fun simpanCatatan(
        @Part("nama_pekerjaan") nama: RequestBody,
        @Part("perencanaan") perencanaan: RequestBody?,
        @Part("pelaksanaan") pelaksanaan: RequestBody?,
        @Part foto: MultipartBody.Part? = null
    ): Response<ApiResponse<Any>>

    @Multipart
    @POST("api/jurnal/catatan_update/{id}")
    suspend fun updateCatatan(
        @Path("id") id: Int,
        @Part("nama_pekerjaan") nama: RequestBody,
        @Part("perencanaan") perencanaan: RequestBody?,
        @Part("pelaksanaan") pelaksanaan: RequestBody?,
        @Part foto: MultipartBody.Part? = null
    ): Response<ApiResponse<Any>>


    @GET("api/export/catatan")
    suspend fun getUrlExportCatatan(): Response<ApiResponse<ExportUrlData>>

    @GET("api/export/absensi")
    suspend fun getUrlExportAbsensi(): Response<ApiResponse<ExportUrlData>>

    // ==========================================
    // API GURU
    // ==========================================
    @GET("api/guru/validasi")
    suspend fun getValidasiGuru(): Response<ApiResponse<GuruValidasiResponse>>
    // ==========================================
    // API GURU - VALIDASI
    // ==========================================
    @POST("api/guru/validasi-jurnal")
    @Headers("Content-Type: application/json")
    suspend fun validasiJurnal(@Body request: ValidasiRequest): Response<ApiResponse<Any>>

    @POST("api/guru/validasi-catatan")
    @Headers("Content-Type: application/json")
    suspend fun validasiCatatan(@Body request: ValidasiRequest): Response<ApiResponse<Any>>

    @POST("api/guru/validasi-absensi")
    @Headers("Content-Type: application/json")
    suspend fun validasiAbsensi(@Body request: ValidasiRequest): Response<ApiResponse<Any>>
    @GET("api/guru/dashboard")
    suspend fun getGuruDashboard(): Response<ApiResponse<GuruDashboardData>>

    @POST("api/profil/simpan_dudi_instruktur")
    @Headers("Content-Type: application/json")
    suspend fun simpanDudiInstruktur(@Body request: UpdateDudiRequest): Response<ApiResponse<Any>>
}