package com.example.ui.screens

import android.content.Context
import android.net.Uri
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.PickVisualMediaRequest
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
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.AddPhotoAlternate
import androidx.compose.material.icons.filled.ArrowDropDown
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Description
import androidx.compose.material.icons.filled.Download
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.FilterList
import androidx.compose.material.icons.filled.Image
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.PhotoCamera
import androidx.compose.material.icons.filled.PictureAsPdf
import androidx.compose.material.icons.filled.Share
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Checkbox
import androidx.compose.material3.CheckboxDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MenuAnchorType
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Scaffold
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
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import coil.compose.AsyncImage
import coil.request.ImageRequest
import com.example.data.entity.CourseEntity
import com.example.data.entity.NoteEntity
import com.example.data.entity.NoteWithCourse
import com.example.ui.components.CourseTag
import com.example.ui.theme.CrimsonRed
import com.example.ui.theme.ElectricCyan
import com.example.ui.theme.InkBlack
import com.example.ui.theme.MahaTheme
import com.example.ui.theme.NeonEmerald
import com.example.ui.theme.SunsetAmber
import com.example.ui.viewmodel.MahaSigmaViewModel
import com.example.util.DateUtils
import com.example.util.PdfExportHelper
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.File

@Composable
fun NotesScreen(
    viewModel: MahaSigmaViewModel,
    modifier: Modifier = Modifier
) {
    val colors = MahaTheme.colors
    val context = LocalContext.current
    val coroutineScope = rememberCoroutineScope()

    val filteredNotes by viewModel.filteredNotes.collectAsStateWithLifecycle()
    val courses by viewModel.courses.collectAsStateWithLifecycle()
    val selectedCourseFilter by viewModel.selectedNoteCourseFilter.collectAsStateWithLifecycle()

    var showAddEditDialog by remember { mutableStateOf(false) }
    var noteToEdit by remember { mutableStateOf<NoteEntity?>(null) }
    var noteToDelete by remember { mutableStateOf<NoteEntity?>(null) }
    var noteToViewDetail by remember { mutableStateOf<NoteWithCourse?>(null) }
    var noteToExportPdf by remember { mutableStateOf<NoteWithCourse?>(null) }
    var fullScreenImageUrl by remember { mutableStateOf<String?>(null) }
    var filterDropdownExpanded by remember { mutableStateOf(false) }

    val activeSelectedCourse = courses.firstOrNull { it.id == selectedCourseFilter }

    // SAF Document Creator launcher for saving PDF to storage
    val createPdfDocLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.CreateDocument("application/pdf")
    ) { uri: Uri? ->
        if (uri != null && noteToExportPdf != null) {
            val item = noteToExportPdf!!
            coroutineScope.launch(Dispatchers.IO) {
                try {
                    context.contentResolver.openOutputStream(uri)?.use { outputStream ->
                        PdfExportHelper.exportNoteToPdfStream(context, item.note, item.course, outputStream)
                    }
                    withContext(Dispatchers.Main) {
                        Toast.makeText(context, "PDF berhasil disimpan ke perangkat!", Toast.LENGTH_SHORT).show()
                        noteToExportPdf = null
                    }
                } catch (e: Exception) {
                    e.printStackTrace()
                    withContext(Dispatchers.Main) {
                        Toast.makeText(context, "Gagal menyimpan PDF: ${e.message}", Toast.LENGTH_LONG).show()
                    }
                }
            }
        }
    }

    Scaffold(
        modifier = modifier.fillMaxSize(),
        containerColor = colors.background,
        floatingActionButton = {
            FloatingActionButton(
                onClick = {
                    noteToEdit = null
                    showAddEditDialog = true
                },
                containerColor = colors.accentPrimary,
                contentColor = InkBlack,
                modifier = Modifier
                    .padding(bottom = 80.dp)
                    .testTag("add_note_fab")
            ) {
                Icon(imageVector = Icons.Default.Add, contentDescription = "Tambah Catatan")
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
                    .background(colors.surface)
                    .border(1.dp, colors.borderSubtle, RoundedCornerShape(bottomStart = 20.dp, bottomEnd = 20.dp))
                    .padding(16.dp)
            ) {
                Column {
                    Text(
                        text = "Catatan & Foto Papan Tulis",
                        color = colors.textPrimary,
                        fontSize = 20.sp,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = "Simpan hingga 5 foto materi & ekspor rapi ke format PDF",
                        color = colors.textSecondary,
                        fontSize = 12.sp
                    )

                    Spacer(modifier = Modifier.height(14.dp))

                    // Course Filter Dropdown
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Box(modifier = Modifier.weight(1f)) {
                            Surface(
                                onClick = { filterDropdownExpanded = true },
                                shape = RoundedCornerShape(10.dp),
                                color = colors.surfaceVariant,
                                border = BorderStroke(
                                    1.dp,
                                    if (selectedCourseFilter != null) colors.accentPrimary else colors.borderSubtle
                                ),
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(horizontal = 12.dp, vertical = 9.dp),
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.SpaceBetween
                                ) {
                                    Row(
                                        verticalAlignment = Alignment.CenterVertically,
                                        modifier = Modifier.weight(1f)
                                    ) {
                                        Icon(
                                            imageVector = Icons.Default.FilterList,
                                            contentDescription = "Filter Matkul",
                                            tint = if (selectedCourseFilter != null) colors.accentPrimary else colors.textSecondary,
                                            modifier = Modifier.size(18.dp)
                                        )
                                        Spacer(modifier = Modifier.width(8.dp))

                                        if (activeSelectedCourse != null) {
                                            val dotColor = try {
                                                Color(android.graphics.Color.parseColor(activeSelectedCourse.colorHex))
                                            } catch (e: Exception) {
                                                colors.accentPrimary
                                            }
                                            Box(
                                                modifier = Modifier
                                                    .size(8.dp)
                                                    .clip(CircleShape)
                                                    .background(dotColor)
                                            )
                                            Spacer(modifier = Modifier.width(6.dp))
                                            Text(
                                                text = activeSelectedCourse.name,
                                                color = colors.textPrimary,
                                                fontSize = 13.sp,
                                                fontWeight = FontWeight.SemiBold,
                                                maxLines = 1,
                                                overflow = TextOverflow.Ellipsis
                                            )
                                        } else {
                                            Text(
                                                text = "Semua Mata Kuliah (${filteredNotes.size} Catatan)",
                                                color = colors.textPrimary,
                                                fontSize = 13.sp,
                                                fontWeight = FontWeight.Medium
                                            )
                                        }
                                    }

                                    Icon(
                                        imageVector = Icons.Default.ArrowDropDown,
                                        contentDescription = "Pilih",
                                        tint = colors.textSecondary,
                                        modifier = Modifier.size(20.dp)
                                    )
                                }
                            }

                            // Dropdown Options
                            DropdownMenu(
                                expanded = filterDropdownExpanded,
                                onDismissRequest = { filterDropdownExpanded = false },
                                modifier = Modifier
                                    .fillMaxWidth(0.85f)
                                    .background(colors.surface)
                            ) {
                                DropdownMenuItem(
                                    text = {
                                        Text(
                                            text = "Semua Mata Kuliah",
                                            color = if (selectedCourseFilter == null) colors.accentPrimary else colors.textPrimary,
                                            fontWeight = if (selectedCourseFilter == null) FontWeight.Bold else FontWeight.Normal,
                                            fontSize = 13.sp
                                        )
                                    },
                                    leadingIcon = {
                                        Icon(
                                            imageVector = Icons.Default.Check,
                                            contentDescription = null,
                                            tint = if (selectedCourseFilter == null) colors.accentPrimary else Color.Transparent,
                                            modifier = Modifier.size(16.dp)
                                        )
                                    },
                                    onClick = {
                                        viewModel.setSelectedNoteCourseFilter(null)
                                        filterDropdownExpanded = false
                                    }
                                )

                                if (courses.isNotEmpty()) {
                                    HorizontalDivider(color = colors.borderSubtle.copy(alpha = 0.5f), modifier = Modifier.padding(vertical = 4.dp))
                                }

                                courses.forEach { course ->
                                    val isSelected = selectedCourseFilter == course.id
                                    val courseColor = try {
                                        Color(android.graphics.Color.parseColor(course.colorHex))
                                    } catch (e: Exception) {
                                        colors.accentPrimary
                                    }

                                    DropdownMenuItem(
                                        text = {
                                            Row(
                                                verticalAlignment = Alignment.CenterVertically,
                                                modifier = Modifier.fillMaxWidth()
                                            ) {
                                                Box(
                                                    modifier = Modifier
                                                        .size(9.dp)
                                                        .clip(CircleShape)
                                                        .background(courseColor)
                                                )
                                                Spacer(modifier = Modifier.width(8.dp))
                                                Text(
                                                    text = course.name,
                                                    color = if (isSelected) colors.accentPrimary else colors.textPrimary,
                                                    fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                                                    fontSize = 13.sp,
                                                    maxLines = 1,
                                                    overflow = TextOverflow.Ellipsis
                                                )
                                            }
                                        },
                                        leadingIcon = {
                                            Icon(
                                                imageVector = Icons.Default.Check,
                                                contentDescription = null,
                                                tint = if (isSelected) colors.accentPrimary else Color.Transparent,
                                                modifier = Modifier.size(16.dp)
                                            )
                                        },
                                        onClick = {
                                            viewModel.setSelectedNoteCourseFilter(course.id)
                                            filterDropdownExpanded = false
                                        }
                                    )
                                }
                            }
                        }

                        // Reset filter icon if active
                        if (selectedCourseFilter != null) {
                            Spacer(modifier = Modifier.width(8.dp))
                            IconButton(
                                onClick = { viewModel.setSelectedNoteCourseFilter(null) },
                                modifier = Modifier
                                    .size(38.dp)
                                    .clip(RoundedCornerShape(10.dp))
                                    .background(colors.surfaceVariant)
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Close,
                                    contentDescription = "Hapus Filter",
                                    tint = colors.textSecondary,
                                    modifier = Modifier.size(18.dp)
                                )
                            }
                        }
                    }
                }
            }

            // Notes List
            if (filteredNotes.isEmpty()) {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(32.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Icon(
                            imageVector = Icons.Default.Description,
                            contentDescription = null,
                            tint = colors.textSecondary,
                            modifier = Modifier.size(56.dp)
                        )
                        Spacer(modifier = Modifier.height(12.dp))
                        Text(
                            text = if (selectedCourseFilter != null) "Tidak ada catatan untuk mata kuliah ini" else "Belum Ada Catatan Kuliah",
                            color = colors.textPrimary,
                            fontSize = 16.sp,
                            fontWeight = FontWeight.Bold
                        )
                        Spacer(modifier = Modifier.height(6.dp))
                        Text(
                            text = if (selectedCourseFilter != null) "Coba pilih mata kuliah lain atau tambah catatan baru." else "Simpan rangkuman materi dosen, lampirkan hingga 5 foto papan tulis, dan ekspor ke PDF.",
                            color = colors.textSecondary,
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
                    items(filteredNotes, key = { it.note.id }) { item ->
                        NoteCard(
                            noteWithCourse = item,
                            onClick = { noteToViewDetail = item },
                            onEditClick = {
                                noteToEdit = item.note
                                showAddEditDialog = true
                            },
                            onDeleteClick = {
                                noteToDelete = item.note
                            },
                            onExportPdfClick = {
                                noteToExportPdf = item
                            }
                        )
                    }
                }
            }
        }
    }

    // Detail Dialog / Full Reader
    if (noteToViewDetail != null) {
        NoteDetailDialog(
            noteWithCourse = noteToViewDetail!!,
            onDismiss = { noteToViewDetail = null },
            onEdit = {
                val note = noteToViewDetail!!.note
                noteToViewDetail = null
                noteToEdit = note
                showAddEditDialog = true
            },
            onExportPdf = {
                val item = noteToViewDetail!!
                noteToExportPdf = item
            },
            onImageClick = { path ->
                fullScreenImageUrl = path
            }
        )
    }

    // Dialog Tambah / Edit Catatan (Bisa simpan sampai 5 foto)
    if (showAddEditDialog) {
        AddEditNoteDialog(
            note = noteToEdit,
            courses = courses,
            onDismiss = { showAddEditDialog = false },
            onSave = { id, courseId, title, content, newUris, enhance, existingPaths ->
                viewModel.saveNote(
                    id = id,
                    courseId = courseId,
                    title = title,
                    content = content,
                    newImageUris = newUris,
                    enhanceImage = enhance,
                    existingImagePaths = existingPaths
                )
                showAddEditDialog = false
            }
        )
    }

    // Dialog Konfirmasi Hapus Catatan
    if (noteToDelete != null) {
        AlertDialog(
            onDismissRequest = { noteToDelete = null },
            containerColor = colors.surface,
            title = { Text("Hapus Catatan?", color = colors.textPrimary) },
            text = { Text("Catatan '${noteToDelete?.title}' beserta seluruh foto lampirannya akan dihapus permanen.", color = colors.textSecondary) },
            confirmButton = {
                Button(
                    onClick = {
                        noteToDelete?.let { viewModel.deleteNote(it) }
                        noteToDelete = null
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = CrimsonRed)
                ) {
                    Text("Hapus", color = Color.White)
                }
            },
            dismissButton = {
                TextButton(onClick = { noteToDelete = null }) {
                    Text("Batal", color = colors.textSecondary)
                }
            }
        )
    }

    // Dialog Ekspor ke PDF
    if (noteToExportPdf != null) {
        ExportPdfDialog(
            noteWithCourse = noteToExportPdf!!,
            onDismiss = { noteToExportPdf = null },
            onSaveToDevice = {
                val item = noteToExportPdf!!
                val defaultFileName = "Catatan_${item.note.title.replace(Regex("[^a-zA-Z0-9-_ ]"), "").trim().replace(" ", "_").take(25)}.pdf"
                createPdfDocLauncher.launch(defaultFileName)
            }
        )
    }

    // Dialog Fullscreen Preview Foto Papan Tulis
    if (fullScreenImageUrl != null) {
        FullScreenImageViewerDialog(
            imagePath = fullScreenImageUrl!!,
            onDismiss = { fullScreenImageUrl = null }
        )
    }
}

