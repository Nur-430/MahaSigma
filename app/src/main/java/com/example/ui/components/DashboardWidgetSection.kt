package com.example.ui.components

import android.appwidget.AppWidgetManager
import android.content.ComponentName
import android.content.Intent
import android.os.Build
import android.widget.Toast
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Assignment
import androidx.compose.material.icons.filled.CalendarMonth
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Description
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.PhotoCamera
import androidx.compose.material.icons.filled.Schedule
import androidx.compose.material.icons.filled.Widgets
import androidx.compose.material.icons.outlined.CheckCircleOutline
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Checkbox
import androidx.compose.material3.CheckboxDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
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
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.entity.NoteWithCourse
import com.example.data.entity.ScheduleWithDetails
import com.example.data.entity.TaskWithCourse
import com.example.ui.theme.CrimsonRed
import com.example.ui.theme.ElectricCyan
import com.example.ui.theme.MahaTheme
import com.example.ui.theme.NeonEmerald
import com.example.ui.theme.SunsetAmber
import com.example.util.DateUtils
import com.example.widget.QuickNotesWidgetProvider
import com.example.widget.TodayScheduleWidgetProvider
import com.example.widget.UpcomingTasksWidgetProvider

/**
 * Widget 1: Jadwal Kuliah Hari Ini (Interactive Dashboard Component)
 */
