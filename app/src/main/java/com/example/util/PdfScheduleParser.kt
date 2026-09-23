package com.example.util

import android.content.Context
import android.net.Uri
import com.tom_roush.pdfbox.android.PDFBoxResourceLoader
import com.tom_roush.pdfbox.pdmodel.PDDocument
import com.tom_roush.pdfbox.text.PDFTextStripper
import com.tom_roush.pdfbox.text.TextPosition
import java.io.InputStream
import java.util.Locale
import java.util.UUID

/**
 * Model data representasi satu jadwal kuliah hasil parsing dari PDF KRS atau tabel.
 * Sesuai kebutuhan:
 * 1. courseName  -> Nama Matakuliah
 * 2. lecturer    -> Dosen Pengampu
 * 3. room        -> Keterangan (Teori / Praktik)
 * 4. dayOfWeek   -> Hari (1=Senin .. 5=Jumat)
 * 5. startTime & endTime -> Waktu
 */
data class ParsedScheduleItem(
    val tempId: String = UUID.randomUUID().toString(),
    var courseName: String,
    var courseCode: String = "",
    var lecturer: String = "",
    var dayOfWeek: Int = 1, // 1=Senin .. 5=Jumat
    var startTime: String = "08:00",
    var endTime: String = "09:40",
    var room: String = "", // Berisi Keterangan (Teori / Praktik)
    var colorHex: String = "#38BDF8",
    var isSelected: Boolean = true
)

/**
 * Representasi satu kata pada PDF beserta koordinat titik (X, Y) dan bounding box-nya.
 */
data class PdfWordChunk(
    val text: String,
    val minX: Float,
    val minY: Float,
    val maxX: Float,
    val maxY: Float,
    val page: Int
)

/**
 * Custom PDFTextStripper yang meng-intercept setiap karakter melalui [processTextPosition]
 * dan mengelompokkan karakter-karakter tersebut menjadi kata-kata (words) terpisah
 * dengan koordinat X dan Y yang presisi.
 */
class CoordinatePdfTextStripper : PDFTextStripper() {
    val words = mutableListOf<PdfWordChunk>()
    private val currentWord = StringBuilder()
    private val wordGlyphs = mutableListOf<TextPosition>()

    init {
        sortByPosition = true
    }

    override fun processTextPosition(text: TextPosition) {
        val ch = text.unicode
        if (ch.isBlank()) {
            flushWord()
        } else {
            if (wordGlyphs.isNotEmpty()) {
                val prev = wordGlyphs.last()
                val gap = text.xDirAdj - (prev.xDirAdj + prev.widthDirAdj)
                val yDiff = Math.abs(text.yDirAdj - prev.yDirAdj)
                // Jika terdapat spasi horizontal > 3.0pt atau baris baru (yDiff > 2.5pt), selesaikan kata
                if (gap > 3.0f || yDiff > 2.5f) {
                    flushWord()
                }
            }
            currentWord.append(ch)
            wordGlyphs.add(text)
        }
        super.processTextPosition(text)
    }

    override fun writeLineSeparator() {
        flushWord()
        super.writeLineSeparator()
    }

    override fun endDocument(document: PDDocument) {
        flushWord()
        super.endDocument(document)
    }

    private fun flushWord() {
        if (currentWord.isNotEmpty() && wordGlyphs.isNotEmpty()) {
            val str = currentWord.toString().trim()
            if (str.isNotEmpty()) {
                val minX = wordGlyphs.minOf { it.xDirAdj }
                val minY = wordGlyphs.minOf { it.yDirAdj }
                val maxX = wordGlyphs.maxOf { it.xDirAdj + it.widthDirAdj }
                val maxY = wordGlyphs.maxOf { it.yDirAdj + it.heightDir }

                words.add(
                    PdfWordChunk(
                        text = str,
                        minX = minX,
                        minY = minY,
                        maxX = maxX,
                        maxY = maxY,
                        page = currentPageNo
                    )
                )
            }
            currentWord.clear()
            wordGlyphs.clear()
        }
    }
}

