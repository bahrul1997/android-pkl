package id.sch.smkn1kraksaan.pkl.adapters

import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentActivity
import androidx.viewpager2.adapter.FragmentStateAdapter
import id.sch.smkn1kraksaan.pkl.fragments.JurnalHarianFragment
import id.sch.smkn1kraksaan.pkl.fragments.CatatanFragment

class JurnalPagerAdapter(activity: FragmentActivity) : FragmentStateAdapter(activity) {

    override fun getItemCount(): Int = 2

    override fun createFragment(position: Int): Fragment {
        return when (position) {
            0 -> JurnalHarianFragment()
            1 -> CatatanFragment()
            else -> JurnalHarianFragment()
        }
    }
}