@Composable
fun TodayScheduleWidgetCard(
    todaySchedules: List<ScheduleWithDetails>,
    onViewAllClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val colors = MahaTheme.colors
    val sortedSchedules = remember(todaySchedules) {
        todaySchedules.sortedBy { it.schedule.startTime }
    }

    Card(
        modifier = modifier
            .fillMaxWidth()
            .testTag("widget_today_schedule"),
        colors = CardDefaults.cardColors(containerColor = colors.surface),
        shape = RoundedCornerShape(16.dp),
        border = BorderStroke(1.dp, colors.border.copy(alpha = 0.5f))
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            // Header
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier = Modifier
                            .size(32.dp)
                            .clip(CircleShape)
                            .background(ElectricCyan.copy(alpha = 0.2f)),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.CalendarMonth,
                            contentDescription = null,
                            tint = ElectricCyan,
                            modifier = Modifier.size(16.dp)
                        )
                    }
                    Spacer(modifier = Modifier.width(10.dp))
                    Column {
                        Text(
                            text = "Jadwal Kuliah Hari Ini",
                            color = colors.textPrimary,
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            text = "${DateUtils.getDayName(DateUtils.getCurrentDayOfWeek())}, ${DateUtils.formatDate(System.currentTimeMillis())}",
                            color = colors.textSecondary,
                            fontSize = 10.sp
                        )
                    }
                }

                Row(
                    modifier = Modifier
                        .clip(RoundedCornerShape(8.dp))
                        .clickable { onViewAllClick() }
                        .padding(horizontal = 8.dp, vertical = 4.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Semua (${todaySchedules.size})",
                        color = colors.accentPrimary,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.SemiBold
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.ArrowForward,
                        contentDescription = "Lihat Semua Jadwal",
                        tint = colors.accentPrimary,
                        modifier = Modifier.size(12.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            if (sortedSchedules.isEmpty()) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(10.dp))
                        .background(colors.surfaceVariant.copy(alpha = 0.6f))
                        .padding(vertical = 16.dp, horizontal = 12.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text(
                            text = "☕ Tidak ada kelas hari ini!",
                            color = colors.textPrimary,
                            fontSize = 13.sp,
                            fontWeight = FontWeight.Bold
                        )
                        Spacer(modifier = Modifier.height(2.dp))
                        Text(
                            text = "Bebas dari ruang kuliah. Waktunya nugas atau istirahat santai.",
                            color = colors.textSecondary,
                            fontSize = 11.sp
                        )
                    }
                }
            } else {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    sortedSchedules.take(3).forEach { item ->
                        val override = item.overrides.firstOrNull()
                        val course = item.course
                        val effectiveStartTime = if (override?.status == "CHANGED_TIME" && !override.newStartTime.isNullOrBlank()) {
                            override.newStartTime
                        } else item.schedule.startTime

                        val effectiveEndTime = if (override?.status == "CHANGED_TIME" && !override.newEndTime.isNullOrBlank()) {
                            override.newEndTime
                        } else item.schedule.endTime

                        val effectiveRoom = if (override?.status == "CHANGED_ROOM" && !override.newRoom.isNullOrBlank()) {
                            override.newRoom
                        } else if (override?.status == "ONLINE") {
                            "Online / Zoom"
                        } else item.schedule.room

                        val isCanceled = override?.status == "CANCELED"

                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(10.dp))
                                .background(colors.surfaceVariant)
                                .border(
                                    1.dp,
                                    if (isCanceled) CrimsonRed.copy(alpha = 0.4f) else colors.borderSubtle,
                                    RoundedCornerShape(10.dp)
                                )
                                .padding(10.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            // Time Badge
                            Box(
                                modifier = Modifier
                                    .clip(RoundedCornerShape(6.dp))
                                    .background(colors.surface)
                                    .border(1.dp, colors.borderSubtle, RoundedCornerShape(6.dp))
                                    .padding(horizontal = 8.dp, vertical = 6.dp),
                                contentAlignment = Alignment.Center
                            ) {
                                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                    Text(
                                        text = effectiveStartTime,
                                        color = if (isCanceled) CrimsonRed else colors.accentPrimary,
                                        fontSize = 11.sp,
                                        fontWeight = FontWeight.Bold,
                                        fontFamily = FontFamily.Monospace
                                    )
                                    Text(
                                        text = effectiveEndTime,
                                        color = colors.textSecondary,
                                        fontSize = 9.sp,
                                        fontFamily = FontFamily.Monospace
                                    )
                                }
                            }

                            Spacer(modifier = Modifier.width(10.dp))

                            Column(modifier = Modifier.weight(1f)) {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Text(
                                        text = course?.name ?: "Mata Kuliah",
                                        color = if (isCanceled) colors.textSecondary else colors.textPrimary,
                                        fontSize = 12.sp,
                                        fontWeight = FontWeight.Bold,
                                        maxLines = 1,
                                        overflow = TextOverflow.Ellipsis,
                                        textDecoration = if (isCanceled) TextDecoration.LineThrough else null,
                                        modifier = Modifier.weight(1f)
                                    )

                                    if (override != null) {
                                        StatusOverrideBadge(status = override.status)
                                    }
                                }

                                Spacer(modifier = Modifier.height(4.dp))

                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Icon(
                                        imageVector = Icons.Default.LocationOn,
                                        contentDescription = null,
                                        tint = colors.textSecondary,
                                        modifier = Modifier.size(12.dp)
                                    )
                                    Spacer(modifier = Modifier.width(4.dp))
                                    Text(
                                        text = effectiveRoom,
                                        color = colors.textSecondary,
                                        fontSize = 10.sp,
                                        maxLines = 1,
                                        overflow = TextOverflow.Ellipsis
                                    )

                                    if (!course?.lecturer.isNullOrBlank()) {
                                        Text(text = " • ", color = colors.textSecondary, fontSize = 10.sp)
                                        Text(
                                            text = course?.lecturer.orEmpty(),
                                            color = colors.textSecondary,
                                            fontSize = 10.sp,
                                            maxLines = 1,
                                            overflow = TextOverflow.Ellipsis
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
}

/**
 * Widget 2: Deadline Tugas (Diurutkan dari yang Paling Dekat Deadline)
 */
@Composable
fun UpcomingTasksWidgetCard(
    allTasks: List<TaskWithCourse>,
    onToggleTask: (TaskWithCourse) -> Unit,
    onViewAllClick: () -> Unit,
    onAddTaskClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val colors = MahaTheme.colors

    // Sort strictly by nearest deadline first
    val urgentPendingTasks = remember(allTasks) {
        allTasks
            .filter { !it.task.isCompleted }
            .sortedBy { it.task.dueDate }
    }

    Card(
        modifier = modifier
            .fillMaxWidth()
            .testTag("widget_upcoming_tasks"),
        colors = CardDefaults.cardColors(containerColor = colors.surface),
        shape = RoundedCornerShape(16.dp),
        border = BorderStroke(1.dp, colors.border.copy(alpha = 0.5f))
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            // Header
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier = Modifier
                            .size(32.dp)
                            .clip(CircleShape)
                            .background(SunsetAmber.copy(alpha = 0.2f)),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.Assignment,
                            contentDescription = null,
                            tint = SunsetAmber,
                            modifier = Modifier.size(16.dp)
                        )
                    }
                    Spacer(modifier = Modifier.width(10.dp))
                    Column {
                        Text(
                            text = "Tugas & Deadline Terdekat",
                            color = colors.textPrimary,
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            text = "Diurutkan dari deadline paling mendesak",
                            color = colors.textSecondary,
                            fontSize = 10.sp
                        )
                    }
                }

                Row(verticalAlignment = Alignment.CenterVertically) {
                    IconButton(
                        onClick = onAddTaskClick,
                        modifier = Modifier.size(28.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Add,
                            contentDescription = "Tambah Tugas",
                            tint = SunsetAmber,
                            modifier = Modifier.size(18.dp)
                        )
                    }
                    Spacer(modifier = Modifier.width(4.dp))
                    Row(
                        modifier = Modifier
                            .clip(RoundedCornerShape(8.dp))
                            .clickable { onViewAllClick() }
                            .padding(horizontal = 6.dp, vertical = 4.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "Kelola (${urgentPendingTasks.size})",
                            color = SunsetAmber,
                            fontSize = 11.sp,
                            fontWeight = FontWeight.SemiBold
                        )
                        Spacer(modifier = Modifier.width(2.dp))
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowForward,
                            contentDescription = "Buka Tugas",
                            tint = SunsetAmber,
                            modifier = Modifier.size(12.dp)
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            if (urgentPendingTasks.isEmpty()) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(10.dp))
                        .background(colors.surfaceVariant.copy(alpha = 0.6f))
                        .padding(vertical = 16.dp, horizontal = 12.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text(
                            text = "🎉 Semua tugas kuliah beres!",
                            color = NeonEmerald,
                            fontSize = 13.sp,
                            fontWeight = FontWeight.Bold
                        )
                        Spacer(modifier = Modifier.height(2.dp))
                        Text(
                            text = "Tidak ada beban deadline tugas dalam waktu dekat.",
                            color = colors.textSecondary,
                            fontSize = 11.sp
                        )
                    }
                }
            } else {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    urgentPendingTasks.take(3).forEachIndexed { index, taskWithCourse ->
                        val task = taskWithCourse.task
                        val course = taskWithCourse.course
                        val relativeTime = DateUtils.getRelativeDeadline(task.dueDate, false)

                        // Highlight urgency with colors
                        val isOverdue = task.dueDate < System.currentTimeMillis()
                        val isLessThan24h = (task.dueDate - System.currentTimeMillis()) in 0..(24 * 3600 * 1000)

                        val urgencyColor = when {
                            isOverdue -> CrimsonRed
                            isLessThan24h -> SunsetAmber
                            else -> colors.accentPrimary
                        }

                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(10.dp))
                                .background(colors.surfaceVariant)
                                .border(
                                    1.dp,
                                    if (index == 0) urgencyColor.copy(alpha = 0.5f) else colors.borderSubtle,
                                    RoundedCornerShape(10.dp)
                                )
                                .padding(horizontal = 8.dp, vertical = 6.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Checkbox(
                                checked = task.isCompleted,
                                onCheckedChange = { onToggleTask(taskWithCourse) },
                                colors = CheckboxDefaults.colors(
                                    checkedColor = NeonEmerald,
                                    uncheckedColor = colors.textSecondary
                                ),
                                modifier = Modifier.size(32.dp)
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
                                        color = colors.textPrimary,
                                        fontSize = 12.sp,
                                        fontWeight = FontWeight.Bold,
                                        maxLines = 1,
                                        overflow = TextOverflow.Ellipsis,
                                        modifier = Modifier.weight(1f)
                                    )
                                    Spacer(modifier = Modifier.width(6.dp))
                                    // Countdown Tag
                                    Box(
                                        modifier = Modifier
                                            .clip(RoundedCornerShape(4.dp))
                                            .background(urgencyColor.copy(alpha = 0.15f))
                                            .padding(horizontal = 6.dp, vertical = 2.dp)
                                    ) {
                                        Text(
                                            text = relativeTime,
                                            color = urgencyColor,
                                            fontSize = 9.sp,
                                            fontWeight = FontWeight.Bold
                                        )
                                    }
                                }

                                Spacer(modifier = Modifier.height(3.dp))

                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Text(
                                        text = course?.name ?: "Tugas Umum",
                                        color = colors.textSecondary,
                                        fontSize = 10.sp,
                                        maxLines = 1,
                                        overflow = TextOverflow.Ellipsis
                                    )
                                    Spacer(modifier = Modifier.width(6.dp))
                                    PriorityBadge(priority = task.priority)
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

/**
 * Widget 3: Akses Cepat Fitur Catatan Materi & Foto Papan Tulis
 */
@Composable
fun QuickNotesWidgetCard(
    allNotes: List<NoteWithCourse>,
    onNewNoteClick: () -> Unit,
    onCameraNoteClick: () -> Unit,
    onViewAllClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val colors = MahaTheme.colors
    val latestNotes = remember(allNotes) {
        allNotes.sortedByDescending { it.note.updatedAt }
    }

    Card(
        modifier = modifier
            .fillMaxWidth()
            .testTag("widget_quick_notes"),
        colors = CardDefaults.cardColors(containerColor = colors.surface),
        shape = RoundedCornerShape(16.dp),
        border = BorderStroke(1.dp, colors.border.copy(alpha = 0.5f))
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            // Header
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier = Modifier
                            .size(32.dp)
                            .clip(CircleShape)
                            .background(NeonEmerald.copy(alpha = 0.2f)),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.Description,
                            contentDescription = null,
                            tint = NeonEmerald,
                            modifier = Modifier.size(16.dp)
                        )
                    }
                    Spacer(modifier = Modifier.width(10.dp))
                    Column {
                        Text(
                            text = "Akses Fitur Catatan",
                            color = colors.textPrimary,
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            text = "Catatan materi & foto papan tulis pintar",
                            color = colors.textSecondary,
                            fontSize = 10.sp
                        )
                    }
                }

                Row(
                    modifier = Modifier
                        .clip(RoundedCornerShape(8.dp))
                        .clickable { onViewAllClick() }
                        .padding(horizontal = 6.dp, vertical = 4.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Semua (${allNotes.size})",
                        color = NeonEmerald,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.SemiBold
                    )
                    Spacer(modifier = Modifier.width(2.dp))
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.ArrowForward,
                        contentDescription = "Lihat Catatan",
                        tint = NeonEmerald,
                        modifier = Modifier.size(12.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Action Buttons
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Button(
                    onClick = onNewNoteClick,
                    modifier = Modifier.weight(1f),
                    colors = ButtonDefaults.buttonColors(containerColor = colors.accentPrimary),
                    shape = RoundedCornerShape(10.dp)
                ) {
                    Icon(Icons.Default.Add, contentDescription = null, modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(6.dp))
                    Text("Tulis Catatan", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                }

                OutlinedButton(
                    onClick = onCameraNoteClick,
                    modifier = Modifier.weight(1f),
                    colors = ButtonDefaults.outlinedButtonColors(contentColor = NeonEmerald),
                    shape = RoundedCornerShape(10.dp),
                    border = BorderStroke(1.dp, NeonEmerald.copy(alpha = 0.5f))
                ) {
                    Icon(Icons.Default.PhotoCamera, contentDescription = null, modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(6.dp))
                    Text("Foto Papan Tulis", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            // Recent Note Preview
            if (latestNotes.isNotEmpty()) {
                val latest = latestNotes.first()
                val note = latest.note
                val course = latest.course

                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(10.dp))
                        .background(colors.surfaceVariant)
                        .clickable { onViewAllClick() }
                        .padding(10.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Box(
                        modifier = Modifier
                            .size(36.dp)
                            .clip(RoundedCornerShape(8.dp))
                            .background(colors.surface)
                            .border(1.dp, colors.borderSubtle, RoundedCornerShape(8.dp)),
                        contentAlignment = Alignment.Center
                    ) {
                        val hasPhotos = note.getAllImages().isNotEmpty()
                        Icon(
                            imageVector = if (hasPhotos) Icons.Default.PhotoCamera else Icons.Default.Description,
                            contentDescription = null,
                            tint = if (hasPhotos) SunsetAmber else colors.accentPrimary,
                            modifier = Modifier.size(18.dp)
                        )
                    }
                    Spacer(modifier = Modifier.width(10.dp))
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = note.title.ifBlank { "Catatan Tanpa Judul" },
                            color = colors.textPrimary,
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                        Text(
                            text = "${course?.name ?: "Umum"} • ${DateUtils.formatDate(note.updatedAt)}",
                            color = colors.textSecondary,
                            fontSize = 10.sp,
                            maxLines = 1
                        )
                    }
                }
            }
        }
    }
}

