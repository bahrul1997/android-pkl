package id.sch.smkn1kraksaan.pkl.fragments

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.AdapterView
import android.widget.ArrayAdapter
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import id.sch.smkn1kraksaan.pkl.adapters.JurnalAdapter
import id.sch.smkn1kraksaan.pkl.api.ApiClient
import id.sch.smkn1kraksaan.pkl.databinding.FragmentJurnalHarianBinding
import id.sch.smkn1kraksaan.pkl.models.JurnalHarianListData
import id.sch.smkn1kraksaan.pkl.models.JurnalItem
import kotlinx.coroutines.launch
import java.util.Calendar
import android.content.Intent
import android.net.Uri
import id.sch.smkn1kraksaan.pkl.models.ExportUrlData

class JurnalHarianFragment : Fragment() {

    private var _binding: FragmentJurnalHarianBinding? = null
    private val binding get() = _binding!!

    private lateinit var jurnalAdapter: JurnalAdapter

    private var bulanTerpilih = Calendar.getInstance().get(Calendar.MONTH) + 1
    private var tahunTerpilih = Calendar.getInstance().get(Calendar.YEAR)

    // ==========================================
    // LIFECYCLE
    // ==========================================
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentJurnalHarianBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        setupRecyclerView()
        setupSpinner()
        setupSwipeRefresh()
        setupFab()

        binding.btnCetakPdf.setOnClickListener {
            cetakPdf()
        }

