package com.example.data.repository

import com.example.data.dao.MahaSigmaDao
import com.example.data.entity.CourseEntity
import com.example.data.entity.NoteEntity
import com.example.data.entity.NoteWithCourse
import com.example.data.entity.ScheduleEntity
import com.example.data.entity.ScheduleOverrideEntity
import com.example.data.entity.ScheduleWithDetails
import com.example.data.entity.TaskEntity
import com.example.data.entity.TaskWithCourse
import kotlinx.coroutines.flow.Flow

class MahaSigmaRepository(private val dao: MahaSigmaDao) {

    // Courses
    val allCourses: Flow<List<CourseEntity>> = dao.getAllCourses()
    suspend fun insertCourse(course: CourseEntity): Long = dao.insertCourse(course)
    suspend fun updateCourse(course: CourseEntity) = dao.updateCourse(course)
    suspend fun deleteCourse(course: CourseEntity) = dao.deleteCourse(course)
    suspend fun getCourseById(id: Int): CourseEntity? = dao.getCourseById(id)

    // Schedules
    val allSchedules: Flow<List<ScheduleWithDetails>> = dao.getAllSchedulesWithDetails()
    fun getSchedulesByDay(dayOfWeek: Int): Flow<List<ScheduleWithDetails>> = dao.getSchedulesByDay(dayOfWeek)
    suspend fun insertSchedule(schedule: ScheduleEntity): Long = dao.insertSchedule(schedule)
    suspend fun updateSchedule(schedule: ScheduleEntity) = dao.updateSchedule(schedule)
    suspend fun deleteSchedule(schedule: ScheduleEntity) = dao.deleteSchedule(schedule)

    // Overrides
    val allOverrides: Flow<List<ScheduleOverrideEntity>> = dao.getAllOverrides()
    suspend fun insertOverride(override: ScheduleOverrideEntity): Long = dao.insertOverride(override)
    suspend fun deleteOverride(override: ScheduleOverrideEntity) = dao.deleteOverride(override)
    suspend fun deleteOverrideForDate(scheduleId: Int, targetDate: Long) = dao.deleteOverrideForDate(scheduleId, targetDate)

    // Tasks
    val allTasks: Flow<List<TaskWithCourse>> = dao.getAllTasksWithCourses()
    val activeTasks: Flow<List<TaskWithCourse>> = dao.getActiveTasksWithCourses()
    fun getUpcomingTasks(thresholdMillis: Long): Flow<List<TaskWithCourse>> = dao.getUpcomingTasks(thresholdMillis)
    suspend fun insertTask(task: TaskEntity): Long = dao.insertTask(task)
    suspend fun updateTask(task: TaskEntity) = dao.updateTask(task)
    suspend fun updateTaskStatus(id: Int, isCompleted: Boolean) = dao.updateTaskStatus(id, isCompleted)
    suspend fun deleteTask(task: TaskEntity) = dao.deleteTask(task)
    suspend fun getTaskById(id: Int): TaskEntity? = dao.getTaskById(id)

    // Notes
    val allNotes: Flow<List<NoteWithCourse>> = dao.getAllNotesWithCourses()
    fun getNotesByCourse(courseId: Int): Flow<List<NoteWithCourse>> = dao.getNotesByCourse(courseId)
    suspend fun insertNote(note: NoteEntity): Long = dao.insertNote(note)
    suspend fun updateNote(note: NoteEntity) = dao.updateNote(note)
    suspend fun deleteNote(note: NoteEntity) = dao.deleteNote(note)
    suspend fun getNoteById(id: Int): NoteEntity? = dao.getNoteById(id)

    // Backup & Restore
    suspend fun getAllCoursesSnapshot() = dao.getAllCoursesSnapshot()
    suspend fun getAllSchedulesSnapshot() = dao.getAllSchedulesSnapshot()
    suspend fun getAllOverridesSnapshot() = dao.getAllOverridesSnapshot()
    suspend fun getAllTasksSnapshot() = dao.getAllTasksSnapshot()
    suspend fun getAllNotesSnapshot() = dao.getAllNotesSnapshot()

    suspend fun restoreDatabase(
        courses: List<CourseEntity>,
        schedules: List<ScheduleEntity>,
        overrides: List<ScheduleOverrideEntity>,
        tasks: List<TaskEntity>,
        notes: List<NoteEntity>
    ) {
        dao.clearAllData()
        dao.insertAllCourses(courses)
        dao.insertAllSchedules(schedules)
        dao.insertAllOverrides(overrides)
        dao.insertAllTasks(tasks)
        dao.insertAllNotes(notes)
    }

    suspend fun clearAllData() = dao.clearAllData()
}
