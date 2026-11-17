package id.sch.smkn1kraksaan.pkl.utils

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.provider.MediaStore
import androidx.core.content.FileProvider
import java.io.File

/**
 * Helper buat ambil gambar dari kamera / galeri
 *
 * Cara pakai:
 *
 * // 1. Di Fragment, deklarasikan:
 * private val imagePicker = ImagePickerHelper()
 *
 * // 2. Daftarkan launcher:
 * private val cameraLauncher = registerForActivityResult(
 *     ActivityResultContracts.StartActivityForResult()
 * ) { result ->
 *     if (result.resultCode == Activity.RESULT_OK) {
 *         val file = imagePicker.getCapturedFile(requireContext())
 *         // Pakai file...
 *     }
 * }
 *
 * // 3. Buka kamera:
 * cameraLauncher.launch(imagePicker.createCameraIntent(requireContext()))
 */
class ImagePickerHelper {

    private var currentPhotoFile: File? = null

    /**
     * Bikin intent buat buka kamera
     */
    fun createCameraIntent(context: Context): Intent {
        currentPhotoFile = FileUtils.createImageFile(context, "CAMERA")

        val uri: Uri = FileProvider.getUriForFile(
            context,
            "${context.packageName}.fileprovider",
            currentPhotoFile!!
        )

        return Intent(MediaStore.ACTION_IMAGE_CAPTURE).apply {
            putExtra(MediaStore.EXTRA_OUTPUT, uri)
            addFlags(Intent.FLAG_GRANT_WRITE_URI_PERMISSION)
        }
    }

    /**
     * Bikin intent buat buka galeri
     */
    fun createGalleryIntent(): Intent {
        return Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI).apply {
            type = "image/*"
        }
    }

    /**
     * Ambil file hasil capture dari kamera
     */
    fun getCapturedFile(context: Context): File? {
        return currentPhotoFile?.let {
            if (it.exists()) {
                FileUtils.compressImage(it)
            } else null
        }
    }

    /**
     * Ambil file dari galeri berdasarkan Uri
     */
    fun getPickedFile(context: Context, uri: Uri): File? {
        val file = FileUtils.getFileFromUri(context, uri)
        return file?.let { FileUtils.compressImage(it) }
    }
}