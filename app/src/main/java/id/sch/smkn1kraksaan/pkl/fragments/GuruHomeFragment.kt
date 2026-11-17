package id.sch.smkn1kraksaan.pkl.fragments

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import id.sch.smkn1kraksaan.pkl.api.ApiClient
import id.sch.smkn1kraksaan.pkl.databinding.FragmentGuruHomeBinding
import kotlinx.coroutines.launch

class GuruHomeFragment : Fragment() {

    private var _binding: FragmentGuruHomeBinding? = null
    private val binding get() = _binding!!

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        _binding = FragmentGuruHomeBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        loadData()
    }

    private fun loadData() {
        lifecycleScope.launch {
            try {
                val response = ApiClient.getApiService().getGuruDashboard()
                if (response.isSuccessful && response.body()?.status == true) {
                    val data = response.body()?.data

                    binding.tvSambutan.text = "Selamat datang, ${data?.namaGuru}"
                    binding.tvTotalSiswa.text = data?.totalSiswa.toString()
                    binding.tvTotalDudi.text = data?.totalDudi.toString()
                    binding.tvTotalPending.text = "${data?.totalPending} Pending"
                }
            } catch (e: Exception) {
                Toast.makeText(requireContext(), "Error: ${e.message}", Toast.LENGTH_SHORT).show()
            }
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}