package com.example.ui

import android.Manifest
import android.content.pm.PackageManager
import android.os.Build
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.navigationBars
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Assignment
import androidx.compose.material.icons.filled.CalendarMonth
import androidx.compose.material.icons.filled.Description
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.outlined.Assignment
import androidx.compose.material.icons.outlined.CalendarMonth
import androidx.compose.material.icons.outlined.Description
import androidx.compose.material.icons.outlined.Home
import androidx.compose.material.icons.outlined.Settings
import androidx.compose.material3.Icon
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.content.ContextCompat
import com.example.ui.screens.DashboardScreen
import com.example.ui.screens.NotesScreen
import com.example.ui.screens.ScheduleScreen
import com.example.ui.screens.SettingsScreen
import com.example.ui.screens.TasksScreen
import com.example.ui.theme.AlabasterGrey
import com.example.ui.theme.DuskBlue
import com.example.ui.theme.DustyDenim
import com.example.ui.theme.ElectricCyan
import com.example.ui.theme.InkBlack
import com.example.ui.theme.MahaTheme
import com.example.ui.theme.PrussianBlue
import com.example.ui.viewmodel.MahaSigmaViewModel
import kotlinx.coroutines.flow.collectLatest

sealed class NavItem(
    val title: String,
    val selectedIcon: ImageVector,
    val unselectedIcon: ImageVector,
    val testTag: String
) {
    data object Dashboard : NavItem("Beranda", Icons.Filled.Home, Icons.Outlined.Home, "nav_dashboard")
    data object Schedule : NavItem("Jadwal", Icons.Filled.CalendarMonth, Icons.Outlined.CalendarMonth, "nav_schedule")
    data object Tasks : NavItem("Tugas", Icons.Filled.Assignment, Icons.Outlined.Assignment, "nav_tasks")
    data object Notes : NavItem("Catatan", Icons.Filled.Description, Icons.Outlined.Description, "nav_notes")
    data object Settings : NavItem("Pengaturan", Icons.Filled.Settings, Icons.Outlined.Settings, "nav_settings")
}

@Composable
fun MainAppContainer(
    viewModel: MahaSigmaViewModel,
    modifier: Modifier = Modifier,
    initialTab: Int? = null,
    initialAction: String? = null
) {
    val context = LocalContext.current
    val colors = MahaTheme.colors
    var selectedTabIndex by rememberSaveable { mutableIntStateOf(initialTab?.takeIf { it in 0..4 } ?: 0) }
    val snackbarHostState = remember { SnackbarHostState() }

    LaunchedEffect(initialTab) {
        if (initialTab != null && initialTab in 0..4) {
            selectedTabIndex = initialTab
        }
    }

    // Request notification permission if Android 13+
    val notificationPermissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission()
    ) { _ -> }

    LaunchedEffect(Unit) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            val permissionGranted = ContextCompat.checkSelfPermission(
                context,
                Manifest.permission.POST_NOTIFICATIONS
            ) == PackageManager.PERMISSION_GRANTED

            if (!permissionGranted) {
                notificationPermissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
            }
        }
    }

    // Collect snackbar messages
    LaunchedEffect(viewModel) {
        viewModel.userMessage.collectLatest { message ->
            snackbarHostState.showSnackbar(message)
        }
    }

    val navItems = listOf(
        NavItem.Dashboard,
        NavItem.Schedule,
        NavItem.Tasks,
        NavItem.Notes,
        NavItem.Settings
    )

    Scaffold(
        modifier = modifier.fillMaxSize(),
        containerColor = colors.background,
        contentWindowInsets = WindowInsets.navigationBars,
        snackbarHost = { SnackbarHost(snackbarHostState) },
        bottomBar = {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .border(1.dp, colors.border.copy(alpha = 0.4f), RoundedCornerShape(topStart = 20.dp, topEnd = 20.dp))
                    .clip(RoundedCornerShape(topStart = 20.dp, topEnd = 20.dp))
            ) {
                NavigationBar(
                    containerColor = colors.surface,
                    tonalElevation = 8.dp
                ) {
                    navItems.forEachIndexed { index, item ->
                        val isSelected = selectedTabIndex == index
                        NavigationBarItem(
                            selected = isSelected,
                            onClick = { selectedTabIndex = index },
                            icon = {
                                Icon(
                                    imageVector = if (isSelected) item.selectedIcon else item.unselectedIcon,
                                    contentDescription = item.title,
                                    modifier = Modifier.size(22.dp)
                                )
                            },
                            label = {
                                Text(
                                    text = item.title,
                                    fontSize = 11.sp,
                                    fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal
                                )
                            },
                            colors = NavigationBarItemDefaults.colors(
                                selectedIconColor = colors.accentPrimary,
                                selectedTextColor = colors.accentPrimary,
                                unselectedIconColor = colors.textSecondary,
                                unselectedTextColor = colors.textSecondary,
                                indicatorColor = colors.surfaceVariant
                            ),
                            modifier = Modifier.testTag(item.testTag)
                        )
                    }
                }
            }
        }
    ) { paddingValues ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            when (selectedTabIndex) {
                0 -> DashboardScreen(
                    viewModel = viewModel,
                    onNavigateToSchedule = { selectedTabIndex = 1 },
                    onNavigateToTasks = { selectedTabIndex = 2 },
                    onNavigateToNotes = { selectedTabIndex = 3 },
                    onAddTaskClick = { selectedTabIndex = 2 },
                    onAddNoteClick = { selectedTabIndex = 3 },
                    onCameraNoteClick = { selectedTabIndex = 3 }
                )
                1 -> ScheduleScreen(viewModel = viewModel)
                2 -> TasksScreen(viewModel = viewModel)
                3 -> NotesScreen(viewModel = viewModel)
                4 -> SettingsScreen(viewModel = viewModel)
            }
        }
    }
}
