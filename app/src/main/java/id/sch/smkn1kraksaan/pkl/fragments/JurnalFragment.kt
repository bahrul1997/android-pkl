package id.sch.smkn1kraksaan.pkl.fragments

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import com.google.android.material.tabs.TabLayoutMediator
import id.sch.smkn1kraksaan.pkl.adapters.JurnalPagerAdapter
import id.sch.smkn1kraksaan.pkl.databinding.FragmentJurnalBinding

class JurnalFragment : Fragment() {

    private var _binding: FragmentJurnalBinding? = null
    private val binding get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentJurnalBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // Setup ViewPager
        val pagerAdapter = JurnalPagerAdapter(requireActivity())
        binding.viewPager.adapter = pagerAdapter

        // Setup TabLayout
        TabLayoutMediator(binding.tabLayout, binding.viewPager) { tab, position ->
            tab.text = when (position) {
                0 -> "Harian"
                1 -> "Catatan Kegiatan"
                else -> ""
            }
        }.attach()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}