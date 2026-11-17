package id.sch.smkn1kraksaan.pkl.fragments

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.EditText
import android.widget.LinearLayout
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AlertDialog
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import com.bumptech.glide.Glide
import id.sch.smkn1kraksaan.pkl.R
import id.sch.smkn1kraksaan.pkl.activities.LoginActivity
import id.sch.smkn1kraksaan.pkl.api.ApiClient
import id.sch.smkn1kraksaan.pkl.databinding.FragmentProfilBinding
import id.sch.smkn1kraksaan.pkl.models.GantiPasswordRequest
import id.sch.smkn1kraksaan.pkl.models.ProfilData
import id.sch.smkn1kraksaan.pkl.utils.ImagePickerHelper
import id.sch.smkn1kraksaan.pkl.utils.MultipartUtils
import id.sch.smkn1kraksaan.pkl.utils.SessionManager
import kotlinx.coroutines.launch
import java.io.File

class ProfilFragment : Fragment() {

    private var _binding: FragmentProfilBinding? = null
    private val binding get() = _binding!!

    private lateinit var session: SessionManager
    private val imagePicker = ImagePickerHelper()

    private var currentProfil: ProfilData? = null

    // ==========================================
    // LAUNCHER
    // ==========================================
    private val cameraLauncher = registerForActivityResult(
        ActivityResultContracts.StartActivityForResult()
    ) { result ->
        if (result.resultCode == android.app.Activity.RESULT_OK) {
            val file = imagePicker.getCapturedFile(requireContext())
            file?.let { uploadFoto(it) }
        }
    }

