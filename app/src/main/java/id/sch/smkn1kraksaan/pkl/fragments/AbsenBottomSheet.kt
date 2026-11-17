package id.sch.smkn1kraksaan.pkl.fragments

import android.Manifest
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
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import id.sch.smkn1kraksaan.pkl.R
import id.sch.smkn1kraksaan.pkl.api.ApiClient
import id.sch.smkn1kraksaan.pkl.databinding.BottomSheetAbsenBinding
import id.sch.smkn1kraksaan.pkl.utils.ImagePickerHelper
import id.sch.smkn1kraksaan.pkl.utils.MultipartUtils
import kotlinx.coroutines.launch
import java.io.File
import android.app.Dialog
import com.google.android.material.bottomsheet.BottomSheetBehavior
import com.google.android.material.bottomsheet.BottomSheetDialog

class AbsenBottomSheet : BottomSheetDialogFragment() {

    private var _binding: BottomSheetAbsenBinding? = null
    private val binding get() = _binding!!

    private val imagePicker = ImagePickerHelper()

    // ==========================================
    // STATE
    // ==========================================
    private var statusTerpilih = "hadir"
    private var fileSelfie: File? = null
    private var fileSurat: File? = null
    private var isTakingSurat = false

    // Mode reupload
    private var isReuploadMode = false
    private var reuploadId: Int = 0
    private var reuploadCatatan: String? = null

    // Callback
    private var onSuccess: (() -> Unit)? = null

    // ==========================================
    // PUBLIC METHOD
    // ==========================================
    fun setOnSuccessListener(listener: () -> Unit) {
        onSuccess = listener
    }

    fun setReuploadMode(id: Int, catatan: String?) {
        this.isReuploadMode = true
        this.reuploadId = id
        this.reuploadCatatan = catatan
    }

    // ==========================================
    // LAUNCHER: KAMERA
    // ==========================================
    private val cameraLauncher = registerForActivityResult(
        ActivityResultContracts.StartActivityForResult()
    ) { result ->
        if (result.resultCode == android.app.Activity.RESULT_OK) {
            val file = imagePicker.getCapturedFile(requireContext())
            if (file != null) {
                if (isTakingSurat) {
                    fileSurat = file
                    binding.ivPreviewSurat.setImageURI(Uri.fromFile(file))
                    binding.layoutEmptySurat.isVisible = false
                } else {
                    fileSelfie = file
                    binding.ivPreviewSelfie.setImageURI(Uri.fromFile(file))
                    binding.layoutEmptySelfie.isVisible = false
                }
            }
        }
    }

    // ==========================================
    // LAUNCHER: GALERI
    // ==========================================
    private val galleryLauncher = registerForActivityResult(
        ActivityResultContracts.StartActivityForResult()
    ) { result ->
        if (result.resultCode == android.app.Activity.RESULT_OK) {
            result.data?.data?.let { uri ->
                val file = imagePicker.getPickedFile(requireContext(), uri)
                if (file != null) {
                    if (isTakingSurat) {
                        fileSurat = file
                        binding.ivPreviewSurat.setImageURI(Uri.fromFile(file))
                        binding.layoutEmptySurat.isVisible = false
                    } else {
                        fileSelfie = file
                        binding.ivPreviewSelfie.setImageURI(Uri.fromFile(file))
                        binding.layoutEmptySelfie.isVisible = false
                    }
                }
            }
        }
    }

    // ==========================================
    // LAUNCHER: PERMISSION
    // ==========================================
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
        _binding = BottomSheetAbsenBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // Setup mode
        if (isReuploadMode) {
            setupReuploadMode()
        }

        setupStatusCards()
        setupButtons()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        val dialog = super.onCreateDialog(savedInstanceState) as BottomSheetDialog

        dialog.setOnShowListener { dialogInterface ->
            val bottomSheet = (dialogInterface as BottomSheetDialog)
                .findViewById<View>(com.google.android.material.R.id.design_bottom_sheet)

            bottomSheet?.let {
                val behavior = BottomSheetBehavior.from(it)
                behavior.state = BottomSheetBehavior.STATE_EXPANDED
                behavior.skipCollapsed = true
                behavior.isDraggable = false   // biar gak bisa di-drag nutup
            }
        }

