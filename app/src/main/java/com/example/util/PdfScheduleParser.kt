package com.example.util

import android.content.Context
import android.net.Uri
import com.tom_roush.pdfbox.android.PDFBoxResourceLoader
import com.tom_roush.pdfbox.pdmodel.PDDocument
import com.tom_roush.pdfbox.text.PDFTextStripper
import java.util.Locale
import java.util.UUID

data class ParsedScheduleItem(
    val tempId: String = UUID.randomUUID().toString(),
    var courseName: String,
    var courseCode: String = "",
    var lecturer: String = "",
    var dayOfWeek: Int = 1, // 1=Senin..7=Minggu
    var startTime: String = "08:00",
    var endTime: String = "09:40",
    var room: String = "",
    var colorHex: String = "#38BDF8",
    var isSelected: Boolean = true
)

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
        "jumat" to 5, "jum'at" to 5, "jum" to 5, "fri" to 5, "friday" to 5,
        "sabtu" to 6, "sab" to 6, "sat" to 6, "saturday" to 6,
        "minggu" to 7, "min" to 7, "sun" to 7, "sunday" to 7
    )

    // Supports HH:mm or HH:mm:ss, single line or multiple lines separated by -
    private val TIME_RANGE_REGEX = Regex(
        """(\d{1,2})[:.](\d{2})(?:[:.]\d{2})?\s*(?:-|–|—|s/d|sampai|to)\s*(\d{1,2})[:.](\d{2})(?:[:.]\d{2})?""",
        RegexOption.IGNORE_CASE
    )

    private val DAY_REGEX = Regex(
        """\b(senin|selasa|rabu|kamis|jum['a]?at|sabtu|minggu|monday|tuesday|wednesday|thursday|friday|saturday|sunday)\b""",
        RegexOption.IGNORE_CASE
    )

    private val ROW_START_REGEX = Regex(
        """^(?:(\d{1,2})\s+)?([A-Z0-9]{5,10})(?:\s+(.*))?$"""
    )

    /**
     * Extracts raw text from a PDF file Uri.
     */
    fun extractTextFromPdf(context: Context, uri: Uri): Result<String> {
        return try {
            PDFBoxResourceLoader.init(context.applicationContext)
            context.contentResolver.openInputStream(uri)?.use { inputStream ->
                val document = PDDocument.load(inputStream)
                val stripper = PDFTextStripper()
                stripper.sortByPosition = true // Keep tabular structure intact
                val text = stripper.getText(document)
                document.close()
                Result.success(text)
            } ?: Result.failure(Exception("Tidak dapat membuka file PDF"))
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    /**
     * Parses raw text (from PDF or direct paste) into structured schedule items.
     * Specially optimized for Indonesian university KRS formats (like UNY SIAKAD, etc.)
     */
    fun parseScheduleText(text: String): List<ParsedScheduleItem> {
        if (text.isBlank()) return emptyList()

        // First normalize times split across newlines like "11:00:00\n-\n12:39:00"
        val normalizedText = text
            .replace(Regex("""(\d{1,2}[:.]\d{2}(?:[:.]\d{2})?)\s*\n\s*(-|–|—)\s*\n\s*(\d{1,2}[:.]\d{2}(?:[:.]\d{2})?)"""), "$1 - $3")
            .replace(Regex("""(\d{1,2}[:.]\d{2}(?:[:.]\d{2})?)\s*\n\s*(-|–|—)\s*(\d{1,2}[:.]\d{2}(?:[:.]\d{2})?)"""), "$1 - $3")
            .replace(Regex("""(\d{1,2}[:.]\d{2}(?:[:.]\d{2})?)\s*(-|–|—)\s*\n\s*(\d{1,2}[:.]\d{2}(?:[:.]\d{2})?)"""), "$1 - $3")

        // Strategy 1: Check if this is a SIAKAD UNY / tabular KRS with row numbers
        val blockResults = parseSiakadTableFormat(normalizedText)
        if (blockResults.isNotEmpty()) {
            return blockResults
        }

        // Strategy 2: Check if text has delimited table lines (Tabs / Semicolon / CSV / Pipes)
        val tableRows = TableScheduleParser.parseDelimitedText(normalizedText)
        if (tableRows.size >= 2) {
            val tableResults = TableScheduleParser.parseTableGrid(tableRows)
            if (tableResults.isNotEmpty()) {
                return tableResults
            }
        }

        // Strategy 3: Fallback line-by-line / generic regex parsing
        return parseGenericFormat(normalizedText)
    }

    /**
     * Parses SIAKAD KRS table formats where each record begins with:
     * "[No] [KodeMatkul] [NamaMatkul...]" or "[KodeMatkul]"
     */
    private fun parseSiakadTableFormat(text: String): List<ParsedScheduleItem> {
        val rawLines = text.lines().map { it.trim() }.filter { it.isNotBlank() }
        val lines = mutableListOf<String>()
        var lineIdx = 0
        while (lineIdx < rawLines.size) {
            val l = rawLines[lineIdx]
            // If current line is just a row number (e.g. "1") and next line has course code (e.g. "MWK60201...")
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

        if (rowIndices.isEmpty()) {
            return emptyList()
        }

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

        // Look for Day and Time inside the block
        var foundDay: Int? = null
        var foundStartTime = "08:00"
        var foundEndTime = "09:40"
        var timeLineIndex = -1
        var dayLineIndex = -1

        for (i in blockLines.indices.reversed()) {
            val line = blockLines[i]
            val timeMatch = TIME_RANGE_REGEX.find(line)
            if (timeMatch != null && timeLineIndex == -1) {
                timeLineIndex = i
                val (st, et) = parseTimes(timeMatch)
                foundStartTime = st
                foundEndTime = et
            }

            val dayMatch = DAY_REGEX.find(line)
            if (dayMatch != null && dayLineIndex == -1) {
                dayLineIndex = i
                foundDay = parseDay(dayMatch.value)
            }
        }

        // If no day found in this block, default to Senin
        val dayOfWeek = foundDay ?: 1

        // Extract Room
        var room = ""
        val roomIndex = blockLines.indexOfFirst {
            it.contains("RUANG", ignoreCase = true) ||
            it.contains("LAB.", ignoreCase = true) ||
            it.contains("LABORATORIUM", ignoreCase = true) ||
            it.contains("GEDUNG", ignoreCase = true)
        }

        if (roomIndex != -1) {
            val roomLines = mutableListOf<String>()
            val maxLimit = if (dayLineIndex != -1) minOf(dayLineIndex, blockLines.size) else blockLines.size
            for (r in roomIndex until maxLimit) {
                val rLine = blockLines[r]
                if (DAY_REGEX.containsMatchIn(rLine) || TIME_RANGE_REGEX.containsMatchIn(rLine)) break
                roomLines.add(rLine)
            }
            room = formatRoomText(roomLines.joinToString(" "))
        }

        // Extract Course Name
        // The course name starts in firstLineRemainder and may continue on line 1,
        // until the SKS / Rombel / Teori / Praktik / Lecturer starts
        val courseNameParts = mutableListOf<String>()
        courseNameParts.add(firstLineRemainder)

        for (i in 1 until blockLines.size) {
            val l = blockLines[i]
            // Stop if we hit lecturer degree, Teori, Praktik, Ruang, Day, or Time
            if (l.equals("Teori", ignoreCase = true) ||
                l.equals("Praktik", ignoreCase = true) ||
                l.contains("RUANG", ignoreCase = true) ||
                l.contains("LAB", ignoreCase = true) ||
                DAY_REGEX.containsMatchIn(l) ||
                TIME_RANGE_REGEX.containsMatchIn(l) ||
                l.contains("M.Pd", ignoreCase = true) ||
                l.contains("S.T", ignoreCase = true) ||
                l.contains("M.Eng", ignoreCase = true) ||
                l.contains("M.T", ignoreCase = true) ||
                l.contains("Dr.", ignoreCase = true) ||
                l.contains("Prof.", ignoreCase = true) ||
                l.contains("Ir.", ignoreCase = true)
            ) {
                break
            }
            courseNameParts.add(l)
        }

        var fullCourseName = courseNameParts.joinToString(" ")
        // Clean SKS and Rombel (e.g. "2 C", "2 C1", "3 SKS C")
        fullCourseName = fullCourseName
            .replace(Regex("""\s+\d+\s+[A-Z0-9]{1,3}$""", RegexOption.IGNORE_CASE), "")
            .replace(Regex("""\b\d+\s*SKS\b""", RegexOption.IGNORE_CASE), "")
            .trim()

        // Extract Lecturer
        var lecturer = ""
        val lecturerLines = mutableListOf<String>()
        val startLecturerIdx = if (courseNameParts.size < blockLines.size) courseNameParts.size else -1
        if (startLecturerIdx != -1) {
            val stopIdx = if (roomIndex != -1) roomIndex else (if (dayLineIndex != -1) dayLineIndex else blockLines.size)
            for (k in startLecturerIdx until stopIdx) {
                val lk = blockLines[k]
                if (lk.equals("Teori", ignoreCase = true) || lk.equals("Praktik", ignoreCase = true)) continue
                if (lk.matches(Regex("""^\d+\s+[A-Z0-9]{1,3}$"""))) continue
                lecturerLines.add(lk)
            }
            lecturer = lecturerLines.joinToString(" ").trim()
        }

        return ParsedScheduleItem(
            courseName = fullCourseName.ifBlank { "Mata Kuliah $courseCode" },
            courseCode = courseCode,
            lecturer = lecturer,
            dayOfWeek = dayOfWeek,
            startTime = foundStartTime,
            endTime = foundEndTime,
            room = room,
            colorHex = colorHex
        )
    }

    private fun formatRoomText(rawRoom: String): String {
        // Look for clean room name or size code
        val codeMatch = Regex("""\[(.*?)\]""").find(rawRoom)
        val roomCode = codeMatch?.groupValues?.get(1)

        val firstPart = rawRoom.split(",")[0].trim()
        return if (roomCode != null && !firstPart.contains(roomCode)) {
            "$firstPart ($roomCode)"
        } else {
            firstPart
        }
    }

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

                if (cleaned.length < 3 && i > 0) {
                    val prevLine = cleanNoise(lines[i - 1])
                    if (prevLine.length >= 3 && !DAY_REGEX.containsMatchIn(prevLine) && !TIME_RANGE_REGEX.containsMatchIn(prevLine)) {
                        cleaned = prevLine
                    }
                }

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
            } else if (timeMatch != null) {
                var foundDay = 1
                for (back in (i - 1) downTo maxOf(0, i - 4)) {
                    val prevDayMatch = DAY_REGEX.find(lines[back])
                    if (prevDayMatch != null) {
                        foundDay = parseDay(prevDayMatch.value)
                        break
                    }
                }

                val (startTime, endTime) = parseTimes(timeMatch)
                var courseCode = Regex("""\b([A-Z]{2,5}\s?[-_]?\s?\d{3,5}[A-Z]?)\b""").find(line)?.value?.trim() ?: ""
                var cleaned = line.replace(timeMatch.value, " ")
                if (courseCode.isNotEmpty()) cleaned = cleaned.replace(courseCode, " ")
                cleaned = cleanNoise(cleaned)

                if (cleaned.length < 3 && i > 0) {
                    val prev = cleanNoise(lines[i - 1])
                    if (prev.length >= 3 && !TIME_RANGE_REGEX.containsMatchIn(prev)) {
                        cleaned = prev
                    }
                }

                results.add(
                    ParsedScheduleItem(
                        courseName = cleaned.ifBlank { "Mata Kuliah #${results.size + 1}" },
                        courseCode = courseCode,
                        dayOfWeek = foundDay,
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
