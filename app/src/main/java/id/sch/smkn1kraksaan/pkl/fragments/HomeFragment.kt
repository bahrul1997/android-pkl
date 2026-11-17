package id.sch.smkn1kraksaan.pkl.fragments

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.Observer
import com.bumptech.glide.Glide
import com.google.android.material.bottomnavigation.BottomNavigationView
import id.sch.smkn1kraksaan.pkl.R
import id.sch.smkn1kraksaan.pkl.activities.MainActivity
import id.sch.smkn1kraksaan.pkl.databinding.FragmentHomeBinding
import id.sch.smkn1kraksaan.pkl.models.DashboardData
import id.sch.smkn1kraksaan.pkl.viewmodels.HomeViewModel

class HomeFragment : Fragment() {

    private var _binding: FragmentHomeBinding? = null
    private val binding get() = _binding!!

    private val viewModel: HomeViewModel by viewModels()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentHomeBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        setupSwipeRefresh()
        setupQuickActions()
        observeViewModel()

        binding.btnEditDudi.setOnClickListener {
            val data = viewModel.dashboard.value
            if (data != null && data.penempatan != null) {
                val bottomSheet = EditDudiBottomSheet()
                bottomSheet.setData(
                    data.penempatan.namaPimpinan,
                    data.user.namaInstruktur,
                    data.user.jabatanInstruktur,
                    null // Nohp instruktur kalau mau ditambahin di User
                )
                bottomSheet.setOnSuccessListener {
                    viewModel.loadDashboard() // Refresh data
                }
                bottomSheet.show(childFragmentManager, "EditDudiBottomSheet")
            }
        }

