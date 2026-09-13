package com.example.util

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Canvas
import android.graphics.ColorMatrix
import android.graphics.ColorMatrixColorFilter
import android.graphics.Paint
import android.net.Uri
import java.io.File
import java.io.FileOutputStream
import java.io.InputStream
import java.util.UUID

object ImageHelper {

    /**
     * Mempertajam foto papan tulis menggunakan ColorMatrix:
     * Menurunkan saturasi dan meningkatkan kontras agar tulisan spidol/kapur lebih terbaca jelas.
     */
    fun enhanceBoardImage(originalBitmap: Bitmap): Bitmap {
        val enhancedBitmap = Bitmap.createBitmap(
            originalBitmap.width,
            originalBitmap.height,
            originalBitmap.config ?: Bitmap.Config.ARGB_8888
        )
        val canvas = Canvas(enhancedBitmap)
        val paint = Paint(Paint.ANTI_ALIAS_FLAG)

        // Saturasi turun, kontras naik
        val saturationMatrix = ColorMatrix().apply { setSaturation(0.35f) }
        val contrastMatrix = ColorMatrix(
            floatArrayOf(
                1.6f, 0f, 0f, 0f, -25f,
                0f, 1.6f, 0f, 0f, -25f,
                0f, 0f, 1.6f, 0f, -25f,
                0f, 0f, 0f, 1f, 0f
            )
        ).apply { preConcat(saturationMatrix) }

        paint.colorFilter = ColorMatrixColorFilter(contrastMatrix)
        canvas.drawBitmap(originalBitmap, 0f, 0f, paint)
        return enhancedBitmap
    }

    /**
     * Menyimpan Bitmap ke internal storage aplikasi (sandboxed & offline)
     * Mengembalikan absolute path file
     */
    fun saveBitmapToInternalStorage(context: Context, bitmap: Bitmap): String {
        val dir = File(context.filesDir, "board_notes").apply {
            if (!exists()) mkdirs()
        }
        val fileName = "note_${System.currentTimeMillis()}_${UUID.randomUUID().toString().take(6)}.jpg"
        val file = File(dir, fileName)

        FileOutputStream(file).use { out ->
            bitmap.compress(Bitmap.CompressFormat.JPEG, 90, out)
        }
        return file.absolutePath
    }

    /**
     * Membaca Bitmap dari Uri galeri/kamera dengan downsampling aman agar tidak OOM
     */
    fun loadBitmapFromUri(context: Context, uri: Uri, maxDimension: Int = 1600): Bitmap? {
        return try {
            // Read bounds
            val options = BitmapFactory.Options().apply { inJustDecodeBounds = true }
            context.contentResolver.openInputStream(uri)?.use { input ->
                BitmapFactory.decodeStream(input, null, options)
            }

            var inSampleSize = 1
            if (options.outHeight > maxDimension || options.outWidth > maxDimension) {
                val halfHeight = options.outHeight / 2
                val halfWidth = options.outWidth / 2
                while ((halfHeight / inSampleSize) >= maxDimension && (halfWidth / inSampleSize) >= maxDimension) {
                    inSampleSize *= 2
                }
            }

            val decodeOptions = BitmapFactory.Options().apply {
                this.inSampleSize = inSampleSize
            }

            context.contentResolver.openInputStream(uri)?.use { input ->
                BitmapFactory.decodeStream(input, null, decodeOptions)
            }
        } catch (e: Exception) {
            e.printStackTrace()
            null
        }
    }

    fun loadBitmapFromPath(path: String): Bitmap? {
        return try {
            val file = File(path)
            if (file.exists()) {
                BitmapFactory.decodeFile(file.absolutePath)
            } else {
                null
            }
        } catch (e: Exception) {
            e.printStackTrace()
            null
        }
    }

    fun deleteFile(path: String?): Boolean {
        if (path.isNullOrBlank()) return false
        return try {
            val file = File(path)
            if (file.exists()) file.delete() else false
        } catch (e: Exception) {
            false
        }
    }
}
