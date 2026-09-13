package com.example.ui.screens

import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.Image
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
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Description
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Image
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material.icons.filled.PhotoCamera
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
import androidx.compose.ui.graphics.asImageBitmap
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
import com.example.util.ImageHelper
import java.io.File

@Composable
fun NotesScreen(
    viewModel: MahaSigmaViewModel,
    modifier: Modifier = Modifier
) {
    val filteredNotes by viewModel.filteredNotes.collectAsStateWithLifecycle()
    val courses by viewModel.courses.collectAsStateWithLifecycle()
    val selectedCourseFilter by viewModel.selectedNoteCourseFilter.collectAsStateWithLifecycle()

    var showAddEditDialog by remember { mutableStateOf(false) }
    var noteToEdit by remember { mutableStateOf<NoteEntity?>(null) }
    var noteToDelete by remember { mutableStateOf<NoteEntity?>(null) }
    var noteToViewDetail by remember { mutableStateOf<NoteWithCourse?>(null) }

    Scaffold(
        modifier = modifier.fillMaxSize(),
        containerColor = InkBlack,
        floatingActionButton = {
            FloatingActionButton(
                onClick = {
                    noteToEdit = null
                    showAddEditDialog = true
                },
                containerColor = DuskBlue,
                contentColor = AlabasterGrey,
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
                    .background(PrussianBlue)
                    .border(1.dp, DuskBlue.copy(alpha = 0.4f), RoundedCornerShape(bottomStart = 20.dp, bottomEnd = 20.dp))
                    .padding(16.dp)
            ) {
                Column {
                    Text(
                        text = "Catatan & Foto Papan Tulis",
                        color = AlabasterGrey,
                        fontSize = 20.sp,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = "Arsip materi kuliah dengan fitur penajaman kontras papan tulis",
                        color = DustyDenim,
                        fontSize = 12.sp
                    )

                    Spacer(modifier = Modifier.height(14.dp))

                    // Course filter chips horizontal
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .horizontalScroll(rememberScrollState()),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        // All courses chip
                        val isAllSelected = selectedCourseFilter == null
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(10.dp))
                                .background(if (isAllSelected) DuskBlue else SurfaceDarkVariant)
                                .border(
                                    1.dp,
                                    if (isAllSelected) ElectricCyan else DuskBlue.copy(alpha = 0.4f),
                                    RoundedCornerShape(10.dp)
                                )
                                .clickable { viewModel.setSelectedNoteCourseFilter(null) }
                                .padding(horizontal = 12.dp, vertical = 7.dp)
                        ) {
                            Text(
                                text = "Semua Matkul",
                                color = if (isAllSelected) AlabasterGrey else DustyDenim,
                                fontSize = 12.sp,
                                fontWeight = if (isAllSelected) FontWeight.Bold else FontWeight.Medium
                            )
                        }

                        // Each course chip
                        courses.forEach { course ->
                            val isSelected = selectedCourseFilter == course.id
                            Box(
                                modifier = Modifier
                                    .clip(RoundedCornerShape(10.dp))
                                    .background(if (isSelected) DuskBlue else SurfaceDarkVariant)
                                    .border(
                                        1.dp,
                                        if (isSelected) ElectricCyan else DuskBlue.copy(alpha = 0.4f),
                                        RoundedCornerShape(10.dp)
                                    )
                                    .clickable { viewModel.setSelectedNoteCourseFilter(course.id) }
                                    .padding(horizontal = 12.dp, vertical = 7.dp)
                            ) {
                                Text(
                                    text = course.code,
                                    color = if (isSelected) AlabasterGrey else DustyDenim,
                                    fontSize = 12.sp,
                                    fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium
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
                            tint = DustyDenim,
                            modifier = Modifier.size(56.dp)
                        )
                        Spacer(modifier = Modifier.height(12.dp))
                        Text(
                            text = "Belum Ada Catatan Kuliah",
                            color = AlabasterGrey,
                            fontSize = 16.sp,
                            fontWeight = FontWeight.Bold
                        )
                        Spacer(modifier = Modifier.height(6.dp))
                        Text(
                            text = "Simpan materi dosen, resume kuliah, atau foto papan tulis dengan mode penajaman teks.",
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
            }
        )
    }

    // Dialog Tambah / Edit Catatan
    if (showAddEditDialog) {
        AddEditNoteDialog(
            note = noteToEdit,
            courses = courses,
            onDismiss = { showAddEditDialog = false },
            onSave = { id, courseId, title, content, uri, enhance, existingPath ->
                viewModel.saveNote(id, courseId, title, content, uri, enhance, existingPath)
                showAddEditDialog = false
            }
        )
    }

    // Dialog Konfirmasi Hapus Catatan
    if (noteToDelete != null) {
        AlertDialog(
            onDismissRequest = { noteToDelete = null },
            containerColor = PrussianBlue,
            title = { Text("Hapus Catatan?", color = AlabasterGrey) },
            text = { Text("Catatan '${noteToDelete?.title}' beserta foto papan tulisnya akan dihapus permanen.", color = DustyDenim) },
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
                    Text("Batal", color = DustyDenim)
                }
            }
        )
    }
}

