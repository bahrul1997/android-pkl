package id.sch.smkn1kraksaan.pkl.fragments

import android.os.Bundle
import android.text.InputType
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.EditText
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import id.sch.smkn1kraksaan.pkl.adapters.ValidasiAbsensiAdapter
import id.sch.smkn1kraksaan.pkl.api.ApiClient
import id.sch.smkn1kraksaan.pkl.databinding.FragmentValidasiAbsensiBinding
import id.sch.smkn1kraksaan.pkl.models.ValidasiRequest
import kotlinx.coroutines.launch

class ValidasiAbsensiFragment : Fragment() {

    private var _binding: FragmentValidasiAbsensiBinding? = null
    private val binding get() = _binding!!

    private lateinit var adapter: ValidasiAbsensiAdapter

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        _binding = FragmentValidasiAbsensiBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        setupRecyclerView()
        loadData()
    }

    private fun setupRecyclerView() {
        adapter = ValidasiAbsensiAdapter(
            emptyList(),
            onApprove = { item -> prosesValidasi(item.id, "approved", null) },
            onReject = { item -> showRejectDialog(item.id) }
        )

        binding.rvValidasiAbsensi.apply {
            layoutManager = LinearLayoutManager(requireContext())
            adapter = this@ValidasiAbsensiFragment.adapter
        }
    }

    private fun loadData() {
        binding.progressBar.visibility = View.VISIBLE

        lifecycleScope.launch {
            try {
                val response = ApiClient.getApiService().getValidasiGuru()

                if (response.isSuccessful && response.body()?.status == true) {
                    val data = response.body()?.data
                    val listAbsensi = data?.absensi ?: emptyList()

                    adapter.updateData(listAbsensi)

                    if (listAbsensi.isEmpty()) {
                        binding.tvEmpty.visibility = View.VISIBLE
                        binding.rvValidasiAbsensi.visibility = View.GONE
                    } else {
                        binding.tvEmpty.visibility = View.GONE
                        binding.rvValidasiAbsensi.visibility = View.VISIBLE
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
                val response = ApiClient.getApiService().validasiAbsensi(request)

                if (response.isSuccessful && response.body()?.status == true) {
                    Toast.makeText(requireContext(), "✅ Validasi berhasil", Toast.LENGTH_SHORT).show()
                    loadData()
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
            hint = "Contoh: Selfie blur..."
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