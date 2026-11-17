package id.sch.smkn1kraksaan.pkl.activities

import android.content.Intent
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import id.sch.smkn1kraksaan.pkl.api.ApiClient
import id.sch.smkn1kraksaan.pkl.utils.SessionManager
import kotlinx.coroutines.launch
import kotlinx.coroutines.withTimeoutOrNull

class SplashActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // Init ApiClient
        ApiClient.init(this)
        val session = SessionManager(this)

        // Tampilkan splash selama 1.5 detik
        Handler(Looper.getMainLooper()).postDelayed({
            if (session.isLoggedIn()) {
                // Ada token — validasi ke server dulu
                validateToken(session)
            } else {
                // Gak ada token — langsung Login
                goToLogin()
            }
        }, 1500)
    }

    /**
     * Validasi token ke server sebelum masuk MainActivity
     * Kalau token expired → auto-logout → Login
     */
    private fun validateToken(session: SessionManager) {
        lifecycleScope.launch {
            try {
                // Timeout 5 detik biar gak nunggu lama kalau internet lemot
                val response = withTimeoutOrNull(5000L) {
                    ApiClient.getApiService().me()
                }

                if (response == null) {
                    // Timeout — tetap ke MainActivity (biar user gak bingung)
                    goToMain()
                } else if (response.isSuccessful && response.body()?.status == true) {
                    // Token masih valid → MainActivity
                    goToMain()
                } else {
                    // Token expired / invalid → Login
                    session.logout()
                    goToLogin()
                }
            } catch (e: Exception) {
                // Error (misal no internet) — tetap ke MainActivity
                // (biar user bisa liat dashboard, nanti refresh sendiri)
                goToMain()
            }
        }
    }

    private fun goToMain() {
        startActivity(Intent(this, MainActivity::class.java))
        finish()
    }

    private fun goToLogin() {
        startActivity(Intent(this, LoginActivity::class.java))
        finish()
    }
}