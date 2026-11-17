package id.sch.smkn1kraksaan.pkl.activities

import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import id.sch.smkn1kraksaan.pkl.api.ApiClient
import id.sch.smkn1kraksaan.pkl.databinding.ActivityLoginBinding
import id.sch.smkn1kraksaan.pkl.models.LoginRequest
import id.sch.smkn1kraksaan.pkl.utils.SessionManager
import kotlinx.coroutines.launch
import id.sch.smkn1kraksaan.pkl.activities.GuruMainActivity
import id.sch.smkn1kraksaan.pkl.activities.MainActivity

class LoginActivity : AppCompatActivity() {

    private lateinit var binding: ActivityLoginBinding
    private lateinit var session: SessionManager

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityLoginBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // Init
        session = SessionManager(this)
        ApiClient.init(this)

        // Auto-login: kalau udah ada token, langsung ke dashboard
        if (session.isLoggedIn()) {
            goToDashboard()
            return
        }

        // Setup button login
        binding.btnLogin.setOnClickListener {
            val username = binding.etUsername.text.toString().trim()
            val password = binding.etPassword.text.toString().trim()

            if (username.isEmpty() || password.isEmpty()) {
                Toast.makeText(this, "Username dan password wajib diisi", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            doLogin(username, password)
        }
    }

    private fun doLogin(username: String, password: String) {
        setLoading(true)

        lifecycleScope.launch {
            try {
                val response = ApiClient.getApiService().login(
                    LoginRequest(username, password)
                )

                if (response.isSuccessful && response.body()?.status == true) {
                    val loginData = response.body()!!.data!!

                    // Simpan session
                    session.saveToken(loginData.token)
                    session.saveUser(
                        id = loginData.user.id,
                        username = loginData.user.username,
                        nama = loginData.user.nama,
                        role = loginData.user.role,
                        foto = loginData.user.foto,
                        nisn = loginData.user.nisn,
                        kelas = loginData.user.kelas,
                        jurusan = loginData.user.jurusan
                    )

                    Toast.makeText(
                        this@LoginActivity,
                        "Login berhasil. Selamat datang, ${loginData.user.nama}",
                        Toast.LENGTH_SHORT
                    ).show()

                    goToDashboard()

                } else {
                    val message = response.body()?.message ?: "Login gagal"
                    Toast.makeText(this@LoginActivity, message, Toast.LENGTH_LONG).show()
                }

            } catch (e: Exception) {
                Toast.makeText(
                    this@LoginActivity,
                    "Error: ${e.message}",
                    Toast.LENGTH_LONG
                ).show()
            } finally {
                setLoading(false)
            }
        }
    }

    private fun setLoading(isLoading: Boolean) {
        binding.progressBar.visibility = if (isLoading) View.VISIBLE else View.GONE
        binding.btnLogin.isEnabled = !isLoading
        binding.etUsername.isEnabled = !isLoading
        binding.etPassword.isEnabled = !isLoading
    }

    private fun goToDashboard() {
        val session = SessionManager(this)
        val role = session.getRole()

        if (role == "guru") {
            startActivity(Intent(this, GuruMainActivity::class.java))
        } else {
            startActivity(Intent(this, MainActivity::class.java))
        }
        finish()
    }
}