package com.example.ui.viewmodel

import android.app.Application
import android.graphics.Bitmap
import android.net.Uri
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.database.MahaSigmaDatabase
import com.example.data.entity.CourseEntity
import com.example.data.entity.NoteEntity
import com.example.data.entity.NoteWithCourse
import com.example.data.entity.ScheduleEntity
import com.example.data.entity.ScheduleOverrideEntity
import com.example.data.entity.ScheduleWithDetails
import com.example.data.entity.TaskEntity
import com.example.data.entity.TaskWithCourse
import com.example.data.repository.MahaSigmaRepository
import com.example.util.BackupHelper
import com.example.util.DateUtils
import com.example.util.ImageHelper
import com.example.util.MahaSigmaBackup
import com.example.util.ParsedScheduleItem
import com.example.util.PdfScheduleParser
import com.example.util.ReminderHelper
import com.example.util.TableScheduleParser
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

enum class TaskFilter {
    SEMUA,
    AKTIF,
    SELESAI
}

class MahaSigmaViewModel(application: Application) : AndroidViewModel(application) {

    private val repository: MahaSigmaRepository
    private val context = application.applicationContext

    init {
        val db = MahaSigmaDatabase.getDatabase(application)
        repository = MahaSigmaRepository(db.mahaSigmaDao())
    }

    // UI Feedback Event
    private val _userMessage = MutableSharedFlow<String>()
    val userMessage: SharedFlow<String> = _userMessage.asSharedFlow()

    // --- Courses ---
    val courses: StateFlow<List<CourseEntity>> = repository.allCourses
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    // --- Schedules ---
    val allSchedules: StateFlow<List<ScheduleWithDetails>> = repository.allSchedules
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val allOverrides: StateFlow<List<ScheduleOverrideEntity>> = repository.allOverrides
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    private val _selectedDayOfWeek = MutableStateFlow(DateUtils.getCurrentDayOfWeek().coerceIn(1, 5))
    val selectedDayOfWeek: StateFlow<Int> = _selectedDayOfWeek.asStateFlow()

    fun setSelectedDayOfWeek(day: Int) {
        _selectedDayOfWeek.value = day.coerceIn(1, 5)
    }

