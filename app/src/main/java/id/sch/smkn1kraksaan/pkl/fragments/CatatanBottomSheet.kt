package id.sch.smkn1kraksaan.pkl.fragments

import android.Manifest
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AlertDialog
import androidx.core.content.ContextCompat
import androidx.core.view.isVisible
import androidx.lifecycle.lifecycleScope
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import id.sch.smkn1kraksaan.pkl.api.ApiClient
import id.sch.smkn1kraksaan.pkl.databinding.BottomSheetCatatanBinding
import id.sch.smkn1kraksaan.pkl.models.CatatanItem
import id.sch.smkn1kraksaan.pkl.utils.ImagePickerHelper
import id.sch.smkn1kraksaan.pkl.utils.MultipartUtils
import kotlinx.coroutines.launch
import java.io.File

class CatatanBottomSheet : BottomSheetDialogFragment() {

    private var _binding: BottomSheetCatatanBinding? = null
    private val binding get() = _binding!!

    private val imagePicker = ImagePickerHelper()
    private var fileFoto: File? = null
    private var isEditMode = false
    private var editId: Int = 0

    private var onSuccess: (() -> Unit)? = null

    fun setOnSuccessListener(listener: () -> Unit) {
        onSuccess = listener
    }

    fun setEditMode(item: CatatanItem) {
        isEditMode = true
        editId = item.id
        bundleEdit = item
    }

    private var bundleEdit: CatatanItem? = null

    private val cameraLauncher = registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
        if (result.resultCode == android.app.Activity.RESULT_OK) {
            fileFoto = imagePicker.getCapturedFile(requireContext())
            fileFoto?.let { binding.ivPreview.setImageURI(Uri.fromFile(it)); binding.layoutEmpty.isVisible = false }
        }
    }

    private val galleryLauncher = registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
        if (result.resultCode == android.app.Activity.RESULT_OK) {
            result.data?.data?.let { uri ->
                fileFoto = imagePicker.getPickedFile(requireContext(), uri)
                fileFoto?.let { binding.ivPreview.setImageURI(Uri.fromFile(it)); binding.layoutEmpty.isVisible = false }
            }
        }
    }

    private val permissionLauncher = registerForActivityResult(ActivityResultContracts.RequestPermission()) { granted ->
        if (granted) cameraLauncher.launch(imagePicker.createCameraIntent(requireContext()))
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        _binding = BottomSheetCatatanBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        if (isEditMode && bundleEdit != null) {
            val item = bundleEdit!!
            binding.tvJudul.text = "Edit Catatan Kegiatan"
            binding.etNamaPekerjaan.setText(item.namaPekerjaan)
            binding.etPerencanaan.setText(item.perencanaan)
            binding.etPelaksanaan.setText(item.pelaksanaan)
            if (!item.foto.isNullOrEmpty()) {
                binding.ivPreview.setImageURI(Uri.parse(item.foto))
                binding.layoutEmpty.isVisible = false
            }
        }

        binding.btnAmbilFoto.setOnClickListener { showPhotoSourceDialog() }
        binding.btnSimpan.setOnClickListener { submitData() }
    }

    private fun showPhotoSourceDialog() {
        val options = arrayOf("📷 Kamera", "🖼️ Galeri")
        AlertDialog.Builder(requireContext())
            .setTitle("Ambil Foto Kegiatan")
            .setItems(options) { _, which ->
                when (which) {
                    0 -> {
                        if (ContextCompat.checkSelfPermission(requireContext(), Manifest.permission.CAMERA) == PackageManager.PERMISSION_GRANTED) {
                            cameraLauncher.launch(imagePicker.createCameraIntent(requireContext()))
                        } else {
                            permissionLauncher.launch(Manifest.permission.CAMERA)
                        }
                    }
                    1 -> galleryLauncher.launch(imagePicker.createGalleryIntent())
                }
            }.show()
    }

    private fun submitData() {
        val nama = binding.etNamaPekerjaan.text.toString().trim()
        val perencanaan = binding.etPerencanaan.text.toString().trim()
        val pelaksanaan = binding.etPelaksanaan.text.toString().trim()

        if (nama.isEmpty()) {
            Toast.makeText(requireContext(), "Nama Pekerjaan wajib diisi", Toast.LENGTH_SHORT).show()
            return
        }

        setLoading(true)

        lifecycleScope.launch {
            try {
                val namaBody = MultipartUtils.stringToBody(nama)
                val perencanaanBody = if (perencanaan.isNotEmpty()) MultipartUtils.stringToBody(perencanaan) else null
                val pelaksanaanBody = if (pelaksanaan.isNotEmpty()) MultipartUtils.stringToBody(pelaksanaan) else null
                val fotoPart = fileFoto?.let { MultipartUtils.fileToPart("foto", it) }

                val response = if (isEditMode) {
                    ApiClient.getApiService().updateCatatan(editId, namaBody, perencanaanBody, pelaksanaanBody, fotoPart)
                } else {
                    ApiClient.getApiService().simpanCatatan(namaBody, perencanaanBody, pelaksanaanBody, fotoPart)
                }

                if (response.isSuccessful && response.body()?.status == true) {
                    Toast.makeText(requireContext(), "✅ ${response.body()?.message}", Toast.LENGTH_SHORT).show()
                    onSuccess?.invoke()
                    dismiss()
                } else {
                    Toast.makeText(requireContext(), "❌ ${response.body()?.message}", Toast.LENGTH_LONG).show()
                    setLoading(false)
                }
            } catch (e: Exception) {
                Toast.makeText(requireContext(), "❌ Error: ${e.message}", Toast.LENGTH_LONG).show()
                setLoading(false)
            }
        }
    }

    private fun setLoading(isLoading: Boolean) {
        binding.btnSimpan.isEnabled = !isLoading
        binding.btnSimpan.text = if (isLoading) "⏳ Menyimpan..." else "🚀 Simpan Catatan"
        binding.progressBar.isVisible = isLoading
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}