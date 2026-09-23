package com.example.util

import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.Rect
import android.graphics.RectF
import android.graphics.pdf.PdfDocument
import android.net.Uri
import android.os.Build
import android.text.Layout
import android.text.StaticLayout
import android.text.TextPaint
import androidx.core.content.FileProvider
import com.example.data.entity.CourseEntity
import com.example.data.entity.NoteEntity
import java.io.File
import java.io.FileOutputStream
import java.io.OutputStream

object PdfExportHelper {

    private const val PAGE_WIDTH = 595 // A4 standard width in points (72 dpi)
    private const val PAGE_HEIGHT = 842 // A4 standard height in points (72 dpi)
    private const val MARGIN_LEFT = 40f
    private const val MARGIN_RIGHT = 555f
    private const val MARGIN_TOP = 40f
    private const val MARGIN_BOTTOM = 802f
    private const val CONTENT_WIDTH = (MARGIN_RIGHT - MARGIN_LEFT).toInt() // 515 points

    /**
     * Mengekspor catatan lengkap beserta metadata dan foto ke File PDF di direktori cache.
     */
    fun exportNoteToPdfFile(context: Context, note: NoteEntity, course: CourseEntity?): File {
        val exportDir = File(context.cacheDir, "exported_notes").apply {
            if (!exists()) mkdirs()
        }
        val safeTitle = note.title
            .replace(Regex("[^a-zA-Z0-9-_ ]"), "")
            .trim()
            .replace(" ", "_")
            .take(30)
            .ifBlank { "Catatan" }

        val fileName = "${safeTitle}_${System.currentTimeMillis()}.pdf"
        val pdfFile = File(exportDir, fileName)

        FileOutputStream(pdfFile).use { outputStream ->
            exportNoteToPdfStream(context, note, course, outputStream)
        }
        return pdfFile
    }

    /**
     * Menulis konten dokumen PDF ke OutputStream apapun (e.g. FileOutputStream atau SAF ContentResolver).
     */
    fun exportNoteToPdfStream(
        context: Context,
        note: NoteEntity,
        course: CourseEntity?,
        outputStream: OutputStream
    ) {
        val document = PdfDocument()
        var pageNumber = 1

        var currentPageInfo = PdfDocument.PageInfo.Builder(PAGE_WIDTH, PAGE_HEIGHT, pageNumber).create()
        var currentPage = document.startPage(currentPageInfo)
        var canvas = currentPage.canvas

        // Paint definitions
        val primaryPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            color = Color.rgb(14, 116, 144) // Deep Cyan / Teal
        }