/**
 * Parser KRS & Jadwal Kuliah.
 * Merekonstruksi tabel sesuai susunan asli tabel PDF SIAKAD UNY:
 * - Matakuliah
 * - Pengampu
 * - Keterangan (Teori/Praktik)
 * - Hari (Senin - Jumat)
 * - Waktu (Jam Mulai - Jam Selesai)
 */
object PdfScheduleParser {

    private val colorPalette = listOf(
        "#38BDF8", // Sky Blue
        "#34D399", // Emerald
        "#FBBF24", // Amber
        "#F87171", // Coral Red
        "#A78BFA", // Violet
        "#FB923C", // Orange
        "#EC4899", // Pink
        "#2DD4BF"  // Teal
    )

    private val DAY_MAP = mapOf(
        "senin" to 1, "sen" to 1, "mon" to 1, "monday" to 1,
        "selasa" to 2, "sel" to 2, "tue" to 2, "tuesday" to 2,
        "rabu" to 3, "rab" to 3, "wed" to 3, "wednesday" to 3,
        "kamis" to 4, "kam" to 4, "thu" to 4, "thursday" to 4,
        "jumat" to 5, "jum'at" to 5, "jum" to 5, "fri" to 5, "friday" to 5
    )

    private val TIME_RANGE_REGEX = Regex(
        """(\d{1,2})[:.](\d{2})(?:[:.]\d{2})?\s*(?:-|–|—|s/d|sampai|to)\s*(\d{1,2})[:.](\d{2})(?:[:.]\d{2})?""",
        RegexOption.IGNORE_CASE
    )

    private val DAY_REGEX = Regex(
        """\b(senin|selasa|rabu|kamis|jum['a]?at)\b""",
        RegexOption.IGNORE_CASE
    )

    private val ROW_START_REGEX = Regex(
        """^(?:(\d{1,2})\s+)?([A-Z0-9]{5,10})(?:\s+(.*))?$"""
    )

    private val ACADEMIC_TITLE_REGEX = Regex(
        """\b(?:(?:Prof|Dr|Dra|Drs|Ir|H|Hj)\.|\b(?:[SMB]\.[A-Za-z]{1,4}|Ph\.D|A\.Md|Sp\.[A-Za-z]{1,3})\.?)\b""",
        RegexOption.IGNORE_CASE
    )

    private const val TSV_HEADER_PREFIX = "No\tKode\tMatakuliah\tPengampu\tKeterangan\tHari\tWaktu"

