package id.sch.smkn1kraksaan.pkl.fragments

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import id.sch.smkn1kraksaan.pkl.adapters.CatatanAdapter
import id.sch.smkn1kraksaan.pkl.api.ApiClient
import id.sch.smkn1kraksaan.pkl.databinding.FragmentCatatanBinding
import id.sch.smkn1kraksaan.pkl.models.CatatanItem
import kotlinx.coroutines.launch
import android.content.Intent
import android.net.Uri
import id.sch.smkn1kraksaan.pkl.models.ExportUrlData

class CatatanFragment : Fragment() {

    private var _binding: FragmentCatatanBinding? = null
    private val binding get() = _binding!!

    private lateinit var adapter: CatatanAdapter

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentCatatanBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        setupRecyclerView()
        setupSwipeRefresh()
        setupFab()

        binding.btnCetakCatatan.setOnClickListener {
            cetakPdf()
        }

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

    // ==========================================
    // SETUP
    // ==========================================
    private fun setupRecyclerView() {
        adapter = CatatanAdapter(
            emptyList(),
            onEditClick = { item ->
                if (item.validasiStatus == "approved") {
                    Toast.makeText(requireContext(), "Catatan sudah disetujui, tidak bisa diedit.", Toast.LENGTH_SHORT).show()
                } else {
                    showEditBottomSheet(item)
                }
            }
        )

        binding.rvCatatan.apply {
            layoutManager = LinearLayoutManager(requireContext())
            adapter = this@CatatanFragment.adapter
        }
    }

    private fun cetakPdf() {
        Toast.makeText(requireContext(), "Menyiapkan PDF...", Toast.LENGTH_SHORT).show()

        lifecycleScope.launch {
            try {
                val response = ApiClient.getApiService().getUrlExportCatatan()

                if (response.isSuccessful && response.body()?.status == true) {
                    val urlPdf = response.body()?.data?.url

                    if (!urlPdf.isNullOrEmpty()) {
                        val browserIntent = Intent(Intent.ACTION_VIEW, Uri.parse(urlPdf))
                        startActivity(browserIntent)
                    } else {
                        Toast.makeText(requireContext(), "URL PDF kosong", Toast.LENGTH_SHORT).show()
                    }
                } else {
                    Toast.makeText(requireContext(), "Gagal minta URL", Toast.LENGTH_SHORT).show()
                }
            } catch (e: Exception) {
                Toast.makeText(requireContext(), "Error: ${e.message}", Toast.LENGTH_LONG).show()
            }
        }
    }

    private fun setupSwipeRefresh() {
        binding.swipeRefresh.setOnRefreshListener {
            loadData()
        }
    }

    private fun setupFab() {
        binding.fabTambahCatatan.setOnClickListener {
            showTambahBottomSheet()
        }

        // Cek status periode
        cekStatusPeriode()
    }

    private fun cekStatusPeriode() {
        lifecycleScope.launch {
            try {
                val response = ApiClient.getApiService().getDashboard()
                if (response.isSuccessful && response.body()?.status == true) {
                    val data = response.body()?.data
                    val canJurnal = data?.periode?.canJurnal ?: false

                    if (canJurnal) {
                        binding.fabTambahCatatan.visibility = View.VISIBLE
                    } else {
                        binding.fabTambahCatatan.visibility = View.GONE
                    }
                }
            } catch (e: Exception) {
                binding.fabTambahCatatan.visibility = View.GONE
            }
        }
    }

    // ==========================================
    // BOTTOM SHEET
    // ==========================================
    private fun showTambahBottomSheet() {
        val bottomSheet = CatatanBottomSheet()
        bottomSheet.setOnSuccessListener {
            loadData()
        }
        bottomSheet.show(childFragmentManager, "CatatanTambahBottomSheet")
    }

    private fun showEditBottomSheet(item: CatatanItem) {
        val bottomSheet = CatatanBottomSheet()
        bottomSheet.setEditMode(item)
        bottomSheet.setOnSuccessListener {
            loadData()
        }
        bottomSheet.show(childFragmentManager, "CatatanEditBottomSheet")
    }

    // ==========================================
    // LOAD DATA
    // ==========================================
    private fun loadData() {
        binding.progressBar.visibility = View.VISIBLE
        cekStatusPeriode() // ★ Cek periode tiap load data

        lifecycleScope.launch {
            try {
                val response = ApiClient.getApiService().getCatatanKegiatan()

                if (response.isSuccessful && response.body()?.status == true) {
                    val list = response.body()?.data ?: emptyList()
                    adapter.updateData(list)

                    if (list.isEmpty()) {
                        binding.tvEmpty.visibility = View.VISIBLE
                        binding.rvCatatan.visibility = View.GONE
                    } else {
                        binding.tvEmpty.visibility = View.GONE
                        binding.rvCatatan.visibility = View.VISIBLE
                    }
                } else {
                    Toast.makeText(requireContext(), "Gagal memuat data", Toast.LENGTH_SHORT).show()
                }
            } catch (e: Exception) {
                Toast.makeText(requireContext(), "Error: ${e.message}", Toast.LENGTH_SHORT).show()
            } finally {
                binding.progressBar.visibility = View.GONE
                binding.swipeRefresh.isRefreshing = false
            }
        }
    }
}