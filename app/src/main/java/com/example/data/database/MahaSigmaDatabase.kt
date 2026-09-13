package com.example.data.database

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.sqlite.db.SupportSQLiteDatabase
import com.example.data.dao.MahaSigmaDao
import com.example.data.entity.CourseEntity
import com.example.data.entity.NoteEntity
import com.example.data.entity.ScheduleEntity
import com.example.data.entity.ScheduleOverrideEntity
import com.example.data.entity.TaskEntity
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

@Database(
    entities = [
        CourseEntity::class,
        ScheduleEntity::class,
        ScheduleOverrideEntity::class,
        TaskEntity::class,
        NoteEntity::class
    ],
    version = 1,
    exportSchema = false
)
abstract class MahaSigmaDatabase : RoomDatabase() {

    abstract fun mahaSigmaDao(): MahaSigmaDao

    companion object {
        @Volatile
        private var INSTANCE: MahaSigmaDatabase? = null

        fun getDatabase(context: Context): MahaSigmaDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    MahaSigmaDatabase::class.java,
                    "mahasigma_database.db"
                )
                    .addCallback(DatabaseCallback())
                    .fallbackToDestructiveMigration()
                    .build()
                INSTANCE = instance
                instance
            }
        }

        private class DatabaseCallback : RoomDatabase.Callback() {
            override fun onCreate(db: SupportSQLiteDatabase) {
                super.onCreate(db)
                INSTANCE?.let { database ->
                    CoroutineScope(Dispatchers.IO).launch {
                        populateInitialData(database.mahaSigmaDao())
                    }
                }
            }

            private suspend fun populateInitialData(dao: MahaSigmaDao) {
                // Initial university courses
                val c1 = dao.insertCourse(
                    CourseEntity(
                        name = "Struktur Data & Algoritma",
                        code = "IF201",
                        lecturer = "Dr. Ir. Hendra Prasetyo, M.T.",
                        colorHex = "#38BDF8"
                    )
                ).toInt()

                val c2 = dao.insertCourse(
                    CourseEntity(
                        name = "Basis Data Lanjut",
                        code = "IF203",
                        lecturer = "Prof. Siti Rahmawati, Ph.D.",
                        colorHex = "#10B981"
                    )
                ).toInt()

                val c3 = dao.insertCourse(
                    CourseEntity(
                        name = "Rekayasa Perangkat Lunak",
                        code = "IF205",
                        lecturer = "Budi Hartono, S.Kom., M.Cs.",
                        colorHex = "#F59E0B"
                    )
                ).toInt()

                val c4 = dao.insertCourse(
                    CourseEntity(
                        name = "Kecerdasan Buatan (AI)",
                        code = "IF302",
                        lecturer = "Dr. Maya Indriani, M.Sc.",
                        colorHex = "#A855F7"
                    )
                ).toInt()

                // Schedules (1=Senin, 2=Selasa, 3=Rabu, 4=Kamis, 5=Jumat)
                dao.insertSchedule(
                    ScheduleEntity(
                        courseId = c1,
                        dayOfWeek = 1,
                        startTime = "08:00",
                        endTime = "10:30",
                        room = "Lab Komputer 3"
                    )
                )

                dao.insertSchedule(
                    ScheduleEntity(
                        courseId = c2,
                        dayOfWeek = 2,
                        startTime = "10:45",
                        endTime = "13:15",
                        room = "Gedung D R.204"
                    )
                )

                dao.insertSchedule(
                    ScheduleEntity(
                        courseId = c3,
                        dayOfWeek = 3,
                        startTime = "13:30",
                        endTime = "16:00",
                        room = "Aula Teater 1"
                    )
                )

                dao.insertSchedule(
                    ScheduleEntity(
                        courseId = c4,
                        dayOfWeek = 4,
                        startTime = "08:00",
                        endTime = "10:30",
                        room = "Lab AI Lt.4"
                    )
                )

                // Tasks (sample upcoming deadlines)
                val now = System.currentTimeMillis()
                val oneDay = 24 * 60 * 60 * 1000L
                dao.insertTask(
                    TaskEntity(
                        courseId = c1,
                        title = "Implementasi Binary Search Tree",
                        description = "Kerjakan latihan modul 4, buat file .cpp dan analisis time complexity O(log n).",
                        submissionLink = "https://classroom.google.com",
                        dueDate = now + (oneDay * 2), // 2 hari lagi
                        isCompleted = false,
                        priority = "TINGGI",
                        reminderType = "1_DAY_BEFORE"
                    )
                )

                dao.insertTask(
                    TaskEntity(
                        courseId = c2,
                        title = "Desain Normalisasi 3NF Database Kampus",
                        description = "Kumpulkan ERD revisi beserta script DDL SQL ke portal e-learning.",
                        submissionLink = "https://kuliah.ac.id/submission",
                        dueDate = now + (oneDay * 4), // 4 hari lagi
                        isCompleted = false,
                        priority = "SEDANG",
                        reminderType = "1_DAY_BEFORE"
                    )
                )

                // Note
                dao.insertNote(
                    NoteEntity(
                        courseId = c1,
                        title = "Rangkuman Teori Red-Black Tree",
                        content = "1. Setiap node merah atau hitam.\n2. Root selalu hitam.\n3. Tidak boleh ada dua node merah berurutan (Red property).\n4. Black height harus konsisten di setiap path daun.",
                        localImagePath = null,
                        createdAt = now,
                        updatedAt = now
                    )
                )
            }
        }
    }
}
