package com.example.ui.screens

import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
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
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Cancel
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.EventAvailable
import androidx.compose.material.icons.filled.EventBusy
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.PictureAsPdf
import androidx.compose.material.icons.filled.Schedule
import androidx.compose.material.icons.filled.TableChart
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.MenuAnchorType
import androidx.compose.material3.OutlinedButton
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
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.data.entity.CourseEntity
import com.example.data.entity.ScheduleEntity
import com.example.data.entity.ScheduleOverrideEntity
import com.example.data.entity.ScheduleWithDetails
import com.example.ui.components.BackgroundPdfImportDialog
import com.example.ui.components.StatusOverrideBadge
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
import com.example.util.DateUtils

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ScheduleScreen(
    viewModel: MahaSigmaViewModel,
    modifier: Modifier = Modifier
) {
    val selectedDay by viewModel.selectedDayOfWeek.collectAsStateWithLifecycle()
    val schedulesForDay by viewModel.schedulesForSelectedDay.collectAsStateWithLifecycle()
    val courses by viewModel.courses.collectAsStateWithLifecycle()
    val currentDayOfWeek = DateUtils.getCurrentDayOfWeek()

    var showAddEditDialog by remember { mutableStateOf(false) }
    var scheduleToEdit by remember { mutableStateOf<ScheduleEntity?>(null) }

    var showOverrideDialog by remember { mutableStateOf(false) }
    var scheduleForOverride by remember { mutableStateOf<ScheduleEntity?>(null) }

    var scheduleToDelete by remember { mutableStateOf<ScheduleEntity?>(null) }

    val context = LocalContext.current
    val pdfPickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.OpenDocument()
    ) { uri: Uri? ->
        if (uri != null) {
            viewModel.startBackgroundTableImport(context, uri)
        }
    }

    Scaffold(
        modifier = modifier.fillMaxSize(),
        containerColor = InkBlack,
        floatingActionButton = {
            FloatingActionButton(
                onClick = {
                    scheduleToEdit = null
                    showAddEditDialog = true
                },
                containerColor = DuskBlue,
                contentColor = AlabasterGrey,
                modifier = Modifier
                    .padding(bottom = 80.dp)
                    .testTag("add_schedule_fab")
            ) {
                Icon(imageVector = Icons.Default.Add, contentDescription = "Tambah Jadwal")
            }
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            // Screen Header
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(PrussianBlue)
                    .border(1.dp, DuskBlue.copy(alpha = 0.4f), RoundedCornerShape(bottomStart = 20.dp, bottomEnd = 20.dp))
                    .padding(16.dp)
            ) {
                Column {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = "Jadwal Mingguan",
                                color = AlabasterGrey,
                                fontSize = 20.sp,
                                fontWeight = FontWeight.Bold
                            )
                            Text(
                                text = "Kelola jam kuliah dan override jadwal dinamis",
                                color = DustyDenim,
                                fontSize = 12.sp
                            )
                        }

                        OutlinedButton(
                            onClick = {
                                pdfPickerLauncher.launch(
                                    arrayOf(
                                        "application/pdf",
                                        "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet",
                                        "application/vnd.ms-excel",
                                        "text/csv",
                                        "text/plain",
                                        "*/*"
                                    )
                                )
                            },
                            shape = RoundedCornerShape(10.dp),
                            colors = ButtonDefaults.outlinedButtonColors(
                                containerColor = SurfaceDarkVariant.copy(alpha = 0.6f),
                                contentColor = NeonEmerald
                            ),
                            border = androidx.compose.foundation.BorderStroke(1.dp, NeonEmerald.copy(alpha = 0.5f)),
                            contentPadding = PaddingValues(horizontal = 12.dp, vertical = 6.dp),
                            modifier = Modifier.testTag("import_pdf_schedule_button")
                        ) {
                            Icon(
                                imageVector = Icons.Default.PictureAsPdf,
                                contentDescription = null,
                                modifier = Modifier.size(16.dp),
                                tint = CrimsonRed
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text("Impor PDF", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                        }
                    }

                    Spacer(modifier = Modifier.height(14.dp))

                    // Day of Week selector pills (Senin - Jumat)
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .horizontalScroll(rememberScrollState()),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        for (day in 1..5) {
                            val isSelected = (day == selectedDay)
                            val isToday = (day == currentDayOfWeek)
                            val dayName = DateUtils.getDayName(day)

                            Box(
                                modifier = Modifier
                                    .clip(RoundedCornerShape(12.dp))
                                    .background(
                                        when {
                                            isSelected -> DuskBlue
                                            isToday -> SurfaceDarkVariant
                                            else -> PrussianBlue
                                        }
                                    )
                                    .border(
                                        1.dp,
                                        when {
                                            isSelected -> ElectricCyan
                                            isToday -> SunsetAmber.copy(alpha = 0.8f)
                                            else -> DuskBlue.copy(alpha = 0.3f)
                                        },
                                        RoundedCornerShape(12.dp)
                                    )
                                    .clickable { viewModel.setSelectedDayOfWeek(day) }
                                    .padding(horizontal = 14.dp, vertical = 8.dp),
                                contentAlignment = Alignment.Center
                            ) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    if (isToday) {
                                        Box(
                                            modifier = Modifier
                                                .size(6.dp)
                                                .clip(CircleShape)
                                                .background(SunsetAmber)
                                        )
                                        Spacer(modifier = Modifier.width(6.dp))
                                    }
                                    Text(
                                        text = dayName,
                                        color = if (isSelected) AlabasterGrey else DustyDenim,
                                        fontSize = 13.sp,
                                        fontWeight = if (isSelected || isToday) FontWeight.Bold else FontWeight.Medium
                                    )
                                }
                            }
                        }
                    }
                }
            }

            // List of schedules for the selected day
            if (schedulesForDay.isEmpty()) {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(32.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Icon(
                            imageVector = Icons.Default.EventAvailable,
                            contentDescription = null,
                            tint = DustyDenim,
                            modifier = Modifier.size(56.dp)
                        )
                        Spacer(modifier = Modifier.height(12.dp))
                        Text(
                            text = "Tidak Ada Jadwal di Hari ${DateUtils.getDayName(selectedDay)}",
                            color = AlabasterGrey,
                            fontSize = 16.sp,
                            fontWeight = FontWeight.Bold
                        )
                        Spacer(modifier = Modifier.height(6.dp))
                        Text(
                            text = "Tekan tombol '+' di bawah untuk menambahkan jadwal kuliah baru.",
                            color = DustyDenim,
                            fontSize = 13.sp,
                            textAlign = androidx.compose.ui.text.style.TextAlign.Center
                        )
                        Spacer(modifier = Modifier.height(16.dp))
                        Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                            FilledTonalButton(
                                onClick = {
                                    scheduleToEdit = null
                                    showAddEditDialog = true
                                },
                                colors = ButtonDefaults.filledTonalButtonColors(
                                    containerColor = SurfaceDarkVariant,
                                    contentColor = ElectricCyan
                                )
                            ) {
                                Icon(Icons.Default.Add, contentDescription = null, modifier = Modifier.size(18.dp))
                                Spacer(modifier = Modifier.width(6.dp))
                                Text("Tambah Manual")
                            }

                            Button(
                                onClick = {
                                    pdfPickerLauncher.launch(
                                        arrayOf(
                                            "application/pdf",
                                            "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet",
                                            "application/vnd.ms-excel",
                                            "text/csv",
                                            "text/plain",
                                            "*/*"
                                        )
                                    )
                                },
                                colors = ButtonDefaults.buttonColors(containerColor = DuskBlue)
                            ) {
                                Icon(Icons.Default.PictureAsPdf, contentDescription = null, modifier = Modifier.size(18.dp), tint = CrimsonRed)
                                Spacer(modifier = Modifier.width(6.dp))
                                Text("Impor Tabel PDF")
                            }
                        }
                    }
                }
            } else {
                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    contentPadding = PaddingValues(top = 12.dp, start = 16.dp, end = 16.dp, bottom = 120.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    items(schedulesForDay, key = { it.schedule.id }) { item ->
                        ScheduleDetailCard(
                            scheduleWithDetails = item,
                            onEditClick = {
                                scheduleToEdit = item.schedule
                                showAddEditDialog = true
                            },
                            onDeleteClick = {
                                scheduleToDelete = item.schedule
                            },
                            onOverrideClick = {
                                scheduleForOverride = item.schedule
                                showOverrideDialog = true
                            },
                            onRemoveOverride = { override ->
                                viewModel.deleteScheduleOverride(override)
                            }
                        )
                    }
                }
            }
        }
    }

    // Dialog Tambah/Edit Jadwal
    if (showAddEditDialog) {
        AddEditScheduleDialog(
            schedule = scheduleToEdit,
            initialDay = selectedDay,
            courses = courses,
            onDismiss = { showAddEditDialog = false },
            onSave = { newSchedule ->
                viewModel.saveSchedule(newSchedule)
                showAddEditDialog = false
            }
        )
    }

    // Dialog Override Jadwal (Dinamis: Batal / Ganti Jam / Ganti Ruangan / Online)
    if (showOverrideDialog && scheduleForOverride != null) {
        val targetSchedule = scheduleForOverride!!
        val activeOverride = schedulesForDay
            .firstOrNull { it.schedule.id == targetSchedule.id }
            ?.overrides?.firstOrNull()

        ScheduleOverrideDialog(
            schedule = targetSchedule,
            existingOverride = activeOverride,
            onDismiss = { showOverrideDialog = false },
            onSave = { override, newDay ->
                if (newDay != null && newDay != targetSchedule.dayOfWeek) {
                    viewModel.saveSchedule(
                        targetSchedule.copy(
                            dayOfWeek = newDay,
                            startTime = override.newStartTime ?: targetSchedule.startTime,
                            endTime = override.newEndTime ?: targetSchedule.endTime
                        )
                    )
                }
                viewModel.saveScheduleOverride(override)
                showOverrideDialog = false
            }
        )
    }

    // Dialog Impor Jadwal dari PDF (Proses di Belakang Layar)
    BackgroundPdfImportDialog(viewModel = viewModel)

    // Dialog Konfirmasi Hapus Jadwal
    if (scheduleToDelete != null) {
        AlertDialog(
            onDismissRequest = { scheduleToDelete = null },
            containerColor = PrussianBlue,
            title = { Text("Hapus Jadwal Kuliah?", color = AlabasterGrey) },
            text = { Text("Jadwal ini akan dihapus permanen dari daftar mingguan.", color = DustyDenim) },
            confirmButton = {
                Button(
                    onClick = {
                        scheduleToDelete?.let { viewModel.deleteSchedule(it) }
                        scheduleToDelete = null
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = CrimsonRed)
                ) {
                    Text("Hapus", color = Color.White)
                }
            },
            dismissButton = {
                TextButton(onClick = { scheduleToDelete = null }) {
                    Text("Batal", color = DustyDenim)
                }
            }
        )
    }
}

