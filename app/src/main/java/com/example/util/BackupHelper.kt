package com.example.util

import com.example.data.entity.CourseEntity
import com.example.data.entity.NoteEntity
import com.example.data.entity.ScheduleEntity
import com.example.data.entity.ScheduleOverrideEntity
import com.example.data.entity.TaskEntity
import com.squareup.moshi.JsonClass
import com.squareup.moshi.Moshi
import com.squareup.moshi.kotlin.reflect.KotlinJsonAdapterFactory

@JsonClass(generateAdapter = true)
data class MahaSigmaBackup(
    val version: Int = 1,
    val exportedAt: Long = System.currentTimeMillis(),
    val courses: List<CourseEntity> = emptyList(),
    val schedules: List<ScheduleEntity> = emptyList(),
    val scheduleOverrides: List<ScheduleOverrideEntity> = emptyList(),
    val tasks: List<TaskEntity> = emptyList(),
    val notes: List<NoteEntity> = emptyList()
)

object BackupHelper {

    private val moshi: Moshi by lazy {
        Moshi.Builder()
            .addLast(KotlinJsonAdapterFactory())
            .build()
    }

    private val adapter by lazy {
        moshi.adapter(MahaSigmaBackup::class.java).indent("  ")
    }

    fun exportToJson(backup: MahaSigmaBackup): String {
        return adapter.toJson(backup)
    }

    fun parseFromJson(jsonString: String): MahaSigmaBackup? {
        return try {
            adapter.fromJson(jsonString)
        } catch (e: Exception) {
            e.printStackTrace()
            null
        }
    }
}