        val textPrimaryPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            color = Color.rgb(15, 23, 42) // Dark Slate
            textSize = 10f
        }

        val textSecondaryPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            color = Color.rgb(71, 85, 105) // Muted Slate
            textSize = 9f
        }

        val borderPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            color = Color.rgb(203, 213, 225) // Light Slate Border
            style = Paint.Style.STROKE
            strokeWidth = 1f
        }

        val cardBgPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            color = Color.rgb(248, 250, 252) // Off-white / light slate background
            style = Paint.Style.FILL
        }

        val contentTextPaint = TextPaint(Paint.ANTI_ALIAS_FLAG).apply {
            color = Color.rgb(30, 41, 59)
            textSize = 10.5f
            isAntiAlias = true
        }

        // Helper to draw the header on any page
        fun drawHeader(c: Canvas, isFirstPage: Boolean) {
            // Top accent bar
            c.drawRect(0f, 0f, PAGE_WIDTH.toFloat(), 10f, primaryPaint)

            if (isFirstPage) {
                // Brand label
                val brandPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
                    color = Color.rgb(14, 116, 144)
                    textSize = 10f
                    isFakeBoldText = true
                }
                c.drawText("MAHASIGMA // ACADEMIC HUB", MARGIN_LEFT, MARGIN_TOP + 10f, brandPaint)

                // Title
                val headerTitlePaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
                    color = Color.rgb(15, 23, 42)
                    textSize = 16f
                    isFakeBoldText = true
                }
                c.drawText("CATATAN PERKULIAHAN & MATERI", MARGIN_LEFT, MARGIN_TOP + 28f, headerTitlePaint)

                // Thin divider
                val dividerPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
                    color = Color.rgb(226, 232, 240)
                    strokeWidth = 1.5f
                }
                c.drawLine(MARGIN_LEFT, MARGIN_TOP + 36f, MARGIN_RIGHT, MARGIN_TOP + 36f, dividerPaint)
            } else {
                val subHeaderPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
                    color = Color.rgb(100, 116, 139)
                    textSize = 8.5f
                }
                c.drawText("MahaSigma Academic Hub — ${note.title}", MARGIN_LEFT, MARGIN_TOP + 10f, subHeaderPaint)

                val dividerPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
                    color = Color.rgb(226, 232, 240)
                    strokeWidth = 1f
                }
                c.drawLine(MARGIN_LEFT, MARGIN_TOP + 16f, MARGIN_RIGHT, MARGIN_TOP + 16f, dividerPaint)
            }
        }

        // Helper to draw the footer on any page
        fun drawFooter(c: Canvas, pageNum: Int) {
            val dividerPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
                color = Color.rgb(226, 232, 240)
                strokeWidth = 1f
            }
            c.drawLine(MARGIN_LEFT, MARGIN_BOTTOM - 20f, MARGIN_RIGHT, MARGIN_BOTTOM - 20f, dividerPaint)

            val footerPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
                color = Color.rgb(148, 163, 184)
                textSize = 8f
            }
            c.drawText("Dokumen dibuat otomatis via MahaSigma Mobile", MARGIN_LEFT, MARGIN_BOTTOM - 8f, footerPaint)

            val pageStr = "Halaman $pageNum"
            val textWidth = footerPaint.measureText(pageStr)
            c.drawText(pageStr, MARGIN_RIGHT - textWidth, MARGIN_BOTTOM - 8f, footerPaint)
        }

        // Draw initial page header
        drawHeader(canvas, isFirstPage = true)

        var currentY = MARGIN_TOP + 50f

        // 1. Metadata Info Card (Mata Kuliah, Dosen, Judul, Tanggal & Waktu)
        val infoBoxTop = currentY
        val infoBoxHeight = 110f
        val infoRect = RectF(MARGIN_LEFT, infoBoxTop, MARGIN_RIGHT, infoBoxTop + infoBoxHeight)
        canvas.drawRoundRect(infoRect, 8f, 8f, cardBgPaint)
        canvas.drawRoundRect(infoRect, 8f, 8f, borderPaint)

        // Accent strip on left of card
        val stripPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            color = Color.rgb(14, 116, 144)
        }
        val stripRect = RectF(MARGIN_LEFT, infoBoxTop, MARGIN_LEFT + 5f, infoBoxTop + infoBoxHeight)
        canvas.drawRoundRect(stripRect, 3f, 3f, stripPaint)

        // Draw Metadata Fields
        var metaY = infoBoxTop + 20f
        val metaLabelX = MARGIN_LEFT + 16f
        val metaValX = MARGIN_LEFT + 130f

        val boldLabelPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            color = Color.rgb(51, 65, 85)
            textSize = 9.5f
            isFakeBoldText = true
        }

        // Judul Catatan
        canvas.drawText("Judul Catatan", metaLabelX, metaY, boldLabelPaint)
        canvas.drawText(": ${note.title}", metaValX, metaY, boldLabelPaint)
        metaY += 18f

        // Mata Kuliah & Kode
        canvas.drawText("Mata Kuliah", metaLabelX, metaY, textSecondaryPaint)
        val courseText = if (course != null) {
            if (course.code.isNotBlank()) "${course.name} (${course.code})" else course.name
        } else {
            "Catatan Umum (Tanpa Mata Kuliah)"
        }
        canvas.drawText(": $courseText", metaValX, metaY, textPrimaryPaint)
        metaY += 18f

        // Dosen Pengampu
        canvas.drawText("Dosen Pengampu", metaLabelX, metaY, textSecondaryPaint)
        val lecturerText = if (!course?.lecturer.isNullOrBlank()) course.lecturer else "-"
        canvas.drawText(": $lecturerText", metaValX, metaY, textPrimaryPaint)
        metaY += 18f

        // Tanggal & Waktu Catatan Dibuat
        canvas.drawText("Waktu Dibuat", metaLabelX, metaY, textSecondaryPaint)
        val createdText = DateUtils.formatDateTime(note.createdAt)
        canvas.drawText(": $createdText WIB", metaValX, metaY, textPrimaryPaint)
        metaY += 18f

        // Terakhir Diperbarui (jika berbeda)
        if (note.updatedAt > note.createdAt + 60_000L) {
            canvas.drawText("Terakhir Diubah", metaLabelX, metaY, textSecondaryPaint)
            val updatedText = DateUtils.formatDateTime(note.updatedAt)
            canvas.drawText(": $updatedText WIB", metaValX, metaY, textSecondaryPaint)
        }

        currentY = infoBoxTop + infoBoxHeight + 24f

        // 2. Section: Isi Materi / Catatan
        val sectionHeaderPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            color = Color.rgb(15, 23, 42)
            textSize = 12f
            isFakeBoldText = true
        }
        canvas.drawText("ISI MATERI / CATATAN", MARGIN_LEFT, currentY, sectionHeaderPaint)
        currentY += 14f

        val contentText = note.content.ifBlank { "(Catatan ini tidak memiliki teks materi tambahan)" }

        val staticLayout = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            StaticLayout.Builder.obtain(contentText, 0, contentText.length, contentTextPaint, CONTENT_WIDTH)
                .setLineSpacing(3f, 1.15f)
                .setAlignment(Layout.Alignment.ALIGN_NORMAL)
                .build()
        } else {
            @Suppress("DEPRECATION")
            StaticLayout(contentText, contentTextPaint, CONTENT_WIDTH, Layout.Alignment.ALIGN_NORMAL, 1.15f, 3f, false)
        }

        val totalContentHeight = staticLayout.height
        val maxContentYOnPage = MARGIN_BOTTOM - 30f

        if (currentY + totalContentHeight <= maxContentYOnPage) {
            // Fits entirely on current page
            canvas.save()
            canvas.translate(MARGIN_LEFT, currentY)
            staticLayout.draw(canvas)
            canvas.restore()
            currentY += totalContentHeight + 20f
        } else {
            // Need multi-page handling for long text
            // Draw lines line by line
            val lineCount = staticLayout.lineCount
            var lineStart = 0

            while (lineStart < lineCount) {
                val availableHeight = MARGIN_BOTTOM - 30f - currentY
                var linesThatFit = 0
                var accumulatedHeight = 0

                for (i in lineStart until lineCount) {
                    val lineHeight = staticLayout.getLineBottom(i) - staticLayout.getLineTop(i)
                    if (accumulatedHeight + lineHeight <= availableHeight) {
                        accumulatedHeight += lineHeight
                        linesThatFit++
                    } else {
                        break
                    }
                }

                if (linesThatFit == 0) {
                    // Start new page immediately
                    drawFooter(canvas, pageNumber)
                    document.finishPage(currentPage)
                    pageNumber++

                    currentPageInfo = PdfDocument.PageInfo.Builder(PAGE_WIDTH, PAGE_HEIGHT, pageNumber).create()
                    currentPage = document.startPage(currentPageInfo)
                    canvas = currentPage.canvas
                    drawHeader(canvas, isFirstPage = false)
                    currentY = MARGIN_TOP + 30f
                } else {
                    val startChar = staticLayout.getLineStart(lineStart)
                    val endLineIndex = lineStart + linesThatFit - 1
                    val endChar = staticLayout.getLineEnd(endLineIndex)
                    val textChunk = contentText.substring(startChar, endChar)

                    val chunkLayout = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                        StaticLayout.Builder.obtain(textChunk, 0, textChunk.length, contentTextPaint, CONTENT_WIDTH)
                            .setLineSpacing(3f, 1.15f)
                            .build()
                    } else {
                        @Suppress("DEPRECATION")
                        StaticLayout(textChunk, contentTextPaint, CONTENT_WIDTH, Layout.Alignment.ALIGN_NORMAL, 1.15f, 3f, false)
                    }

                    canvas.save()
                    canvas.translate(MARGIN_LEFT, currentY)
                    chunkLayout.draw(canvas)
                    canvas.restore()

                    currentY += chunkLayout.height + 20f
                    lineStart += linesThatFit

                    if (lineStart < lineCount) {
                        drawFooter(canvas, pageNumber)
                        document.finishPage(currentPage)
                        pageNumber++

                        currentPageInfo = PdfDocument.PageInfo.Builder(PAGE_WIDTH, PAGE_HEIGHT, pageNumber).create()
                        currentPage = document.startPage(currentPageInfo)
                        canvas = currentPage.canvas
                        drawHeader(canvas, isFirstPage = false)
                        currentY = MARGIN_TOP + 30f
                    }
                }
            }
        }

        // 3. Section: Foto Dokumentasi / Papan Tulis (Up to 5 photos)
        val imagePaths = note.getAllImages()
        if (imagePaths.isNotEmpty()) {
            val photoSectionNeededHeight = 40f
            if (currentY + photoSectionNeededHeight > MARGIN_BOTTOM - 80f) {
                drawFooter(canvas, pageNumber)
                document.finishPage(currentPage)
                pageNumber++

                currentPageInfo = PdfDocument.PageInfo.Builder(PAGE_WIDTH, PAGE_HEIGHT, pageNumber).create()
                currentPage = document.startPage(currentPageInfo)
                canvas = currentPage.canvas
                drawHeader(canvas, isFirstPage = false)
                currentY = MARGIN_TOP + 30f
            }

            canvas.drawText("DOKUMENTASI FOTO / PAPAN TULIS (${imagePaths.size} Foto)", MARGIN_LEFT, currentY, sectionHeaderPaint)
            currentY += 16f

            val captionPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
                color = Color.rgb(71, 85, 105)
                textSize = 9f
                isFakeBoldText = true
            }

            for ((index, path) in imagePaths.withIndex()) {
                val imgFile = File(path)
                if (!imgFile.exists()) continue

                val bitmap = decodeSampledBitmap(imgFile.absolutePath, 1200, 900) ?: continue

                // Compute scaled dimensions for PDF (max width: CONTENT_WIDTH, max height: 260pt)
                val maxPhotoWidth = CONTENT_WIDTH.toFloat()
                val maxPhotoHeight = 250f

                val widthRatio = maxPhotoWidth / bitmap.width.toFloat()
                val heightRatio = maxPhotoHeight / bitmap.height.toFloat()
                val scale = minOf(widthRatio, heightRatio, 1.0f)

                val drawWidth = bitmap.width * scale
                val drawHeight = bitmap.height * scale
                val totalItemHeight = drawHeight + 32f // photo + caption + spacing

                if (currentY + totalItemHeight > MARGIN_BOTTOM - 25f) {
                    drawFooter(canvas, pageNumber)
                    document.finishPage(currentPage)
                    pageNumber++

                    currentPageInfo = PdfDocument.PageInfo.Builder(PAGE_WIDTH, PAGE_HEIGHT, pageNumber).create()
                    currentPage = document.startPage(currentPageInfo)
                    canvas = currentPage.canvas
                    drawHeader(canvas, isFirstPage = false)
                    currentY = MARGIN_TOP + 30f
                }

                // Draw photo container background & border
                val photoRect = RectF(MARGIN_LEFT, currentY, MARGIN_LEFT + drawWidth, currentY + drawHeight)
                val photoBorderRect = RectF(photoRect.left - 1, photoRect.top - 1, photoRect.right + 1, photoRect.bottom + 1)
                canvas.drawRect(photoBorderRect, borderPaint)

                val srcRect = Rect(0, 0, bitmap.width, bitmap.height)
                val dstRect = Rect(
                    photoRect.left.toInt(),
                    photoRect.top.toInt(),
                    photoRect.right.toInt(),
                    photoRect.bottom.toInt()
                )
                canvas.drawBitmap(bitmap, srcRect, dstRect, Paint(Paint.FILTER_BITMAP_FLAG))

                // Caption
                val captionY = currentY + drawHeight + 14f
                canvas.drawText("Foto ${index + 1} dari ${imagePaths.size}: Dokumentasi Papan Tulis / Catatan", MARGIN_LEFT, captionY, captionPaint)

                currentY += totalItemHeight + 10f
                bitmap.recycle()
            }
        }

        // Draw footer on last page
        drawFooter(canvas, pageNumber)
        document.finishPage(currentPage)

        // Write document to stream
        document.writeTo(outputStream)
        document.close()
    }

    /**
     * Membagikan file PDF menggunakan Android ShareSheet (WhatsApp, Telegram, Google Drive, Email, dll.)
     */
    fun sharePdf(context: Context, pdfFile: File) {
        val uri: Uri = FileProvider.getUriForFile(
            context,
            "${context.packageName}.fileprovider",
            pdfFile
        )
        val shareIntent = Intent(Intent.ACTION_SEND).apply {
            type = "application/pdf"
            putExtra(Intent.EXTRA_STREAM, uri)
            putExtra(Intent.EXTRA_SUBJECT, pdfFile.nameWithoutExtension)
            addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
        }
        val chooser = Intent.createChooser(shareIntent, "Bagikan Catatan PDF via")
        chooser.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        context.startActivity(chooser)
    }

    /**
     * Membuka file PDF langsung di viewer PDF yang terpasang di HP (e.g. Google PDF Viewer, WPS, Drive)
     */
    fun openPdf(context: Context, pdfFile: File) {
        val uri: Uri = FileProvider.getUriForFile(
            context,
            "${context.packageName}.fileprovider",
            pdfFile
        )
        val viewIntent = Intent(Intent.ACTION_VIEW).apply {
            setDataAndType(uri, "application/pdf")
            addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
            addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        }
        val chooser = Intent.createChooser(viewIntent, "Buka Dokumen PDF dengan")
        chooser.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        context.startActivity(chooser)
    }

    private fun decodeSampledBitmap(filePath: String, reqWidth: Int, reqHeight: Int): Bitmap? {
        return try {
            val options = BitmapFactory.Options().apply { inJustDecodeBounds = true }
            BitmapFactory.decodeFile(filePath, options)

            var inSampleSize = 1
            if (options.outHeight > reqHeight || options.outWidth > reqWidth) {
                val halfHeight = options.outHeight / 2
                val halfWidth = options.outWidth / 2
                while ((halfHeight / inSampleSize) >= reqHeight && (halfWidth / inSampleSize) >= reqWidth) {
                    inSampleSize *= 2
                }
            }

            val decodeOptions = BitmapFactory.Options().apply {
                this.inSampleSize = inSampleSize
                inPreferredConfig = Bitmap.Config.RGB_565 // memory efficient
            }
            BitmapFactory.decodeFile(filePath, decodeOptions)
        } catch (e: Exception) {
            e.printStackTrace()
            null
        }
    }
}
