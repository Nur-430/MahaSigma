package com.example.util

import android.content.Context
import android.net.Uri
import android.util.Xml
import org.xmlpull.v1.XmlPullParser
import java.io.ByteArrayInputStream
import java.io.InputStream
import java.util.Locale
import java.util.zip.ZipInputStream

object TableScheduleParser {

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
        "jumat" to 5, "jum'at" to 5, "jum" to 5, "fri" to 5, "friday" to 5,
        "sabtu" to 6, "sab" to 6, "sat" to 6, "saturday" to 6,
        "minggu" to 7, "min" to 7, "sun" to 7, "sunday" to 7
    )

    private val TIME_RANGE_REGEX = Regex(
        """(\d{1,2})[:.](\d{2})(?:[:.]\d{2})?\s*(?:-|–|—|s/d|sampai|to)\s*(\d{1,2})[:.](\d{2})(?:[:.]\d{2})?""",
        RegexOption.IGNORE_CASE
    )

    /**
     * Parses an Excel (.xlsx) file stream into a list of parsed schedule items.
     */
    fun parseXlsxFromUri(context: Context, uri: Uri): Result<List<ParsedScheduleItem>> {
        return try {
            val rows = context.contentResolver.openInputStream(uri)?.use { stream ->
                parseXlsx(stream)
            } ?: emptyList()

            if (rows.isEmpty()) {
                Result.failure(Exception("File Excel tidak memiliki data atau lembar sheet kosong."))
            } else {
                val items = parseTableGrid(rows)
                Result.success(items)
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    /**
     * Parses a CSV/TSV or plain text file into schedule items.
     */
    fun parseCsvFromUri(context: Context, uri: Uri): Result<List<ParsedScheduleItem>> {
        return try {
            val text = context.contentResolver.openInputStream(uri)?.bufferedReader()?.use { it.readText() } ?: ""
            if (text.isBlank()) {
                Result.failure(Exception("File CSV kosong."))
            } else {
                val rows = parseDelimitedText(text)
                val items = parseTableGrid(rows)
                Result.success(items)
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    /**
     * Extracts tabular grid from an .xlsx InputStream using standard Android zip + XmlPullParser.
     */
    fun parseXlsx(inputStream: InputStream): List<List<String>> {
        val zip = ZipInputStream(inputStream)
        var entry = zip.nextEntry
        val sharedStrings = mutableListOf<String>()
        var sheetBytes: ByteArray? = null

        while (entry != null) {
            val name = entry.name.lowercase(Locale.ROOT)
            if (name.endsWith("sharedstrings.xml")) {
                sharedStrings.addAll(parseSharedStrings(zip))
            } else if (name.endsWith("sheet1.xml") || (sheetBytes == null && name.contains("worksheets/sheet"))) {
                sheetBytes = zip.readBytes()
            }
            zip.closeEntry()
            entry = zip.nextEntry
        }

        if (sheetBytes == null) return emptyList()
        return parseSheetXml(ByteArrayInputStream(sheetBytes), sharedStrings)
    }

    private fun parseSharedStrings(stream: InputStream): List<String> {
        val parser = Xml.newPullParser()
        parser.setInput(stream, "UTF-8")
        val list = mutableListOf<String>()
        var eventType = parser.eventType
        var inT = false
        var currentText = StringBuilder()

        while (eventType != XmlPullParser.END_DOCUMENT) {
            val name = parser.name
            when (eventType) {
                XmlPullParser.START_TAG -> {
                    if (name == "si") {
                        currentText = StringBuilder()
                    } else if (name == "t") {
                        inT = true
                    }
                }
                XmlPullParser.TEXT -> {
                    if (inT) {
                        currentText.append(parser.text)
                    }
                }
                XmlPullParser.END_TAG -> {
                    if (name == "t") {
                        inT = false
                    } else if (name == "si") {
                        list.add(currentText.toString())
                    }
                }
            }
            eventType = parser.next()
        }
        return list
    }

    private fun parseSheetXml(stream: InputStream, sharedStrings: List<String>): List<List<String>> {
        val parser = Xml.newPullParser()
        parser.setInput(stream, "UTF-8")
        val rows = mutableListOf<List<String>>()
        var currentRow = mutableMapOf<Int, String>()
        var currentCellCol = 0
        var cellType = ""
        var inV = false
        var cellVal = StringBuilder()

        var eventType = parser.eventType
        while (eventType != XmlPullParser.END_DOCUMENT) {
            val name = parser.name
            when (eventType) {
                XmlPullParser.START_TAG -> {
                    if (name == "row") {
                        currentRow = mutableMapOf()
                    } else if (name == "c") {
                        val r = parser.getAttributeValue(null, "r") ?: ""
                        currentCellCol = colRefToIndex(r)
                        cellType = parser.getAttributeValue(null, "t") ?: ""
                        cellVal = StringBuilder()
                    } else if (name == "v") {
                        inV = true
                    }
                }
                XmlPullParser.TEXT -> {
                    if (inV) {
                        cellVal.append(parser.text)
                    }
                }
                XmlPullParser.END_TAG -> {
                    if (name == "v") {
                        inV = false
                    } else if (name == "c") {
                        val rawVal = cellVal.toString().trim()
                        val finalVal = if (cellType == "s") {
                            val idx = rawVal.toIntOrNull()
                            if (idx != null && idx in sharedStrings.indices) sharedStrings[idx] else rawVal
                        } else {
                            rawVal
                        }
                        currentRow[currentCellCol] = finalVal
                    } else if (name == "row") {
                        if (currentRow.isNotEmpty()) {
                            val maxCol = currentRow.keys.maxOrNull() ?: 0
                            val rowList = (0..maxCol).map { col -> currentRow[col] ?: "" }
                            rows.add(rowList)
                        }
                    }
                }
            }
            eventType = parser.next()
        }
        return rows
    }

    private fun colRefToIndex(cellRef: String): Int {
        var col = 0
        for (ch in cellRef) {
            if (ch in 'A'..'Z') {
                col = col * 26 + (ch - 'A' + 1)
            } else break
        }
        return maxOf(0, col - 1)
    }

    /**
     * Splits delimited text (Tab-separated from Excel/Sheets copy-paste, or CSV/semicolon).
     */
    fun parseDelimitedText(text: String): List<List<String>> {
        val lines = text.lines().map { it.trim() }.filter { it.isNotEmpty() }
        if (lines.isEmpty()) return emptyList()

        // Check delimiter frequency
        val tabLines = lines.count { it.contains('\t') }
        val semiLines = lines.count { it.contains(';') }
        val pipeLines = lines.count { it.contains('|') }
        val commaLines = lines.count { it.contains(',') }

        val delimiter = when {
            tabLines >= 2 -> "\t"
            semiLines >= 2 -> ";"
            pipeLines >= 2 -> "|"
            commaLines >= 2 -> ","
            tabLines > 0 -> "\t"
            else -> return emptyList() // Not a delimited table
        }

        val rows = lines.map { line ->
            line.split(delimiter).map { cell ->
                cell.trim().trim('"', '\'').trim()
            }
        }

        // Only consider it a table if at least 2 rows have 3 or more columns
        val multiColRows = rows.count { it.size >= 3 }
        if (multiColRows < 2) return emptyList()

        return rows
    }

    /**
     * Converts a 2D table grid into parsed schedule items with smart column detection.
     */
    fun parseTableGrid(rows: List<List<String>>): List<ParsedScheduleItem> {
        if (rows.isEmpty()) return emptyList()

        // Find header row
        var headerRowIndex = -1
        var colCode = -1
        var colName = -1
        var colDay = -1
        var colTime = -1
        var colRoom = -1
        var colLecturer = -1

        for (r in rows.indices) {
            val row = rows[r].map { it.lowercase(Locale.ROOT).trim() }
            val matchCount = row.count { cell ->
                cell.contains("kode") || cell.contains("matakuliah") || cell.contains("mata kuliah") ||
                cell.contains("hari") || cell.contains("waktu") || cell.contains("jam") ||
                cell.contains("ruang") || cell.contains("pengampu") || cell.contains("dosen")
            }

            if (matchCount >= 2) {
                headerRowIndex = r
                for (c in row.indices) {
                    val h = row[c]
                    when {
                        h.contains("kode") || h == "code" -> if (colCode == -1) colCode = c
                        h.contains("matakuliah") || h.contains("mata kuliah") || h.contains("nama mk") || h.contains("course") || (h == "nama" && colName == -1) -> if (colName == -1) colName = c
                        h.contains("hari") || h == "day" -> if (colDay == -1) colDay = c
                        h.contains("waktu") || h.contains("jam") || h.contains("time") || h.contains("pukul") -> if (colTime == -1) colTime = c
                        h.contains("ruang") || h.contains("room") || h.contains("lab") || h.contains("lokasi") -> if (colRoom == -1) colRoom = c
                        h.contains("pengampu") || h.contains("dosen") || h.contains("lecturer") -> if (colLecturer == -1) colLecturer = c
                    }
                }
                break
            }
        }

        val dataRows = if (headerRowIndex != -1) rows.subList(headerRowIndex + 1, rows.size) else rows
        val results = mutableListOf<ParsedScheduleItem>()
        var colorIdx = 0

        for (row in dataRows) {
            if (row.all { it.isBlank() }) continue

            var courseCode = if (colCode != -1 && colCode < row.size) row[colCode].trim() else ""
            var courseName = if (colName != -1 && colName < row.size) row[colName].trim() else ""
            var dayStr = if (colDay != -1 && colDay < row.size) row[colDay].trim() else ""
            var timeStr = if (colTime != -1 && colTime < row.size) row[colTime].trim() else ""
            var room = if (colRoom != -1 && colRoom < row.size) row[colRoom].trim() else ""
            var lecturer = if (colLecturer != -1 && colLecturer < row.size) row[colLecturer].trim() else ""

            // Fallback: If header detection missed columns, check cells dynamically
            if (dayStr.isEmpty() || timeStr.isEmpty() || courseName.isEmpty()) {
                for (c in row.indices) {
                    val cell = row[c].trim()
                    if (cell.isBlank()) continue

                    // Check if cell is day
                    if (dayStr.isEmpty() && isDayName(cell)) {
                        dayStr = cell
                        continue
                    }

                    // Check if cell is time range
                    if (timeStr.isEmpty() && TIME_RANGE_REGEX.containsMatchIn(cell)) {
                        timeStr = cell
                        continue
                    }

                    // Check if cell is course code
                    if (courseCode.isEmpty() && cell.matches(Regex("""^[A-Z0-9]{5,10}$"""))) {
                        courseCode = cell
                        continue
                    }

                    // Check if cell is room
                    if (room.isEmpty() && (cell.contains("RUANG", ignoreCase = true) || cell.contains("LAB", ignoreCase = true) || cell.contains("GEDUNG", ignoreCase = true))) {
                        room = cell
                        continue
                    }

                    // Check if cell is lecturer
                    if (lecturer.isEmpty() && (cell.contains("M.Pd", ignoreCase = true) || cell.contains("S.T", ignoreCase = true) || cell.contains("Dr.", ignoreCase = true) || cell.contains("Prof.", ignoreCase = true))) {
                        lecturer = cell
                        continue
                    }

                    // Otherwise candidate for course name
                    if (courseName.isEmpty() && cell.length > 3 && !cell.matches(Regex("""^\d+$""")) && !cell.equals("Teori", ignoreCase = true) && !cell.equals("Praktik", ignoreCase = true)) {
                        courseName = cell
                    }
                }
            }

            // Clean course name (remove trailing SKS/Rombel like "2 C" or "3 SKS")
            courseName = courseName
                .replace(Regex("""\s+\d+\s+[A-Z0-9]{1,3}$""", RegexOption.IGNORE_CASE), "")
                .replace(Regex("""\b\d+\s*SKS\b""", RegexOption.IGNORE_CASE), "")
                .trim()

            // Skip title/header/footer rows
            val lowerName = courseName.lowercase(Locale.ROOT)
            if (lowerName.contains("kartu rencana studi") || lowerName.contains("krs") ||
                lowerName.contains("fakultas") || lowerName.contains("universitas") ||
                lowerName.contains("semester") || lowerName.contains("kementerian") ||
                lowerName.contains("program studi") || lowerName.contains("tahun akademik")
            ) {
                continue
            }
            if (courseName.contains("Jumlah", ignoreCase = true) && courseName.contains("SKS", ignoreCase = true)) continue
            if (courseName.contains("IP semester", ignoreCase = true) || courseName.contains("Pembimbing", ignoreCase = true)) continue
            if (courseCode.contains("Jumlah", ignoreCase = true)) continue

            // A valid table course row MUST have either a course code OR a valid day / time
            val hasValidDayOrTime = isDayName(dayStr) || TIME_RANGE_REGEX.containsMatchIn(timeStr)
            val hasValidCode = courseCode.isNotBlank() && courseCode.matches(Regex("""^[A-Z0-9]{5,10}$"""))

            // Parse day and time
            val dayOfWeek = parseDay(dayStr)
            val (startTime, endTime) = parseTimes(timeStr)

            if ((courseName.isNotBlank() && hasValidDayOrTime) || hasValidCode) {
                val finalName = courseName.ifBlank { "Mata Kuliah $courseCode" }
                results.add(
                    ParsedScheduleItem(
                        courseName = finalName,
                        courseCode = courseCode,
                        lecturer = lecturer,
                        dayOfWeek = dayOfWeek,
                        startTime = startTime,
                        endTime = endTime,
                        room = cleanRoom(room),
                        colorHex = colorPalette[colorIdx % colorPalette.size]
                    )
                )
                colorIdx++
            }
        }

        return results
    }

    private fun isDayName(text: String): Boolean {
        val lower = text.lowercase(Locale.ROOT)
        return DAY_MAP.containsKey(lower)
    }

    private fun parseDay(dayString: String): Int {
        val lower = dayString.lowercase(Locale.ROOT).trim()
        for ((key, value) in DAY_MAP) {
            if (lower.contains(key)) return value
        }
        return 1
    }

    private fun parseTimes(timeString: String): Pair<String, String> {
        val match = TIME_RANGE_REGEX.find(timeString)
        if (match != null) {
            val startH = match.groupValues[1].toIntOrNull() ?: 8
            val startM = match.groupValues[2].toIntOrNull() ?: 0
            val endH = match.groupValues[3].toIntOrNull() ?: 9
            val endM = match.groupValues[4].toIntOrNull() ?: 40
            return Pair(
                String.format(Locale.getDefault(), "%02d:%02d", startH, startM),
                String.format(Locale.getDefault(), "%02d:%02d", endH, endM)
            )
        }
        return Pair("08:00", "09:40")
    }

    private fun cleanRoom(raw: String): String {
        if (raw.isBlank()) return ""
        val codeMatch = Regex("""\[(.*?)\]""").find(raw)
        val roomCode = codeMatch?.groupValues?.get(1)
        val firstPart = raw.split(",")[0].trim()
        return if (roomCode != null && !firstPart.contains(roomCode)) {
            "$firstPart ($roomCode)"
        } else {
            firstPart
        }
    }

    /**
     * Exports a list of schedule items to CSV text for opening in Microsoft Excel / Google Sheets.
     */
    fun exportToCsv(items: List<ParsedScheduleItem>): String {
        val sb = StringBuilder()
        sb.append("Kode,Mata Kuliah,Hari,Jam Mulai,Jam Selesai,Ruangan,Dosen Pengampu\n")
        items.forEach { item ->
            val dayName = DateUtils.getDayName(item.dayOfWeek)
            sb.append("${item.courseCode},\"${item.courseName}\",$dayName,${item.startTime},${item.endTime},\"${item.room}\",\"${item.lecturer}\"\n")
        }
        return sb.toString()
    }
}
