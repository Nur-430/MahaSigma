package com.example.ui.screens

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.BorderStroke
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
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Book
import androidx.compose.material.icons.filled.CloudDownload
import androidx.compose.material.icons.filled.CloudUpload
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.PictureAsPdf
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Share
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.data.entity.CourseEntity
import com.example.ui.components.AboutAppSection
import com.example.ui.components.BackgroundPdfImportDialog
import com.example.ui.components.ThemeSelectorCard
import com.example.ui.components.WidgetGuideCard
import com.example.ui.theme.CrimsonRed
import com.example.ui.theme.InkBlack
import com.example.ui.theme.MahaTheme
import com.example.ui.theme.NeonEmerald
import com.example.ui.theme.SunsetAmber
import com.example.ui.viewmodel.MahaSigmaViewModel
import kotlinx.coroutines.launch

@Composable
fun SettingsScreen(
    viewModel: MahaSigmaViewModel,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val coroutineScope = rememberCoroutineScope()
    val courses by viewModel.courses.collectAsStateWithLifecycle()
    val themeMode by viewModel.themeMode.collectAsStateWithLifecycle()
    val colors = MahaTheme.colors

    var showAddEditCourseDialog by remember { mutableStateOf(false) }
    var courseToEdit by remember { mutableStateOf<CourseEntity?>(null) }
    var courseToDelete by remember { mutableStateOf<CourseEntity?>(null) }

    // Export Dialog state
    var showExportDialog by remember { mutableStateOf(false) }
    var exportedJsonText by remember { mutableStateOf("") }

    // Import Dialog state
    var showImportDialog by remember { mutableStateOf(false) }
    var importInputText by remember { mutableStateOf("") }

    // Reset Confirmation Dialog
    var showResetConfirmDialog by remember { mutableStateOf(false) }

    val pdfPickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.OpenDocument()
    ) { uri: Uri? ->
        if (uri != null) {
            viewModel.startBackgroundTableImport(context, uri)
        }
    }

    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .background(colors.background),
        contentPadding = PaddingValues(top = 16.dp, start = 16.dp, end = 16.dp, bottom = 120.dp),
        verticalArrangement = Arrangement.spacedBy(18.dp)
    ) {
        // Section Header
        item {
            Column {
                Text(
                    text = "Pengaturan & Manajemen",
                    color = colors.textPrimary,
                    fontSize = 22.sp,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    text = "Mode tampilan, widget beranda, backup JSON, & info pengembang",
                    color = colors.textSecondary,
                    fontSize = 12.sp
                )
            }
        }

        // Section: Mode Tampilan (Dark / Light)
        item {
            ThemeSelectorCard(
                currentMode = themeMode,
                onModeSelected = { viewModel.setThemeMode(it) }
            )
        }

        // Section: Widget Home Screen HP
        item {
            WidgetGuideCard()
        }

        // Section 1: Manajemen Mata Kuliah
        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = colors.surface),
                shape = RoundedCornerShape(16.dp),
                border = BorderStroke(1.dp, colors.borderSubtle)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Default.Book, contentDescription = null, tint = colors.accentPrimary, modifier = Modifier.size(20.dp))
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = "Mata Kuliah Semester Ini",
                                color = colors.textPrimary,
                                fontSize = 16.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }

                        FilledTonalButton(
                            onClick = {
                                courseToEdit = null
                                showAddEditCourseDialog = true
                            },
                            colors = ButtonDefaults.filledTonalButtonColors(
                                containerColor = colors.surfaceVariant,
                                contentColor = colors.accentPrimary
                            ),
                            shape = RoundedCornerShape(10.dp)
                        ) {
                            Icon(Icons.Default.Add, contentDescription = null, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("Tambah", fontSize = 12.sp)
                        }
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    if (courses.isEmpty()) {
                        Text(
                            text = "Belum ada mata kuliah yang didaftarkan.",
                            color = colors.textSecondary,
                            fontSize = 13.sp,
                            modifier = Modifier.padding(vertical = 8.dp)
                        )
                    } else {
                        Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                            courses.forEach { course ->
                                CourseItemRow(
                                    course = course,
                                    onEdit = {
                                        courseToEdit = course
                                        showAddEditCourseDialog = true
                                    },
                                    onDelete = {
                                        courseToDelete = course
                                    }
                                )
                            }
                        }
                    }
                }
            }
        }

        // Section: Impor Jadwal Kuliah dari PDF (Proses di Belakang Layar)
        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = colors.surface),
                shape = RoundedCornerShape(16.dp),
                border = BorderStroke(1.dp, colors.borderSubtle)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Default.PictureAsPdf, contentDescription = null, tint = CrimsonRed, modifier = Modifier.size(20.dp))
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "Impor Tabel Jadwal dari PDF",
                            color = colors.textPrimary,
                            fontSize = 16.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }

                    Spacer(modifier = Modifier.height(6.dp))
                    Text(
                        text = "Pilih file PDF KRS atau spreadsheet jadwal dari portal kampus (SIAKAD). Sistem akan memproses dan mengekstrak tabel jadwal mata kuliah langsung di latar belakang secara otomatis.",
                        color = colors.textSecondary,
                        fontSize = 12.sp,
                        lineHeight = 18.sp
                    )

                    Spacer(modifier = Modifier.height(14.dp))

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
                        colors = ButtonDefaults.buttonColors(containerColor = colors.accentPrimary),
                        shape = RoundedCornerShape(10.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Icon(Icons.Default.PictureAsPdf, contentDescription = null, tint = InkBlack, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("Pilih Berkas PDF / Excel", color = InkBlack, fontWeight = FontWeight.Bold)
                    }
                }
            }
        }

        // Section 2: Backup & Restore (JSON)
        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = colors.surface),
                shape = RoundedCornerShape(16.dp),
                border = BorderStroke(1.dp, colors.borderSubtle)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Default.CloudUpload, contentDescription = null, tint = SunsetAmber, modifier = Modifier.size(20.dp))
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "Cadangan & Pemulihan (JSON)",
                            color = colors.textPrimary,
                            fontSize = 16.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }

                    Spacer(modifier = Modifier.height(6.dp))
                    Text(
                        text = "Ekspor seluruh jadwal, tugas, catatan, dan mata kuliah ke berkas teks JSON. Aman, mandiri, dan dapat dipindahkan antar perangkat tanpa cloud server.",
                        color = colors.textSecondary,
                        fontSize = 12.sp,
                        lineHeight = 18.sp
                    )

                    Spacer(modifier = Modifier.height(14.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        // Export Button
                        OutlinedButton(
                            onClick = {
                                coroutineScope.launch {
                                    val json = viewModel.createBackupJson()
                                    exportedJsonText = json
                                    showExportDialog = true
                                }
                            },
                            colors = ButtonDefaults.outlinedButtonColors(contentColor = colors.accentPrimary),
                            modifier = Modifier.weight(1f),
                            shape = RoundedCornerShape(10.dp),
                            border = BorderStroke(1.dp, colors.accentPrimary.copy(alpha = 0.5f))
                        ) {
                            Icon(Icons.Default.CloudUpload, contentDescription = null, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(6.dp))
                            Text("Ekspor JSON", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                        }

                        // Import Button
                        OutlinedButton(
                            onClick = {
                                importInputText = ""
                                showImportDialog = true
                            },
                            colors = ButtonDefaults.outlinedButtonColors(contentColor = NeonEmerald),
                            modifier = Modifier.weight(1f),
                            shape = RoundedCornerShape(10.dp),
                            border = BorderStroke(1.dp, NeonEmerald.copy(alpha = 0.5f))
                        ) {
                            Icon(Icons.Default.CloudDownload, contentDescription = null, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(6.dp))
                            Text("Impor JSON", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                        }
                    }
                }
            }
        }

        // Section 3: Tentang Aplikasi & Informasi Pengembang (Versi Ringkas & Inti)
        item {
            AboutAppSection()
        }

        // Section 4: Tindakan Berbahaya (Reset Data)
        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = colors.surface),
                shape = RoundedCornerShape(16.dp),
                border = BorderStroke(1.dp, CrimsonRed.copy(alpha = 0.3f))
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Default.Refresh, contentDescription = null, tint = CrimsonRed, modifier = Modifier.size(20.dp))
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "Zona Berbahaya",
                            color = CrimsonRed,
                            fontSize = 15.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                    Spacer(modifier = Modifier.height(6.dp))
                    Text(
                        text = "Mengosongkan seluruh database lokal (jadwal, tugas, dan catatan). Tindakan ini tidak dapat dibatalkan.",
                        color = colors.textSecondary,
                        fontSize = 11.sp
                    )
                    Spacer(modifier = Modifier.height(12.dp))
                    OutlinedButton(
                        onClick = { showResetConfirmDialog = true },
                        colors = ButtonDefaults.outlinedButtonColors(contentColor = CrimsonRed),
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(10.dp),
                        border = BorderStroke(1.dp, CrimsonRed.copy(alpha = 0.5f))
                    ) {
                        Icon(Icons.Default.Refresh, contentDescription = null, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(6.dp))
                        Text("Reset / Kosongkan Seluruh Data", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                    }
                }
            }
        }
    }

    // Dialog Tambah/Ubah Matkul
    if (showAddEditCourseDialog) {
        AddEditCourseDialog(
            course = courseToEdit,
            onDismiss = { showAddEditCourseDialog = false },
            onSave = { savedCourse ->
                viewModel.saveCourse(savedCourse)
                showAddEditCourseDialog = false
            }
        )
    }

    // Dialog Konfirmasi Hapus Matkul
    if (courseToDelete != null) {
        AlertDialog(
            onDismissRequest = { courseToDelete = null },
            containerColor = colors.surface,
            title = { Text("Hapus Mata Kuliah?", color = colors.textPrimary) },
            text = {
                Text(
                    "Menghapus '${courseToDelete?.name}' juga akan menghapus jadwal terkait yang terhubung.",
                    color = colors.textSecondary
                )
            },
            confirmButton = {
                Button(
                    onClick = {
                        courseToDelete?.let { viewModel.deleteCourse(it) }
                        courseToDelete = null
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = CrimsonRed)
                ) {
                    Text("Hapus", color = Color.White)
                }
            },
            dismissButton = {
                TextButton(onClick = { courseToDelete = null }) {
                    Text("Batal", color = colors.textSecondary)
                }
            }
        )
    }

    // Dialog Ekspor JSON
    if (showExportDialog) {
        AlertDialog(
            onDismissRequest = { showExportDialog = false },
            containerColor = colors.surface,
            title = {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Default.CloudUpload, contentDescription = null, tint = colors.accentPrimary)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Hasil Ekspor Backup JSON", color = colors.textPrimary, fontSize = 18.sp, fontWeight = FontWeight.Bold)
                }
            },
            text = {
                Column(modifier = Modifier.fillMaxWidth()) {
                    Text(
                        text = "Data database siap dicadangkan. Kamu dapat menyalin teks JSON atau membagikannya ke aplikasi lain.",
                        color = colors.textSecondary,
                        fontSize = 12.sp
                    )
                    Spacer(modifier = Modifier.height(10.dp))
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(180.dp)
                            .clip(RoundedCornerShape(8.dp))
                            .background(colors.surfaceVariant)
                            .border(1.dp, colors.borderSubtle, RoundedCornerShape(8.dp))
                            .padding(8.dp)
                            .verticalScroll(rememberScrollState())
                    ) {
                        Text(
                            text = exportedJsonText,
                            color = colors.textPrimary,
                            fontSize = 11.sp,
                            fontFamily = FontFamily.Monospace
                        )
                    }
                }
            },
            confirmButton = {
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    // Copy button
                    FilledTonalButton(
                        onClick = {
                            val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
                            val clip = ClipData.newPlainText("MahaSigma Backup JSON", exportedJsonText)
                            clipboard.setPrimaryClip(clip)
                            Toast.makeText(context, "JSON berhasil disalin ke Clipboard!", Toast.LENGTH_SHORT).show()
                        },
                        colors = ButtonDefaults.filledTonalButtonColors(
                            containerColor = colors.surfaceVariant,
                            contentColor = colors.accentPrimary
                        )
                    ) {
                        Icon(Icons.Default.ContentCopy, contentDescription = null, modifier = Modifier.size(14.dp))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("Salin JSON", fontSize = 12.sp)
                    }

                    // Share button
                    Button(
                        onClick = {
                            val shareIntent = Intent(Intent.ACTION_SEND).apply {
                                type = "text/plain"
                                putExtra(Intent.EXTRA_SUBJECT, "MahaSigma Backup Data.json")
                                putExtra(Intent.EXTRA_TEXT, exportedJsonText)
                            }
                            context.startActivity(Intent.createChooser(shareIntent, "Bagikan Cadangan MahaSigma"))
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = colors.accentPrimary)
                    ) {
                        Icon(Icons.Default.Share, contentDescription = null, modifier = Modifier.size(14.dp), tint = InkBlack)
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("Bagikan", fontSize = 12.sp, color = InkBlack, fontWeight = FontWeight.Bold)
                    }
                }
            },
            dismissButton = {
                TextButton(onClick = { showExportDialog = false }) {
                    Text("Tutup", color = colors.textSecondary)
                }
            }
        )
    }

    // Dialog Impor JSON
    if (showImportDialog) {
        var importErrorMsg by remember { mutableStateOf<String?>(null) }

        AlertDialog(
            onDismissRequest = { showImportDialog = false },
            containerColor = colors.surface,
            title = {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Default.CloudDownload, contentDescription = null, tint = NeonEmerald)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Pulihkan dari Teks JSON", color = colors.textPrimary, fontSize = 18.sp, fontWeight = FontWeight.Bold)
                }
            },
            text = {
                Column(modifier = Modifier.fillMaxWidth()) {
                    Text(
                        text = "Tempelkan teks JSON backup MahaSigma ke kotak di bawah untuk memulihkan seluruh data.",
                        color = colors.textSecondary,
                        fontSize = 12.sp
                    )
                    Spacer(modifier = Modifier.height(10.dp))

                    OutlinedTextField(
                        value = importInputText,
                        onValueChange = {
                            importInputText = it
                            importErrorMsg = null
                        },
                        label = { Text("Teks JSON Backup") },
                        placeholder = { Text("{\n  \"version\": 1,\n  \"courses\": [...]\n}") },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(180.dp),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedTextColor = colors.textPrimary,
                            unfocusedTextColor = colors.textPrimary,
                            focusedBorderColor = NeonEmerald,
                            unfocusedBorderColor = colors.borderSubtle
                        )
                    )

                    if (importErrorMsg != null) {
                        Spacer(modifier = Modifier.height(6.dp))
                        Text(
                            text = importErrorMsg!!,
                            color = CrimsonRed,
                            fontSize = 12.sp
                        )
                    }
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        if (importInputText.isBlank()) {
                            importErrorMsg = "Teks JSON tidak boleh kosong."
                            return@Button
                        }

                        viewModel.restoreFromJson(importInputText) { success, message ->
                            if (success) {
                                Toast.makeText(context, message, Toast.LENGTH_LONG).show()
                                showImportDialog = false
                            } else {
                                importErrorMsg = message
                            }
                        }
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = NeonEmerald)
                ) {
                    Text("Pulihkan Sekarang", color = InkBlack, fontWeight = FontWeight.Bold)
                }
            },
            dismissButton = {
                TextButton(onClick = { showImportDialog = false }) {
                    Text("Batal", color = colors.textSecondary)
                }
            }
        )
    }

    // Dialog Konfirmasi Reset Data
    if (showResetConfirmDialog) {
        AlertDialog(
            onDismissRequest = { showResetConfirmDialog = false },
            containerColor = colors.surface,
            title = { Text("Kosongkan Seluruh Data?", color = CrimsonRed, fontWeight = FontWeight.Bold) },
            text = {
                Text(
                    "Semua mata kuliah, jadwal, tugas, dan catatan materi akan dihapus permanen dari memori internal HP kamu. Tindakan ini tidak dapat dibatalkan.",
                    color = colors.textPrimary
                )
            },
            confirmButton = {
                Button(
                    onClick = {
                        viewModel.resetAllData {
                            Toast.makeText(context, "Seluruh data telah dikosongkan.", Toast.LENGTH_SHORT).show()
                        }
                        showResetConfirmDialog = false
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = CrimsonRed)
                ) {
                    Text("Ya, Kosongkan", color = Color.White)
                }
            },
            dismissButton = {
                TextButton(onClick = { showResetConfirmDialog = false }) {
                    Text("Batal", color = colors.textSecondary)
                }
            }
        )
    }

    // Dialog Impor Jadwal dari PDF (Proses di Belakang Layar)
    BackgroundPdfImportDialog(viewModel = viewModel)
}

