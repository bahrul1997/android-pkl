package id.sch.smkn1kraksaan.pkl.fragments

import android.Manifest
import android.app.DatePickerDialog
import android.app.Dialog
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AlertDialog
import androidx.core.content.ContextCompat
import androidx.core.view.isVisible
import androidx.lifecycle.lifecycleScope
import com.bumptech.glide.Glide
import com.google.android.material.bottomsheet.BottomSheetBehavior
import com.google.android.material.bottomsheet.BottomSheetDialog
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import id.sch.smkn1kraksaan.pkl.R
import id.sch.smkn1kraksaan.pkl.api.ApiClient
import id.sch.smkn1kraksaan.pkl.databinding.BottomSheetJurnalBinding
import id.sch.smkn1kraksaan.pkl.models.JurnalItem
import id.sch.smkn1kraksaan.pkl.utils.ImagePickerHelper
import id.sch.smkn1kraksaan.pkl.utils.MultipartUtils
import kotlinx.coroutines.launch
import java.io.File
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale

class JurnalBottomSheet : BottomSheetDialogFragment() {

    private var _binding: BottomSheetJurnalBinding? = null
    private val binding get() = _binding!!

    private val imagePicker = ImagePickerHelper()

    // Mode
    private var isEditMode = false
    private var editId: Int = 0

    // Data
    private var fileFoto: File? = null
    private var tanggalPilih: String = ""
    private var fotoUrlLama: String? = null

    // Callback
    private var onSuccess: (() -> Unit)? = null

    // ==========================================
    // PUBLIC METHODS
    // ==========================================
    fun setOnSuccessListener(listener: () -> Unit) {
        onSuccess = listener
    }

    fun setEditMode(item: JurnalItem) {
        isEditMode = true
        editId = item.id
        // Data akan di-set di onViewCreated
        bundleEdit = item
    }

    private var bundleEdit: JurnalItem? = null

