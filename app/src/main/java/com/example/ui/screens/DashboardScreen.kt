package com.example.ui.screens

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
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Assignment
import androidx.compose.material.icons.filled.Book
import androidx.compose.material.icons.filled.CalendarMonth
import androidx.compose.material.icons.filled.DarkMode
import androidx.compose.material.icons.filled.LightMode
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.ui.components.QuickNotesWidgetCard
import com.example.ui.components.TodayScheduleWidgetCard
import com.example.ui.components.UpcomingTasksWidgetCard
import com.example.ui.theme.AppThemeMode
import com.example.ui.theme.ElectricCyan
import com.example.ui.theme.MahaTheme
import com.example.ui.theme.NeonEmerald
import com.example.ui.theme.SunsetAmber
import com.example.ui.viewmodel.MahaSigmaViewModel
import com.example.util.DateUtils

@Composable
fun DashboardScreen(
    viewModel: MahaSigmaViewModel,
    onNavigateToSchedule: () -> Unit,
    onNavigateToTasks: () -> Unit,
    onNavigateToNotes: () -> Unit,
    onAddTaskClick: () -> Unit,
    onAddNoteClick: () -> Unit,
    onCameraNoteClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val colors = MahaTheme.colors

    val todaySchedules by viewModel.todaySchedules.collectAsStateWithLifecycle()
    val allTasks by viewModel.allTasks.collectAsStateWithLifecycle()
    val allNotes by viewModel.allNotes.collectAsStateWithLifecycle()
    val courses by viewModel.courses.collectAsStateWithLifecycle()
    val themeMode by viewModel.themeMode.collectAsStateWithLifecycle()

    val pendingTasksCount = allTasks.count { !it.task.isCompleted }
    val todayDayName = DateUtils.getDayName(DateUtils.getCurrentDayOfWeek())
    val todayFormatted = DateUtils.formatDate(System.currentTimeMillis())

    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .background(colors.background)
            .testTag("dashboard_screen"),
        contentPadding = PaddingValues(bottom = 96.dp)
    ) {
        // Hero Header
        item {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(colors.surface)
                    .border(
                        1.dp,
                        colors.border.copy(alpha = 0.4f),
                        RoundedCornerShape(bottomStart = 24.dp, bottomEnd = 24.dp)
                    )
                    .clip(RoundedCornerShape(bottomStart = 24.dp, bottomEnd = 24.dp))
                    .padding(20.dp)
            ) {
                Column {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column {
                            Text(
                                text = "MahaSigma // Academic Hub",
                                color = colors.accentPrimary,
                                fontSize = 12.sp,
                                fontFamily = FontFamily.Monospace,
                                fontWeight = FontWeight.Bold
                            )
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                text = "Halo, Mahasiswa! 🎓",
                                color = colors.textPrimary,
                                fontSize = 22.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }

                        Row(verticalAlignment = Alignment.CenterVertically) {
                            // Quick Theme Mode Toggle Button
                            IconButton(
                                onClick = {
                                    val nextMode = when (themeMode) {
                                        AppThemeMode.DARK -> AppThemeMode.LIGHT
                                        AppThemeMode.LIGHT -> AppThemeMode.DARK
                                        AppThemeMode.SYSTEM -> if (colors.isDark) AppThemeMode.LIGHT else AppThemeMode.DARK
                                    }
                                    viewModel.setThemeMode(nextMode)
                                },
                                modifier = Modifier
                                    .size(36.dp)
                                    .clip(CircleShape)
                                    .background(colors.surfaceVariant)
                                    .border(1.dp, colors.borderSubtle, CircleShape)
                            ) {
                                Icon(
                                    imageVector = if (colors.isDark) Icons.Default.LightMode else Icons.Default.DarkMode,
                                    contentDescription = "Ganti Tema",
                                    tint = if (colors.isDark) SunsetAmber else colors.accentPrimary,
                                    modifier = Modifier.size(18.dp)
                                )
                            }

                            Spacer(modifier = Modifier.width(8.dp))

                            // Date Badge
                            Box(
                                modifier = Modifier
                                    .clip(RoundedCornerShape(12.dp))
                                    .background(colors.surfaceVariant)
                                    .border(1.dp, colors.borderSubtle, RoundedCornerShape(12.dp))
                                    .padding(horizontal = 12.dp, vertical = 6.dp)
                            ) {
                                Text(
                                    text = todayDayName,
                                    color = colors.textPrimary,
                                    fontSize = 13.sp,
                                    fontWeight = FontWeight.SemiBold
                                )
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = todayFormatted,
                        color = colors.textSecondary,
                        fontSize = 12.sp
                    )

                    Spacer(modifier = Modifier.height(16.dp))

                    // 3 Metric Badges Row
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        MetricBadge(
                            title = "Jadwal Hari Ini",
                            value = "${todaySchedules.size}",
                            unit = "Kelas",
                            accentColor = ElectricCyan,
                            icon = Icons.Default.CalendarMonth,
                            modifier = Modifier.weight(1f)
                        )
                        MetricBadge(
                            title = "Deadline Aktif",
                            value = "$pendingTasksCount",
                            unit = "Tugas",
                            accentColor = if (pendingTasksCount > 0) SunsetAmber else NeonEmerald,
                            icon = Icons.Default.Assignment,
                            modifier = Modifier.weight(1f)
                        )
                        MetricBadge(
                            title = "Mata Kuliah",
                            value = "${courses.size}",
                            unit = "Total",
                            accentColor = NeonEmerald,
                            icon = Icons.Default.Book,
                            modifier = Modifier.weight(1f)
                        )
                    }
                }
            }
        }

        // Widget 1: Jadwal Kuliah Hari Ini
        item {
            TodayScheduleWidgetCard(
                todaySchedules = todaySchedules,
                onViewAllClick = onNavigateToSchedule,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(start = 16.dp, end = 16.dp, top = 16.dp)
            )
        }

        // Widget 2: Deadline Tugas (Diurutkan dari yang paling dekat deadline)
        item {
            UpcomingTasksWidgetCard(
                allTasks = allTasks,
                onToggleTask = { taskWithCourse -> viewModel.toggleTaskCompletion(taskWithCourse.task) },
                onViewAllClick = onNavigateToTasks,
                onAddTaskClick = onAddTaskClick,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(start = 16.dp, end = 16.dp, top = 16.dp)
            )
        }

        // Widget 3: Akses Cepat Fitur Catatan (Tulis Cepat, Foto Papan Tulis, Preview)
        item {
            QuickNotesWidgetCard(
                allNotes = allNotes,
                onNewNoteClick = onAddNoteClick,
                onCameraNoteClick = onCameraNoteClick,
                onViewAllClick = onNavigateToNotes,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(start = 16.dp, end = 16.dp, top = 16.dp, bottom = 24.dp)
            )
        }
    }
}

