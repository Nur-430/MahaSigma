package com.example

import android.content.Context
import androidx.test.core.app.ApplicationProvider
import com.example.data.entity.CourseEntity
import com.example.util.BackupHelper
import com.example.util.DateUtils
import com.example.util.MahaSigmaBackup
import com.example.util.PdfScheduleParser
import com.example.util.TableScheduleParser
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [34])
class ExampleRobolectricTest {

    @Test
    fun `read string from context`() {
        val context = ApplicationProvider.getApplicationContext<Context>()
        val appName = context.getString(R.string.app_name)
        assertEquals("MahaSigma", appName)
    }

    @Test
    fun `date utils returns Indonesian day names`() {
        assertEquals("Senin", DateUtils.getDayName(1))
        assertEquals("Jumat", DateUtils.getDayName(5))
        assertEquals("Minggu", DateUtils.getDayName(7))
    }

    @Test
    fun `backup helper serializes and parses json correctly`() {
        val sampleCourse = CourseEntity(
            id = 1,
            name = "Algoritma Pemrograman",
            code = "IF101",
            lecturer = "Dr. Irwan",
            colorHex = "#38BDF8"
        )
        val backup = MahaSigmaBackup(
            version = 1,
            courses = listOf(sampleCourse)
        )

        val json = BackupHelper.exportToJson(backup)
        assertTrue(json.contains("Algoritma Pemrograman"))
        assertTrue(json.contains("IF101"))

        val parsed = BackupHelper.parseFromJson(json)
        assertNotNull(parsed)
        assertEquals(1, parsed?.courses?.size)
        assertEquals("Algoritma Pemrograman", parsed?.courses?.firstOrNull()?.name)
    }

    @Test
    fun `pdf schedule parser extracts indonesian course schedule lines correctly`() {
        val sampleKrsText = """
            KARTU RENCANA STUDI (KRS)
            FAKULTAS ILMU KOMPUTER
            1 | IF201 | Pemrograman Berorientasi Objek | 3 SKS | Senin | 08:00 - 10:30 | Lab Komputer 3 | Dr. Budi Santoso
            2 | IF202 | Sistem Operasi | 3 SKS | Rabu | 13.00 - 15.30 | Ruang R.302 | Prof. Ahmad Wijaya
            3 | IF203 | Basis Data Lanjut | 2 SKS | Jumat | 07:30 - 09:10 | Gedung B Lt. 2 | Ir. Sri Wahyuni, M.Kom
        """.trimIndent()

        val parsed = PdfScheduleParser.parseScheduleText(sampleKrsText)
        assertEquals(3, parsed.size)

        val first = parsed[0]
        assertEquals("IF201", first.courseCode)
        assertEquals(1, first.dayOfWeek) // Senin
        assertEquals("08:00", first.startTime)
        assertEquals("10:30", first.endTime)

        val second = parsed[1]
        assertEquals("IF202", second.courseCode)
        assertEquals(3, second.dayOfWeek) // Rabu
        assertEquals("13:00", second.startTime)
        assertEquals("15:30", second.endTime)

        val third = parsed[2]
        assertEquals("IF203", third.courseCode)
        assertEquals(5, third.dayOfWeek) // Jumat
        assertEquals("07:30", third.startTime)
        assertEquals("09:10", third.endTime)
    }

