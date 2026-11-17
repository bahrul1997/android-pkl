package id.sch.smkn1kraksaan.pkl.activities

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import id.sch.smkn1kraksaan.pkl.R
import id.sch.smkn1kraksaan.pkl.api.ApiClient
import id.sch.smkn1kraksaan.pkl.databinding.ActivityGuruMainBinding
import id.sch.smkn1kraksaan.pkl.fragments.*

class GuruMainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityGuruMainBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityGuruMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        ApiClient.init(this)

        binding.bottomNavGuru.setOnItemSelectedListener { item ->
            val fragment: Fragment = when (item.itemId) {
                R.id.nav_guru_home -> GuruHomeFragment()
                R.id.nav_guru_validasi -> GuruValidasiFragment()
                R.id.nav_guru_profil -> GuruProfilFragment()
                else -> GuruHomeFragment()
            }
            loadFragment(fragment)
            true
        }

        if (savedInstanceState == null) {
            binding.bottomNavGuru.selectedItemId = R.id.nav_guru_home
        }
    }

    private fun loadFragment(fragment: Fragment) {
        supportFragmentManager.beginTransaction()
            .replace(R.id.fragmentContainerGuru, fragment)
            .commit()
    }
}