/**
 * Widget 4: Banner Panduan & Pin Widget ke Homescreen Android
 */
@Composable
fun LauncherWidgetPinBanner(
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val colors = MahaTheme.colors

    fun pinScheduleWidget() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val appWidgetManager = context.getSystemService(AppWidgetManager::class.java)
            if (appWidgetManager != null && appWidgetManager.isRequestPinAppWidgetSupported) {
                val provider = ComponentName(context, TodayScheduleWidgetProvider::class.java)
                appWidgetManager.requestPinAppWidget(provider, null, null)
                Toast.makeText(context, "Permintaan sematkan Widget Jadwal dikirim!", Toast.LENGTH_SHORT).show()
                return
            }
        }
        Toast.makeText(context, "Buka layar utama HP, tekan tahan layar kosong > pilih 'Widget' > MahaSigma.", Toast.LENGTH_LONG).show()
    }

    Card(
        modifier = modifier
            .fillMaxWidth()
            .testTag("banner_launcher_widget"),
        colors = CardDefaults.cardColors(containerColor = colors.surface),
        shape = RoundedCornerShape(16.dp),
        border = BorderStroke(1.dp, colors.accentPrimary.copy(alpha = 0.35f))
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(14.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(40.dp)
                    .clip(CircleShape)
                    .background(colors.accentPrimary.copy(alpha = 0.15f)),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Default.Widgets,
                    contentDescription = null,
                    tint = colors.accentPrimary,
                    modifier = Modifier.size(20.dp)
                )
            }
            Spacer(modifier = Modifier.width(12.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = "Pasang Widget di Layar Utama HP",
                    color = colors.textPrimary,
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    text = "Tersedia 3 widget: Jadwal Kuliah, Deadline Tugas, & Akses Catatan.",
                    color = colors.textSecondary,
                    fontSize = 10.sp
                )
            }
            Spacer(modifier = Modifier.width(8.dp))
            OutlinedButton(
                onClick = { pinScheduleWidget() },
                shape = RoundedCornerShape(8.dp)
            ) {
                Text("Pasang", fontSize = 11.sp, fontWeight = FontWeight.Bold)
            }
        }
    }
}
