package com.example.data.entity

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.PrimaryKey
import com.squareup.moshi.JsonClass

@JsonClass(generateAdapter = true)
@Entity(tableName = "courses")
data class CourseEntity(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val name: String,
    val code: String,
    val lecturer: String,
    val colorHex: String
)

@JsonClass(generateAdapter = true)
@Entity(
    tableName = "schedules",
    foreignKeys = [
        ForeignKey(
            entity = CourseEntity::class,
            parentColumns = ["id"],
            childColumns = ["courseId"],
            onDelete = ForeignKey.CASCADE
        )
    ]
)
data class ScheduleEntity(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val courseId: Int,
    val dayOfWeek: Int, // 1 = Senin, 2 = Selasa, ... 7 = Minggu
    val startTime: String, // "08:00"
    val endTime: String,   // "10:30"
    val room: String       // "R. 302", "Lab Jaringan"
)

@JsonClass(generateAdapter = true)
@Entity(
    tableName = "schedule_overrides",
    foreignKeys = [
        ForeignKey(
            entity = ScheduleEntity::class,
            parentColumns = ["id"],
            childColumns = ["scheduleId"],
            onDelete = ForeignKey.CASCADE
        )
    ]
)
data class ScheduleOverrideEntity(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val scheduleId: Int,
    val targetDate: Long, // Epoch day (millis or day start)
    val status: String,   // CANCELED, CHANGED_TIME, CHANGED_ROOM, ONLINE
    val newStartTime: String? = null,
    val newEndTime: String? = null,
    val newRoom: String? = null,
    val note: String? = null
)

@JsonClass(generateAdapter = true)
@Entity(
    tableName = "tasks",
    foreignKeys = [
        ForeignKey(
            entity = CourseEntity::class,
            parentColumns = ["id"],
            childColumns = ["courseId"],
            onDelete = ForeignKey.SET_NULL
        )
    ]
)
data class TaskEntity(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val courseId: Int? = null,
    val title: String,
    val description: String = "",
    val submissionLink: String? = null,
    val dueDate: Long,
    val isCompleted: Boolean = false,
    val priority: String = "SEDANG", // TINGGI, SEDANG, RENDAH
    val reminderType: String = "1_DAY_BEFORE" // NONE, 1_HOUR_BEFORE, 1_DAY_BEFORE
)

@JsonClass(generateAdapter = true)
@Entity(
    tableName = "notes",
    foreignKeys = [
        ForeignKey(
            entity = CourseEntity::class,
            parentColumns = ["id"],
            childColumns = ["courseId"],
            onDelete = ForeignKey.SET_NULL
        )
    ]
)
data class NoteEntity(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val courseId: Int? = null,
    val title: String,
    val content: String,
    val localImagePath: String? = null,
    val createdAt: Long = System.currentTimeMillis(),
    val updatedAt: Long = System.currentTimeMillis()
)