        return dialog
    }

    // ==========================================
    // MODE SETUP
    // ==========================================
    private fun setupReuploadMode() {
        // Ubah judul & subjudul
        binding.root.findViewById<TextView>(R.id.tvJudulBottomSheet)?.text = "🔄 Upload Ulang Absen"
        binding.root.findViewById<TextView>(R.id.tvSubjudulBottomSheet)?.text =
            "Alasan ditolak: ${reuploadCatatan ?: "-"}"

        // Ubah tombol
        binding.btnKirimAbsen.text = "🔄 Kirim Ulang"

        // ★★★ SEMBUNYIKAN SELURUH SECTION STATUS ★★★
        binding.root.findViewById<View>(R.id.labelStatus)?.visibility = View.GONE
        binding.root.findViewById<View>(R.id.layoutStatusCards)?.visibility = View.GONE

        // ★★★ TAMPILKAN FORM KETERANGAN + SURAT ★★★
        binding.layoutIzinSakit.isVisible = true
    }

    // ==========================================
    // SETUP STATUS CARDS
    // ==========================================
    private fun setupStatusCards() {
        updateStatusUI()

        // Kalau bukan reupload mode, cards bisa diklik
        if (!isReuploadMode) {
            binding.cardStatusHadir.setOnClickListener {
                statusTerpilih = "hadir"
                updateStatusUI()
            }

            binding.cardStatusIzin.setOnClickListener {
                statusTerpilih = "izin"
                updateStatusUI()
            }

            binding.cardStatusSakit.setOnClickListener {
                statusTerpilih = "sakit"
                updateStatusUI()
            }
        }
    }

    private fun updateStatusUI() {
        // Reset semua card
        val cards = listOf(
            binding.cardStatusHadir to "#4CAF50",
            binding.cardStatusIzin to "#FF9800",
            binding.cardStatusSakit to "#2196F3"
        )

        cards.forEach { (card, _) ->
            when {
                statusTerpilih == "hadir" && card == binding.cardStatusHadir -> {
                    card.strokeWidth = 2
                    card.setStrokeColor(android.graphics.Color.parseColor("#4CAF50"))
                    card.setCardBackgroundColor(android.graphics.Color.parseColor("#E8F5E9"))
                }
                statusTerpilih == "izin" && card == binding.cardStatusIzin -> {
                    card.strokeWidth = 2
                    card.setStrokeColor(android.graphics.Color.parseColor("#FF9800"))
                    card.setCardBackgroundColor(android.graphics.Color.parseColor("#FFF3E0"))
                }
                statusTerpilih == "sakit" && card == binding.cardStatusSakit -> {
                    card.strokeWidth = 2
                    card.setStrokeColor(android.graphics.Color.parseColor("#2196F3"))
                    card.setCardBackgroundColor(android.graphics.Color.parseColor("#E3F2FD"))
                }
                else -> {
                    card.strokeWidth = 1
                    card.setStrokeColor(android.graphics.Color.parseColor("#DDDDDD"))
                    card.setCardBackgroundColor(android.graphics.Color.WHITE)
                }
            }
        }

        // Tampilkan form izin/sakit (kalau bukan reupload mode)
        if (!isReuploadMode) {
            binding.layoutIzinSakit.isVisible = statusTerpilih != "hadir"
        }
    }

    // ==========================================
    // SETUP BUTTONS
    // ==========================================
    private fun setupButtons() {
        binding.btnAmbilSelfie.setOnClickListener {
            isTakingSurat = false
            showPhotoSourceDialog()
        }

        binding.btnAmbilSurat.setOnClickListener {
            isTakingSurat = true
            showPhotoSourceDialog()
        }

        binding.btnKirimAbsen.setOnClickListener {
            submitAbsen()
        }
    }

    private fun showPhotoSourceDialog() {
        val options = arrayOf("📷 Kamera", "🖼️ Galeri")
        val title = if (isTakingSurat) "Ambil Foto Surat" else "Ambil Selfie"

        AlertDialog.Builder(requireContext())
            .setTitle(title)
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
    private fun submitAbsen() {
        // Validasi
        if (!isReuploadMode && fileSelfie == null) {
            Toast.makeText(requireContext(), "Selfie wajib diambil dulu", Toast.LENGTH_SHORT).show()
            return
        }

        val keterangan = binding.etKeterangan.text?.toString()?.trim() ?: ""

        // Validasi izin/sakit (hanya untuk mode absen baru)
        if (!isReuploadMode && statusTerpilih != "hadir") {
            if (keterangan.isEmpty()) {
                Toast.makeText(requireContext(), "Keterangan wajib diisi", Toast.LENGTH_SHORT).show()
                return
            }
            if (fileSurat == null) {
                Toast.makeText(requireContext(), "Foto surat wajib diupload", Toast.LENGTH_SHORT).show()
                return
            }
        }

        // Proses
        if (isReuploadMode) {
            doReupload(keterangan)
        } else {
            doUpload(keterangan)
        }
    }

    // ==========================================
    // ABSEN BARU
    // ==========================================
    private fun doUpload(keterangan: String) {
        setLoading(true)

        lifecycleScope.launch {
            try {
                val statusBody = MultipartUtils.stringToBody(statusTerpilih)
                val keteranganBody = if (keterangan.isNotEmpty()) {
                    MultipartUtils.stringToBody(keterangan)
                } else null

                val selfiePart = MultipartUtils.fileToPart("foto_selfie", fileSelfie!!)

                val suratPart = fileSurat?.let {
                    MultipartUtils.fileToPart("foto_surat", it)
                }

                val response = ApiClient.getApiService().simpanAbsen(
                    status = statusBody,
                    keterangan = keteranganBody,
                    fotoSelfie = selfiePart,
                    fotoSurat = suratPart
                )

                // ★ UBAH BAGIAN INI: CUKUP CEK isSuccessful ★
                if (response.isSuccessful) {
                    Toast.makeText(requireContext(), "✅ Absen berhasil dikirim", Toast.LENGTH_SHORT).show()
                    onSuccess?.invoke()
                    dismiss()
                } else {
                    Toast.makeText(requireContext(), "❌ Gagal: ${response.message()}", Toast.LENGTH_LONG).show()
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

    // ==========================================
    // RE-UPLOAD
    // ==========================================
    private fun doReupload(keterangan: String) {
        setLoading(true)

        lifecycleScope.launch {
            try {
                val keteranganBody = if (keterangan.isNotEmpty()) {
                    MultipartUtils.stringToBody(keterangan)
                } else null

                val selfiePart = fileSelfie?.let {
                    MultipartUtils.fileToPart("foto_selfie", it)
                }

                val suratPart = fileSurat?.let {
                    MultipartUtils.fileToPart("foto_surat", it)
                }

                val response = ApiClient.getApiService().reuploadAbsen(
                    id = reuploadId,
                    keterangan = keteranganBody,
                    fotoSelfie = selfiePart,
                    fotoSurat = suratPart
                )

                // ★ UBAH BAGIAN INI: CUKUP CEK isSuccessful ★
                if (response.isSuccessful) {
                    Toast.makeText(
                        requireContext(),
                        "✅ Re-upload berhasil",
                        Toast.LENGTH_LONG
                    ).show()

                    onSuccess?.invoke()
                    dismiss()
                } else {
                    Toast.makeText(
                        requireContext(),
                        "❌ Gagal: ${response.message()}",
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
        binding.btnKirimAbsen.isEnabled = !isLoading
        binding.btnKirimAbsen.text = if (isLoading) {
            "⏳ Mengirim..."
        } else {
            if (isReuploadMode) "🔄 Kirim Ulang" else "🚀 Kirim Absen"
        }

        binding.btnAmbilSelfie.isEnabled = !isLoading
        binding.btnAmbilSurat.isEnabled = !isLoading
        binding.progressBarBottomSheet.isVisible = isLoading

        binding.cardStatusHadir.isEnabled = !isLoading
        binding.cardStatusIzin.isEnabled = !isLoading
        binding.cardStatusSakit.isEnabled = !isLoading
    }
}