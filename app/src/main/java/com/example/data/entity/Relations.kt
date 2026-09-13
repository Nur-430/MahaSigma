package com.example.data.entity

import androidx.room.Embedded
import androidx.room.Relation

data class TaskWithCourse(
    @Embedded val task: TaskEntity,
    @Relation(
        parentColumn = "courseId",
        entityColumn = "id"
    )
    val course: CourseEntity?
)

data class ScheduleWithDetails(
    @Embedded val schedule: ScheduleEntity,
    @Relation(
        parentColumn = "courseId",
        entityColumn = "id"
    )
    val course: CourseEntity?,
    @Relation(
        parentColumn = "id",
        entityColumn = "scheduleId"
    )
    val overrides: List<ScheduleOverrideEntity> = emptyList()
)

data class NoteWithCourse(
    @Embedded val note: NoteEntity,
    @Relation(
        parentColumn = "courseId",
        entityColumn = "id"
    )
    val course: CourseEntity?
)
