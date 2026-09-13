package com.example.ui.screens

import android.app.DatePickerDialog
import android.app.TimePickerDialog
import android.content.Intent
import android.net.Uri
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.OpenInNew
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Alarm
import androidx.compose.material.icons.filled.Assignment
import androidx.compose.material.icons.filled.CalendarMonth
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.FilterList
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Checkbox
import androidx.compose.material3.CheckboxDefaults
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MenuAnchorType
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.data.entity.CourseEntity
import com.example.data.entity.TaskEntity
import com.example.data.entity.TaskWithCourse
import com.example.ui.components.CourseTag
import com.example.ui.components.PriorityBadge
import com.example.ui.theme.AlabasterGrey
import com.example.ui.theme.CrimsonRed
import com.example.ui.theme.DuskBlue
import com.example.ui.theme.DustyDenim
import com.example.ui.theme.ElectricCyan
import com.example.ui.theme.InkBlack
import com.example.ui.theme.NeonEmerald
import com.example.ui.theme.PrussianBlue
import com.example.ui.theme.SunsetAmber
import com.example.ui.theme.SurfaceDarkVariant
import com.example.ui.viewmodel.MahaSigmaViewModel
import com.example.ui.viewmodel.TaskFilter
import com.example.util.DateUtils
import java.util.Calendar

