package com.example.util

import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale
import java.util.concurrent.TimeUnit

object DateUtils {

    private val idLocale = Locale("id", "ID")

    fun getDayName(dayOfWeek: Int): String {
        return when (dayOfWeek) {
            1 -> "Senin"
            2 -> "Selasa"
            3 -> "Rabu"
            4 -> "Kamis"
            5 -> "Jumat"
            6 -> "Sabtu"
            7 -> "Minggu"
            else -> "Hari $dayOfWeek"
        }
    }

    /**
     * Mengembalikan 1 (Senin) sampai 7 (Minggu) untuk hari ini
     */
    fun getCurrentDayOfWeek(): Int {
        val cal = Calendar.getInstance()
        return when (cal.get(Calendar.DAY_OF_WEEK)) {
            Calendar.MONDAY -> 1
            Calendar.TUESDAY -> 2
            Calendar.WEDNESDAY -> 3
            Calendar.THURSDAY -> 4
            Calendar.FRIDAY -> 5
            Calendar.SATURDAY -> 6
            Calendar.SUNDAY -> 7
            else -> 1
        }
    }

    /**
     * Epoch millis awal hari ini (00:00:00.000)
     */
    fun getStartOfTodayMillis(): Long {
        val cal = Calendar.getInstance().apply {
            set(Calendar.HOUR_OF_DAY, 0)
            set(Calendar.MINUTE, 0)
            set(Calendar.SECOND, 0)
            set(Calendar.MILLISECOND, 0)
        }
        return cal.timeInMillis
    }

    fun formatDate(epochMillis: Long): String {
        val sdf = SimpleDateFormat("EEEE, d MMM yyyy", idLocale)
        return sdf.format(Date(epochMillis))
    }

    fun formatShortDate(epochMillis: Long): String {
        val sdf = SimpleDateFormat("d MMM yyyy", idLocale)
        return sdf.format(Date(epochMillis))
    }

    fun formatDateTime(epochMillis: Long): String {
        val sdf = SimpleDateFormat("d MMM yyyy, HH:mm", idLocale)
        return sdf.format(Date(epochMillis))
    }

    fun formatTime(epochMillis: Long): String {
        val sdf = SimpleDateFormat("HH:mm", idLocale)
        return sdf.format(Date(epochMillis))
    }

    /**
     * Menghasilkan teks relatif: "Hari ini", "Besok", "3 hari lagi", "Terlewat 2 hari"
     */
    fun getRelativeDeadline(dueMillis: Long, isCompleted: Boolean): String {
        if (isCompleted) return "Terselesaikan"

        val now = System.currentTimeMillis()
        val diff = dueMillis - now

        val oneDayMillis = 24 * 60 * 60 * 1000L
        val oneHourMillis = 60 * 60 * 1000L

        return when {
            diff < 0 -> {
                val overdueDays = (-diff / oneDayMillis).toInt()
                if (overdueDays == 0) "Terlewat beberapa jam"
                else "Terlewat $overdueDays hari lalu"
            }
            diff < oneHourMillis -> {
                val mins = (diff / (60 * 1000L)).toInt()
                "Tersisa $mins menit!"
            }
            diff < oneDayMillis -> {
                val hours = (diff / oneHourMillis).toInt()
                "Hari ini (${hours} jam lagi)"
            }
            diff < 2 * oneDayMillis -> {
                "Besok, " + formatTime(dueMillis)
            }
            else -> {
                val days = (diff / oneDayMillis).toInt()
                "$days hari lagi (${formatShortDate(dueMillis)})"
            }
        }
    }
}
