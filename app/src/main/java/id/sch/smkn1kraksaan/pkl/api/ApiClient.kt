package id.sch.smkn1kraksaan.pkl.api

import android.content.Context
import android.content.Intent
import id.sch.smkn1kraksaan.pkl.activities.LoginActivity
import id.sch.smkn1kraksaan.pkl.utils.SessionManager
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import com.google.gson.GsonBuilder
import java.util.concurrent.TimeUnit

object ApiClient {

    private var retrofit: Retrofit? = null
    private var sessionManager: SessionManager? = null
    private var appContext: Context? = null

    // ★ BASE URL LANGSUNG DI-HARDCODE ★
    private const val BASE_URL = "https://pkl.smkn1kraksan.sch.id/"

    fun init(context: Context) {
        sessionManager = SessionManager(context)
        appContext = context.applicationContext
    }

    fun getInstance(): Retrofit {
        if (retrofit == null) {
            // 1. Logging Interceptor (Selalu nampilin log)
            val logging = HttpLoggingInterceptor().apply {
                level = HttpLoggingInterceptor.Level.BODY
            }

            // 2. OkHttp Client
            val client = OkHttpClient.Builder()
                .addInterceptor(logging)
                .addInterceptor { chain ->
                    val request = chain.request()
                    val builder = request.newBuilder()

                    // Auto-attach token
                    val token = sessionManager?.getToken()
                    if (!token.isNullOrEmpty()) {
                        builder.header("Authorization", "Bearer $token")
                    }

                    builder.header("Accept", "application/json")
                    val response = chain.proceed(builder.build())

                    // Handle 401 (Auto Logout)
                    if (response.code == 401) {
                        handleUnauthorized()
                    }

                    response
                }
                .connectTimeout(30, TimeUnit.SECONDS)
                .readTimeout(30, TimeUnit.SECONDS)
                .writeTimeout(30, TimeUnit.SECONDS)
                .build()

            // 3. Gson Lenient (Biar gak error "JsonReader...")
            val gson = GsonBuilder()
                .setLenient()
                .create()

            // 4. Retrofit Builder
            retrofit = Retrofit.Builder()
                .baseUrl(BASE_URL)
                .client(client)
                .addConverterFactory(GsonConverterFactory.create(gson))
                .build()
        }

        return retrofit!!
    }

    private fun handleUnauthorized() {
        sessionManager?.logout()

        appContext?.let { ctx ->
            val intent = Intent(ctx, LoginActivity::class.java).apply {
                flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
            }
            ctx.startActivity(intent)
        }
    }

    fun getApiService(): ApiService = getInstance().create(ApiService::class.java)
}