@Composable
fun ScheduleDetailCard(
    scheduleWithDetails: ScheduleWithDetails,
    onEditClick: () -> Unit,
    onDeleteClick: () -> Unit,
    onOverrideClick: () -> Unit,
    onRemoveOverride: (ScheduleOverrideEntity) -> Unit,
    modifier: Modifier = Modifier
) {
    val schedule = scheduleWithDetails.schedule
    val course = scheduleWithDetails.course
    val override = scheduleWithDetails.overrides.firstOrNull()

    var showMenu by remember { mutableStateOf(false) }

    val courseColor = try {
        if (!course?.colorHex.isNullOrBlank()) Color(android.graphics.Color.parseColor(course.colorHex))
        else ElectricCyan
    } catch (e: Exception) {
        ElectricCyan
    }

    Card(
        modifier = modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = PrussianBlue),
        shape = RoundedCornerShape(16.dp),
        border = androidx.compose.foundation.BorderStroke(
            1.dp,
            if (override != null) SunsetAmber.copy(alpha = 0.6f) else DuskBlue.copy(alpha = 0.5f)
        )
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            // Header: Color, Title, Code, & 3-dot Menu
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Box(
                    modifier = Modifier
                        .size(10.dp)
                        .clip(CircleShape)
                        .background(courseColor)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = course?.name ?: "Mata Kuliah Tidak Dikenal",
                        color = AlabasterGrey,
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Bold,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                    if (!course?.code.isNullOrBlank()) {
                        Text(
                            text = "Kode: ${course.code}",
                            color = DustyDenim,
                            fontSize = 11.sp,
                            fontFamily = FontFamily.Monospace
                        )
                    }
                }

                Box {
                    IconButton(onClick = { showMenu = true }) {
                        Icon(
                            imageVector = Icons.Default.MoreVert,
                            contentDescription = "Menu",
                            tint = DustyDenim
                        )
                    }
                    DropdownMenu(
                        expanded = showMenu,
                        onDismissRequest = { showMenu = false },
                        modifier = Modifier.background(SurfaceDarkVariant)
                    ) {
                        DropdownMenuItem(
                            text = { Text("Ubah Jadwal", color = AlabasterGrey) },
                            onClick = {
                                showMenu = false
                                onEditClick()
                            },
                            leadingIcon = { Icon(Icons.Default.Edit, contentDescription = null, tint = ElectricCyan) }
                        )
                        DropdownMenuItem(
                            text = { Text("Override Status Kuliah", color = AlabasterGrey) },
                            onClick = {
                                showMenu = false
                                onOverrideClick()
                            },
                            leadingIcon = { Icon(Icons.Default.EventBusy, contentDescription = null, tint = SunsetAmber) }
                        )
                        DropdownMenuItem(
                            text = { Text("Hapus Jadwal", color = CrimsonRed) },
                            onClick = {
                                showMenu = false
                                onDeleteClick()
                            },
                            leadingIcon = { Icon(Icons.Default.Delete, contentDescription = null, tint = CrimsonRed) }
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            // Time and Room row
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Default.Schedule,
                        contentDescription = null,
                        tint = ElectricCyan,
                        modifier = Modifier.size(15.dp)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = "${schedule.startTime} - ${schedule.endTime}",
                        color = AlabasterGrey,
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Medium
                    )
                }

                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Default.LocationOn,
                        contentDescription = null,
                        tint = SunsetAmber,
                        modifier = Modifier.size(15.dp)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = schedule.room,
                        color = AlabasterGrey,
                        fontSize = 13.sp
                    )
                }
            }

            if (!course?.lecturer.isNullOrBlank()) {
                Spacer(modifier = Modifier.height(6.dp))
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Default.Person,
                        contentDescription = null,
                        tint = DustyDenim,
                        modifier = Modifier.size(14.dp)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = course.lecturer,
                        color = DustyDenim,
                        fontSize = 12.sp
                    )
                }
            }

            // Override Banner if active
            if (override != null) {
                Spacer(modifier = Modifier.height(12.dp))
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(10.dp))
                        .background(SurfaceDarkVariant)
                        .border(1.dp, SunsetAmber.copy(alpha = 0.5f), RoundedCornerShape(10.dp))
                        .padding(10.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            StatusOverrideBadge(status = override.status)
                            if (!override.note.isNullOrBlank()) {
                                Spacer(modifier = Modifier.height(4.dp))
                                Text(
                                    text = "Catatan: ${override.note}",
                                    color = AlabasterGrey,
                                    fontSize = 12.sp
                                )
                            }
                            if (override.status == "CHANGED_TIME" && !override.newStartTime.isNullOrBlank()) {
                                Spacer(modifier = Modifier.height(2.dp))
                                Text(
                                    text = "Jam baru: ${override.newStartTime} - ${override.newEndTime}",
                                    color = ElectricCyan,
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                            if (override.status == "CHANGED_ROOM" && !override.newRoom.isNullOrBlank()) {
                                Spacer(modifier = Modifier.height(2.dp))
                                Text(
                                    text = "Ruangan baru: ${override.newRoom}",
                                    color = ElectricCyan,
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                        }

                        IconButton(
                            onClick = { onRemoveOverride(override) },
                            modifier = Modifier.size(32.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.Close,
                                contentDescription = "Batalkan Override",
                                tint = DustyDenim,
                                modifier = Modifier.size(18.dp)
                            )
                        }
                    }
                }
            } else {
                // Quick override action chip
                Spacer(modifier = Modifier.height(10.dp))
                Row(
                    modifier = Modifier
                        .clip(RoundedCornerShape(8.dp))
                        .background(SurfaceDarkVariant)
                        .clickable { onOverrideClick() }
                        .padding(horizontal = 10.dp, vertical = 5.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = Icons.Default.EventBusy,
                        contentDescription = null,
                        tint = SunsetAmber,
                        modifier = Modifier.size(13.dp)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = "Set Status Hari Ini (Libur / Pindah Jam / Online)",
                        color = DustyDenim,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Medium
                    )
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddEditScheduleDialog(
    schedule: ScheduleEntity?,
    initialDay: Int,
    courses: List<CourseEntity>,
    onDismiss: () -> Unit,
    onSave: (ScheduleEntity) -> Unit
) {
    var selectedCourseId by remember {
        mutableStateOf(schedule?.courseId ?: courses.firstOrNull()?.id ?: 0)
    }
    var dayOfWeek by remember { mutableStateOf(schedule?.dayOfWeek ?: initialDay) }
    var startTime by remember { mutableStateOf(schedule?.startTime ?: "08:00") }
    var endTime by remember { mutableStateOf(schedule?.endTime ?: "10:30") }
    var room by remember { mutableStateOf(schedule?.room ?: "R. 301") }

    var courseDropdownExpanded by remember { mutableStateOf(false) }
    var dayDropdownExpanded by remember { mutableStateOf(false) }

    AlertDialog(
        onDismissRequest = onDismiss,
        containerColor = PrussianBlue,
        title = {
            Text(
                text = if (schedule == null) "Tambah Jadwal Kuliah" else "Ubah Jadwal Kuliah",
                color = AlabasterGrey,
                fontSize = 18.sp,
                fontWeight = FontWeight.Bold
            )
        },
        text = {
            Column(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                // Course Picker
                if (courses.isEmpty()) {
                    Text(
                        text = "Belum ada mata kuliah terdaftar. Silakan tambahkan di menu Pengaturan terlebih dahulu.",
                        color = CrimsonRed,
                        fontSize = 13.sp
                    )
                } else {
                    ExposedDropdownMenuBox(
                        expanded = courseDropdownExpanded,
                        onExpandedChange = { courseDropdownExpanded = it }
                    ) {
                        val selectedCourse = courses.find { it.id == selectedCourseId } ?: courses.first()
                        OutlinedTextField(
                            value = "${selectedCourse.code} - ${selectedCourse.name}",
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
                }

                // Day Picker
                ExposedDropdownMenuBox(
                    expanded = dayDropdownExpanded,
                    onExpandedChange = { dayDropdownExpanded = it }
                ) {
                    OutlinedTextField(
                        value = DateUtils.getDayName(dayOfWeek),
                        onValueChange = {},
                        readOnly = true,
                        label = { Text("Hari") },
                        trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = dayDropdownExpanded) },
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
                        expanded = dayDropdownExpanded,
                        onDismissRequest = { dayDropdownExpanded = false },
                        modifier = Modifier.background(SurfaceDarkVariant)
                    ) {
                        for (d in 1..5) {
                            DropdownMenuItem(
                                text = { Text(DateUtils.getDayName(d), color = AlabasterGrey) },
                                onClick = {
                                    dayOfWeek = d
                                    dayDropdownExpanded = false
                                }
                            )
                        }
                    }
                }

                // Start and End Time
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    OutlinedTextField(
                        value = startTime,
                        onValueChange = { startTime = it },
                        label = { Text("Mulai") },
                        placeholder = { Text("08:00") },
                        modifier = Modifier.weight(1f),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedTextColor = AlabasterGrey,
                            unfocusedTextColor = AlabasterGrey,
                            focusedBorderColor = ElectricCyan,
                            unfocusedBorderColor = DuskBlue
                        )
                    )
                    OutlinedTextField(
                        value = endTime,
                        onValueChange = { endTime = it },
                        label = { Text("Selesai") },
                        placeholder = { Text("10:30") },
                        modifier = Modifier.weight(1f),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedTextColor = AlabasterGrey,
                            unfocusedTextColor = AlabasterGrey,
                            focusedBorderColor = ElectricCyan,
                            unfocusedBorderColor = DuskBlue
                        )
                    )
                }

                // Room
                OutlinedTextField(
                    value = room,
                    onValueChange = { room = it },
                    label = { Text("Ruangan / Kelas") },
                    placeholder = { Text("e.g. Lab Komputer 3 / Gedung B R.201") },
                    modifier = Modifier.fillMaxWidth(),
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
                    if (selectedCourseId != 0 && startTime.isNotBlank() && endTime.isNotBlank()) {
                        onSave(
                            ScheduleEntity(
                                id = schedule?.id ?: 0,
                                courseId = selectedCourseId,
                                dayOfWeek = dayOfWeek,
                                startTime = startTime.trim(),
                                endTime = endTime.trim(),
                                room = room.trim()
                            )
                        )
                    }
                },
                colors = ButtonDefaults.buttonColors(containerColor = DuskBlue),
                enabled = courses.isNotEmpty()
            ) {
                Text("Simpan", color = AlabasterGrey)
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Batal", color = DustyDenim)
            }
        }
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ScheduleOverrideDialog(
    schedule: ScheduleEntity,
    existingOverride: ScheduleOverrideEntity?,
    onDismiss: () -> Unit,
    onSave: (ScheduleOverrideEntity, Int?) -> Unit
) {
    var status by remember {
        mutableStateOf(existingOverride?.status ?: "CANCELED")
    }
    var newDayOfWeek by remember { mutableStateOf(schedule.dayOfWeek.coerceIn(1, 5)) }
    var newStartTime by remember { mutableStateOf(existingOverride?.newStartTime ?: schedule.startTime) }
    var newEndTime by remember { mutableStateOf(existingOverride?.newEndTime ?: schedule.endTime) }
    var newRoom by remember { mutableStateOf(existingOverride?.newRoom ?: schedule.room) }
    var note by remember { mutableStateOf(existingOverride?.note ?: "") }

    val statusOptions = listOf(
        "CANCELED" to "🚫 Kelas Diliburkan / Dibatalkan",
        "CHANGED_TIME" to "⏱ Perubahan Jam Kuliah",
        "CHANGED_ROOM" to "📍 Pindah Ruangan",
        "ONLINE" to "💻 Kuliah Online (Daring)"
    )

    var statusDropdownExpanded by remember { mutableStateOf(false) }
    var overrideDayDropdownExpanded by remember { mutableStateOf(false) }

    AlertDialog(
        onDismissRequest = onDismiss,
        containerColor = PrussianBlue,
        title = {
            Text(
                text = "Override Status Kuliah",
                color = AlabasterGrey,
                fontSize = 18.sp,
                fontWeight = FontWeight.Bold
            )
        },
        text = {
            Column(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Text(
                    text = "Gunakan fitur ini jika dosen membatalkan kelas, mengganti jam, atau pindah ruangan untuk hari ini.",
                    color = DustyDenim,
                    fontSize = 12.sp
                )

                // Status Dropdown
                ExposedDropdownMenuBox(
                    expanded = statusDropdownExpanded,
                    onExpandedChange = { statusDropdownExpanded = it }
                ) {
                    val currentLabel = statusOptions.find { it.first == status }?.second ?: status
                    OutlinedTextField(
                        value = currentLabel,
                        onValueChange = {},
                        readOnly = true,
                        label = { Text("Kondisi / Status") },
                        trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = statusDropdownExpanded) },
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
                        expanded = statusDropdownExpanded,
                        onDismissRequest = { statusDropdownExpanded = false },
                        modifier = Modifier.background(SurfaceDarkVariant)
                    ) {
                        statusOptions.forEach { (key, label) ->
                            DropdownMenuItem(
                                text = { Text(label, color = AlabasterGrey) },
                                onClick = {
                                    status = key
                                    statusDropdownExpanded = false
                                }
                            )
                        }
                    }
                }

                // Fields based on status
                if (status == "CHANGED_TIME") {
                    // Opsi Ganti Hari Kuliah
                    ExposedDropdownMenuBox(
                        expanded = overrideDayDropdownExpanded,
                        onExpandedChange = { overrideDayDropdownExpanded = it }
                    ) {
                        val isDayChanged = (newDayOfWeek != schedule.dayOfWeek)
                        OutlinedTextField(
                            value = "Hari: ${DateUtils.getDayName(newDayOfWeek)}" + if (isDayChanged) " (Dipindahkan)" else " (Tetap)",
                            onValueChange = {},
                            readOnly = true,
                            label = { Text("Pilihan Hari Kuliah") },
                            trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = overrideDayDropdownExpanded) },
                            modifier = Modifier
                                .fillMaxWidth()
                                .menuAnchor(MenuAnchorType.PrimaryNotEditable),
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedTextColor = if (isDayChanged) SunsetAmber else AlabasterGrey,
                                unfocusedTextColor = if (isDayChanged) SunsetAmber else AlabasterGrey,
                                focusedBorderColor = ElectricCyan,
                                unfocusedBorderColor = DuskBlue
                            )
                        )
                        ExposedDropdownMenu(
                            expanded = overrideDayDropdownExpanded,
                            onDismissRequest = { overrideDayDropdownExpanded = false },
                            modifier = Modifier.background(SurfaceDarkVariant)
                        ) {
                            for (d in 1..5) {
                                DropdownMenuItem(
                                    text = {
                                        Text(
                                            text = DateUtils.getDayName(d) + if (d == schedule.dayOfWeek) " (Hari Asal)" else "",
                                            color = if (d == newDayOfWeek) ElectricCyan else AlabasterGrey,
                                            fontWeight = if (d == newDayOfWeek) FontWeight.Bold else FontWeight.Normal
                                        )
                                    },
                                    onClick = {
                                        newDayOfWeek = d
                                        overrideDayDropdownExpanded = false
                                    }
                                )
                            }
                        }
                    }

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        OutlinedTextField(
                            value = newStartTime,
                            onValueChange = { newStartTime = it },
                            label = { Text("Jam Mulai Baru") },
                            modifier = Modifier.weight(1f),
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedTextColor = AlabasterGrey,
                                unfocusedTextColor = AlabasterGrey,
                                focusedBorderColor = ElectricCyan,
                                unfocusedBorderColor = DuskBlue
                            )
                        )
                        OutlinedTextField(
                            value = newEndTime,
                            onValueChange = { newEndTime = it },
                            label = { Text("Jam Selesai Baru") },
                            modifier = Modifier.weight(1f),
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedTextColor = AlabasterGrey,
                                unfocusedTextColor = AlabasterGrey,
                                focusedBorderColor = ElectricCyan,
                                unfocusedBorderColor = DuskBlue
                            )
                        )
                    }
                }

                if (status == "CHANGED_ROOM") {
                    OutlinedTextField(
                        value = newRoom,
                        onValueChange = { newRoom = it },
                        label = { Text("Ruangan Baru") },
                        placeholder = { Text("e.g. Lab Jaringan Lt. 2") },
                        modifier = Modifier.fillMaxWidth(),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedTextColor = AlabasterGrey,
                            unfocusedTextColor = AlabasterGrey,
                            focusedBorderColor = ElectricCyan,
                            unfocusedBorderColor = DuskBlue
                        )
                    )
                }

                OutlinedTextField(
                    value = note,
                    onValueChange = { note = it },
                    label = { Text("Catatan Tambahan (Opsional)") },
                    placeholder = { Text("e.g. Info dari Bu Dosen di WA") },
                    modifier = Modifier.fillMaxWidth(),
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
                    onSave(
                        ScheduleOverrideEntity(
                            id = existingOverride?.id ?: 0,
                            scheduleId = schedule.id,
                            targetDate = DateUtils.getStartOfTodayMillis(),
                            status = status,
                            newStartTime = if (status == "CHANGED_TIME") newStartTime.trim() else null,
                            newEndTime = if (status == "CHANGED_TIME") newEndTime.trim() else null,
                            newRoom = if (status == "CHANGED_ROOM") newRoom.trim() else null,
                            note = note.trim().ifEmpty { null }
                        ),
                        if (status == "CHANGED_TIME") newDayOfWeek else null
                    )
                },
                colors = ButtonDefaults.buttonColors(containerColor = SunsetAmber)
            ) {
                Text("Terapkan Override", color = InkBlack, fontWeight = FontWeight.Bold)
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Batal", color = DustyDenim)
            }
        }
    )
}
