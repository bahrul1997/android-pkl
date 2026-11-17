package id.sch.smkn1kraksaan.pkl.activities

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import id.sch.smkn1kraksaan.pkl.R
import id.sch.smkn1kraksaan.pkl.api.ApiClient
import id.sch.smkn1kraksaan.pkl.databinding.ActivityMainBinding
import id.sch.smkn1kraksaan.pkl.fragments.AbsensiFragment
import id.sch.smkn1kraksaan.pkl.fragments.HomeFragment
import id.sch.smkn1kraksaan.pkl.fragments.JurnalFragment
import id.sch.smkn1kraksaan.pkl.fragments.ProfilFragment

class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)
        ApiClient.init(this)

        // Setup bottom navigation
        binding.bottomNav.setOnItemSelectedListener { item ->
            val fragment: Fragment = when (item.itemId) {
                R.id.nav_home -> HomeFragment()
                R.id.nav_jurnal -> JurnalFragment()
                R.id.nav_absensi -> AbsensiFragment()
                R.id.nav_profil -> ProfilFragment()
                else -> HomeFragment()
            }
            loadFragment(fragment)
            true
        }

        // Set default fragment = Home
        if (savedInstanceState == null) {
            binding.bottomNav.selectedItemId = R.id.nav_home
        }
    }

    private fun loadFragment(fragment: Fragment) {
        supportFragmentManager.beginTransaction()
            .replace(R.id.fragmentContainer, fragment)
            .commit()
    }
}