@Composable
fun TasksScreen(
    viewModel: MahaSigmaViewModel,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val currentFilter by viewModel.taskFilter.collectAsStateWithLifecycle()
    val filteredTasks by viewModel.filteredTasks.collectAsStateWithLifecycle()
    val allTasks by viewModel.allTasks.collectAsStateWithLifecycle()
    val courses by viewModel.courses.collectAsStateWithLifecycle()

    var showAddEditDialog by remember { mutableStateOf(false) }
    var taskToEdit by remember { mutableStateOf<TaskEntity?>(null) }
    var taskToDelete by remember { mutableStateOf<TaskEntity?>(null) }

    val activeCount = allTasks.count { !it.task.isCompleted }
    val completedCount = allTasks.count { it.task.isCompleted }

    Scaffold(
        modifier = modifier.fillMaxSize(),
        containerColor = InkBlack,
        floatingActionButton = {
            FloatingActionButton(
                onClick = {
                    taskToEdit = null
                    showAddEditDialog = true
                },
                containerColor = DuskBlue,
                contentColor = AlabasterGrey,
                modifier = Modifier
                    .padding(bottom = 80.dp)
                    .testTag("add_task_fab")
            ) {
                Icon(imageVector = Icons.Default.Add, contentDescription = "Tambah Tugas")
            }
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            // Header
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(PrussianBlue)
                    .border(1.dp, DuskBlue.copy(alpha = 0.4f), RoundedCornerShape(bottomStart = 20.dp, bottomEnd = 20.dp))
                    .padding(16.dp)
            ) {
                Column {
                    Text(
                        text = "Manajemen Tugas & Deadline",
                        color = AlabasterGrey,
                        fontSize = 20.sp,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = "Pantau tenggat waktu, tautan pengumpulan, dan alarm pengingat",
                        color = DustyDenim,
                        fontSize = 12.sp
                    )

                    Spacer(modifier = Modifier.height(14.dp))

                    // Filter Pills (Semua, Aktif, Selesai)
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        TaskFilterChip(
                            label = "Semua (${allTasks.size})",
                            isSelected = currentFilter == TaskFilter.SEMUA,
                            onClick = { viewModel.setTaskFilter(TaskFilter.SEMUA) },
                            modifier = Modifier.weight(1f)
                        )
                        TaskFilterChip(
                            label = "Aktif ($activeCount)",
                            isSelected = currentFilter == TaskFilter.AKTIF,
                            onClick = { viewModel.setTaskFilter(TaskFilter.AKTIF) },
                            modifier = Modifier.weight(1f)
                        )
                        TaskFilterChip(
                            label = "Selesai ($completedCount)",
                            isSelected = currentFilter == TaskFilter.SELESAI,
                            onClick = { viewModel.setTaskFilter(TaskFilter.SELESAI) },
                            modifier = Modifier.weight(1f)
                        )
                    }
                }
            }

            // Tasks List
            if (filteredTasks.isEmpty()) {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(32.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Icon(
                            imageVector = Icons.Default.CheckCircle,
                            contentDescription = null,
                            tint = NeonEmerald,
                            modifier = Modifier.size(56.dp)
                        )
                        Spacer(modifier = Modifier.height(12.dp))
                        Text(
                            text = if (currentFilter == TaskFilter.AKTIF) "Tidak Ada Tugas Aktif!" else "Daftar Tugas Kosong",
                            color = AlabasterGrey,
                            fontSize = 16.sp,
                            fontWeight = FontWeight.Bold
                        )
                        Spacer(modifier = Modifier.height(6.dp))
                        Text(
                            text = if (currentFilter == TaskFilter.AKTIF) {
                                "Semua tugas perkuliahan sudah kamu selesaikan. Luar biasa! 🎉"
                            } else {
                                "Klik tombol '+' di bawah untuk mencatat tugas baru."
                            },
                            color = DustyDenim,
                            fontSize = 13.sp,
                            textAlign = androidx.compose.ui.text.style.TextAlign.Center
                        )
                    }
                }
            } else {
                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    contentPadding = PaddingValues(top = 12.dp, start = 16.dp, end = 16.dp, bottom = 120.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    items(filteredTasks, key = { it.task.id }) { item ->
                        TaskItemCard(
                            taskWithCourse = item,
                            onToggleComplete = { viewModel.toggleTaskCompletion(item.task) },
                            onEditClick = {
                                taskToEdit = item.task
                                showAddEditDialog = true
                            },
                            onDeleteClick = {
                                taskToDelete = item.task
                            },
                            onOpenLink = { url ->
                                try {
                                    val formattedUrl = if (!url.startsWith("http://") && !url.startsWith("https://")) {
                                        "https://$url"
                                    } else url
                                    val intent = Intent(Intent.ACTION_VIEW, Uri.parse(formattedUrl))
                                    context.startActivity(intent)
                                } catch (e: Exception) {
                                    // Fallback
                                }
                            }
                        )
                    }
                }
            }
        }
    }

    // Dialog Tambah / Edit Tugas
    if (showAddEditDialog) {
        AddEditTaskDialog(
            task = taskToEdit,
            courses = courses,
            onDismiss = { showAddEditDialog = false },
            onSave = { savedTask, courseName ->
                viewModel.saveTask(savedTask, courseName)
                showAddEditDialog = false
            }
        )
    }

    // Dialog Konfirmasi Hapus Tugas
    if (taskToDelete != null) {
        AlertDialog(
            onDismissRequest = { taskToDelete = null },
            containerColor = PrussianBlue,
            title = { Text("Hapus Tugas?", color = AlabasterGrey) },
            text = { Text("Tugas '${taskToDelete?.title}' akan dihapus permanen.", color = DustyDenim) },
            confirmButton = {
                Button(
                    onClick = {
                        taskToDelete?.let { viewModel.deleteTask(it) }
                        taskToDelete = null
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = CrimsonRed)
                ) {
                    Text("Hapus", color = Color.White)
                }
            },
            dismissButton = {
                TextButton(onClick = { taskToDelete = null }) {
                    Text("Batal", color = DustyDenim)
                }
            }
        )
    }
}

@Composable
fun TaskFilterChip(
    label: String,
    isSelected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .clip(RoundedCornerShape(10.dp))
            .background(if (isSelected) DuskBlue else SurfaceDarkVariant)
            .border(
                1.dp,
                if (isSelected) ElectricCyan else DuskBlue.copy(alpha = 0.4f),
                RoundedCornerShape(10.dp)
            )
            .clickable { onClick() }
            .padding(vertical = 8.dp),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = label,
            color = if (isSelected) AlabasterGrey else DustyDenim,
            fontSize = 12.sp,
            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium
        )
    }
}

