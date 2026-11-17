package id.sch.smkn1kraksaan.pkl.fragments

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import com.google.android.material.tabs.TabLayoutMediator
import id.sch.smkn1kraksaan.pkl.adapters.GuruValidasiPagerAdapter
import id.sch.smkn1kraksaan.pkl.databinding.FragmentGuruValidasiBinding

class GuruValidasiFragment : Fragment() {

    private var _binding: FragmentGuruValidasiBinding? = null
    private val binding get() = _binding!!

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        _binding = FragmentGuruValidasiBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val pagerAdapter = GuruValidasiPagerAdapter(requireActivity())
        binding.viewPagerValidasi.adapter = pagerAdapter

        TabLayoutMediator(binding.tabLayoutValidasi, binding.viewPagerValidasi) { tab, position ->
            tab.text = when (position) {
                0 -> "Jurnal"
                1 -> "Catatan"
                2 -> "Absensi"
                else -> ""
            }
        }.attach()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}