    // ==========================================
    // LAUNCHER
    // ==========================================
    private val cameraLauncher = registerForActivityResult(
        ActivityResultContracts.StartActivityForResult()
    ) { result ->
        if (result.resultCode == android.app.Activity.RESULT_OK) {
            val file = imagePicker.getCapturedFile(requireContext())
            if (file != null) {
                fileFoto = file
                binding.ivPreviewFoto.setImageURI(Uri.fromFile(file))
                binding.layoutEmptyFoto.isVisible = false
                binding.btnHapusFoto.isVisible = true
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
                    fileFoto = file
                    binding.ivPreviewFoto.setImageURI(Uri.fromFile(file))
                    binding.layoutEmptyFoto.isVisible = false
                    binding.btnHapusFoto.isVisible = true
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
    // DIALOG
    // ==========================================
    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        val dialog = super.onCreateDialog(savedInstanceState) as BottomSheetDialog

        dialog.setOnShowListener { dialogInterface ->
            val bottomSheet = (dialogInterface as BottomSheetDialog)
                .findViewById<View>(com.google.android.material.R.id.design_bottom_sheet)

            bottomSheet?.let {
                val behavior = BottomSheetBehavior.from(it)
                behavior.state = BottomSheetBehavior.STATE_EXPANDED
                behavior.skipCollapsed = true
            }
        }

        return dialog
    }

    // ==========================================
    // LIFECYCLE
    // ==========================================
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = BottomSheetJurnalBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // Set tanggal default = hari ini
        tanggalPilih = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(Calendar.getInstance().time)
        binding.etTanggal.setText(tanggalPilih)

        // Kalau mode edit, populate data
        if (isEditMode && bundleEdit != null) {
            setupEditMode(bundleEdit!!)
        }

        setupButtons()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    // ==========================================
    // SETUP EDIT MODE
    // ==========================================
    private fun setupEditMode(item: JurnalItem) {
        // Ubah judul
        binding.tvJudulJurnal.text = "✏️ Edit Jurnal"
        binding.tvSubjudulJurnal.text = "Perbarui kegiatan PKL"
        binding.btnSimpan.text = "💾 Update Jurnal"

        // Isi data
        tanggalPilih = item.tanggal
        binding.etTanggal.setText(item.tanggal)
        binding.etKegiatan.setText(item.kegiatan)

        // Foto lama
        if (!item.foto.isNullOrEmpty()) {
            fotoUrlLama = item.foto
            binding.layoutEmptyFoto.isVisible = false
            binding.btnHapusFoto.isVisible = true

            Glide.with(this)
                .load(item.foto)
                .into(binding.ivPreviewFoto)
        }
    }

    // ==========================================
    // SETUP BUTTONS
    // ==========================================
    private fun setupButtons() {
        // Klik tanggal → date picker
        binding.etTanggal.setOnClickListener {
            showDatePicker()
        }

        // Ambil foto
        binding.btnAmbilFoto.setOnClickListener {
            showPhotoSourceDialog()
        }

        // Hapus foto
        binding.btnHapusFoto.setOnClickListener {
            fileFoto = null
            fotoUrlLama = null
            binding.ivPreviewFoto.setImageDrawable(null)
            binding.layoutEmptyFoto.isVisible = true
            binding.btnHapusFoto.isVisible = false
        }

        // Simpan
        binding.btnSimpan.setOnClickListener {
            submitJurnal()
        }
    }

    // ==========================================
    // DATE PICKER
    // ==========================================
    private fun showDatePicker() {
        val calendar = Calendar.getInstance()
        try {
            val sdf = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
            calendar.time = sdf.parse(tanggalPilih) ?: calendar.time
        } catch (e: Exception) {
            // Ignore
        }

        val picker = DatePickerDialog(
            requireContext(),
            { _, year, month, dayOfMonth ->
                val cal = Calendar.getInstance()
                cal.set(year, month, dayOfMonth)
                tanggalPilih = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(cal.time)
                binding.etTanggal.setText(tanggalPilih)
            },
            calendar.get(Calendar.YEAR),
            calendar.get(Calendar.MONTH),
            calendar.get(Calendar.DAY_OF_MONTH)
        )

        picker.show()
    }

    // ==========================================
    // PHOTO PICKER
    // ==========================================
    private fun showPhotoSourceDialog() {
        val options = arrayOf("📷 Kamera", "🖼️ Galeri")

        AlertDialog.Builder(requireContext())
            .setTitle("Ambil Foto Jurnal")
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

    // ==========================================
    // SUBMIT
    // ==========================================
    private fun submitJurnal() {
        val tanggal = binding.etTanggal.text?.toString()?.trim() ?: ""
        val kegiatan = binding.etKegiatan.text?.toString()?.trim() ?: ""

        if (tanggal.isEmpty()) {
            Toast.makeText(requireContext(), "Tanggal wajib diisi", Toast.LENGTH_SHORT).show()
            return
        }

        if (kegiatan.isEmpty()) {
            Toast.makeText(requireContext(), "Kegiatan wajib diisi", Toast.LENGTH_SHORT).show()
            return
        }

        setLoading(true)

        lifecycleScope.launch {
            try {
                val tanggalBody = MultipartUtils.stringToBody(tanggal)
                val kegiatanBody = MultipartUtils.stringToBody(kegiatan)
                val fotoPart = fileFoto?.let { MultipartUtils.fileToPart("foto", it) }

                val response = if (isEditMode) {
                    ApiClient.getApiService().updateJurnalHarian(
                        id = editId,
                        tanggal = tanggalBody,
                        kegiatan = kegiatanBody,
                        foto = fotoPart
                    )
                } else {
                    ApiClient.getApiService().simpanJurnalHarian(
                        tanggal = tanggalBody,
                        kegiatan = kegiatanBody,
                        foto = fotoPart
                    )
                }

                if (response.isSuccessful && response.body()?.status == true) {
                    Toast.makeText(
                        requireContext(),
                        "✅ ${response.body()?.message ?: "Berhasil"}",
                        Toast.LENGTH_LONG
                    ).show()

                    onSuccess?.invoke()
                    dismiss()
                } else {
                    Toast.makeText(
                        requireContext(),
                        "❌ ${response.body()?.message ?: "Gagal"}",
                        Toast.LENGTH_LONG
                    ).show()
                    setLoading(false)
                }

            } catch (e: Exception) {
                Toast.makeText(
                    requireContext(),
                    "❌ Error: ${e.message}",
                    Toast.LENGTH_LONG
                ).show()
                setLoading(false)
            }
        }
    }

    private fun setLoading(isLoading: Boolean) {
        binding.btnSimpan.isEnabled = !isLoading
        binding.btnSimpan.text = if (isLoading) {
            "⏳ Menyimpan..."
        } else {
            if (isEditMode) "💾 Update Jurnal" else "🚀 Simpan Jurnal"
        }

        binding.btnAmbilFoto.isEnabled = !isLoading
        binding.etTanggal.isEnabled = !isLoading
        binding.etKegiatan.isEnabled = !isLoading
        binding.progressBarBottomSheet.isVisible = isLoading
    }
}