@Composable
fun NoteCard(
    noteWithCourse: NoteWithCourse,
    onClick: () -> Unit,
    onEditClick: () -> Unit,
    onDeleteClick: () -> Unit,
    onExportPdfClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val colors = MahaTheme.colors
    val context = LocalContext.current
    val note = noteWithCourse.note
    val course = noteWithCourse.course
    val allImages = note.getAllImages()
    var showMenu by remember { mutableStateOf(false) }

    Card(
        modifier = modifier
            .fillMaxWidth()
            .clickable { onClick() },
        colors = CardDefaults.cardColors(containerColor = colors.surface),
        shape = RoundedCornerShape(16.dp),
        border = BorderStroke(1.dp, colors.borderSubtle)
    ) {
        Column(modifier = Modifier.padding(14.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                if (course != null) {
                    CourseTag(
                        name = course.name,
                        code = course.code,
                        colorHex = course.colorHex,
                        modifier = Modifier.weight(1f, fill = false)
                    )
                } else {
                    Text(text = "Catatan Umum", color = colors.textSecondary, fontSize = 11.sp)
                }

                Spacer(modifier = Modifier.weight(1f))

                Text(
                    text = DateUtils.formatShortDate(note.updatedAt),
                    color = colors.textSecondary,
                    fontSize = 11.sp
                )

                Box {
                    IconButton(
                        onClick = { showMenu = true },
                        modifier = Modifier.size(32.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.MoreVert,
                            contentDescription = "Menu",
                            tint = colors.textSecondary,
                            modifier = Modifier.size(18.dp)
                        )
                    }
                    DropdownMenu(
                        expanded = showMenu,
                        onDismissRequest = { showMenu = false },
                        modifier = Modifier.background(colors.surface)
                    ) {
                        DropdownMenuItem(
                            text = { Text("Ekspor ke PDF", color = colors.accentPrimary, fontWeight = FontWeight.SemiBold) },
                            onClick = {
                                showMenu = false
                                onExportPdfClick()
                            },
                            leadingIcon = { Icon(Icons.Default.PictureAsPdf, contentDescription = null, tint = colors.accentPrimary) }
                        )
                        DropdownMenuItem(
                            text = { Text("Edit Catatan", color = colors.textPrimary) },
                            onClick = {
                                showMenu = false
                                onEditClick()
                            },
                            leadingIcon = { Icon(Icons.Default.Edit, contentDescription = null, tint = colors.accentPrimary) }
                        )
                        DropdownMenuItem(
                            text = { Text("Hapus Catatan", color = CrimsonRed) },
                            onClick = {
                                showMenu = false
                                onDeleteClick()
                            },
                            leadingIcon = { Icon(Icons.Default.Delete, contentDescription = null, tint = CrimsonRed) }
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            Text(
                text = note.title,
                color = colors.textPrimary,
                fontSize = 16.sp,
                fontWeight = FontWeight.Bold,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )

            if (note.content.isNotBlank()) {
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = note.content,
                    color = colors.textSecondary,
                    fontSize = 13.sp,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis
                )
            }

            // Image attachment preview strip (supports up to 5 photos)
            if (allImages.isNotEmpty()) {
                Spacer(modifier = Modifier.height(10.dp))
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(10.dp))
                        .background(colors.surfaceVariant)
                        .border(1.dp, colors.borderSubtle, RoundedCornerShape(10.dp))
                        .padding(8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    // Preview thumbnail(s)
                    Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                        allImages.take(3).forEach { imgPath ->
                            Box(
                                modifier = Modifier
                                    .size(42.dp)
                                    .clip(RoundedCornerShape(6.dp))
                                    .background(colors.background)
                                    .border(1.dp, colors.borderSubtle, RoundedCornerShape(6.dp))
                            ) {
                                AsyncImage(
                                    model = ImageRequest.Builder(context)
                                        .data(File(imgPath))
                                        .crossfade(true)
                                        .build(),
                                    contentDescription = "Foto Lampiran",
                                    contentScale = ContentScale.Crop,
                                    modifier = Modifier.fillMaxSize()
                                )
                            }
                        }
                    }

                    Spacer(modifier = Modifier.width(10.dp))

                    Column(modifier = Modifier.weight(1f)) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = Icons.Default.Image,
                                contentDescription = null,
                                tint = NeonEmerald,
                                modifier = Modifier.size(13.dp)
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(
                                text = "${allImages.size} Foto Papan Tulis",
                                color = NeonEmerald,
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }
                        Text(
                            text = "Ketuk untuk membaca catatan & foto",
                            color = colors.textSecondary,
                            fontSize = 11.sp
                        )
                    }

                    // PDF shortcut button on card
                    IconButton(
                        onClick = onExportPdfClick,
                        modifier = Modifier.size(32.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.PictureAsPdf,
                            contentDescription = "Ekspor PDF",
                            tint = colors.accentPrimary,
                            modifier = Modifier.size(18.dp)
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun NoteDetailDialog(
    noteWithCourse: NoteWithCourse,
    onDismiss: () -> Unit,
    onEdit: () -> Unit,
    onExportPdf: () -> Unit,
    onImageClick: (String) -> Unit
) {
    val colors = MahaTheme.colors
    val note = noteWithCourse.note
    val course = noteWithCourse.course
    val allImages = note.getAllImages()
    val context = LocalContext.current

    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(colors.background)
                .padding(16.dp)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .verticalScroll(rememberScrollState())
            ) {
                // Top Bar with Close, PDF Export & Edit
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    IconButton(onClick = onDismiss) {
                        Icon(Icons.Default.Close, contentDescription = "Tutup", tint = colors.textPrimary)
                    }

                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Button(
                            onClick = onExportPdf,
                            colors = ButtonDefaults.buttonColors(containerColor = colors.accentPrimary),
                            contentPadding = PaddingValues(horizontal = 12.dp, vertical = 6.dp),
                            shape = RoundedCornerShape(8.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.PictureAsPdf,
                                contentDescription = null,
                                tint = InkBlack,
                                modifier = Modifier.size(16.dp)
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text("Ekspor PDF", color = InkBlack, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                        }

                        IconButton(onClick = onEdit) {
                            Icon(Icons.Default.Edit, contentDescription = "Edit", tint = colors.accentPrimary)
                        }
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))

                // Metadata Card
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(12.dp))
                        .background(colors.surface)
                        .border(1.dp, colors.borderSubtle, RoundedCornerShape(12.dp))
                        .padding(14.dp)
                ) {
                    Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                        if (course != null) {
                            CourseTag(name = course.name, code = course.code, colorHex = course.colorHex)
                        } else {
                            Text(
                                text = "Catatan Umum (Tanpa Matkul)",
                                color = colors.textSecondary,
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Medium
                            )
                        }

                        if (!course?.lecturer.isNullOrBlank()) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(
                                    imageVector = Icons.Default.Person,
                                    contentDescription = null,
                                    tint = colors.textSecondary,
                                    modifier = Modifier.size(14.dp)
                                )
                                Spacer(modifier = Modifier.width(6.dp))
                                Text(
                                    text = "Dosen Pengampu: ${course!!.lecturer}",
                                    color = colors.textSecondary,
                                    fontSize = 12.sp
                                )
                            }
                        }

                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text(
                                text = "Dibuat: ${DateUtils.formatDateTime(note.createdAt)}",
                                color = colors.textSecondary,
                                fontSize = 11.sp
                            )
                        }

                        if (note.updatedAt > note.createdAt + 60_000L) {
                            Text(
                                text = "Terakhir diubah: ${DateUtils.formatDateTime(note.updatedAt)}",
                                color = colors.textSecondary.copy(alpha = 0.8f),
                                fontSize = 11.sp
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                // Title
                Text(
                    text = note.title,
                    color = colors.textPrimary,
                    fontSize = 22.sp,
                    fontWeight = FontWeight.Bold
                )

                Spacer(modifier = Modifier.height(16.dp))

                // Note Content Text
                if (note.content.isNotBlank()) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(12.dp))
                            .background(colors.surface)
                            .border(1.dp, colors.borderSubtle, RoundedCornerShape(12.dp))
                            .padding(14.dp)
                    ) {
                        Text(
                            text = note.content,
                            color = colors.textPrimary,
                            fontSize = 15.sp,
                            lineHeight = 22.sp
                        )
                    }
                    Spacer(modifier = Modifier.height(20.dp))
                }

                // Photos Section (Up to 5 photos)
                if (allImages.isNotEmpty()) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = Icons.Default.PhotoCamera,
                                contentDescription = null,
                                tint = NeonEmerald,
                                modifier = Modifier.size(18.dp)
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = "Lampiran Foto Papan Tulis (${allImages.size} Foto)",
                                color = colors.textPrimary,
                                fontSize = 15.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }
                        Text(
                            text = "Ketuk foto untuk perbesar",
                            color = colors.textSecondary,
                            fontSize = 11.sp
                        )
                    }

                    Spacer(modifier = Modifier.height(10.dp))

                    allImages.forEachIndexed { index, imgPath ->
                        val file = File(imgPath)
                        if (file.exists()) {
                            Card(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clickable { onImageClick(imgPath) },
                                colors = CardDefaults.cardColors(containerColor = colors.surface),
                                shape = RoundedCornerShape(12.dp),
                                border = BorderStroke(1.dp, colors.borderSubtle)
                            ) {
                                Column {
                                    Box {
                                        AsyncImage(
                                            model = ImageRequest.Builder(context)
                                                .data(file)
                                                .crossfade(true)
                                                .build(),
                                            contentDescription = "Foto ${index + 1}",
                                            contentScale = ContentScale.FillWidth,
                                            modifier = Modifier
                                                .fillMaxWidth()
                                                .clip(RoundedCornerShape(topStart = 12.dp, topEnd = 12.dp))
                                        )

                                        // Badge index foto
                                        Box(
                                            modifier = Modifier
                                                .padding(8.dp)
                                                .clip(RoundedCornerShape(6.dp))
                                                .background(InkBlack.copy(alpha = 0.75f))
                                                .padding(horizontal = 8.dp, vertical = 4.dp)
                                                .align(Alignment.TopStart)
                                        ) {
                                            Text(
                                                text = "Foto ${index + 1} / ${allImages.size}",
                                                color = Color.White,
                                                fontSize = 11.sp,
                                                fontWeight = FontWeight.Bold
                                            )
                                        }

                                        // Zoom icon indicator
                                        Box(
                                            modifier = Modifier
                                                .padding(8.dp)
                                                .size(28.dp)
                                                .clip(CircleShape)
                                                .background(InkBlack.copy(alpha = 0.75f))
                                                .align(Alignment.TopEnd),
                                            contentAlignment = Alignment.Center
                                        ) {
                                            Icon(
                                                imageVector = Icons.Default.Visibility,
                                                contentDescription = "Perbesar",
                                                tint = Color.White,
                                                modifier = Modifier.size(16.dp)
                                            )
                                        }
                                    }

                                    Row(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .padding(10.dp),
                                        horizontalArrangement = Arrangement.SpaceBetween,
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Text(
                                            text = "Dokumentasi Foto ${index + 1}",
                                            color = colors.textSecondary,
                                            fontSize = 12.sp,
                                            fontWeight = FontWeight.Medium
                                        )
                                        Text(
                                            text = "Ketuk untuk full view",
                                            color = colors.accentPrimary,
                                            fontSize = 11.sp,
                                            fontWeight = FontWeight.SemiBold
                                        )
                                    }
                                }
                            }
                            Spacer(modifier = Modifier.height(14.dp))
                        }
                    }
                }

                Spacer(modifier = Modifier.height(40.dp))
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddEditNoteDialog(
    note: NoteEntity?,
    courses: List<CourseEntity>,
    onDismiss: () -> Unit,
    onSave: (Int, Int?, String, String, List<Uri>, Boolean, List<String>) -> Unit
) {
    val colors = MahaTheme.colors
    val context = LocalContext.current

    var title by remember { mutableStateOf(note?.title ?: "") }
    var content by remember { mutableStateOf(note?.content ?: "") }
    var selectedCourseId by remember { mutableStateOf(note?.courseId ?: courses.firstOrNull()?.id) }
    var enhanceImage by remember { mutableStateOf(true) }

    // List of existing saved local paths (e.g. from previous edit)
    var existingPhotos by remember { mutableStateOf(note?.getAllImages() ?: emptyList()) }

    // List of newly chosen image Uris (from gallery/camera)
    var newPhotoUris by remember { mutableStateOf<List<Uri>>(emptyList()) }

    var courseDropdownExpanded by remember { mutableStateOf(false) }

    val totalPhotosCount = existingPhotos.size + newPhotoUris.size
    val remainingSlots = (5 - totalPhotosCount).coerceAtLeast(0)

    // Multiple photo picker (up to 5 total photos)
    val multiPhotoPickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.PickMultipleVisualMedia(maxItems = 5)
    ) { uris: List<Uri> ->
        if (uris.isNotEmpty()) {
            val toAdd = uris.take(remainingSlots)
            newPhotoUris = (newPhotoUris + toAdd).take(5 - existingPhotos.size)
        }
    }

    AlertDialog(
        onDismissRequest = onDismiss,
        containerColor = colors.surface,
        title = {
            Text(
                text = if (note == null) "Tambah Catatan Materi" else "Ubah Catatan",
                color = colors.textPrimary,
                fontSize = 18.sp,
                fontWeight = FontWeight.Bold
            )
        },
        text = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                // Title
                OutlinedTextField(
                    value = title,
                    onValueChange = { title = it },
                    label = { Text("Judul Catatan *") },
                    placeholder = { Text("e.g. Pertemuan 4: Algoritma Greedy") },
                    modifier = Modifier.fillMaxWidth(),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedTextColor = colors.textPrimary,
                        unfocusedTextColor = colors.textPrimary,
                        focusedBorderColor = colors.accentPrimary,
                        unfocusedBorderColor = colors.borderSubtle
                    )
                )

                // Course selector
                ExposedDropdownMenuBox(
                    expanded = courseDropdownExpanded,
                    onExpandedChange = { courseDropdownExpanded = it }
                ) {
                    val currentCourse = courses.find { it.id == selectedCourseId }
                    OutlinedTextField(
                        value = currentCourse?.name ?: "Catatan Umum (Tanpa Matkul)",
                        onValueChange = {},
                        readOnly = true,
                        label = { Text("Mata Kuliah") },
                        trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = courseDropdownExpanded) },
                        modifier = Modifier
                            .fillMaxWidth()
                            .menuAnchor(MenuAnchorType.PrimaryNotEditable),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedTextColor = colors.textPrimary,
                            unfocusedTextColor = colors.textPrimary,
                            focusedBorderColor = colors.accentPrimary,
                            unfocusedBorderColor = colors.borderSubtle
                        )
                    )
                    ExposedDropdownMenu(
                        expanded = courseDropdownExpanded,
                        onDismissRequest = { courseDropdownExpanded = false },
                        modifier = Modifier.background(colors.surface)
                    ) {
                        DropdownMenuItem(
                            text = { Text("Catatan Umum (Tanpa Matkul)", color = colors.textPrimary) },
                            onClick = {
                                selectedCourseId = null
                                courseDropdownExpanded = false
                            }
                        )
                        courses.forEach { course ->
                            DropdownMenuItem(
                                text = { Text(course.name, color = colors.textPrimary) },
                                onClick = {
                                    selectedCourseId = course.id
                                    courseDropdownExpanded = false
                                }
                            )
                        }
                    }
                }

                // Note Content Text
                OutlinedTextField(
                    value = content,
                    onValueChange = { content = it },
                    label = { Text("Isi Materi / Rangkuman") },
                    placeholder = { Text("Ketik materi perkuliahan, rumus, poin pembahasan...") },
                    modifier = Modifier.fillMaxWidth(),
                    minLines = 4,
                    maxLines = 8,
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedTextColor = colors.textPrimary,
                        unfocusedTextColor = colors.textPrimary,
                        focusedBorderColor = colors.accentPrimary,
                        unfocusedBorderColor = colors.borderSubtle
                    )
                )

                Spacer(modifier = Modifier.height(4.dp))

                // Photos Section: Supports up to 5 photos
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(12.dp))
                        .background(colors.surfaceVariant)
                        .border(1.dp, colors.borderSubtle, RoundedCornerShape(12.dp))
                        .padding(12.dp)
                ) {
                    Column {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column {
                                Text(
                                    text = "Foto Papan Tulis / Dokumen",
                                    color = colors.textPrimary,
                                    fontSize = 13.sp,
                                    fontWeight = FontWeight.Bold
                                )
                                Text(
                                    text = "Tersimpan: $totalPhotosCount / 5 Foto",
                                    color = if (totalPhotosCount >= 5) SunsetAmber else colors.accentPrimary,
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.SemiBold
                                )
                            }

                            if (totalPhotosCount < 5) {
                                OutlinedButton(
                                    onClick = {
                                        multiPhotoPickerLauncher.launch(
                                            PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly)
                                        )
                                    },
                                    colors = ButtonDefaults.outlinedButtonColors(contentColor = colors.accentPrimary),
                                    contentPadding = PaddingValues(horizontal = 10.dp, vertical = 4.dp)
                                ) {
                                    Icon(Icons.Default.AddPhotoAlternate, contentDescription = null, modifier = Modifier.size(16.dp))
                                    Spacer(modifier = Modifier.width(4.dp))
                                    Text("Tambah Foto", fontSize = 11.sp)
                                }
                            }
                        }

                        // Photo Thumbnails Strip
                        if (totalPhotosCount > 0) {
                            Spacer(modifier = Modifier.height(10.dp))
                            LazyRow(
                                horizontalArrangement = Arrangement.spacedBy(8.dp),
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                // Existing saved photos
                                itemsIndexed(existingPhotos) { idx, path ->
                                    Box(
                                        modifier = Modifier
                                            .size(72.dp)
                                            .clip(RoundedCornerShape(8.dp))
                                            .background(colors.background)
                                            .border(1.dp, colors.borderSubtle, RoundedCornerShape(8.dp))
                                    ) {
                                        AsyncImage(
                                            model = ImageRequest.Builder(context)
                                                .data(File(path))
                                                .crossfade(true)
                                                .build(),
                                            contentDescription = "Foto ${idx + 1}",
                                            contentScale = ContentScale.Crop,
                                            modifier = Modifier.fillMaxSize()
                                        )

                                        // Badge number
                                        Box(
                                            modifier = Modifier
                                                .padding(3.dp)
                                                .clip(CircleShape)
                                                .background(InkBlack.copy(alpha = 0.7f))
                                                .size(18.dp)
                                                .align(Alignment.TopStart),
                                            contentAlignment = Alignment.Center
                                        ) {
                                            Text("${idx + 1}", color = Color.White, fontSize = 9.sp, fontWeight = FontWeight.Bold)
                                        }

                                        // Delete button
                                        IconButton(
                                            onClick = {
                                                existingPhotos = existingPhotos.filterIndexed { i, _ -> i != idx }
                                            },
                                            modifier = Modifier
                                                .size(22.dp)
                                                .align(Alignment.TopEnd)
                                                .background(CrimsonRed.copy(alpha = 0.85f), CircleShape)
                                        ) {
                                            Icon(Icons.Default.Close, contentDescription = "Hapus", tint = Color.White, modifier = Modifier.size(12.dp))
                                        }
                                    }
                                }

                                // Newly added Uris
                                itemsIndexed(newPhotoUris) { idx, uri ->
                                    val overallIdx = existingPhotos.size + idx + 1
                                    Box(
                                        modifier = Modifier
                                            .size(72.dp)
                                            .clip(RoundedCornerShape(8.dp))
                                            .background(colors.background)
                                            .border(1.dp, colors.accentPrimary, RoundedCornerShape(8.dp))
                                    ) {
                                        AsyncImage(
                                            model = ImageRequest.Builder(context)
                                                .data(uri)
                                                .crossfade(true)
                                                .build(),
                                            contentDescription = "Foto Baru $overallIdx",
                                            contentScale = ContentScale.Crop,
                                            modifier = Modifier.fillMaxSize()
                                        )

                                        // Badge number (green for new)
                                        Box(
                                            modifier = Modifier
                                                .padding(3.dp)
                                                .clip(CircleShape)
                                                .background(NeonEmerald)
                                                .size(18.dp)
                                                .align(Alignment.TopStart),
                                            contentAlignment = Alignment.Center
                                        ) {
                                            Text("$overallIdx", color = InkBlack, fontSize = 9.sp, fontWeight = FontWeight.Bold)
                                        }

                                        // Delete button
                                        IconButton(
                                            onClick = {
                                                newPhotoUris = newPhotoUris.filterIndexed { i, _ -> i != idx }
                                            },
                                            modifier = Modifier
                                                .size(22.dp)
                                                .align(Alignment.TopEnd)
                                                .background(CrimsonRed.copy(alpha = 0.85f), CircleShape)
                                        ) {
                                            Icon(Icons.Default.Close, contentDescription = "Hapus", tint = Color.White, modifier = Modifier.size(12.dp))
                                        }
                                    }
                                }
                            }
                        }

                        Spacer(modifier = Modifier.height(8.dp))

                        // Checkbox mode penajaman papan tulis
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.clickable { enhanceImage = !enhanceImage }
                        ) {
                            Checkbox(
                                checked = enhanceImage,
                                onCheckedChange = { enhanceImage = it },
                                colors = CheckboxDefaults.colors(checkedColor = colors.accentPrimary, checkmarkColor = InkBlack)
                            )
                            Column {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Icon(Icons.Default.AutoAwesome, contentDescription = null, tint = colors.accentPrimary, modifier = Modifier.size(14.dp))
                                    Spacer(modifier = Modifier.width(4.dp))
                                    Text(
                                        text = "Pertajam Teks Papan Tulis (Board Enhance)",
                                        color = colors.textPrimary,
                                        fontSize = 12.sp,
                                        fontWeight = FontWeight.Bold
                                    )
                                }
                                Text(
                                    text = "Otomatis mengoptimalkan kontras & filter warna agar tulisan spidol/kapur tampak tajam.",
                                    color = colors.textSecondary,
                                    fontSize = 10.sp
                                )
                            }
                        }
                    }
                }
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    if (title.isNotBlank()) {
                        onSave(
                            note?.id ?: 0,
                            selectedCourseId,
                            title.trim(),
                            content.trim(),
                            newPhotoUris,
                            enhanceImage,
                            existingPhotos
                        )
                    }
                },
                colors = ButtonDefaults.buttonColors(containerColor = colors.accentPrimary)
            ) {
                Text("Simpan Catatan", color = InkBlack, fontWeight = FontWeight.Bold)
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Batal", color = colors.textSecondary)
            }
        }
    )
}

