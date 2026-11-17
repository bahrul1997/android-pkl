package id.sch.smkn1kraksaan.pkl.fragments

import android.os.Bundle
import android.text.InputType
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.EditText
import android.widget.LinearLayout
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import id.sch.smkn1kraksaan.pkl.adapters.ValidasiJurnalAdapter
import id.sch.smkn1kraksaan.pkl.api.ApiClient
import id.sch.smkn1kraksaan.pkl.databinding.FragmentValidasiJurnalBinding
import id.sch.smkn1kraksaan.pkl.models.ValidasiJurnal
import id.sch.smkn1kraksaan.pkl.models.ValidasiRequest
import kotlinx.coroutines.launch

class ValidasiJurnalFragment : Fragment() {

    private var _binding: FragmentValidasiJurnalBinding? = null
    private val binding get() = _binding!!

    private lateinit var adapter: ValidasiJurnalAdapter

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        _binding = FragmentValidasiJurnalBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        setupRecyclerView()
        loadData()
    }

    private fun setupRecyclerView() {
        adapter = ValidasiJurnalAdapter(
            emptyList(),
            onApprove = { item -> prosesValidasi(item.id, "approved", null) },
            onReject = { item -> showRejectDialog(item.id) }
        )

        binding.rvValidasiJurnal.apply {
            layoutManager = LinearLayoutManager(requireContext())
            adapter = this@ValidasiJurnalFragment.adapter
        }
    }

    private fun loadData() {
        binding.progressBar.visibility = View.VISIBLE

        lifecycleScope.launch {
            try {
                val response = ApiClient.getApiService().getValidasiGuru()

                if (response.isSuccessful && response.body()?.status == true) {
                    val data = response.body()?.data
                    val listJurnal = data?.jurnal ?: emptyList()

                    adapter.updateData(listJurnal)

                    if (listJurnal.isEmpty()) {
                        binding.tvEmpty.visibility = View.VISIBLE
                        binding.rvValidasiJurnal.visibility = View.GONE
                    } else {
                        binding.tvEmpty.visibility = View.GONE
                        binding.rvValidasiJurnal.visibility = View.VISIBLE
                    }
                } else {
                    Toast.makeText(requireContext(), "Gagal memuat data", Toast.LENGTH_SHORT).show()
                }
            } catch (e: Exception) {
                Toast.makeText(requireContext(), "Error: ${e.message}", Toast.LENGTH_SHORT).show()
            } finally {
                binding.progressBar.visibility = View.GONE
            }
        }
    }

    private fun prosesValidasi(id: Int, status: String, catatan: String?) {
        lifecycleScope.launch {
            try {
                val request = ValidasiRequest(id, status, catatan)
                val response = ApiClient.getApiService().validasiJurnal(request)

                if (response.isSuccessful && response.body()?.status == true) {
                    Toast.makeText(requireContext(), "✅ Validasi berhasil", Toast.LENGTH_SHORT).show()
                    loadData() // Refresh list
                } else {
                    Toast.makeText(requireContext(), "❌ Gagal: ${response.body()?.message}", Toast.LENGTH_SHORT).show()
                }
            } catch (e: Exception) {
                Toast.makeText(requireContext(), "❌ Error: ${e.message}", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun showRejectDialog(id: Int) {
        val input = EditText(requireContext()).apply {
            hint = "Contoh: Kegiatan terlalu singkat..."
            setInputType(InputType.TYPE_CLASS_TEXT)
            setPadding(40, 30, 40, 30)
        }

        AlertDialog.Builder(requireContext())
            .setTitle("Alasan Penolakan")
            .setView(input)
            .setPositiveButton("Kirim") { _, _ ->
                val catatan = input.text.toString().trim()
                if (catatan.isNotEmpty()) {
                    prosesValidasi(id, "rejected", catatan)
                } else {
                    Toast.makeText(requireContext(), "Alasan wajib diisi", Toast.LENGTH_SHORT).show()
                }
            }
            .setNegativeButton("Batal", null)
            .show()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}