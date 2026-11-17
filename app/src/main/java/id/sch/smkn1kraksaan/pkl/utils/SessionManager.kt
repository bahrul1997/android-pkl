package id.sch.smkn1kraksaan.pkl.utils

import android.content.Context
import android.content.SharedPreferences

class SessionManager(context: Context) {

    private val prefs: SharedPreferences = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE)

    fun saveToken(token: String) {
        prefs.edit().putString(KEY_TOKEN, token).apply()
    }

    fun getToken(): String? {
        return prefs.getString(KEY_TOKEN, null)
    }

    fun saveUser(id: Int, username: String, nama: String, role: String, foto: String?, nisn: String?, kelas: String?, jurusan: String?) {
        prefs.edit()
            .putInt(KEY_USER_ID, id)
            .putString(KEY_USERNAME, username)
            .putString(KEY_NAMA, nama)
            .putString(KEY_ROLE, role)
            .putString(KEY_FOTO, foto)
            .putString(KEY_NISN, nisn)
            .putString(KEY_KELAS, kelas)
            .putString(KEY_JURUSAN, jurusan)
            .apply()
    }

    fun getUserId(): Int = prefs.getInt(KEY_USER_ID, 0)
    fun getUsername(): String? = prefs.getString(KEY_USERNAME, null)
    fun getNama(): String? = prefs.getString(KEY_NAMA, null)
    fun getRole(): String? = prefs.getString(KEY_ROLE, null)
    fun getFoto(): String? = prefs.getString(KEY_FOTO, null)
    fun getNisn(): String? = prefs.getString(KEY_NISN, null)
    fun getKelas(): String? = prefs.getString(KEY_KELAS, null)
    fun getJurusan(): String? = prefs.getString(KEY_JURUSAN, null)

    fun isLoggedIn(): Boolean = !getToken().isNullOrEmpty()

    fun logout() {
        prefs.edit().clear().apply()
    }

    companion object {
        private const val PREF_NAME = "pkl_session"
        private const val KEY_TOKEN = "token"
        private const val KEY_USER_ID = "user_id"
        private const val KEY_USERNAME = "username"
        private const val KEY_NAMA = "nama"
        private const val KEY_ROLE = "role"
        private const val KEY_FOTO = "foto"
        private const val KEY_NISN = "nisn"
        private const val KEY_KELAS = "kelas"
        private const val KEY_JURUSAN = "jurusan"
    }

}