    @Test
    fun `pdf schedule parser extracts UNY SIAKAD KRS correctly`() {
        val unyKrsText = """
            Kartu Rencana Studi
            NIM : 26090620005 Semester / Tahun Ajaran : 1 / 2026
            Nama : MUHAMMAD NUROKHIM Prodi : TEKNIK ELEKTRONIKA - D4
            Angkatan : 2026 Pembimbing : Ahmad Taufiq Musaddid S.T., M.Eng.
            No Kode Matakuliah SKS /Rombel Pengampu Keterangan Ruang Hari Waktu
            1 MWK60201 PENDIDIKAN AGAMA
            ISLAM 2 C
            Nujumun
            Niswah
            M.Pd.
            Teori
            RUANG KULIAH
            ELEKTRONIKA 1, GEDUNG
            LABORATORIUM VOKASI
            KAMPUS GUNUNGKIDUL,
            size:45 [J.41.3.01.01]
            Senin
            11:00:00
            -
            12:39:00
            2 MWK60207 PENDIDIKAN
            KEWARGANEGARAAN 2 C
            Alfin Harits
            Norma
            Wildan
            M.Pd.
            Teori
            RUANG KULIAH
            ELEKTRONIKA 1, GEDUNG
            LABORATORIUM VOKASI
            KAMPUS GUNUNGKIDUL,
            size:45 [J.41.3.01.01]
            Kamis
            07:30:00
            -
            09:09:00
            3 VTA60201 MATEMATIKA 1 2 C
            Ahmad
            Taufiq
            Musaddid
            S.T., M.Eng.
            Teori
            RUANG KULIAH
            ELEKTRONIKA 1, GEDUNG
            LABORATORIUM VOKASI
            KAMPUS GUNUNGKIDUL,
            size:45 [J.41.3.01.01]
            Senin
            07:30:00
            -
            09:09:00
            6 VTA60204 PRAKTIK
            ELEKTRONIKA DASAR 2 C1
            Rizky
            Hidayat
            Prasetyo
            M.T.
            Praktik
            LAB.SISTEM OTOMASI
            DAN KENDALI, GEDUNG
            LABORATORIUM VOKASI
            KAMPUS GUNUNGKIDUL,
            size:25 [J.41..3.03.06]
            Senin
            14:40:00
            -
            18:00:00
        """.trimIndent()

        val parsed = PdfScheduleParser.parseScheduleText(unyKrsText)
        assertEquals(4, parsed.size)

        // Item 1: Pendidikan Agama Islam
        val item1 = parsed[0]
        assertEquals("MWK60201", item1.courseCode)
        assertTrue(item1.courseName.contains("PENDIDIKAN AGAMA ISLAM", ignoreCase = true))
        assertEquals(1, item1.dayOfWeek) // Senin
        assertEquals("11:00", item1.startTime)
        assertEquals("12:39", item1.endTime)
        assertTrue(item1.room.contains("RUANG KULIAH ELEKTRONIKA 1"))

        // Item 2: Pendidikan Kewarganegaraan
        val item2 = parsed[1]
        assertEquals("MWK60207", item2.courseCode)
        assertTrue(item2.courseName.contains("PENDIDIKAN KEWARGANEGARAAN", ignoreCase = true))
        assertEquals(4, item2.dayOfWeek) // Kamis
        assertEquals("07:30", item2.startTime)
        assertEquals("09:09", item2.endTime)

        // Item 3: Matematika 1
        val item3 = parsed[2]
        assertEquals("VTA60201", item3.courseCode)
        assertTrue(item3.courseName.contains("MATEMATIKA 1", ignoreCase = true))
        assertEquals(1, item3.dayOfWeek) // Senin
        assertEquals("07:30", item3.startTime)
        assertEquals("09:09", item3.endTime)

        // Item 4: Praktik Elektronika Dasar
        val item4 = parsed[3]
        assertEquals("VTA60204", item4.courseCode)
        assertTrue(item4.courseName.contains("PRAKTIK ELEKTRONIKA DASAR", ignoreCase = true))
        assertEquals(1, item4.dayOfWeek) // Senin
        assertEquals("14:40", item4.startTime)
        assertEquals("18:00", item4.endTime)
        assertTrue(item4.room.contains("LAB.SISTEM OTOMASI"))
    }

    @Test
    fun `table schedule parser handles tab-separated rows from Excel or Web`() {
        val tsvText = """
            No	Kode	Matakuliah	SKS / Rombel	Pengampu	Ruang	Hari	Waktu
            1	MWK60201	PENDIDIKAN AGAMA ISLAM	2 C	Nujumun Niswah M.Pd.	RUANG ELEKTRONIKA 1	Senin	11:00:00 - 12:39:00
            2	MWK60207	PENDIDIKAN KEWARGANEGARAAN	2 C	Alfin Harits M.Pd.	RUANG ELEKTRONIKA 1	Kamis	07:30:00 - 09:09:00
            3	VTA60201	MATEMATIKA 1	2 C	Ahmad Taufiq M.Eng.	RUANG ELEKTRONIKA 1	Senin	07:30:00 - 09:09:00
        """.trimIndent()

        val rows = TableScheduleParser.parseDelimitedText(tsvText)
        assertEquals(4, rows.size)

        val items = TableScheduleParser.parseTableGrid(rows)
        assertEquals(3, items.size)

        assertEquals("MWK60201", items[0].courseCode)
        assertEquals("PENDIDIKAN AGAMA ISLAM", items[0].courseName)
        assertEquals(1, items[0].dayOfWeek) // Senin
        assertEquals("11:00", items[0].startTime)
        assertEquals("12:39", items[0].endTime)
        assertEquals("RUANG ELEKTRONIKA 1", items[0].room)
        assertEquals("Nujumun Niswah M.Pd.", items[0].lecturer)

        assertEquals("MWK60207", items[1].courseCode)
        assertEquals(4, items[1].dayOfWeek) // Kamis

        // Verify CSV export produces valid comma-separated text
        val exportedCsv = TableScheduleParser.exportToCsv(items)
        assertTrue(exportedCsv.contains("Kode,Mata Kuliah,Hari,Jam Mulai,Jam Selesai,Ruangan,Dosen Pengampu"))
        assertTrue(exportedCsv.contains("MWK60201,\"PENDIDIKAN AGAMA ISLAM\",Senin,11:00,12:39,\"RUANG ELEKTRONIKA 1\",\"Nujumun Niswah M.Pd.\""))
    }
}