    /**
     * Ekstraksi teks dari berkas PDF.
     * Menggunakan PDFTextStripper standar untuk mengekstrak urutan teks linier dokumen KRS.
     */
    fun extractTextFromPdf(context: Context, uri: Uri): Result<String> {
        return try {
            PDFBoxResourceLoader.init(context.applicationContext)
            context.contentResolver.openInputStream(uri)?.use { inputStream ->
                val document = PDDocument.load(inputStream)
                val stripper = PDFTextStripper()
                val rawText = stripper.getText(document)
                document.close()
                Result.success(rawText)
            } ?: Result.failure(Exception("Tidak dapat membuka file PDF"))
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    /**
     * Mem-parsing teks jadwal menjadi daftar [ParsedScheduleItem].
     */
    fun parseScheduleText(text: String): List<ParsedScheduleItem> {
        if (text.isBlank()) return emptyList()

        val normalizedText = text
            .replace(Regex("""(\d{1,2}[:.]\d{2}(?:[:.]\d{2})?)\s*\n\s*(-|–|—)\s*\n\s*(\d{1,2}[:.]\d{2}(?:[:.]\d{2})?)"""), "$1 - $3")
            .replace(Regex("""(\d{1,2}[:.]\d{2}(?:[:.]\d{2})?)\s*\n\s*(-|–|—)\s*(\d{1,2}[:.]\d{2}(?:[:.]\d{2})?)"""), "$1 - $3")
            .replace(Regex("""(\d{1,2}[:.]\d{2}(?:[:.]\d{2})?)\s*(-|–|—)\s*\n\s*(\d{1,2}[:.]\d{2}(?:[:.]\d{2})?)"""), "$1 - $3")

        // Strategi 1: TSV Rekonstruksi Tabel Koordinat
        if (normalizedText.contains("\t") && (normalizedText.startsWith("No\t") || normalizedText.lines().any { it.matches(Regex("""^\d{1,2}\t.*""")) })) {
            val tsvResults = parseTsvTableFormat(normalizedText)
            if (tsvResults.isNotEmpty()) {
                return tsvResults
            }
        }

        // Strategi 2: Format blok linear SIAKAD UNY
        val blockResults = parseSiakadTableFormat(normalizedText)
        if (blockResults.isNotEmpty()) {
            return blockResults
        }

        // Strategi 3: Format tabel umum
        val tableRows = TableScheduleParser.parseDelimitedText(normalizedText)
        if (tableRows.size >= 2) {
            val tableResults = TableScheduleParser.parseTableGrid(tableRows)
            if (tableResults.isNotEmpty()) {
                return tableResults
            }
        }

        // Strategi 4: Fallback baris-per-baris
        return parseGenericFormat(normalizedText)
    }

    // =========================================================================
    // REKONSTRUKSI TABEL DARI KUMPULAN KATA BERBASIS KOORDINAT
    // =========================================================================

    private data class ColRange(
        val name: String,
        val minX: Float,
        val maxX: Float
    )

    private data class RowRange(
        val num: Int,
        val minY: Float,
        val maxY: Float
    )

    private fun reconstructTableFromWords(words: List<PdfWordChunk>): String {
        if (words.isEmpty()) return ""

        val pageGroups = words.groupBy { it.page }
        val outputLines = mutableListOf<String>()
        outputLines.add(TSV_HEADER_PREFIX)

        for ((_, pageWords) in pageGroups) {
            // 1. Temukan Baris Header
            val headerColRanges = detectTableColumns(pageWords) ?: continue

            // 2. Temukan Baris Data (Nomor 1..N di kolom No)
            val rowRanges = detectTableRows(pageWords, headerColRanges)
            if (rowRanges.isEmpty()) continue

            // 3. Ekstrak data 5 kolom wajib: Matakuliah, Pengampu, Keterangan, Hari, Waktu (serta Kode)
            for (row in rowRanges) {
                val rowWords = pageWords.filter { it.minY >= row.minY && it.minY < row.maxY }

                fun getColumnText(colName: String): String {
                    val range = headerColRanges[colName] ?: return ""
                    val matchingWords = rowWords.filter { word ->
                        val wordCenter = (word.minX + word.maxX) / 2f
                        wordCenter >= range.minX && wordCenter < range.maxX
                    }.sortedWith { a, b ->
                        val yDiff = a.minY - b.minY
                        if (Math.abs(yDiff) > 3.0f) yDiff.compareTo(0f) else a.minX.compareTo(b.minX)
                    }
                    return matchingWords.joinToString(" ") { it.text }
                        .replace(Regex("""\s+"""), " ")
                        .trim()
                }

                val courseCode = getColumnText("Kode")
                var courseName = getColumnText("Matakuliah")
                val lecturer = getColumnText("Pengampu")
                val keterangan = getColumnText("Keterangan")
                val hari = getColumnText("Hari")
                val waktu = getColumnText("Waktu")

                // Bersihkan nama matakuliah dari residu
                courseName = courseName
                    .replace(Regex("""\b[1-6]\s+[A-Z][A-Z0-9]{0,2}\b"""), "")
                    .replace(Regex("""\b\d+\s*SKS\b""", RegexOption.IGNORE_CASE), "")
                    .replace(Regex("""\s+"""), " ")
                    .trim()

                if (courseName.isNotBlank() || courseCode.isNotBlank()) {
                    outputLines.add("${row.num}\t$courseCode\t$courseName\t$lecturer\t$keterangan\t$hari\t$waktu")
                }
            }
        }

        return if (outputLines.size > 1) outputLines.joinToString("\n") else ""
    }

    private fun detectTableColumns(words: List<PdfWordChunk>): Map<String, ColRange>? {
        // Cari kata kunci header: No, Kode, Matakuliah, Pengampu, Keterangan, Ruang, Hari, Waktu
        val noWord = words.find { it.text.equals("No", ignoreCase = true) || it.text.equals("No.", ignoreCase = true) } ?: return null
        val kodeWord = words.find { it.text.equals("Kode", ignoreCase = true) && Math.abs(it.minY - noWord.minY) < 15.0f } ?: return null
        val matkulWord = words.find { (it.text.contains("Matakuliah", ignoreCase = true) || it.text.equals("Mata", ignoreCase = true)) && Math.abs(it.minY - noWord.minY) < 15.0f } ?: return null
        val hariWord = words.find { it.text.equals("Hari", ignoreCase = true) && Math.abs(it.minY - noWord.minY) < 15.0f } ?: return null
        val waktuWord = words.find { (it.text.equals("Waktu", ignoreCase = true) || it.text.equals("Jam", ignoreCase = true)) && Math.abs(it.minY - noWord.minY) < 15.0f } ?: return null

        val pengampuWord = words.find { (it.text.contains("Pengampu", ignoreCase = true) || it.text.equals("Dosen", ignoreCase = true)) && Math.abs(it.minY - noWord.minY) < 15.0f }
        val ketWord = words.find { (it.text.startsWith("Keterangan", ignoreCase = true) || it.text.equals("Ket", ignoreCase = true)) && Math.abs(it.minY - noWord.minY) < 15.0f }
        val ruangWord = words.find { it.text.startsWith("Ruang", ignoreCase = true) && Math.abs(it.minY - noWord.minY) < 15.0f }

        val sksWord = words.find { it.text.equals("SKS", ignoreCase = true) && Math.abs(it.minY - noWord.minY) < 15.0f }

        // Hitung batas-batas kolom horizontal (X)
        val xNo = noWord.minX
        val xKode = kodeWord.minX
        val xMatkul = matkulWord.minX
        val xPengampu = pengampuWord?.minX ?: (xMatkul + 150f)
        val xSks = sksWord?.minX ?: (xMatkul + (xPengampu - xMatkul) * 0.7f)
        val xKet = ketWord?.minX ?: (xPengampu + 60f)
        val xRuang = ruangWord?.minX ?: (xKet + 50f)
        val xHari = hariWord.minX
        val xWaktu = waktuWord.minX

        val result = mutableMapOf<String, ColRange>()

        result["No"] = ColRange("No", 0f, (xNo + xKode) / 2f)
        result["Kode"] = ColRange("Kode", (xNo + xKode) / 2f, (xKode + xMatkul) / 2f)

        // Matakuliah dibatasi sampai sebelum kolom SKS atau Pengampu
        val matkulRight = if (sksWord != null) (xMatkul + xSks) / 2f else (xMatkul + xPengampu) / 2f
        result["Matakuliah"] = ColRange("Matakuliah", (xKode + xMatkul) / 2f, matkulRight)

        // Pengampu dibatasi sampai sebelum Keterangan
        val pengampuLeft = if (sksWord != null) (xSks + xPengampu) / 2f else matkulRight
        val pengampuRight = (xPengampu + xKet) / 2f
        result["Pengampu"] = ColRange("Pengampu", pengampuLeft, pengampuRight)

        // Keterangan dibatasi sampai sebelum Ruang
        val ketRight = (xKet + xRuang) / 2f
        result["Keterangan"] = ColRange("Keterangan", pengampuRight, ketRight)

        // Hari dibatasi sampai sebelum Waktu
        val hariLeft = (xRuang + xHari) / 2f
        val hariRight = (xHari + xWaktu) / 2f
        result["Hari"] = ColRange("Hari", hariLeft, hariRight)

        // Waktu
        result["Waktu"] = ColRange("Waktu", hariRight, Float.MAX_VALUE)

        return result
    }

    private fun detectTableRows(words: List<PdfWordChunk>, colRanges: Map<String, ColRange>): List<RowRange> {
        val noRange = colRanges["No"] ?: return emptyList()
        val kodeRange = colRanges["Kode"] ?: return emptyList()

        val headerY = words.filter {
            it.text.equals("No", ignoreCase = true) || it.text.equals("Kode", ignoreCase = true)
        }.map { it.minY }.average().toFloat().takeIf { !it.isNaN() } ?: 0f

        // Cari angka urut 1, 2, 3... di kolom No di bawah header
        val numbers = words.filter { word ->
            word.minY > headerY && word.maxX <= kodeRange.minX && word.text.matches(Regex("""^\d{1,2}$"""))
        }.sortedBy { it.minY }

        val detectedRows = mutableListOf<Pair<Int, PdfWordChunk>>()
        var expected = 1
        for (w in numbers) {
            val num = w.text.toIntOrNull() ?: continue
            if (num == expected) {
                detectedRows.add(Pair(num, w))
                expected++
            }
        }

        if (detectedRows.isEmpty()) return emptyList()

        val rows = mutableListOf<RowRange>()
        for (i in detectedRows.indices) {
            val (num, chunk) = detectedRows[i]
            val minY = chunk.minY - 4f
            val maxY = if (i + 1 < detectedRows.size) {
                detectedRows[i + 1].second.minY - 4f
            } else {
                val footerLimit = words.filter {
                    it.minY > chunk.minY && (
                        it.text.contains("Jumlah", ignoreCase = true) ||
                        it.text.contains("IP semester", ignoreCase = true) ||
                        it.text.contains("Mahasiswa", ignoreCase = true) ||
                        it.text.contains("Pembimbing", ignoreCase = true)
                    )
                }.minOfOrNull { it.minY } ?: (chunk.minY + 100f)
                footerLimit
            }
            rows.add(RowRange(num, minY, maxY))
        }

        return rows
    }

    private fun parseTsvTableFormat(text: String): List<ParsedScheduleItem> {
        val lines = text.lines().map { it.trim() }.filter { it.isNotBlank() }
        val results = mutableListOf<ParsedScheduleItem>()
        var colorIdx = 0

        val headerLine = lines.firstOrNull { it.startsWith("No\t", ignoreCase = true) || it.contains("\tKode\t", ignoreCase = true) }
        val colMap = mutableMapOf<String, Int>()
        if (headerLine != null) {
            val hCols = headerLine.split("\t").map { it.trim().lowercase() }
            hCols.forEachIndexed { idx, name ->
                when {
                    name.contains("kode") -> colMap["kode"] = idx
                    name.contains("matakuliah") || name.contains("mata kuliah") || name.contains("nama") -> colMap["matkul"] = idx
                    name.contains("pengampu") || name.contains("dosen") -> colMap["pengampu"] = idx
                    name.contains("keterangan") || name.contains("ket") -> colMap["keterangan"] = idx
                    name.contains("hari") -> colMap["hari"] = idx
                    name.contains("waktu") || name.contains("jam") -> colMap["waktu"] = idx
                }
            }
        }

        for (line in lines) {
            if (line.startsWith("No\t", ignoreCase = true) || line.contains("\tKode\t", ignoreCase = true)) continue

            val cols = line.split("\t").map { it.trim() }
            if (cols.size < 4) continue

            val courseCode = (colMap["kode"]?.let { cols.getOrNull(it) }) ?: cols.getOrNull(1) ?: ""
            var courseName = (colMap["matkul"]?.let { cols.getOrNull(it) }) ?: cols.getOrNull(2) ?: ""
            val lecturer = (colMap["pengampu"]?.let { cols.getOrNull(it) }) ?: cols.getOrNull(3) ?: ""
            val keterangan = (colMap["keterangan"]?.let { cols.getOrNull(it) }) ?: cols.getOrNull(4) ?: ""
            val hariRaw = (colMap["hari"]?.let { cols.getOrNull(it) }) ?: cols.getOrNull(5) ?: ""
            val waktuRaw = (colMap["waktu"]?.let { cols.getOrNull(it) }) ?: cols.getOrNull(6) ?: ""

            if (courseName.isBlank() && courseCode.isBlank()) continue

            // Pembersihan nama matkul
            courseName = courseName
                .replace(Regex("""\b[1-6]\s+[A-Z][A-Z0-9]{0,2}\b"""), "")
                .replace(Regex("""\b\d+\s*SKS\b""", RegexOption.IGNORE_CASE), "")
                .replace(Regex("""\s+"""), " ")
                .trim()

            // Hari (Senin - Jumat)
            val dayMatch = DAY_REGEX.find(hariRaw)
            val dayOfWeek = if (dayMatch != null) parseDay(dayMatch.value) else 1

            // Waktu
            var startTime = "08:00"
            var endTime = "09:40"
            val timeMatch = TIME_RANGE_REGEX.find(waktuRaw)
            if (timeMatch != null) {
                val (st, et) = parseTimes(timeMatch)
                startTime = st
                endTime = et
            }

            val courseType = if (keterangan.isNotBlank()) {
                if (keterangan.contains("Praktik", ignoreCase = true)) "Praktik" else "Teori"
            } else if (courseName.contains("PRAKTIK", ignoreCase = true)) {
                "Praktik"
            } else {
                "Teori"
            }

            results.add(
                ParsedScheduleItem(
                    courseName = courseName.ifBlank { "Mata Kuliah $courseCode" },
                    courseCode = courseCode,
                    lecturer = lecturer,
                    dayOfWeek = dayOfWeek,
                    startTime = startTime,
                    endTime = endTime,
                    room = courseType,
                    colorHex = colorPalette[colorIdx % colorPalette.size]
                )
            )
            colorIdx++
        }

        return results
    }

    // =========================================================================
    // FALLBACK STRATEGY 2: LINEAR SIAKAD TABLE PARSING
    // =========================================================================

    private fun parseSiakadTableFormat(text: String): List<ParsedScheduleItem> {
        val rawLines = text.lines().map { it.trim() }.filter { it.isNotBlank() }
        val lines = mutableListOf<String>()
        var lineIdx = 0
        while (lineIdx < rawLines.size) {
            val l = rawLines[lineIdx]
            if (l.matches(Regex("""^\d{1,2}$""")) && lineIdx + 1 < rawLines.size && rawLines[lineIdx + 1].matches(Regex("""^[A-Z0-9]{5,10}\b.*"""))) {
                lines.add("$l ${rawLines[lineIdx + 1]}")
                lineIdx += 2
            } else {
                lines.add(l)
                lineIdx++
            }
        }

        val results = mutableListOf<ParsedScheduleItem>()
        val rowIndices = mutableListOf<Int>()
        for (i in lines.indices) {
            val line = lines[i]
            val match = ROW_START_REGEX.find(line)
            if (match != null) {
                val rowNum = match.groupValues[1].toIntOrNull()
                val code = match.groupValues[2]
                if ((rowNum != null && rowNum in 1..50) || (code.length >= 6 && code.any { it.isDigit() } && code.any { it.isLetter() })) {
                    rowIndices.add(i)
                }
            }
        }

        if (rowIndices.isEmpty()) return emptyList()

        var colorIdx = 0
        for (idx in rowIndices.indices) {
            val startLine = rowIndices[idx]
            val endLine = if (idx + 1 < rowIndices.size) rowIndices[idx + 1] else lines.size

            val blockLines = lines.subList(startLine, endLine)
            val parsedItem = parseSingleSiakadBlock(blockLines, colorPalette[colorIdx % colorPalette.size])
            if (parsedItem != null) {
                results.add(parsedItem)
                colorIdx++
            }
        }

        return results
    }

    private fun parseSingleSiakadBlock(blockLines: List<String>, colorHex: String): ParsedScheduleItem? {
        if (blockLines.isEmpty()) return null

        val firstLine = blockLines[0]
        val match = ROW_START_REGEX.find(firstLine) ?: return null

        val courseCode = match.groupValues[2].trim()
        val firstLineRemainder = match.groupValues[3].trim()

        var foundDay: Int? = null
        var foundStartTime = "08:00"
        var foundEndTime = "09:40"
        var keterangan = "Teori"

        for (line in blockLines) {
            if (line.equals("Praktik", ignoreCase = true)) keterangan = "Praktik"
            else if (line.equals("Teori", ignoreCase = true)) keterangan = "Teori"

            val dayMatch = DAY_REGEX.find(line)
            if (dayMatch != null && foundDay == null) {
                foundDay = parseDay(dayMatch.value)
            }
        }

        // Ekstraksi Waktu: gabungkan seluruh baris blok agar rentang waktu multi-baris ("11:00:00\n-\n12:39:00") terbaca dengan akurat
        val joinedBlock = blockLines.joinToString(" ")
        val timeRangeMatch = TIME_RANGE_REGEX.find(joinedBlock)
        if (timeRangeMatch != null) {
            val (st, et) = parseTimes(timeRangeMatch)
            foundStartTime = st
            foundEndTime = et
        } else {
            val timeMatches = Regex("""\b(\d{1,2})[:.](\d{2})(?:[:.]\d{2})?\b""").findAll(joinedBlock).toList()
            if (timeMatches.size >= 2) {
                val h1 = timeMatches[0].groupValues[1].padStart(2, '0')
                val m1 = timeMatches[0].groupValues[2].padStart(2, '0')
                val h2 = timeMatches[1].groupValues[1].padStart(2, '0')
                val m2 = timeMatches[1].groupValues[2].padStart(2, '0')
                foundStartTime = "$h1:$m1"
                foundEndTime = "$h2:$m2"
            }
        }

        // Cari garis SKS/Rombel: misalnya "2 C" atau "2 C1" atau "ISLAM 2 C"
        var courseNameParts = mutableListOf<String>()
        var sksLineIdx = -1

        val firstLineSksMatch = Regex("""\b[1-6]\s+[A-Z][A-Z0-9]{0,2}$""").find(firstLineRemainder)
        if (firstLineSksMatch != null) {
            sksLineIdx = 0
            val beforeSks = firstLineRemainder.substring(0, firstLineSksMatch.range.first).trim()
            if (beforeSks.isNotEmpty()) {
                courseNameParts.add(beforeSks)
            }
        } else {
            courseNameParts.add(firstLineRemainder)
        }

        if (sksLineIdx == -1) {
            for (i in 1 until blockLines.size) {
                val line = blockLines[i]
                val sksMatch = Regex("""\b[1-6]\s+[A-Z][A-Z0-9]{0,2}$""").find(line)
                if (sksMatch != null) {
                    sksLineIdx = i
                    val beforeSks = line.substring(0, sksMatch.range.first).trim()
                    if (beforeSks.isNotEmpty()) {
                        courseNameParts.add(beforeSks)
                    }
                    break
                } else if (line.equals("Teori", ignoreCase = true) || line.equals("Praktik", ignoreCase = true) || ACADEMIC_TITLE_REGEX.containsMatchIn(line)) {
                    break
                } else {
                    courseNameParts.add(line)
                }
            }
        }

        var fullCourseName = courseNameParts.joinToString(" ")
            .replace(Regex("""\b[1-6]\s+[A-Z][A-Z0-9]{0,2}\b"""), "")
            .replace(Regex("""\b\d+\s*SKS\b""", RegexOption.IGNORE_CASE), "")
            .trim()

        if (keterangan == "Teori" && fullCourseName.contains("PRAKTIK", ignoreCase = true)) {
            keterangan = "Praktik"
        }

        // Lecturer adalah baris setelah SKS/Rombel sampai sebelum Teori/Praktik atau Ruang
        var lecturer = ""
        if (sksLineIdx != -1) {
            val lecturerParts = mutableListOf<String>()
            for (k in (sksLineIdx + 1) until blockLines.size) {
                val lk = blockLines[k]
                if (lk.equals("Teori", ignoreCase = true) || lk.equals("Praktik", ignoreCase = true) ||
                    lk.contains("RUANG", ignoreCase = true) || lk.contains("LAB", ignoreCase = true) ||
                    DAY_REGEX.containsMatchIn(lk) || TIME_RANGE_REGEX.containsMatchIn(lk)) {
                    break
                }
                lecturerParts.add(lk)
            }
            lecturer = lecturerParts.joinToString(" ").trim()
        }

        return ParsedScheduleItem(
            courseName = fullCourseName.ifBlank { "Mata Kuliah $courseCode" },
            courseCode = courseCode,
            lecturer = lecturer,
            dayOfWeek = foundDay ?: 1,
            startTime = foundStartTime,
            endTime = foundEndTime,
            room = keterangan,
            colorHex = colorHex
        )
    }

    // =========================================================================
    // FALLBACK STRATEGY 4: GENERIC LINE-BY-LINE PARSING
    // =========================================================================

    private fun parseGenericFormat(text: String): List<ParsedScheduleItem> {
        val lines = text.lines().map { it.trim() }.filter { it.isNotBlank() }
        val results = mutableListOf<ParsedScheduleItem>()
        var colorIdx = 0

        var i = 0
        while (i < lines.size) {
            val line = lines[i]
            val timeMatch = TIME_RANGE_REGEX.find(line)
            val dayMatch = DAY_REGEX.find(line)

            if (timeMatch != null && dayMatch != null) {
                val day = parseDay(dayMatch.value)
                val (startTime, endTime) = parseTimes(timeMatch)

                var courseCode = Regex("""\b([A-Z]{2,5}\s?[-_]?\s?\d{3,5}[A-Z]?)\b""").find(line)?.value?.trim() ?: ""
                var cleaned = line
                    .replace(timeMatch.value, " ")
                    .replace(dayMatch.value, " ", ignoreCase = true)

                if (courseCode.isNotEmpty()) cleaned = cleaned.replace(courseCode, " ")
                cleaned = cleanNoise(cleaned)

                results.add(
                    ParsedScheduleItem(
                        courseName = cleaned.ifBlank { "Mata Kuliah #${results.size + 1}" },
                        courseCode = courseCode,
                        dayOfWeek = day,
                        startTime = startTime,
                        endTime = endTime,
                        colorHex = colorPalette[colorIdx % colorPalette.size]
                    )
                )
                colorIdx++
            }
            i++
        }

        return results.distinctBy { "${it.courseName.lowercase()}_${it.dayOfWeek}_${it.startTime}" }
    }

    private fun parseDay(dayString: String): Int {
        val lower = dayString.lowercase(Locale.getDefault())
        return DAY_MAP[lower] ?: 1
    }

    private fun parseTimes(match: MatchResult): Pair<String, String> {
        val startH = match.groupValues[1].toIntOrNull() ?: 8
        val startM = match.groupValues[2].toIntOrNull() ?: 0
        val endH = match.groupValues[3].toIntOrNull() ?: 9
        val endM = match.groupValues[4].toIntOrNull() ?: 40

        val startFormatted = String.format(Locale.getDefault(), "%02d:%02d", startH, startM)
        val endFormatted = String.format(Locale.getDefault(), "%02d:%02d", endH, endM)
        return Pair(startFormatted, endFormatted)
    }

    private fun cleanNoise(input: String): String {
        return input
            .replace(Regex("""\b\d+\s*SKS\b""", RegexOption.IGNORE_CASE), " ")
            .replace(Regex("""\b(?:Kelas|Kls|Sem|Semester)\s*[A-Za-z0-9]+\b""", RegexOption.IGNORE_CASE), " ")
            .replace(Regex("""^[0-9]+[.\s|]+"""), "")
            .replace(Regex("""[|;]+"""), " ")
            .replace(Regex("""\s{2,}"""), " ")
            .trim()
    }
}