        loadJurnal()
    }

    override fun onResume() {
        super.onResume()
        loadJurnal()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    // ==========================================
    // SETUP
    // ==========================================
    private fun setupRecyclerView() {
        jurnalAdapter = JurnalAdapter(
            emptyList(),
            onEditClick = { item ->
                if (item.validasiStatus == "approved") {
                    Toast.makeText(requireContext(), "Jurnal sudah disetujui, tidak bisa diedit.", Toast.LENGTH_SHORT).show()
                } else {
                    showEditBottomSheet(item)
                }
            },
            onDeleteClick = { item ->
                if (item.validasiStatus == "approved") {
                    Toast.makeText(requireContext(), "Jurnal sudah disetujui, tidak bisa dihapus.", Toast.LENGTH_SHORT).show()
                } else {
                    showDeleteDialog(item)
                }
            }
        )

        binding.rvJurnal.apply {
            layoutManager = LinearLayoutManager(requireContext())
            adapter = jurnalAdapter
        }
    }
    private fun cetakPdf() {
        Toast.makeText(requireContext(), "Menyiapkan PDF...", Toast.LENGTH_SHORT).show()

        lifecycleScope.launch {
            try {
                // Panggil API buat dapetin URL PDF
                val response = ApiClient.getApiService().getUrlExportHarian(bulanTerpilih, tahunTerpilih)

                if (response.isSuccessful && response.body()?.status == true) {
                    val urlPdf = response.body()?.data?.url

                    if (!urlPdf.isNullOrEmpty()) {
                        // Buka URL di Browser
                        val browserIntent = Intent(Intent.ACTION_VIEW, Uri.parse(urlPdf))
                        startActivity(browserIntent)
                    } else {
                        Toast.makeText(requireContext(), "URL PDF tidak valid", Toast.LENGTH_SHORT).show()
                    }
                } else {
                    Toast.makeText(requireContext(), "Gagal meminta URL PDF", Toast.LENGTH_SHORT).show()
                }
            } catch (e: Exception) {
                Toast.makeText(requireContext(), "Error: ${e.message}", Toast.LENGTH_LONG).show()
            }
        }
    }

    private fun setupSpinner() {
        val bulanList = arrayOf(
            "Januari", "Februari", "Maret", "April", "Mei", "Juni",
            "Juli", "Agustus", "September", "Oktober", "November", "Desember"
        )
        val adapter = ArrayAdapter(
            requireContext(),
            android.R.layout.simple_spinner_dropdown_item,
            bulanList
        )
        binding.spinnerBulan.adapter = adapter
        binding.spinnerBulan.setSelection(bulanTerpilih - 1)

        binding.spinnerBulan.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(
                parent: AdapterView<*>?,
                view: View?,
                position: Int,
                id: Long
            ) {
                bulanTerpilih = position + 1
                loadJurnal()
            }

            override fun onNothingSelected(parent: AdapterView<*>?) {}
        }
    }

    private fun setupSwipeRefresh() {
        binding.swipeRefresh.setOnRefreshListener {
            loadJurnal()
        }
    }

    private fun setupFab() {
        binding.fabTambah.setOnClickListener {
            showTambahBottomSheet()
        }

        // Cek status periode sebelum nampilin FAB
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
                        binding.fabTambah.visibility = View.VISIBLE
                    } else {
                        binding.fabTambah.visibility = View.GONE
                    }
                }
            } catch (e: Exception) {
                // Kalau error (misal gak ada internet), default sembunyikan FAB
                binding.fabTambah.visibility = View.GONE
            }
        }
    }

    // ==========================================
    // BOTTOM SHEET
    // ==========================================
    private fun showTambahBottomSheet() {
        val bottomSheet = JurnalBottomSheet()
        bottomSheet.setOnSuccessListener {
            loadJurnal()
        }
        bottomSheet.show(childFragmentManager, "JurnalTambahBottomSheet")
    }

    private fun showEditBottomSheet(item: JurnalItem) {
        val bottomSheet = JurnalBottomSheet()
        bottomSheet.setEditMode(item)
        bottomSheet.setOnSuccessListener {
            loadJurnal()
        }
        bottomSheet.show(childFragmentManager, "JurnalEditBottomSheet")
    }

    // ==========================================
    // DELETE
    // ==========================================
    private fun showDeleteDialog(item: JurnalItem) {
        AlertDialog.Builder(requireContext())
            .setTitle("Hapus Jurnal?")
            .setMessage("Jurnal tanggal ${item.tanggalFormatted} akan dihapus permanen.")
            .setPositiveButton("Ya, Hapus") { _, _ ->
                deleteJurnal(item.id)
            }
            .setNegativeButton("Batal", null)
            .show()
    }

    private fun deleteJurnal(id: Int) {
        lifecycleScope.launch {
            try {
                val response = ApiClient.getApiService().hapusJurnalHarian(id)

                if (response.isSuccessful && response.body()?.status == true) {
                    Toast.makeText(
                        requireContext(),
                        "✅ ${response.body()?.message ?: "Jurnal dihapus"}",
                        Toast.LENGTH_SHORT
                    ).show()
                    loadJurnal()
                } else {
                    Toast.makeText(
                        requireContext(),
                        "❌ ${response.body()?.message ?: "Gagal menghapus"}",
                        Toast.LENGTH_SHORT
                    ).show()
                }
            } catch (e: Exception) {
                Toast.makeText(
                    requireContext(),
                    "❌ Error: ${e.message}",
                    Toast.LENGTH_SHORT
                ).show()
            }
        }
    }

    // ==========================================
    // LOAD DATA
    // ==========================================
    private fun loadJurnal() {
        binding.progressBar.visibility = View.VISIBLE
        cekStatusPeriode() // ★ Cek periode tiap kali load data

        lifecycleScope.launch {
            try {
                val response = ApiClient.getApiService()
                    .getJurnalHarian(bulanTerpilih, tahunTerpilih)

                if (response.isSuccessful && response.body()?.status == true) {
                    bindJurnal(response.body()!!.data)
                } else {
                    Toast.makeText(
                        requireContext(),
                        response.body()?.message ?: "Gagal memuat jurnal",
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

    private fun bindJurnal(data: JurnalHarianListData?) {
        if (data == null) return

        if (data.jurnal.isEmpty()) {
            binding.tvEmpty.visibility = View.VISIBLE
            binding.rvJurnal.visibility = View.GONE
        } else {
            binding.tvEmpty.visibility = View.GONE
            binding.rvJurnal.visibility = View.VISIBLE
            jurnalAdapter.updateData(data.jurnal)
        }
    }
}