        // Load data
        viewModel.loadDashboard()
    }

    private fun setupSwipeRefresh() {
        binding.swipeRefresh.setOnRefreshListener {
            viewModel.loadDashboard()
        }
    }



    private fun setupQuickActions() {
        binding.btnAksiAbsen.setOnClickListener {
            (activity as? MainActivity)?.let { main ->
                main.findViewById<BottomNavigationView>(R.id.bottomNav).selectedItemId = R.id.nav_absensi
            }
        }

        binding.btnAksiJurnal.setOnClickListener {
            (activity as? MainActivity)?.let { main ->
                main.findViewById<BottomNavigationView>(R.id.bottomNav).selectedItemId = R.id.nav_jurnal
            }
        }
    }

    private fun observeViewModel() {
        // Loading
        viewModel.loading.observe(viewLifecycleOwner, Observer { isLoading ->
            binding.progressBar.visibility = if (isLoading) View.VISIBLE else View.GONE
            binding.swipeRefresh.isRefreshing = isLoading
        })

        // Error
        viewModel.error.observe(viewLifecycleOwner, Observer { error ->
            if (!error.isNullOrEmpty()) {
                Toast.makeText(requireContext(), error, Toast.LENGTH_LONG).show()
            }
        })

        // Dashboard data
        viewModel.dashboard.observe(viewLifecycleOwner, Observer { data ->
            if (data != null) {
                bindData(data)
            }
        })
    }

    private fun bindData(data: DashboardData) {
        // ============ HEADER ============
        binding.tvNama.text = data.user.nama
        binding.tvNisnKelas.text = "${data.user.nisn} • ${data.user.kelas}"

        // Load foto
        Glide.with(this)
            .load(data.user.foto)
            .placeholder(R.mipmap.ic_launcher)
            .error(R.mipmap.ic_launcher)
            .into(binding.ivFoto)

        // ============ PENEMPATAN ============
        if (data.penempatan != null) {
            binding.cardPenempatan.visibility = View.VISIBLE
            binding.cardBelumDitempatkan.visibility = View.GONE

            binding.tvNamaDudi.text = data.penempatan.namaDudi
            binding.tvAlamatDudi.text = data.penempatan.alamatDudi ?: "-"
            binding.tvNamaGuru.text = data.penempatan.namaGuru
            binding.tvPeriode.text = "${formatTanggal(data.penempatan.tanggalMulai)} - ${formatTanggal(data.penempatan.tanggalSelesai)}"

            // ★ PIMPINAN DUDI ★
            binding.tvPimpinanDudi.text = if (!data.penempatan.namaPimpinan.isNullOrEmpty()) {
                data.penempatan.namaPimpinan
            } else {
                "-"
            }

            // ★ INSTRUKTUR LAPANGAN ★
            val namaInstruktur = data.user.namaInstruktur
            val jabatanInstruktur = data.user.jabatanInstruktur

            if (!namaInstruktur.isNullOrEmpty()) {
                if (!jabatanInstruktur.isNullOrEmpty()) {
                    binding.tvInstrukturLapangan.text = "$namaInstruktur ($jabatanInstruktur)"
                } else {
                    binding.tvInstrukturLapangan.text = namaInstruktur
                }
                // Set warna normal (hitam)
                binding.tvInstrukturLapangan.setTextColor(android.graphics.Color.parseColor("#1a1a1a"))
            } else {
                binding.tvInstrukturLapangan.text = "Belum diisi"
                // Set warna merah biar siswa sadar harus isi
                binding.tvInstrukturLapangan.setTextColor(android.graphics.Color.parseColor("#F44336"))
            }

            // Status periode
            data.periode?.let { periode ->
                binding.tvStatusPeriode.text = when (periode.status) {
                    "aktif" -> "Aktif"
                    "belum" -> "Belum Dimulai"
                    "selesai" -> "Selesai"
                    "batal" -> "Batal"
                    else -> "-"
                }
                binding.tvStatusPeriode.setBackgroundColor(
                    when (periode.status) {
                        "aktif" -> 0xFF4CAF50.toInt()
                        "belum" -> 0xFFFF9800.toInt()
                        "selesai" -> 0xFF2196F3.toInt()
                        "batal" -> 0xFFF44336.toInt()
                        else -> 0xFF888888.toInt()
                    }
                )
            }
        } else {
            binding.cardPenempatan.visibility = View.GONE
            binding.cardBelumDitempatkan.visibility = View.VISIBLE
        }

        // ============ STATISTIK ============
        data.statistik?.let { stat ->
            binding.tvStatJurnalHarian.text = (stat.jurnalHarian?.total ?: 0).toString()
            binding.tvStatHadir.text = (stat.absensi?.hadir ?: 0).toString()
            binding.tvStatIzin.text = (stat.absensi?.izin ?: 0).toString()
        }

        // ============ ABSEN HARI INI ============
        if (data.absenHariIni != null) {
            val a = data.absenHariIni
            val statusEmoji = when (a.status) {
                "hadir" -> "✅"
                "izin" -> "📝"
                "sakit" -> "🤒"
                "alpa" -> "❌"
                else -> "❓"
            }
            val jam = if (!a.jamAbsen.isNullOrEmpty() && a.jamAbsen.length >= 5) {
                a.jamAbsen.substring(0, 5)
            } else "-"

            val validasi = when (a.validasiStatus) {
                "approved" -> "✅ Disetujui"
                "rejected" -> "❌ Ditolak"
                else -> "⏳ Menunggu validasi"
            }

            binding.tvStatusAbsen.text = "$statusEmoji ${a.status.replaceFirstChar { it.uppercase() }} • Jam $jam\n$validasi"
        } else {
            val canAbsen = data.periode?.canAbsen ?: false
            binding.tvStatusAbsen.text = if (canAbsen) {
                "❌ Belum absen hari ini"
            } else {
                "⏳ " + (data.periode?.message ?: "Belum bisa absen")
            }
        }
    }

    private fun formatTanggal(tanggal: String?): String {
        if (tanggal.isNullOrEmpty()) return "-"
        return try {
            val parts = tanggal.split("-")
            if (parts.size == 3) {
                "${parts[2]}/${parts[1]}/${parts[0]}"
            } else tanggal
        } catch (e: Exception) {
            tanggal
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}