/**
 * Dialog Ekspor Catatan ke Dokumen PDF
 * Menyediakan opsi: Bagikan (Share), Buka langsung di PDF Viewer, atau Simpan ke Memori HP
 */
@Composable
fun ExportPdfDialog(
    noteWithCourse: NoteWithCourse,
    onDismiss: () -> Unit,
    onSaveToDevice: () -> Unit
) {
    val colors = MahaTheme.colors
    val context = LocalContext.current
    val coroutineScope = rememberCoroutineScope()
    val note = noteWithCourse.note
    val course = noteWithCourse.course
    val allImages = note.getAllImages()

    var isGeneratingPdf by remember { mutableStateOf(false) }

    AlertDialog(
        onDismissRequest = { if (!isGeneratingPdf) onDismiss() },
        containerColor = colors.surface,
        title = {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    imageVector = Icons.Default.PictureAsPdf,
                    contentDescription = null,
                    tint = colors.accentPrimary,
                    modifier = Modifier.size(24.dp)
                )
                Spacer(modifier = Modifier.width(10.dp))
                Text(
                    text = "Ekspor Catatan ke PDF",
                    color = colors.textPrimary,
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold
                )
            }
        },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                Text(
                    text = "Dokumen PDF akan dibuat rapi dengan semua informasi lengkap:",
                    color = colors.textSecondary,
                    fontSize = 12.sp
                )

                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(10.dp))
                        .background(colors.surfaceVariant)
                        .border(1.dp, colors.borderSubtle, RoundedCornerShape(10.dp))
                        .padding(12.dp)
                ) {
                    Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                        Text(
                            text = "• Judul: ${note.title}",
                            color = colors.textPrimary,
                            fontSize = 12.sp,
                            fontWeight = FontWeight.SemiBold
                        )
                        Text(
                            text = "• Matkul: ${course?.name ?: "Catatan Umum"} ${if (!course?.code.isNullOrBlank()) "(${course?.code})" else ""}",
                            color = colors.textPrimary,
                            fontSize = 12.sp
                        )
                        Text(
                            text = "• Dosen Pengampu: ${if (!course?.lecturer.isNullOrBlank()) course?.lecturer else "-"}",
                            color = colors.textPrimary,
                            fontSize = 12.sp
                        )
                        Text(
                            text = "• Waktu: ${DateUtils.formatDateTime(note.createdAt)}",
                            color = colors.textPrimary,
                            fontSize = 12.sp
                        )
                        Text(
                            text = "• Foto Lampiran: ${allImages.size} Foto Papan Tulis",
                            color = if (allImages.isNotEmpty()) NeonEmerald else colors.textSecondary,
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Medium
                        )
                    }
                }

                if (isGeneratingPdf) {
                    Spacer(modifier = Modifier.height(8.dp))
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.Center,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        CircularProgressIndicator(color = colors.accentPrimary, modifier = Modifier.size(24.dp))
                        Spacer(modifier = Modifier.width(12.dp))
                        Text("Membuat dokumen PDF...", color = colors.textPrimary, fontSize = 13.sp)
                    }
                } else {
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = "Pilih tindakan yang ingin dilakukan:",
                        color = colors.textPrimary,
                        fontSize = 12.sp,
                        fontWeight = FontWeight.SemiBold
                    )

                    // Option 1: Share PDF
                    OutlinedButton(
                        onClick = {
                            isGeneratingPdf = true
                            coroutineScope.launch(Dispatchers.IO) {
                                try {
                                    val file = PdfExportHelper.exportNoteToPdfFile(context, note, course)
                                    withContext(Dispatchers.Main) {
                                        isGeneratingPdf = false
                                        PdfExportHelper.sharePdf(context, file)
                                        onDismiss()
                                    }
                                } catch (e: Exception) {
                                    e.printStackTrace()
                                    withContext(Dispatchers.Main) {
                                        isGeneratingPdf = false
                                        Toast.makeText(context, "Gagal membuat PDF: ${e.message}", Toast.LENGTH_LONG).show()
                                    }
                                }
                            }
                        },
                        modifier = Modifier.fillMaxWidth(),
                        colors = ButtonDefaults.outlinedButtonColors(contentColor = colors.accentPrimary),
                        shape = RoundedCornerShape(10.dp)
                    ) {
                        Icon(Icons.Default.Share, contentDescription = null, modifier = Modifier.size(18.dp))
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("Bagikan PDF (WhatsApp / Drive)", fontSize = 12.sp)
                    }

                    // Option 2: Open PDF Directly
                    OutlinedButton(
                        onClick = {
                            isGeneratingPdf = true
                            coroutineScope.launch(Dispatchers.IO) {
                                try {
                                    val file = PdfExportHelper.exportNoteToPdfFile(context, note, course)
                                    withContext(Dispatchers.Main) {
                                        isGeneratingPdf = false
                                        PdfExportHelper.openPdf(context, file)
                                        onDismiss()
                                    }
                                } catch (e: Exception) {
                                    e.printStackTrace()
                                    withContext(Dispatchers.Main) {
                                        isGeneratingPdf = false
                                        Toast.makeText(context, "Gagal membuka PDF: ${e.message}", Toast.LENGTH_LONG).show()
                                    }
                                }
                            }
                        },
                        modifier = Modifier.fillMaxWidth(),
                        colors = ButtonDefaults.outlinedButtonColors(contentColor = NeonEmerald),
                        shape = RoundedCornerShape(10.dp)
                    ) {
                        Icon(Icons.Default.Visibility, contentDescription = null, modifier = Modifier.size(18.dp))
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("Buka Dokumen PDF", fontSize = 12.sp)
                    }

                    // Option 3: Save to Device
                    Button(
                        onClick = {
                            onSaveToDevice()
                        },
                        modifier = Modifier.fillMaxWidth(),
                        colors = ButtonDefaults.buttonColors(containerColor = colors.accentPrimary),
                        shape = RoundedCornerShape(10.dp)
                    ) {
                        Icon(Icons.Default.Download, contentDescription = null, tint = InkBlack, modifier = Modifier.size(18.dp))
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("Simpan ke Memori HP", color = InkBlack, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                    }
                }
            }
        },
        confirmButton = {},
        dismissButton = {
            if (!isGeneratingPdf) {
                TextButton(onClick = onDismiss) {
                    Text("Tutup", color = colors.textSecondary)
                }
            }
        }
    )
}

/**
 * Fullscreen Image Viewer Modal
 */
@Composable
fun FullScreenImageViewerDialog(
    imagePath: String,
    onDismiss: () -> Unit
) {
    val context = LocalContext.current
    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(InkBlack)
        ) {
            AsyncImage(
                model = ImageRequest.Builder(context)
                    .data(File(imagePath))
                    .crossfade(true)
                    .build(),
                contentDescription = "Foto Fullscreen",
                contentScale = ContentScale.Fit,
                modifier = Modifier
                    .fillMaxSize()
                    .clickable { onDismiss() }
            )

            // Close button top-right
            IconButton(
                onClick = onDismiss,
                modifier = Modifier
                    .padding(24.dp)
                    .size(40.dp)
                    .clip(CircleShape)
                    .background(Color.Black.copy(alpha = 0.6f))
                    .align(Alignment.TopEnd)
            ) {
                Icon(
                    imageVector = Icons.Default.Close,
                    contentDescription = "Tutup",
                    tint = Color.White,
                    modifier = Modifier.size(24.dp)
                )
            }
        }
    }
}