@Composable
fun TaskItemCard(
    taskWithCourse: TaskWithCourse,
    onToggleComplete: () -> Unit,
    onEditClick: () -> Unit,
    onDeleteClick: () -> Unit,
    onOpenLink: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    val task = taskWithCourse.task
    val course = taskWithCourse.course
    var showMenu by remember { mutableStateOf(false) }

    val isOverdue = !task.isCompleted && task.dueDate < System.currentTimeMillis()
    val isDueToday = !task.isCompleted && (task.dueDate - System.currentTimeMillis()) < (24 * 60 * 60 * 1000L)

    Card(
        modifier = modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = if (task.isCompleted) PrussianBlue.copy(alpha = 0.7f) else PrussianBlue
        ),
        shape = RoundedCornerShape(16.dp),
        border = androidx.compose.foundation.BorderStroke(
            1.dp,
            when {
                task.isCompleted -> DuskBlue.copy(alpha = 0.3f)
                isOverdue -> CrimsonRed.copy(alpha = 0.6f)
                isDueToday -> SunsetAmber.copy(alpha = 0.6f)
                else -> DuskBlue.copy(alpha = 0.5f)
            }
        )
    ) {
        Column(modifier = Modifier.padding(14.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.Top
            ) {
                Checkbox(
                    checked = task.isCompleted,
                    onCheckedChange = { onToggleComplete() },
                    colors = CheckboxDefaults.colors(
                        checkedColor = NeonEmerald,
                        uncheckedColor = DustyDenim,
                        checkmarkColor = InkBlack
                    ),
                    modifier = Modifier.testTag("task_check_${task.id}")
                )

                Spacer(modifier = Modifier.width(6.dp))

                Column(modifier = Modifier.weight(1f)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = task.title,
                            color = if (task.isCompleted) DustyDenim else AlabasterGrey,
                            fontSize = 15.sp,
                            fontWeight = FontWeight.Bold,
                            textDecoration = if (task.isCompleted) TextDecoration.LineThrough else null,
                            maxLines = 2,
                            overflow = TextOverflow.Ellipsis,
                            modifier = Modifier.weight(1f)
                        )

                        Row(verticalAlignment = Alignment.CenterVertically) {
                            PriorityBadge(priority = task.priority)
                            Box {
                                IconButton(
                                    onClick = { showMenu = true },
                                    modifier = Modifier.size(32.dp)
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.MoreVert,
                                        contentDescription = "Opsi",
                                        tint = DustyDenim,
                                        modifier = Modifier.size(18.dp)
                                    )
                                }
                                DropdownMenu(
                                    expanded = showMenu,
                                    onDismissRequest = { showMenu = false },
                                    modifier = Modifier.background(SurfaceDarkVariant)
                                ) {
                                    DropdownMenuItem(
                                        text = { Text("Edit Tugas", color = AlabasterGrey) },
                                        onClick = {
                                            showMenu = false
                                            onEditClick()
                                        },
                                        leadingIcon = { Icon(Icons.Default.Edit, contentDescription = null, tint = ElectricCyan) }
                                    )
                                    DropdownMenuItem(
                                        text = { Text("Hapus Tugas", color = CrimsonRed) },
                                        onClick = {
                                            showMenu = false
                                            onDeleteClick()
                                        },
                                        leadingIcon = { Icon(Icons.Default.Delete, contentDescription = null, tint = CrimsonRed) }
                                    )
                                }
                            }
                        }
                    }

                    if (task.description.isNotBlank()) {
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = task.description,
                            color = DustyDenim,
                            fontSize = 12.sp,
                            maxLines = 3,
                            overflow = TextOverflow.Ellipsis
                        )
                    }

                    Spacer(modifier = Modifier.height(8.dp))

                    // Tags & Deadline
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        if (course != null) {
                            CourseTag(name = course.name, code = course.code, colorHex = course.colorHex)
                        } else {
                            Text(text = "Tugas Bebas", color = DustyDenim, fontSize = 11.sp)
                        }

                        // Due date string
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = Icons.Default.CalendarMonth,
                                contentDescription = null,
                                tint = when {
                                    task.isCompleted -> DustyDenim
                                    isOverdue -> CrimsonRed
                                    isDueToday -> SunsetAmber
                                    else -> ElectricCyan
                                },
                                modifier = Modifier.size(13.dp)
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(
                                text = DateUtils.getRelativeDeadline(task.dueDate, task.isCompleted),
                                color = when {
                                    task.isCompleted -> DustyDenim
                                    isOverdue -> CrimsonRed
                                    isDueToday -> SunsetAmber
                                    else -> AlabasterGrey
                                },
                                fontSize = 11.sp,
                                fontWeight = FontWeight.SemiBold
                            )
                        }
                    }

                    // Submission Link & Reminder badge
                    if (!task.submissionLink.isNullOrBlank() || task.reminderType != "NONE") {
                        Spacer(modifier = Modifier.height(8.dp))
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            if (!task.submissionLink.isNullOrBlank()) {
                                Row(
                                    modifier = Modifier
                                        .clip(RoundedCornerShape(6.dp))
                                        .background(SurfaceDarkVariant)
                                        .clickable { onOpenLink(task.submissionLink) }
                                        .padding(horizontal = 8.dp, vertical = 4.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Icon(
                                        imageVector = Icons.AutoMirrored.Filled.OpenInNew,
                                        contentDescription = "Buka Link",
                                        tint = ElectricCyan,
                                        modifier = Modifier.size(12.dp)
                                    )
                                    Spacer(modifier = Modifier.width(4.dp))
                                    Text(
                                        text = "Tautan Tugas",
                                        color = ElectricCyan,
                                        fontSize = 11.sp,
                                        fontWeight = FontWeight.Medium
                                    )
                                }
                            } else {
                                Spacer(modifier = Modifier.width(1.dp))
                            }

                            if (task.reminderType != "NONE" && !task.isCompleted) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Icon(
                                        imageVector = Icons.Default.Alarm,
                                        contentDescription = "Pengingat",
                                        tint = SunsetAmber,
                                        modifier = Modifier.size(12.dp)
                                    )
                                    Spacer(modifier = Modifier.width(4.dp))
                                    val reminderLabel = when (task.reminderType) {
                                        "1_HOUR_BEFORE" -> "1 jam sblm"
                                        "1_DAY_BEFORE" -> "1 hari sblm"
                                        "2_DAYS_BEFORE" -> "2 hari sblm"
                                        else -> "Aktif"
                                    }
                                    Text(
                                        text = reminderLabel,
                                        color = DustyDenim,
                                        fontSize = 10.sp
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddEditTaskDialog(
    task: TaskEntity?,
    courses: List<CourseEntity>,
    onDismiss: () -> Unit,
    onSave: (TaskEntity, String) -> Unit
) {
    val context = LocalContext.current

    var title by remember { mutableStateOf(task?.title ?: "") }
    var description by remember { mutableStateOf(task?.description ?: "") }
    var selectedCourseId by remember { mutableStateOf(task?.courseId ?: courses.firstOrNull()?.id) }
    var submissionLink by remember { mutableStateOf(task?.submissionLink ?: "") }
    var priority by remember { mutableStateOf(task?.priority ?: "SEDANG") }
    var reminderType by remember { mutableStateOf(task?.reminderType ?: "1_DAY_BEFORE") }

    // Due Date state
    val calendar = remember {
        Calendar.getInstance().apply {
            if (task != null) {
                timeInMillis = task.dueDate
            } else {
                // Default: 2 days from now at 23:59
                add(Calendar.DAY_OF_YEAR, 2)
                set(Calendar.HOUR_OF_DAY, 23)
                set(Calendar.MINUTE, 59)
                set(Calendar.SECOND, 0)
            }
        }
    }
    var dueMillis by remember { mutableStateOf(calendar.timeInMillis) }

    var courseDropdownExpanded by remember { mutableStateOf(false) }
    var priorityDropdownExpanded by remember { mutableStateOf(false) }
    var reminderDropdownExpanded by remember { mutableStateOf(false) }

    val priorityOptions = listOf("TINGGI" to "Tinggi", "SEDANG" to "Sedang", "RENDAH" to "Rendah")
    val reminderOptions = listOf(
        "NONE" to "Tanpa Pengingat",
        "1_HOUR_BEFORE" to "1 Jam Sebelum Deadline",
        "1_DAY_BEFORE" to "1 Hari Sebelum Deadline",
        "2_DAYS_BEFORE" to "2 Hari Sebelum Deadline"
    )

    AlertDialog(
        onDismissRequest = onDismiss,
        containerColor = PrussianBlue,
        title = {
            Text(
                text = if (task == null) "Tambah Tugas Baru" else "Ubah Tugas",
                color = AlabasterGrey,
                fontSize = 18.sp,
                fontWeight = FontWeight.Bold
            )
        },
        text = {
            Column(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                // Title
                OutlinedTextField(
                    value = title,
                    onValueChange = { title = it },
                    label = { Text("Judul Tugas *") },
                    placeholder = { Text("e.g. Tugas Praktikum Modul 5") },
                    modifier = Modifier.fillMaxWidth(),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedTextColor = AlabasterGrey,
                        unfocusedTextColor = AlabasterGrey,
                        focusedBorderColor = ElectricCyan,
                        unfocusedBorderColor = DuskBlue
                    )
                )

                // Course selector
                ExposedDropdownMenuBox(
                    expanded = courseDropdownExpanded,
                    onExpandedChange = { courseDropdownExpanded = it }
                ) {
                    val currentCourse = courses.find { it.id == selectedCourseId }
                    OutlinedTextField(
                        value = currentCourse?.let { "${it.code} - ${it.name}" } ?: "Tugas Bebas / Tanpa Matkul",
                        onValueChange = {},
                        readOnly = true,
                        label = { Text("Mata Kuliah") },
                        trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = courseDropdownExpanded) },
                        modifier = Modifier
                            .fillMaxWidth()
                            .menuAnchor(MenuAnchorType.PrimaryNotEditable),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedTextColor = AlabasterGrey,
                            unfocusedTextColor = AlabasterGrey,
                            focusedBorderColor = ElectricCyan,
                            unfocusedBorderColor = DuskBlue
                        )
                    )
                    ExposedDropdownMenu(
                        expanded = courseDropdownExpanded,
                        onDismissRequest = { courseDropdownExpanded = false },
                        modifier = Modifier.background(SurfaceDarkVariant)
                    ) {
                        DropdownMenuItem(
                            text = { Text("Tugas Bebas (Tanpa Matkul)", color = AlabasterGrey) },
                            onClick = {
                                selectedCourseId = null
                                courseDropdownExpanded = false
                            }
                        )
                        courses.forEach { course ->
                            DropdownMenuItem(
                                text = { Text("${course.code} - ${course.name}", color = AlabasterGrey) },
                                onClick = {
                                    selectedCourseId = course.id
                                    courseDropdownExpanded = false
                                }
                            )
                        }
                    }
                }

                // Date & Time Picker trigger
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    OutlinedTextField(
                        value = DateUtils.formatShortDate(dueMillis),
                        onValueChange = {},
                        readOnly = true,
                        label = { Text("Tanggal") },
                        modifier = Modifier
                            .weight(1.2f)
                            .clickable {
                                val cal = Calendar.getInstance().apply { timeInMillis = dueMillis }
                                DatePickerDialog(
                                    context,
                                    { _, year, month, day ->
                                        cal.set(Calendar.YEAR, year)
                                        cal.set(Calendar.MONTH, month)
                                        cal.set(Calendar.DAY_OF_MONTH, day)
                                        dueMillis = cal.timeInMillis
                                    },
                                    cal.get(Calendar.YEAR),
                                    cal.get(Calendar.MONTH),
                                    cal.get(Calendar.DAY_OF_MONTH)
                                ).show()
                            },
                        enabled = false,
                        colors = OutlinedTextFieldDefaults.colors(
                            disabledTextColor = AlabasterGrey,
                            disabledBorderColor = DuskBlue,
                            disabledLabelColor = DustyDenim
                        )
                    )

                    OutlinedTextField(
                        value = DateUtils.formatTime(dueMillis),
                        onValueChange = {},
                        readOnly = true,
                        label = { Text("Pukul") },
                        modifier = Modifier
                            .weight(0.8f)
                            .clickable {
                                val cal = Calendar.getInstance().apply { timeInMillis = dueMillis }
                                TimePickerDialog(
                                    context,
                                    { _, hour, minute ->
                                        cal.set(Calendar.HOUR_OF_DAY, hour)
                                        cal.set(Calendar.MINUTE, minute)
                                        dueMillis = cal.timeInMillis
                                    },
                                    cal.get(Calendar.HOUR_OF_DAY),
                                    cal.get(Calendar.MINUTE),
                                    true
                                ).show()
                            },
                        enabled = false,
                        colors = OutlinedTextFieldDefaults.colors(
                            disabledTextColor = AlabasterGrey,
                            disabledBorderColor = DuskBlue,
                            disabledLabelColor = DustyDenim
                        )
                    )
                }

                // Priority & Reminder in row
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    // Priority
                    ExposedDropdownMenuBox(
                        expanded = priorityDropdownExpanded,
                        onExpandedChange = { priorityDropdownExpanded = it },
                        modifier = Modifier.weight(1f)
                    ) {
                        OutlinedTextField(
                            value = priorityOptions.find { it.first == priority }?.second ?: priority,
                            onValueChange = {},
                            readOnly = true,
                            label = { Text("Prioritas") },
                            modifier = Modifier
                                .fillMaxWidth()
                                .menuAnchor(MenuAnchorType.PrimaryNotEditable),
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedTextColor = AlabasterGrey,
                                unfocusedTextColor = AlabasterGrey,
                                focusedBorderColor = ElectricCyan,
                                unfocusedBorderColor = DuskBlue
                            )
                        )
                        ExposedDropdownMenu(
                            expanded = priorityDropdownExpanded,
                            onDismissRequest = { priorityDropdownExpanded = false },
                            modifier = Modifier.background(SurfaceDarkVariant)
                        ) {
                            priorityOptions.forEach { (key, label) ->
                                DropdownMenuItem(
                                    text = { Text(label, color = AlabasterGrey) },
                                    onClick = {
                                        priority = key
                                        priorityDropdownExpanded = false
                                    }
                                )
                            }
                        }
                    }

                    // Reminder
                    ExposedDropdownMenuBox(
                        expanded = reminderDropdownExpanded,
                        onExpandedChange = { reminderDropdownExpanded = it },
                        modifier = Modifier.weight(1.2f)
                    ) {
                        OutlinedTextField(
                            value = reminderOptions.find { it.first == reminderType }?.second ?: reminderType,
                            onValueChange = {},
                            readOnly = true,
                            label = { Text("Pengingat") },
                            modifier = Modifier
                                .fillMaxWidth()
                                .menuAnchor(MenuAnchorType.PrimaryNotEditable),
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedTextColor = AlabasterGrey,
                                unfocusedTextColor = AlabasterGrey,
                                focusedBorderColor = ElectricCyan,
                                unfocusedBorderColor = DuskBlue
                            )
                        )
                        ExposedDropdownMenu(
                            expanded = reminderDropdownExpanded,
                            onDismissRequest = { reminderDropdownExpanded = false },
                            modifier = Modifier.background(SurfaceDarkVariant)
                        ) {
                            reminderOptions.forEach { (key, label) ->
                                DropdownMenuItem(
                                    text = { Text(label, color = AlabasterGrey) },
                                    onClick = {
                                        reminderType = key
                                        reminderDropdownExpanded = false
                                    }
                                )
                            }
                        }
                    }
                }

                // Submission Link
                OutlinedTextField(
                    value = submissionLink,
                    onValueChange = { submissionLink = it },
                    label = { Text("Link Pengumpulan (Opsional)") },
                    placeholder = { Text("e.g. classroom.google.com/...") },
                    modifier = Modifier.fillMaxWidth(),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedTextColor = AlabasterGrey,
                        unfocusedTextColor = AlabasterGrey,
                        focusedBorderColor = ElectricCyan,
                        unfocusedBorderColor = DuskBlue
                    )
                )

                // Description
                OutlinedTextField(
                    value = description,
                    onValueChange = { description = it },
                    label = { Text("Deskripsi / Rincian Soal") },
                    placeholder = { Text("Instruksi tugas, format PDF, dsb.") },
                    modifier = Modifier.fillMaxWidth(),
                    minLines = 2,
                    maxLines = 4,
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedTextColor = AlabasterGrey,
                        unfocusedTextColor = AlabasterGrey,
                        focusedBorderColor = ElectricCyan,
                        unfocusedBorderColor = DuskBlue
                    )
                )
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    if (title.isNotBlank()) {
                        val courseName = courses.find { it.id == selectedCourseId }?.name ?: "Kuliah"
                        onSave(
                            TaskEntity(
                                id = task?.id ?: 0,
                                courseId = selectedCourseId,
                                title = title.trim(),
                                description = description.trim(),
                                submissionLink = submissionLink.trim().ifEmpty { null },
                                dueDate = dueMillis,
                                isCompleted = task?.isCompleted ?: false,
                                priority = priority,
                                reminderType = reminderType
                            ),
                            courseName
                        )
                    }
                },
                colors = ButtonDefaults.buttonColors(containerColor = DuskBlue)
            ) {
                Text("Simpan Tugas", color = AlabasterGrey)
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Batal", color = DustyDenim)
            }
        }
    )
}