    private val galleryLauncher = registerForActivityResult(
        ActivityResultContracts.StartActivityForResult()
    ) { result ->
        if (result.resultCode == android.app.Activity.RESULT_OK) {
            result.data?.data?.let { uri ->
                val file = imagePicker.getPickedFile(requireContext(), uri)
                file?.let { uploadFoto(it) }
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
        _binding = FragmentProfilBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        session = SessionManager(requireContext())

        setupSwipeRefresh()
        setupMenu()
        loadProfil()
    }

    override fun onResume() {
        super.onResume()
        loadProfil()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    // ==========================================
    // SETUP
    // ==========================================
    private fun setupSwipeRefresh() {
        binding.swipeRefresh.setOnRefreshListener {
            loadProfil()
        }
    }

    private fun setupMenu() {
        // Klik foto → ganti
        binding.ivFotoProfil.setOnClickListener { showPhotoSourceDialog() }
        binding.btnEditFoto.setOnClickListener { showPhotoSourceDialog() }

        // Edit profil
        binding.menuEditProfil.setOnClickListener {
            currentProfil?.let { p ->
                val sheet = EditProfilBottomSheet()
                sheet.setData(p.nama, p.username, p.noHp)
                sheet.show(childFragmentManager, "EditProfilBottomSheet")
            }
        }

        // Ganti password
        binding.menuGantiPassword.setOnClickListener {
            showGantiPasswordDialog()
        }

        // Logout
        binding.menuLogout.setOnClickListener {
            showLogoutDialog()
        }
    }

    // ==========================================
    // LOAD PROFIL
    // ==========================================
    private fun loadProfil() {
        binding.progressBar.visibility = View.VISIBLE

        lifecycleScope.launch {
            try {
                val response = ApiClient.getApiService().getProfil()

                if (response.isSuccessful && response.body()?.status == true) {
                    bindProfil(response.body()!!.data)
                } else {
                    Toast.makeText(
                        requireContext(),
                        response.body()?.message ?: "Gagal memuat profil",
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

    private fun bindProfil(p: ProfilData?) {
        if (p == null) return
        currentProfil = p

        // Header
        binding.tvNamaProfil.text = p.nama
        binding.tvNisnKelas.text = "${p.nisn ?: "-"} • ${p.kelas ?: "-"}"

        // Role badge
        binding.tvRoleBadge.text = p.role.replaceFirstChar { it.uppercase() }

        // Foto
        Glide.with(this)
            .load(p.foto)
            .placeholder(R.mipmap.ic_launcher)
            .error(R.mipmap.ic_launcher)
            .into(binding.ivFotoProfil)

        // Detail
        binding.tvDetailNisn.text = p.nisn ?: "-"
        binding.tvDetailKelas.text = p.kelas ?: "-"
        binding.tvDetailJurusan.text = p.jurusan ?: "-"
        binding.tvDetailNoHp.text = p.noHp ?: "-"
        binding.tvDetailUsername.text = p.username
    }

    // ==========================================
    // GANTI FOTO
    // ==========================================
    private fun showPhotoSourceDialog() {
        val options = arrayOf("📷 Kamera", "🖼️ Galeri")

        AlertDialog.Builder(requireContext())
            .setTitle("Ganti Foto Profil")
            .setItems(options) { _, which ->
                when (which) {
                    0 -> {
                        if (hasCameraPermission()) {
                            cameraLauncher.launch(imagePicker.createCameraIntent(requireContext()))
                        } else {
                            permissionLauncher.launch(Manifest.permission.CAMERA)
                        }
                    }
                    1 -> {
                        galleryLauncher.launch(imagePicker.createGalleryIntent())
                    }
                }
            }
            .show()
    }

    private fun hasCameraPermission(): Boolean {
        return ContextCompat.checkSelfPermission(
            requireContext(),
            Manifest.permission.CAMERA
        ) == PackageManager.PERMISSION_GRANTED
    }

    private fun uploadFoto(file: File) {
        Toast.makeText(requireContext(), "Mengupload foto...", Toast.LENGTH_SHORT).show()

        lifecycleScope.launch {
            try {
                val part = MultipartUtils.fileToPart("foto", file)
                val response = ApiClient.getApiService().uploadFotoProfil(part)

                if (response.isSuccessful && response.body()?.status == true) {
                    Toast.makeText(
                        requireContext(),
                        "✅ ${response.body()?.message ?: "Foto diperbarui"}",
                        Toast.LENGTH_LONG
                    ).show()

                    // Update session foto
                    response.body()?.data?.foto?.let {
                        session.saveUser(
                            id = session.getUserId(),
                            username = session.getUsername() ?: "",
                            nama = session.getNama() ?: "",
                            role = session.getRole() ?: "",
                            foto = it,
                            nisn = session.getNisn(),
                            kelas = session.getKelas(),
                            jurusan = session.getJurusan()
                        )
                    }

                    loadProfil()
                } else {
                    Toast.makeText(
                        requireContext(),
                        "❌ ${response.body()?.message ?: "Upload gagal"}",
                        Toast.LENGTH_LONG
                    ).show()
                }
            } catch (e: Exception) {
                Toast.makeText(
                    requireContext(),
                    "❌ Error: ${e.message}",
                    Toast.LENGTH_LONG
                ).show()
            }
        }
    }

    // ==========================================
    // GANTI PASSWORD
    // ==========================================
    private fun showGantiPasswordDialog() {
        val view = LinearLayout(requireContext()).apply {
            orientation = LinearLayout.VERTICAL
            setPadding(48, 32, 48, 16)
        }

        val etLama = EditText(requireContext()).apply {
            hint = "Password Lama"
            inputType = android.text.InputType.TYPE_CLASS_TEXT or android.text.InputType.TYPE_TEXT_VARIATION_PASSWORD
        }
        val etBaru = EditText(requireContext()).apply {
            hint = "Password Baru (min 6 karakter)"
            inputType = android.text.InputType.TYPE_CLASS_TEXT or android.text.InputType.TYPE_TEXT_VARIATION_PASSWORD
        }
        val etKonfirmasi = EditText(requireContext()).apply {
            hint = "Konfirmasi Password Baru"
            inputType = android.text.InputType.TYPE_CLASS_TEXT or android.text.InputType.TYPE_TEXT_VARIATION_PASSWORD
        }

        view.addView(etLama)
        view.addView(etBaru)
        view.addView(etKonfirmasi)

        AlertDialog.Builder(requireContext())
            .setTitle("🔑 Ganti Password")
            .setView(view)
            .setPositiveButton("Ganti") { _, _ ->
                val lama = etLama.text.toString().trim()
                val baru = etBaru.text.toString().trim()
                val konfirmasi = etKonfirmasi.text.toString().trim()

                if (lama.isEmpty() || baru.isEmpty() || konfirmasi.isEmpty()) {
                    Toast.makeText(requireContext(), "Semua field wajib diisi", Toast.LENGTH_SHORT).show()
                    return@setPositiveButton
                }

                if (baru.length < 6) {
                    Toast.makeText(requireContext(), "Password baru minimal 6 karakter", Toast.LENGTH_SHORT).show()
                    return@setPositiveButton
                }

                if (baru != konfirmasi) {
                    Toast.makeText(requireContext(), "Konfirmasi tidak cocok", Toast.LENGTH_SHORT).show()
                    return@setPositiveButton
                }

                doGantiPassword(lama, baru, konfirmasi)
            }
            .setNegativeButton("Batal", null)
            .show()
    }

    private fun doGantiPassword(lama: String, baru: String, konfirmasi: String) {
        lifecycleScope.launch {
            try {
                val response = ApiClient.getApiService().gantiPassword(
                    GantiPasswordRequest(lama, baru, konfirmasi)
                )

                if (response.isSuccessful && response.body()?.status == true) {
                    Toast.makeText(
                        requireContext(),
                        "✅ ${response.body()?.message ?: "Password diubah"}",
                        Toast.LENGTH_LONG
                    ).show()

                    // Logout setelah ganti password
                    session.logout()
                    startActivity(Intent(requireContext(), LoginActivity::class.java))
                    requireActivity().finish()
                } else {
                    Toast.makeText(
                        requireContext(),
                        "❌ ${response.body()?.message ?: "Gagal ganti password"}",
                        Toast.LENGTH_LONG
                    ).show()
                }
            } catch (e: Exception) {
                Toast.makeText(
                    requireContext(),
                    "❌ Error: ${e.message}",
                    Toast.LENGTH_LONG
                ).show()
            }
        }
    }

    // ==========================================
    // LOGOUT
    // ==========================================
    private fun showLogoutDialog() {
        AlertDialog.Builder(requireContext())
            .setTitle("Logout?")
            .setMessage("Anda yakin ingin keluar dari aplikasi?")
            .setPositiveButton("Ya, Logout") { _, _ ->
                doLogout()
            }
            .setNegativeButton("Batal", null)
            .show()
    }

    private fun doLogout() {
        lifecycleScope.launch {
            try {
                ApiClient.getApiService().logout()
            } catch (e: Exception) {
                // Ignore error
            } finally {
                session.logout()
                val intent = Intent(requireContext(), LoginActivity::class.java)
                intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
                startActivity(intent)
                requireActivity().finish()
            }
        }
    }
}