package id.sch.smkn1kraksaan.pkl.adapters

import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentActivity
import androidx.viewpager2.adapter.FragmentStateAdapter
import id.sch.smkn1kraksaan.pkl.fragments.ValidasiAbsensiFragment
import id.sch.smkn1kraksaan.pkl.fragments.ValidasiCatatanFragment
import id.sch.smkn1kraksaan.pkl.fragments.ValidasiJurnalFragment

class GuruValidasiPagerAdapter(activity: FragmentActivity) : FragmentStateAdapter(activity) {

    override fun getItemCount(): Int = 3

    override fun createFragment(position: Int): Fragment {
        return when (position) {
            0 -> ValidasiJurnalFragment()
            1 -> ValidasiCatatanFragment()
            2 -> ValidasiAbsensiFragment()
            else -> ValidasiJurnalFragment()
        }
    }
}