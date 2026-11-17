package id.sch.smkn1kraksaan.pkl.fragments

import android.Manifest
import android.content.pm.PackageManager
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.AdapterView
import android.widget.ArrayAdapter
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AlertDialog
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import com.bumptech.glide.Glide
import id.sch.smkn1kraksaan.pkl.R
import id.sch.smkn1kraksaan.pkl.adapters.AbsensiAdapter
import id.sch.smkn1kraksaan.pkl.api.ApiClient
import id.sch.smkn1kraksaan.pkl.databinding.FragmentAbsensiBinding
import id.sch.smkn1kraksaan.pkl.models.AbsenHariIniData
import id.sch.smkn1kraksaan.pkl.models.AbsenItem
import id.sch.smkn1kraksaan.pkl.models.AbsensiListData
import id.sch.smkn1kraksaan.pkl.utils.ImagePickerHelper
import kotlinx.coroutines.launch
import java.util.Calendar
import android.content.Intent
import android.net.Uri

class AbsensiFragment : Fragment() {

    private var _binding: FragmentAbsensiBinding? = null
    private val binding get() = _binding!!

    private lateinit var absensiAdapter: AbsensiAdapter
    private val imagePicker = ImagePickerHelper()

    private var bulanTerpilih = Calendar.getInstance().get(Calendar.MONTH) + 1
    private var tahunTerpilih = Calendar.getInstance().get(Calendar.YEAR)

    // ★★★ VARIABLE INI YANG PENTING ★★★
    private var currentAbsenHariIni: AbsenItem? = null

    // ==========================================
    // LAUNCHER: KAMERA (buat test di fragment)
    // ==========================================
    private val cameraLauncher = registerForActivityResult(
        ActivityResultContracts.StartActivityForResult()
    ) { result ->
        if (result.resultCode == android.app.Activity.RESULT_OK) {
            val file = imagePicker.getCapturedFile(requireContext())
            if (file != null) {
                Toast.makeText(
                    requireContext(),
                    "📷 Kamera: ${file.name}\nUkuran: ${file.length() / 1024} KB",
                    Toast.LENGTH_LONG
                ).show()
            }
        }
    }

    private val galleryLauncher = registerForActivityResult(
        ActivityResultContracts.StartActivityForResult()
    ) { result ->
        if (result.resultCode == android.app.Activity.RESULT_OK) {
            result.data?.data?.let { uri ->
                val file = imagePicker.getPickedFile(requireContext(), uri)
                if (file != null) {
                    Toast.makeText(
                        requireContext(),
                        "🖼️ Galeri: ${file.name}\nUkuran: ${file.length() / 1024} KB",
                        Toast.LENGTH_LONG
                    ).show()
                }
            }
        }
    }

    private val permissionLauncher = registerForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { granted ->
        if (granted) {
            cameraLauncher.launch(imagePicker.createCameraIntent(requireContext()))
        } else {
            Toast.makeText(requireContext(), "Izin kamera ditolak", Toast.LENGTH_SHORT).show()
        }
    }

    // ==========================================
    // LIFECYCLE
    // ==========================================
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentAbsensiBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        setupRecyclerView()
        setupSpinner()
        setupSwipeRefresh()
        setupButtons()

        binding.btnCetakAbsensi.setOnClickListener {
            cetakPdf()
        }

