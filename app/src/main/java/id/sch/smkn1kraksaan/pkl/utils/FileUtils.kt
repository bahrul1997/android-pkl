package id.sch.smkn1kraksaan.pkl.utils

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Matrix
import android.net.Uri
import android.os.Environment
import androidx.exifinterface.media.ExifInterface
import java.io.File
import java.io.FileOutputStream
import java.io.InputStream

object FileUtils {

    /**
     * Bikin file temporary buat kamera
     */
    fun createImageFile(context: Context, prefix: String = "IMG"): File {
        val storageDir = context.getExternalFilesDir(Environment.DIRECTORY_PICTURES)
        return File.createTempFile(
            "${prefix}_${System.currentTimeMillis()}_",
            ".jpg",
            storageDir
        )
    }

    /**
     * Ambil file dari Uri (dari galeri)
     */
    fun getFileFromUri(context: Context, uri: Uri): File? {
        return try {
            val inputStream: InputStream? = context.contentResolver.openInputStream(uri)
            val file = createImageFile(context, "GALLERY")
            val outputStream = FileOutputStream(file)
            inputStream?.copyTo(outputStream)
            inputStream?.close()
            outputStream.close()
            file
        } catch (e: Exception) {
            e.printStackTrace()
            null
        }
    }

    /**
     * Kompres gambar biar max size tertentu (dalam KB)
     */
    fun compressImage(file: File, maxSizeKb: Int = 1024): File {
        try {
            // Decode dulu buat cek ukuran
            val options = BitmapFactory.Options().apply {
                inJustDecodeBounds = true
            }
            BitmapFactory.decodeFile(file.absolutePath, options)

            // Hitung skala
            val scale = calculateInSampleSize(options, 1280, 720)

            val decodeOptions = BitmapFactory.Options().apply {
                inSampleSize = scale
            }
            var bitmap = BitmapFactory.decodeFile(file.absolutePath, decodeOptions)
                ?: return file

            // Rotate kalau perlu (fix foto miring)
            bitmap = rotateImageIfRequired(bitmap, file)

            // Kompres
            var quality = 85
            var outputFile = File(file.parent, "compressed_${file.name}")
            var outputStream = FileOutputStream(outputFile)

            bitmap.compress(Bitmap.CompressFormat.JPEG, quality, outputStream)
            outputStream.flush()
            outputStream.close()

            // Loop turunin quality sampai < maxSizeKb
            while (outputFile.length() / 1024 > maxSizeKb && quality > 30) {
                quality -= 10
                outputFile = File(file.parent, "compressed_${file.name}")
                outputStream = FileOutputStream(outputFile)
                bitmap.compress(Bitmap.CompressFormat.JPEG, quality, outputStream)
                outputStream.flush()
                outputStream.close()
            }

            bitmap.recycle()

            // Hapus file asli
            if (file.exists()) file.delete()

            return outputFile
        } catch (e: Exception) {
            e.printStackTrace()
            return file
        }
    }

    /**
     * Hitung skala sampling
     */
    private fun calculateInSampleSize(
        options: BitmapFactory.Options,
        reqWidth: Int,
        reqHeight: Int
    ): Int {
        val height = options.outHeight
        val width = options.outWidth
        var inSampleSize = 1

        if (height > reqHeight || width > reqWidth) {
            val halfHeight = height / 2
            val halfWidth = width / 2
            while (halfHeight / inSampleSize >= reqHeight && halfWidth / inSampleSize >= reqWidth) {
                inSampleSize *= 2
            }
        }
        return inSampleSize
    }

    /**
     * Rotate bitmap sesuai EXIF orientation
     */
    private fun rotateImageIfRequired(bitmap: Bitmap, file: File): Bitmap {
        return try {
            val exif = ExifInterface(file.absolutePath)
            val orientation = exif.getAttributeInt(
                ExifInterface.TAG_ORIENTATION,
                ExifInterface.ORIENTATION_NORMAL
            )

            val matrix = Matrix()
            when (orientation) {
                ExifInterface.ORIENTATION_ROTATE_90 -> matrix.postRotate(90f)
                ExifInterface.ORIENTATION_ROTATE_180 -> matrix.postRotate(180f)
                ExifInterface.ORIENTATION_ROTATE_270 -> matrix.postRotate(270f)
                else -> return bitmap
            }

            val rotated = Bitmap.createBitmap(bitmap, 0, 0, bitmap.width, bitmap.height, matrix, true)
            if (rotated != bitmap) bitmap.recycle()
            rotated
        } catch (e: Exception) {
            bitmap
        }
    }
}