@Composable
fun CourseItemRow(
    course: CourseEntity,
    onEdit: () -> Unit,
    onDelete: () -> Unit
) {
    val colors = MahaTheme.colors
    val courseColor = try {
        Color(android.graphics.Color.parseColor(course.colorHex))
    } catch (e: Exception) {
        colors.accentPrimary
    }

    val isPraktik = course.code.equals("Praktik", ignoreCase = true) ||
                    course.name.contains("Praktik", ignoreCase = true)
    val badgeLabel = if (isPraktik) "Praktik" else "Teori"
    val badgeColor = if (isPraktik) SunsetAmber else colors.accentPrimary
    val badgeBg = if (isPraktik) SunsetAmber.copy(alpha = 0.15f) else colors.accentPrimary.copy(alpha = 0.15f)

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(10.dp))
            .background(colors.surfaceVariant)
            .border(1.dp, colors.borderSubtle, RoundedCornerShape(10.dp))
            .padding(horizontal = 12.dp, vertical = 10.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            modifier = Modifier
                .size(14.dp)
                .clip(CircleShape)
                .background(courseColor)
        )
        Spacer(modifier = Modifier.width(10.dp))

        Column(modifier = Modifier.weight(1f)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                // Badge Keterangan Teori / Praktik
                Surface(
                    shape = RoundedCornerShape(6.dp),
                    color = badgeBg,
                    border = BorderStroke(1.dp, badgeColor.copy(alpha = 0.5f))
                ) {
                    Text(
                        text = badgeLabel,
                        color = badgeColor,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        fontFamily = FontFamily.Monospace,
                        modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                    )
                }
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = course.name,
                    color = colors.textPrimary,
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Bold
                )
            }
            if (course.lecturer.isNotBlank()) {
                Spacer(modifier = Modifier.height(2.dp))
                Text(
                    text = course.lecturer,
                    color = colors.textSecondary,
                    fontSize = 11.sp
                )
            }
        }

        IconButton(onClick = onEdit, modifier = Modifier.size(32.dp)) {
            Icon(Icons.Default.Edit, contentDescription = "Edit", tint = colors.textSecondary, modifier = Modifier.size(16.dp))
        }

        IconButton(onClick = onDelete, modifier = Modifier.size(32.dp)) {
            Icon(Icons.Default.Delete, contentDescription = "Hapus", tint = CrimsonRed.copy(alpha = 0.8f), modifier = Modifier.size(16.dp))
        }
    }
}