@Composable
fun MetricBadge(
    title: String,
    value: String,
    unit: String,
    accentColor: Color,
    icon: ImageVector,
    modifier: Modifier = Modifier
) {
    val colors = MahaTheme.colors

    Box(
        modifier = modifier
            .clip(RoundedCornerShape(14.dp))
            .background(colors.surfaceVariant)
            .border(1.dp, colors.borderSubtle, RoundedCornerShape(14.dp))
            .padding(12.dp)
    ) {
        Column {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = title,
                    color = colors.textSecondary,
                    fontSize = 10.sp,
                    fontWeight = FontWeight.Medium,
                    maxLines = 1
                )
                Icon(
                    imageVector = icon,
                    contentDescription = null,
                    tint = accentColor,
                    modifier = Modifier.size(16.dp)
                )
            }
            Spacer(modifier = Modifier.height(6.dp))
            Row(verticalAlignment = Alignment.Bottom) {
                Text(
                    text = value,
                    color = colors.textPrimary,
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Bold,
                    fontFamily = FontFamily.Monospace
                )
                Spacer(modifier = Modifier.width(4.dp))
                Text(
                    text = unit,
                    color = colors.textSecondary,
                    fontSize = 10.sp,
                    modifier = Modifier.padding(bottom = 2.dp)
                )
            }
        }
    }
}