@Composable
fun NoteCard(
    noteWithCourse: NoteWithCourse,
    onClick: () -> Unit,
    onEditClick: () -> Unit,
    onDeleteClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val note = noteWithCourse.note
    val course = noteWithCourse.course
    var showMenu by remember { mutableStateOf(false) }

    Card(
        modifier = modifier
            .fillMaxWidth()
            .clickable { onClick() },
        colors = CardDefaults.cardColors(containerColor = PrussianBlue),
        shape = RoundedCornerShape(16.dp),
        border = androidx.compose.foundation.BorderStroke(1.dp, DuskBlue.copy(alpha = 0.5f))
    ) {
        Column(modifier = Modifier.padding(14.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                if (course != null) {
                    CourseTag(name = course.name, code = course.code, colorHex = course.colorHex)
                } else {
                    Text(text = "Catatan Umum", color = DustyDenim, fontSize = 11.sp)
                }

                Spacer(modifier = Modifier.weight(1f))

                Text(
                    text = DateUtils.formatShortDate(note.updatedAt),
                    color = DustyDenim,
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
                            text = { Text("Edit Catatan", color = AlabasterGrey) },
                            onClick = {
                                showMenu = false
                                onEditClick()
                            },
                            leadingIcon = { Icon(Icons.Default.Edit, contentDescription = null, tint = ElectricCyan) }
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
                color = AlabasterGrey,
                fontSize = 16.sp,
                fontWeight = FontWeight.Bold,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )

            if (note.content.isNotBlank()) {
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = note.content,
                    color = DustyDenim,
                    fontSize = 13.sp,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis
                )
            }

            // Image attachment preview strip
            if (!note.localImagePath.isNullOrBlank()) {
                Spacer(modifier = Modifier.height(10.dp))
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(10.dp))
                        .background(SurfaceDarkVariant)
                        .border(1.dp, DuskBlue.copy(alpha = 0.4f), RoundedCornerShape(10.dp))
                        .padding(8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Box(
                        modifier = Modifier
                            .size(48.dp)
                            .clip(RoundedCornerShape(8.dp))
                            .background(InkBlack)
                    ) {
                        AsyncImage(
                            model = ImageRequest.Builder(LocalContext.current)
                                .data(File(note.localImagePath))
                                .crossfade(true)
                                .build(),
                            contentDescription = "Foto Papan Tulis",
                            contentScale = ContentScale.Crop,
                            modifier = Modifier.fillMaxSize()
                        )
                    }

                    Spacer(modifier = Modifier.width(10.dp))

                    Column {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = Icons.Default.AutoAwesome,
                                contentDescription = null,
                                tint = ElectricCyan,
                                modifier = Modifier.size(13.dp)
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(
                                text = "Foto Papan Tulis (Tersimpan)",
                                color = ElectricCyan,
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }
                        Text(
                            text = "Ketuk untuk melihat detail foto resolusi penuh",
                            color = DustyDenim,
                            fontSize = 11.sp
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
    onEdit: () -> Unit
) {
    val note = noteWithCourse.note
    val course = noteWithCourse.course
    val context = LocalContext.current

    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(InkBlack.copy(alpha = 0.95f))
                .padding(16.dp)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .verticalScroll(rememberScrollState())
            ) {
                // Top Bar with Close & Edit
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    IconButton(onClick = onDismiss) {
                        Icon(Icons.Default.Close, contentDescription = "Tutup", tint = AlabasterGrey)
                    }

                    Row {
                        IconButton(onClick = onEdit) {
                            Icon(Icons.Default.Edit, contentDescription = "Edit", tint = ElectricCyan)
                        }
                    }
                }

                Spacer(modifier = Modifier.height(8.dp))

                if (course != null) {
                    CourseTag(name = course.name, code = course.code, colorHex = course.colorHex)
                    Spacer(modifier = Modifier.height(8.dp))
                }

                Text(
                    text = note.title,
                    color = AlabasterGrey,
                    fontSize = 22.sp,
                    fontWeight = FontWeight.Bold
                )

                Text(
                    text = "Terakhir diperbarui: ${DateUtils.formatDateTime(note.updatedAt)}",
                    color = DustyDenim,
                    fontSize = 12.sp
                )

                Spacer(modifier = Modifier.height(16.dp))

                // Full photo if attached
                if (!note.localImagePath.isNullOrBlank()) {
                    val file = File(note.localImagePath)
                    if (file.exists()) {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(12.dp))
                                .background(PrussianBlue)
                                .border(1.dp, DuskBlue, RoundedCornerShape(12.dp))
                        ) {
                            AsyncImage(
                                model = ImageRequest.Builder(context)
                                    .data(file)
                                    .crossfade(true)
                                    .build(),
                                contentDescription = "Foto Catatan",
                                contentScale = ContentScale.FillWidth,
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clip(RoundedCornerShape(12.dp))
                            )
                        }
                        Spacer(modifier = Modifier.height(16.dp))
                    }
                }

                // Content
                Text(
                    text = note.content,
                    color = AlabasterGrey,
                    fontSize = 15.sp,
                    lineHeight = 22.sp
                )

                Spacer(modifier = Modifier.height(32.dp))
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
    onSave: (Int, Int?, String, String, Uri?, Boolean, String?) -> Unit
) {
    val context = LocalContext.current

    var title by remember { mutableStateOf(note?.title ?: "") }
    var content by remember { mutableStateOf(note?.content ?: "") }
    var selectedCourseId by remember { mutableStateOf(note?.courseId ?: courses.firstOrNull()?.id) }
    var selectedImageUri by remember { mutableStateOf<Uri?>(null) }
    var enhanceImage by remember { mutableStateOf(true) } // default true for board text enhancement
    var existingImagePath by remember { mutableStateOf(note?.localImagePath) }

    var courseDropdownExpanded by remember { mutableStateOf(false) }

    // Photo Picker launcher (Android zero-permission photo picker)
    val photoPickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.PickVisualMedia()
    ) { uri: Uri? ->
        if (uri != null) {
            selectedImageUri = uri
            existingImagePath = null
        }
    }

    AlertDialog(
        onDismissRequest = onDismiss,
        containerColor = PrussianBlue,
        title = {
            Text(
                text = if (note == null) "Tambah Catatan Materi" else "Ubah Catatan",
                color = AlabasterGrey,
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
                    placeholder = { Text("e.g. Resume Pertemuan 4: Normalisasi") },
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
                        value = currentCourse?.let { "${it.code} - ${it.name}" } ?: "Catatan Umum (Tanpa Matkul)",
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
                            text = { Text("Catatan Umum (Tanpa Matkul)", color = AlabasterGrey) },
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

                // Note Content Text
                OutlinedTextField(
                    value = content,
                    onValueChange = { content = it },
                    label = { Text("Isi Materi / Rangkuman") },
                    placeholder = { Text("Ketik rangkuman materi dosen, rumus, poin penting...") },
                    modifier = Modifier.fillMaxWidth(),
                    minLines = 4,
                    maxLines = 8,
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedTextColor = AlabasterGrey,
                        unfocusedTextColor = AlabasterGrey,
                        focusedBorderColor = ElectricCyan,
                        unfocusedBorderColor = DuskBlue
                    )
                )

                Spacer(modifier = Modifier.height(4.dp))

                // Board Photo Picker Section
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(12.dp))
                        .background(SurfaceDarkVariant)
                        .border(1.dp, DuskBlue.copy(alpha = 0.5f), RoundedCornerShape(12.dp))
                        .padding(12.dp)
                ) {
                    Column {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = "Foto Papan Tulis / Dokumen",
                                color = AlabasterGrey,
                                fontSize = 13.sp,
                                fontWeight = FontWeight.Bold
                            )

                            OutlinedButton(
                                onClick = {
                                    photoPickerLauncher.launch(
                                        PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly)
                                    )
                                },
                                colors = ButtonDefaults.outlinedButtonColors(contentColor = ElectricCyan)
                            ) {
                                Icon(Icons.Default.PhotoCamera, contentDescription = null, modifier = Modifier.size(16.dp))
                                Spacer(modifier = Modifier.width(6.dp))
                                Text(if (selectedImageUri != null || existingImagePath != null) "Ganti Foto" else "Pilih Foto", fontSize = 12.sp)
                            }
                        }

                        // Preview of attached image
                        if (selectedImageUri != null || existingImagePath != null) {
                            Spacer(modifier = Modifier.height(8.dp))
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clip(RoundedCornerShape(8.dp))
                                    .background(InkBlack)
                                    .padding(8.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Icon(Icons.Default.Image, contentDescription = null, tint = NeonEmerald, modifier = Modifier.size(24.dp))
                                Spacer(modifier = Modifier.width(8.dp))
                                Text(
                                    text = if (selectedImageUri != null) "Foto baru dipilih" else "Foto tersimpan",
                                    color = AlabasterGrey,
                                    fontSize = 12.sp,
                                    modifier = Modifier.weight(1f)
                                )
                                IconButton(
                                    onClick = {
                                        selectedImageUri = null
                                        existingImagePath = null
                                    },
                                    modifier = Modifier.size(28.dp)
                                ) {
                                    Icon(Icons.Default.Close, contentDescription = "Hapus Foto", tint = CrimsonRed, modifier = Modifier.size(16.dp))
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
                                    colors = CheckboxDefaults.colors(checkedColor = ElectricCyan, checkmarkColor = InkBlack)
                                )
                                Column {
                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        Icon(Icons.Default.AutoAwesome, contentDescription = null, tint = ElectricCyan, modifier = Modifier.size(14.dp))
                                        Spacer(modifier = Modifier.width(4.dp))
                                        Text(
                                            text = "Pertajam Teks Papan Tulis (Board Enhance)",
                                            color = AlabasterGrey,
                                            fontSize = 12.sp,
                                            fontWeight = FontWeight.Bold
                                        )
                                    }
                                    Text(
                                        text = "Otomatis mengoptimalkan kontras & filter warna agar tulisan spidol/kapur tampak tajam.",
                                        color = DustyDenim,
                                        fontSize = 10.sp
                                    )
                                }
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
                            selectedImageUri,
                            enhanceImage,
                            existingImagePath
                        )
                    }
                },
                colors = ButtonDefaults.buttonColors(containerColor = DuskBlue)
            ) {
                Text("Simpan Catatan", color = AlabasterGrey)
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Batal", color = DustyDenim)
            }
        }
    )
}