@Composable
fun AddEditCourseDialog(
    course: CourseEntity?,
    onDismiss: () -> Unit,
    onSave: (CourseEntity) -> Unit
) {
    val colors = MahaTheme.colors
    var name by remember { mutableStateOf(course?.name ?: "") }
    var code by remember { mutableStateOf(course?.code ?: "") }
    var lecturer by remember { mutableStateOf(course?.lecturer ?: "") }
    var colorHex by remember { mutableStateOf(course?.colorHex ?: "#38BDF8") }

    val presetColors = listOf(
        "#38BDF8", // Electric Cyan
        "#10B981", // Emerald Green
        "#F59E0B", // Sunset Amber
        "#EF4444", // Crimson Red
        "#A855F7", // Purple
        "#6366F1", // Indigo
        "#F97316", // Orange
        "#14B8A6"  // Mint
    )

    AlertDialog(
        onDismissRequest = onDismiss,
        containerColor = colors.surface,
        title = {
            Text(
                text = if (course == null) "Tambah Mata Kuliah" else "Ubah Mata Kuliah",
                color = colors.textPrimary,
                fontSize = 18.sp,
                fontWeight = FontWeight.Bold
            )
        },
        text = {
            Column(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                OutlinedTextField(
                    value = name,
                    onValueChange = { name = it },
                    label = { Text("Nama Mata Kuliah *") },
                    placeholder = { Text("e.g. Struktur Data & Algoritma") },
                    modifier = Modifier.fillMaxWidth(),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedTextColor = colors.textPrimary,
                        unfocusedTextColor = colors.textPrimary,
                        focusedBorderColor = colors.accentPrimary,
                        unfocusedBorderColor = colors.borderSubtle
                    )
                )

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    OutlinedTextField(
                        value = code,
                        onValueChange = { code = it },
                        label = { Text("Kode Matkul") },
                        placeholder = { Text("e.g. IF201") },
                        modifier = Modifier.weight(1f),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedTextColor = colors.textPrimary,
                            unfocusedTextColor = colors.textPrimary,
                            focusedBorderColor = colors.accentPrimary,
                            unfocusedBorderColor = colors.borderSubtle
                        )
                    )

                    OutlinedTextField(
                        value = lecturer,
                        onValueChange = { lecturer = it },
                        label = { Text("Dosen Pengampu") },
                        placeholder = { Text("e.g. Dr. Budi") },
                        modifier = Modifier.weight(1.5f),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedTextColor = colors.textPrimary,
                            unfocusedTextColor = colors.textPrimary,
                            focusedBorderColor = colors.accentPrimary,
                            unfocusedBorderColor = colors.borderSubtle
                        )
                    )
                }

                Spacer(modifier = Modifier.height(4.dp))
                Text(text = "Pilih Warna Aksen Matkul:", color = colors.textSecondary, fontSize = 12.sp)

                // Color Swatches
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    presetColors.forEach { hex ->
                        val isSelected = colorHex.equals(hex, ignoreCase = true)
                        val swatchColor = Color(android.graphics.Color.parseColor(hex))

                        Box(
                            modifier = Modifier
                                .size(32.dp)
                                .clip(CircleShape)
                                .background(swatchColor)
                                .border(
                                    if (isSelected) 3.dp else 1.dp,
                                    if (isSelected) colors.accentPrimary else Color.Transparent,
                                    CircleShape
                                )
                                .clickable { colorHex = hex }
                        )
                    }
                }
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    if (name.isNotBlank()) {
                        onSave(
                            CourseEntity(
                                id = course?.id ?: 0,
                                name = name.trim(),
                                code = code.trim().uppercase(),
                                lecturer = lecturer.trim(),
                                colorHex = colorHex
                            )
                        )
                    }
                },
                colors = ButtonDefaults.buttonColors(containerColor = colors.accentPrimary)
            ) {
                Text("Simpan", color = InkBlack, fontWeight = FontWeight.Bold)
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Batal", color = colors.textSecondary)
            }
        }
    )
}
