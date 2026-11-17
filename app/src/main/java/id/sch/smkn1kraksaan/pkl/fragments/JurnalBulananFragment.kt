package id.sch.smkn1kraksaan.pkl.fragments

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import id.sch.smkn1kraksaan.pkl.adapters.JurnalBulananAdapter
import id.sch.smkn1kraksaan.pkl.api.ApiClient
import id.sch.smkn1kraksaan.pkl.databinding.FragmentJurnalBulananBinding
import id.sch.smkn1kraksaan.pkl.models.JurnalBulananItem
import id.sch.smkn1kraksaan.pkl.models.JurnalBulananListData
import kotlinx.coroutines.launch

class JurnalBulananFragment : Fragment() {

    private var _binding: FragmentJurnalBulananBinding? = null
    private val binding get() = _binding!!

    private lateinit var adapter: JurnalBulananAdapter

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentJurnalBulananBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        setupRecyclerView()
        setupSwipeRefresh()
        loadData()
    }

    override fun onResume() {
        super.onResume()
        loadData()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    private fun setupRecyclerView() {
        adapter = JurnalBulananAdapter(
            emptyList(),
            onLihatClick = { item -> showDetailDialog(item) },
            onExportClick = { item -> exportPdf(item) }
        )

        binding.rvJurnalBulanan.apply {
            layoutManager = LinearLayoutManager(requireContext())
            adapter = this@JurnalBulananFragment.adapter
        }
    }

    private fun setupSwipeRefresh() {
        binding.swipeRefresh.setOnRefreshListener {
            loadData()
        }
    }

    private fun loadData() {
        binding.progressBar.visibility = View.VISIBLE

        lifecycleScope.launch {
            try {
                val response = ApiClient.getApiService().getJurnalBulanan()

                if (response.isSuccessful && response.body()?.status == true) {
                    bindData(response.body()!!.data)
                } else {
                    Toast.makeText(
                        requireContext(),
                        response.body()?.message ?: "Gagal memuat",
                        Toast.LENGTH_SHORT
                    ).show()
                }
            } catch (e: Exception) {
                Toast.makeText(
                    requireContext(),
                    "Error: ${e.message}",
                    Toast.LENGTH_SHORT
                ).show()
            } finally {
                binding.progressBar.visibility = View.GONE
                binding.swipeRefresh.isRefreshing = false
            }
        }
    }

    private fun bindData(data: JurnalBulananListData?) {
        if (data == null) return

        if (data.jurnal.isEmpty()) {
            binding.tvEmpty.visibility = View.VISIBLE
            binding.rvJurnalBulanan.visibility = View.GONE
        } else {
            binding.tvEmpty.visibility = View.GONE
            binding.rvJurnalBulanan.visibility = View.VISIBLE
            adapter.updateData(data.jurnal)
        }
    }

    // ==========================================
    // DETAIL DIALOG
    // ==========================================
    private fun showDetailDialog(item: JurnalBulananItem) {
        AlertDialog.Builder(requireContext())
            .setTitle("📅 ${item.periode}")
            .setMessage(item.kegiatan)
            .setPositiveButton("Tutup", null)
            .show()
    }

    // ==========================================
    // EXPORT PDF
    // ==========================================
    private fun exportPdf(item: JurnalBulananItem) {
        Toast.makeText(requireContext(), "Menyiapkan PDF...", Toast.LENGTH_SHORT).show()

        lifecycleScope.launch {
            try {
                val response = ApiClient.getApiService().getUrlExportBulanan(item.id)

                if (response.isSuccessful && response.body()?.status == true) {
                    val url = response.body()!!.data?.url
                    if (!url.isNullOrEmpty()) {
                        // Buka URL di browser
                        val intent = Intent(Intent.ACTION_VIEW, Uri.parse(url))
                        startActivity(intent)
                    } else {
                        Toast.makeText(requireContext(), "URL PDF tidak valid", Toast.LENGTH_SHORT).show()
                    }
                } else {
                    Toast.makeText(
                        requireContext(),
                        response.body()?.message ?: "Gagal generate PDF",
                        Toast.LENGTH_SHORT
                    ).show()
                }
            } catch (e: Exception) {
                Toast.makeText(
                    requireContext(),
                    "Error: ${e.message}",
                    Toast.LENGTH_SHORT
                ).show()
            }
        }
    }
}