        loadAbsenHariIni()
        loadRiwayat()
    }


    override fun onResume() {
        super.onResume()
        loadAbsenHariIni()
        loadRiwayat()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    // ==========================================
    // SETUP
    // ==========================================
    private fun setupRecyclerView() {
        absensiAdapter = AbsensiAdapter(emptyList()) { item ->
            if (item.validasiStatus == "rejected") {
                Toast.makeText(
                    requireContext(),
                    "Absen ditolak: ${item.validasiCatatan ?: "Tanpa alasan"}",
                    Toast.LENGTH_LONG
                ).show()
            }
        }

        binding.rvAbsensi.apply {
            layoutManager = LinearLayoutManager(requireContext())
            adapter = absensiAdapter
        }
    }

    private fun cetakPdf() {
        Toast.makeText(requireContext(), "Menyiapkan PDF...", Toast.LENGTH_SHORT).show()

        lifecycleScope.launch {
            try {
                val response = ApiClient.getApiService().getUrlExportAbsensi()

                if (response.isSuccessful && response.body()?.status == true) {
                    val urlPdf = response.body()?.data?.url

                    if (!urlPdf.isNullOrEmpty()) {
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
                loadRiwayat()
            }

            override fun onNothingSelected(parent: AdapterView<*>?) {}
        }
    }

    private fun setupSwipeRefresh() {
        binding.swipeRefresh.setOnRefreshListener {
            loadAbsenHariIni()
            loadRiwayat()
        }
    }

    private fun setupButtons() {
        binding.btnAbsen.setOnClickListener {
            showAbsenBottomSheet()
        }

        binding.btnReupload.setOnClickListener {
            // ★★★ AMBIL DARI VARIABLE ★★★
            val absenHariIni = currentAbsenHariIni
            if (absenHariIni != null) {
                showReuploadBottomSheet(absenHariIni.id, absenHariIni.validasiCatatan)
            } else {
                Toast.makeText(
                    requireContext(),
                    "Data absen tidak ditemukan. Coba refresh dulu.",
                    Toast.LENGTH_SHORT
                ).show()
            }
        }
    }

    // ==========================================
    // BOTTOM SHEET
    // ==========================================
    private fun showAbsenBottomSheet() {
        val bottomSheet = AbsenBottomSheet()
        bottomSheet.setOnSuccessListener {
            loadAbsenHariIni()
            loadRiwayat()
        }
        bottomSheet.show(childFragmentManager, "AbsenBottomSheet")
    }

    private fun showReuploadBottomSheet(id: Int, catatan: String?) {
        val bottomSheet = AbsenBottomSheet()
        bottomSheet.setReuploadMode(id, catatan)
        bottomSheet.setOnSuccessListener {
            loadAbsenHariIni()
            loadRiwayat()
        }
        bottomSheet.show(childFragmentManager, "ReuploadBottomSheet")
    }

    // ==========================================
    // LOAD DATA
    // ==========================================
    private fun loadAbsenHariIni() {
        binding.progressBar.visibility = View.VISIBLE

        lifecycleScope.launch {
            try {
                val response = ApiClient.getApiService().getAbsenHariIni()

                if (response.isSuccessful && response.body()?.status == true) {
                    bindAbsenHariIni(response.body()!!.data)
                } else {
                    binding.tvInfoHariIni.text = response.body()?.message ?: "Gagal memuat"
                }
            } catch (e: Exception) {
                binding.tvInfoHariIni.text = "Error: ${e.message}"
            } finally {
                binding.progressBar.visibility = View.GONE
            }
        }
    }

    private fun bindAbsenHariIni(data: AbsenHariIniData?) {
        if (data == null) {
            binding.tvInfoHariIni.text = "Data tidak tersedia"
            currentAbsenHariIni = null
            return
        }

        if (data.sudahAbsen && data.absen != null) {
            // ★★★ SIMPAN DI SINI ★★★
            val a = data.absen
            currentAbsenHariIni = a

            val statusEmoji = when (a.status) {
                "hadir" -> "✅"
                "izin" -> "📝"
                "sakit" -> "🤒"
                else -> "❓"
            }
            val jam = if (!a.jamAbsen.isNullOrEmpty() && a.jamAbsen.length >= 5) {
                a.jamAbsen.substring(0, 5)
            } else "-"

            // Info utama
            binding.tvInfoHariIni.text =
                "$statusEmoji ${a.status.replaceFirstChar { it.uppercase() }} • Jam $jam\n" +
                        "Tanggal: ${data.tanggalFormatted}"

            binding.btnAbsen.visibility = View.GONE

            // ★★★ MUNCULIN TOMBOL REUPLOAD KALAU REJECTED ★★★
            if (a.validasiStatus == "rejected") {
                binding.btnReupload.visibility = View.VISIBLE
            } else {
                binding.btnReupload.visibility = View.GONE
            }

            // Foto selfie
            if (!a.fotoSelfie.isNullOrEmpty()) {
                binding.ivSelfieHariIni.visibility = View.VISIBLE
                Glide.with(this)
                    .load(a.fotoSelfie)
                    .into(binding.ivSelfieHariIni)
            } else {
                binding.ivSelfieHariIni.visibility = View.GONE
            }
        } else {
            // Belum absen
            currentAbsenHariIni = null

            if (data.canAbsen) {
                binding.tvInfoHariIni.text = "Belum absen hari ini"
                binding.btnAbsen.visibility = View.VISIBLE
            } else {
                binding.tvInfoHariIni.text = "PKL belum bisa absen (di luar periode)"
                binding.btnAbsen.visibility = View.GONE
            }
            binding.btnReupload.visibility = View.GONE
            binding.ivSelfieHariIni.visibility = View.GONE
        }
    }

    private fun loadRiwayat() {
        binding.progressBar.visibility = View.VISIBLE

        lifecycleScope.launch {
            try {
                val response = ApiClient.getApiService().getAbsensi(bulanTerpilih, tahunTerpilih)

                if (response.isSuccessful && response.body()?.status == true) {
                    bindRiwayat(response.body()!!.data)
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

    private fun bindRiwayat(data: AbsensiListData?) {
        if (data == null) return

        binding.tvStatHadir.text = data.statistik.hadir.toString()
        binding.tvStatIzin.text = data.statistik.izin.toString()
        binding.tvStatSakit.text = data.statistik.sakit.toString()
        binding.tvStatAlpa.text = data.statistik.alpa.toString()

        if (data.riwayat.isEmpty()) {
            binding.tvEmpty.visibility = View.VISIBLE
            binding.rvAbsensi.visibility = View.GONE
        } else {
            binding.tvEmpty.visibility = View.GONE
            binding.rvAbsensi.visibility = View.VISIBLE
            absensiAdapter.updateData(data.riwayat)
        }
    }
}