    // Schedules filtered by selected day
    val schedulesForSelectedDay: StateFlow<List<ScheduleWithDetails>> = combine(
        allSchedules,
        selectedDayOfWeek
    ) { schedules, day ->
        schedules.filter { it.schedule.dayOfWeek == day }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    // Today's schedules for Dashboard
    val todaySchedules: StateFlow<List<ScheduleWithDetails>> = allSchedules.combine(
        MutableStateFlow(DateUtils.getCurrentDayOfWeek())
    ) { schedules, currentDay ->
        schedules.filter { it.schedule.dayOfWeek == currentDay }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    // --- Tasks ---
    val allTasks: StateFlow<List<TaskWithCourse>> = repository.allTasks
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    private val _taskFilter = MutableStateFlow(TaskFilter.SEMUA)
    val taskFilter: StateFlow<TaskFilter> = _taskFilter.asStateFlow()

    fun setTaskFilter(filter: TaskFilter) {
        _taskFilter.value = filter
    }

    val filteredTasks: StateFlow<List<TaskWithCourse>> = combine(
        allTasks,
        taskFilter
    ) { tasks, filter ->
        when (filter) {
            TaskFilter.SEMUA -> tasks
            TaskFilter.AKTIF -> tasks.filter { !it.task.isCompleted }
            TaskFilter.SELESAI -> tasks.filter { it.task.isCompleted }
        }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    // Upcoming tasks for Dashboard (< 3 hari & belum selesai)
    val upcomingTasks: StateFlow<List<TaskWithCourse>> = allTasks.combine(
        MutableStateFlow(System.currentTimeMillis())
    ) { tasks, now ->
        val threeDaysMillis = 3 * 24 * 60 * 60 * 1000L
        tasks.filter { !it.task.isCompleted && (it.task.dueDate - now) <= threeDaysMillis }
            .sortedBy { it.task.dueDate }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    // --- Notes ---
    val allNotes: StateFlow<List<NoteWithCourse>> = repository.allNotes
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    private val _selectedNoteCourseFilter = MutableStateFlow<Int?>(null)
    val selectedNoteCourseFilter: StateFlow<Int?> = _selectedNoteCourseFilter.asStateFlow()

    fun setSelectedNoteCourseFilter(courseId: Int?) {
        _selectedNoteCourseFilter.value = courseId
    }

    val filteredNotes: StateFlow<List<NoteWithCourse>> = combine(
        allNotes,
        selectedNoteCourseFilter
    ) { notes, courseId ->
        if (courseId == null) notes
        else notes.filter { it.note.courseId == courseId }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    // --- Course Operations ---
    fun saveCourse(course: CourseEntity) {
        viewModelScope.launch {
            if (course.id == 0) {
                repository.insertCourse(course)
                _userMessage.emit("Mata kuliah '${course.name}' berhasil ditambahkan")
            } else {
                repository.updateCourse(course)
                _userMessage.emit("Mata kuliah '${course.name}' berhasil diperbarui")
            }
        }
    }

    fun deleteCourse(course: CourseEntity) {
        viewModelScope.launch {
            repository.deleteCourse(course)
            _userMessage.emit("Mata kuliah '${course.name}' telah dihapus")
        }
    }

    // --- Schedule Operations ---
    fun saveSchedule(schedule: ScheduleEntity) {
        viewModelScope.launch {
            if (schedule.id == 0) {
                repository.insertSchedule(schedule)
                _userMessage.emit("Jadwal kuliah berhasil disimpan")
            } else {
                repository.updateSchedule(schedule)
                _userMessage.emit("Jadwal kuliah berhasil diperbarui")
            }
        }
    }

    fun deleteSchedule(schedule: ScheduleEntity) {
        viewModelScope.launch {
            repository.deleteSchedule(schedule)
            _userMessage.emit("Jadwal telah dihapus")
        }
    }

    // --- Schedule Override Operations ---
    fun saveScheduleOverride(override: ScheduleOverrideEntity) {
        viewModelScope.launch {
            repository.insertOverride(override)
            _userMessage.emit("Status khusus jadwal berhasil disimpan (${override.status})")
        }
    }

    fun deleteScheduleOverride(override: ScheduleOverrideEntity) {
        viewModelScope.launch {
            repository.deleteOverride(override)
            _userMessage.emit("Override jadwal dibatalkan")
        }
    }

    // --- Task Operations ---
    fun saveTask(task: TaskEntity, courseName: String = "Kuliah") {
        viewModelScope.launch {
            val insertedId = if (task.id == 0) {
                repository.insertTask(task).toInt()
            } else {
                repository.updateTask(task)
                task.id
            }

            // Handle Alarm Reminder
            if (!task.isCompleted && task.reminderType != "NONE") {
                val triggerTime = ReminderHelper.calculateTriggerTime(task.dueDate, task.reminderType)
                if (triggerTime != null) {
                    ReminderHelper.scheduleTaskReminder(
                        context,
                        insertedId,
                        task.title,
                        courseName,
                        triggerTime
                    )
                }
            } else {
                ReminderHelper.cancelTaskReminder(context, insertedId)
            }

            _userMessage.emit("Tugas '${task.title}' berhasil disimpan")
        }
    }

    fun toggleTaskCompletion(task: TaskEntity) {
        viewModelScope.launch {
            val newStatus = !task.isCompleted
            repository.updateTaskStatus(task.id, newStatus)
            if (newStatus) {
                ReminderHelper.cancelTaskReminder(context, task.id)
                _userMessage.emit("Tugas selesai! 🎉")
            } else {
                _userMessage.emit("Tugas ditandai aktif kembali")
            }
        }
    }

    fun deleteTask(task: TaskEntity) {
        viewModelScope.launch {
            ReminderHelper.cancelTaskReminder(context, task.id)
            repository.deleteTask(task)
            _userMessage.emit("Tugas '${task.title}' telah dihapus")
        }
    }

    // --- Note Operations ---
    fun saveNote(
        id: Int,
        courseId: Int?,
        title: String,
        content: String,
        imageUri: Uri?,
        enhanceImage: Boolean,
        existingImagePath: String?
    ) {
        viewModelScope.launch(Dispatchers.IO) {
            var finalImagePath = existingImagePath

            if (imageUri != null) {
                val rawBitmap = ImageHelper.loadBitmapFromUri(context, imageUri)
                if (rawBitmap != null) {
                    val processedBitmap = if (enhanceImage) {
                        ImageHelper.enhanceBoardImage(rawBitmap)
                    } else {
                        rawBitmap
                    }
                    finalImagePath = ImageHelper.saveBitmapToInternalStorage(context, processedBitmap)
                }
            }

            val note = NoteEntity(
                id = id,
                courseId = courseId,
                title = title,
                content = content,
                localImagePath = finalImagePath,
                createdAt = if (id == 0) System.currentTimeMillis() else System.currentTimeMillis(),
                updatedAt = System.currentTimeMillis()
            )

            if (id == 0) {
                repository.insertNote(note)
                _userMessage.emit("Catatan baru berhasil disimpan")
            } else {
                repository.updateNote(note)
                _userMessage.emit("Catatan berhasil diperbarui")
            }
        }
    }

    fun deleteNote(note: NoteEntity) {
        viewModelScope.launch(Dispatchers.IO) {
            ImageHelper.deleteFile(note.localImagePath)
            repository.deleteNote(note)
            _userMessage.emit("Catatan '${note.title}' telah dihapus")
        }
    }

    // --- Backup & Restore Operations ---
    suspend fun createBackupJson(): String = withContext(Dispatchers.IO) {
        val courses = repository.getAllCoursesSnapshot()
        val schedules = repository.getAllSchedulesSnapshot()
        val overrides = repository.getAllOverridesSnapshot()
        val tasks = repository.getAllTasksSnapshot()
        val notes = repository.getAllNotesSnapshot()

        val backup = MahaSigmaBackup(
            version = 1,
            exportedAt = System.currentTimeMillis(),
            courses = courses,
            schedules = schedules,
            scheduleOverrides = overrides,
            tasks = tasks,
            notes = notes
        )
        BackupHelper.exportToJson(backup)
    }

    fun restoreFromJson(jsonString: String, onResult: (Boolean, String) -> Unit) {
        viewModelScope.launch(Dispatchers.IO) {
            val backup = BackupHelper.parseFromJson(jsonString)
            if (backup == null) {
                withContext(Dispatchers.Main) {
                    onResult(false, "Format JSON tidak valid atau rusak.")
                }
                return@launch
            }

            try {
                repository.restoreDatabase(
                    courses = backup.courses,
                    schedules = backup.schedules,
                    overrides = backup.scheduleOverrides,
                    tasks = backup.tasks,
                    notes = backup.notes
                )
                withContext(Dispatchers.Main) {
                    onResult(
                        true,
                        "Berhasil memulihkan ${backup.courses.size} mata kuliah, ${backup.schedules.size} jadwal, ${backup.tasks.size} tugas, dan ${backup.notes.size} catatan!"
                    )
                }
            } catch (e: Exception) {
                withContext(Dispatchers.Main) {
                    onResult(false, "Gagal memulihkan database: ${e.localizedMessage}")
                }
            }
        }
    }

    fun resetAllData(onComplete: () -> Unit) {
        viewModelScope.launch(Dispatchers.IO) {
            repository.clearAllData()
            withContext(Dispatchers.Main) {
                _userMessage.emit("Seluruh data berhasil direset")
                onComplete()
            }
        }
    }

    fun importParsedSchedules(items: List<ParsedScheduleItem>, onComplete: (Int) -> Unit) {
        viewModelScope.launch(Dispatchers.IO) {
            val selected = items.filter { it.isSelected }
            if (selected.isEmpty()) {
                withContext(Dispatchers.Main) {
                    onComplete(0)
                }
                return@launch
            }

            val existingCourses = repository.getAllCoursesSnapshot().toMutableList()
            var importedCount = 0

            for (item in selected) {
                var course = existingCourses.find {
                    (item.courseCode.isNotBlank() && it.code.equals(item.courseCode, ignoreCase = true)) ||
                    it.name.equals(item.courseName, ignoreCase = true)
                }

                val courseId = if (course != null) {
                    val updatedCourse = course.copy(
                        name = item.courseName.trim(),
                        code = item.courseCode.trim().ifBlank { course.code },
                        lecturer = item.lecturer.trim().ifBlank { course.lecturer }
                    )
                    repository.updateCourse(updatedCourse)
                    course.id
                } else {
                    val newCourse = CourseEntity(
                        name = item.courseName.trim(),
                        code = item.courseCode.trim(),
                        lecturer = item.lecturer.trim(),
                        colorHex = item.colorHex
                    )
                    val newId = repository.insertCourse(newCourse).toInt()
                    val created = newCourse.copy(id = newId)
                    existingCourses.add(created)
                    newId
                }

                val schedule = ScheduleEntity(
                    courseId = courseId,
                    dayOfWeek = item.dayOfWeek,
                    startTime = item.startTime,
                    endTime = item.endTime,
                    room = item.room.trim()
                )
                repository.insertSchedule(schedule)
                importedCount++
            }

            withContext(Dispatchers.Main) {
                _userMessage.emit("Berhasil mengimpor $importedCount jadwal kuliah!")
                onComplete(importedCount)
            }
        }
    }

    // --- FITUR IMPOR TABEL JADWAL PDF DI BELAKANG LAYAR ---
    private val _importState = MutableStateFlow(BackgroundImportState())
    val importState: StateFlow<BackgroundImportState> = _importState.asStateFlow()

    fun startBackgroundTableImport(context: android.content.Context, uri: Uri) {
        var name = "jadwal.pdf"
        try {
            val cursor = context.contentResolver.query(uri, arrayOf(android.provider.OpenableColumns.DISPLAY_NAME), null, null, null)
            cursor?.use {
                if (it.moveToFirst()) {
                    val idx = it.getColumnIndex(android.provider.OpenableColumns.DISPLAY_NAME)
                    if (idx != -1) name = it.getString(idx)
                }
            }
        } catch (_: Exception) {}

        _importState.value = BackgroundImportState(
            isProcessing = true,
            fileName = name,
            items = emptyList(),
            errorMessage = null,
            isDialogVisible = true
        )

        viewModelScope.launch(Dispatchers.IO) {
            val lower = name.lowercase(java.util.Locale.ROOT)
            try {
                val parsedItems: List<ParsedScheduleItem> = when {
                    lower.endsWith(".xlsx") -> {
                        TableScheduleParser.parseXlsxFromUri(context, uri).getOrThrow()
                    }
                    lower.endsWith(".csv") || lower.endsWith(".tsv") -> {
                        TableScheduleParser.parseCsvFromUri(context, uri).getOrThrow()
                    }
                    else -> {
                        // PDF format: extract text and parse table
                        val extractResult = PdfScheduleParser.extractTextFromPdf(context, uri)
                        if (extractResult.isSuccess) {
                            PdfScheduleParser.parseScheduleText(extractResult.getOrThrow())
                        } else {
                            throw extractResult.exceptionOrNull() ?: Exception("Gagal mengekstrak teks dari PDF")
                        }
                    }
                }

                withContext(Dispatchers.Main) {
                    if (parsedItems.isEmpty()) {
                        _importState.value = _importState.value.copy(
                            isProcessing = false,
                            items = emptyList(),
                            errorMessage = "Tidak ada tabel jadwal kuliah yang terdeteksi pada berkas \"$name\". Pastikan berkas adalah PDF KRS atau Spreadsheet jadwal yang memuat tabel mata kuliah."
                        )
                    } else {
                        _importState.value = _importState.value.copy(
                            isProcessing = false,
                            items = parsedItems,
                            errorMessage = null
                        )
                    }
                }
            } catch (e: Exception) {
                withContext(Dispatchers.Main) {
                    _importState.value = _importState.value.copy(
                        isProcessing = false,
                        items = emptyList(),
                        errorMessage = "Gagal memproses tabel berkas: ${e.localizedMessage ?: "Terjadi kesalahan"}"
                    )
                }
            }
        }
    }

    fun toggleImportItemSelection(index: Int, isSelected: Boolean) {
        val current = _importState.value.items.toMutableList()
        if (index in current.indices) {
            current[index] = current[index].copy(isSelected = isSelected)
            _importState.value = _importState.value.copy(items = current)
        }
    }

    fun toggleSelectAllImportItems(selectAll: Boolean) {
        val updated = _importState.value.items.map { it.copy(isSelected = selectAll) }
        _importState.value = _importState.value.copy(items = updated)
    }

    fun dismissImportDialog() {
        _importState.value = BackgroundImportState()
    }

    fun applyImportedSchedules() {
        val selected = _importState.value.items.filter { it.isSelected }
        if (selected.isNotEmpty()) {
            importParsedSchedules(selected) {
                dismissImportDialog()
            }
        } else {
            dismissImportDialog()
        }
    }
}

data class BackgroundImportState(
    val isProcessing: Boolean = false,
    val fileName: String = "",
    val items: List<ParsedScheduleItem> = emptyList(),
    val errorMessage: String? = null,
    val isDialogVisible: Boolean = false
)
