package com.example.data.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Transaction
import androidx.room.Update
import com.example.data.entity.CourseEntity
import com.example.data.entity.NoteEntity
import com.example.data.entity.NoteWithCourse
import com.example.data.entity.ScheduleEntity
import com.example.data.entity.ScheduleOverrideEntity
import com.example.data.entity.ScheduleWithDetails
import com.example.data.entity.TaskEntity
import com.example.data.entity.TaskWithCourse
import kotlinx.coroutines.flow.Flow

@Dao
interface MahaSigmaDao {

    // --- Courses ---
    @Query("SELECT * FROM courses ORDER BY name ASC")
    fun getAllCourses(): Flow<List<CourseEntity>>

    @Query("SELECT * FROM courses WHERE id = :id LIMIT 1")
    suspend fun getCourseById(id: Int): CourseEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertCourse(course: CourseEntity): Long

    @Update
    suspend fun updateCourse(course: CourseEntity)

    @Delete
    suspend fun deleteCourse(course: CourseEntity)

    // --- Schedules ---
    @Transaction
    @Query("SELECT * FROM schedules ORDER BY dayOfWeek ASC, startTime ASC")
    fun getAllSchedulesWithDetails(): Flow<List<ScheduleWithDetails>>

    @Transaction
    @Query("SELECT * FROM schedules WHERE dayOfWeek = :dayOfWeek ORDER BY startTime ASC")
    fun getSchedulesByDay(dayOfWeek: Int): Flow<List<ScheduleWithDetails>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertSchedule(schedule: ScheduleEntity): Long

    @Update
    suspend fun updateSchedule(schedule: ScheduleEntity)

    @Delete
    suspend fun deleteSchedule(schedule: ScheduleEntity)

    // --- Schedule Overrides ---
    @Query("SELECT * FROM schedule_overrides ORDER BY targetDate ASC")
    fun getAllOverrides(): Flow<List<ScheduleOverrideEntity>>

    @Query("SELECT * FROM schedule_overrides WHERE scheduleId = :scheduleId AND targetDate = :targetDate LIMIT 1")
    suspend fun getOverrideForDate(scheduleId: Int, targetDate: Long): ScheduleOverrideEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertOverride(override: ScheduleOverrideEntity): Long

    @Delete
    suspend fun deleteOverride(override: ScheduleOverrideEntity)

    @Query("DELETE FROM schedule_overrides WHERE scheduleId = :scheduleId AND targetDate = :targetDate")
    suspend fun deleteOverrideForDate(scheduleId: Int, targetDate: Long)

    // --- Tasks ---
    @Transaction
    @Query("SELECT * FROM tasks ORDER BY isCompleted ASC, dueDate ASC")
    fun getAllTasksWithCourses(): Flow<List<TaskWithCourse>>

    @Transaction
    @Query("SELECT * FROM tasks WHERE isCompleted = 0 ORDER BY dueDate ASC")
    fun getActiveTasksWithCourses(): Flow<List<TaskWithCourse>>

    @Transaction
    @Query("SELECT * FROM tasks WHERE isCompleted = 0 AND dueDate <= :thresholdMillis ORDER BY dueDate ASC")
    fun getUpcomingTasks(thresholdMillis: Long): Flow<List<TaskWithCourse>>

    @Query("SELECT * FROM tasks WHERE id = :id LIMIT 1")
    suspend fun getTaskById(id: Int): TaskEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertTask(task: TaskEntity): Long

    @Update
    suspend fun updateTask(task: TaskEntity)

    @Query("UPDATE tasks SET isCompleted = :isCompleted WHERE id = :id")
    suspend fun updateTaskStatus(id: Int, isCompleted: Boolean)

    @Delete
    suspend fun deleteTask(task: TaskEntity)

    // --- Notes ---
    @Transaction
    @Query("SELECT * FROM notes ORDER BY updatedAt DESC")
    fun getAllNotesWithCourses(): Flow<List<NoteWithCourse>>

    @Transaction
    @Query("SELECT * FROM notes WHERE courseId = :courseId ORDER BY updatedAt DESC")
    fun getNotesByCourse(courseId: Int): Flow<List<NoteWithCourse>>

    @Query("SELECT * FROM notes WHERE id = :id LIMIT 1")
    suspend fun getNoteById(id: Int): NoteEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertNote(note: NoteEntity): Long

    @Update
    suspend fun updateNote(note: NoteEntity)

    @Delete
    suspend fun deleteNote(note: NoteEntity)

    // --- Bulk Backup & Restore ---
    @Query("SELECT * FROM courses")
    suspend fun getAllCoursesSnapshot(): List<CourseEntity>

    @Query("SELECT * FROM schedules")
    suspend fun getAllSchedulesSnapshot(): List<ScheduleEntity>

    @Query("SELECT * FROM schedule_overrides")
    suspend fun getAllOverridesSnapshot(): List<ScheduleOverrideEntity>

    @Query("SELECT * FROM tasks")
    suspend fun getAllTasksSnapshot(): List<TaskEntity>

    @Query("SELECT * FROM notes")
    suspend fun getAllNotesSnapshot(): List<NoteEntity>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAllCourses(courses: List<CourseEntity>)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAllSchedules(schedules: List<ScheduleEntity>)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAllOverrides(overrides: List<ScheduleOverrideEntity>)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAllTasks(tasks: List<TaskEntity>)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAllNotes(notes: List<NoteEntity>)

    @Query("DELETE FROM schedule_overrides")
    suspend fun clearOverrides()

    @Query("DELETE FROM tasks")
    suspend fun clearTasks()

    @Query("DELETE FROM notes")
    suspend fun clearNotes()

    @Query("DELETE FROM schedules")
    suspend fun clearSchedules()

    @Query("DELETE FROM courses")
    suspend fun clearCourses()

    @Transaction
    suspend fun clearAllData() {
        clearOverrides()
        clearTasks()
        clearNotes()
        clearSchedules()
        clearCourses()
    }
}
