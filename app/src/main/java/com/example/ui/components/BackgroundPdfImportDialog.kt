package com.example.ui.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.ErrorOutline
import androidx.compose.material.icons.filled.PictureAsPdf
import androidx.compose.material.icons.filled.Schedule
import androidx.compose.material.icons.filled.TableChart
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Checkbox
import androidx.compose.material3.CheckboxDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.example.ui.theme.AlabasterGrey
import com.example.ui.theme.CrimsonRed
import com.example.ui.theme.DuskBlue
import com.example.ui.theme.DustyDenim
import com.example.ui.theme.ElectricCyan
import com.example.ui.theme.InkBlack
import com.example.ui.theme.NeonEmerald
import com.example.ui.theme.PrussianBlue
import com.example.ui.theme.SurfaceDarkVariant
import com.example.ui.viewmodel.BackgroundImportState
import com.example.ui.viewmodel.MahaSigmaViewModel
import com.example.util.DateUtils
import com.example.util.ParsedScheduleItem

/**
 * Dialog antarmuka untuk fitur proses tabel PDF di belakang layar.
 * Menampilkan animasi progres saat sistem mengekstrak tabel di latar belakang,
 * dan menampilkan hasil ekstraksi tabel yang siap diterapkan langsung ke jadwal kuliah.
 */
@Composable
fun BackgroundPdfImportDialog(
    viewModel: MahaSigmaViewModel,
    modifier: Modifier = Modifier
) {
    val state by viewModel.importState.collectAsState()

    if (!state.isDialogVisible) return

    Dialog(
        onDismissRequest = {
            if (!state.isProcessing) {
                viewModel.dismissImportDialog()
            }
        },
        properties = DialogProperties(
            usePlatformDefaultWidth = false,
            dismissOnBackPress = !state.isProcessing,
            dismissOnClickOutside = !state.isProcessing
        )
    ) {
        Surface(
            modifier = modifier
                .fillMaxWidth(0.94f)
                .fillMaxHeight(0.85f)
                .clip(RoundedCornerShape(24.dp))
                .border(1.dp, DuskBlue.copy(alpha = 0.8f), RoundedCornerShape(24.dp)),
            color = InkBlack
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(20.dp)
            ) {
                // Header Dialog
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Box(
                            modifier = Modifier
                                .size(40.dp)
                                .clip(CircleShape)
                                .background(NeonEmerald.copy(alpha = 0.15f)),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = if (state.fileName.endsWith(".xlsx", ignoreCase = true)) Icons.Default.TableChart else Icons.Default.PictureAsPdf,
                                contentDescription = null,
                                tint = if (state.fileName.endsWith(".xlsx", ignoreCase = true)) NeonEmerald else CrimsonRed,
                                modifier = Modifier.size(22.dp)
                            )
                        }
                        Spacer(modifier = Modifier.width(12.dp))
                        Column {
                            Text(
                                text = "Impor Tabel Jadwal",
                                color = AlabasterGrey,
                                fontSize = 18.sp,
                                fontWeight = FontWeight.Bold
                            )
                            if (state.fileName.isNotBlank()) {
                                Text(
                                    text = state.fileName,
                                    color = DustyDenim,
                                    fontSize = 12.sp,
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis
                                )
                            }
                        }
                    }

                    if (!state.isProcessing) {
                        IconButton(
                            onClick = { viewModel.dismissImportDialog() },
                            modifier = Modifier.size(32.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.Close,
                                contentDescription = "Tutup",
                                tint = DustyDenim
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))
                HorizontalDivider(color = DuskBlue.copy(alpha = 0.6f))
                Spacer(modifier = Modifier.height(16.dp))

                // Content area based on state
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxWidth(),
                    contentAlignment = Alignment.Center
                ) {
                    when {
                        // 1. SEDANG MEMPROSES DI BELAKANG LAYAR
                        state.isProcessing -> {
                            Column(
                                horizontalAlignment = Alignment.CenterHorizontally,
                                verticalArrangement = Arrangement.Center,
                                modifier = Modifier.padding(24.dp)
                            ) {
                                CircularProgressIndicator(
                                    color = NeonEmerald,
                                    strokeWidth = 3.5.dp,
                                    modifier = Modifier.size(54.dp)
                                )
                                Spacer(modifier = Modifier.height(20.dp))
                                Text(
                                    text = "Memproses Tabel di Belakang Layar...",
                                    color = AlabasterGrey,
                                    fontSize = 16.sp,
                                    fontWeight = FontWeight.Bold,
                                    textAlign = TextAlign.Center
                                )
                                Spacer(modifier = Modifier.height(8.dp))
                                Text(
                                    text = "Mengekstrak baris tabel, mencocokkan kode mata kuliah, hari, jam, ruang, dan dosen pengampu...",
                                    color = DustyDenim,
                                    fontSize = 13.sp,
                                    textAlign = TextAlign.Center,
                                    lineHeight = 18.sp
                                )
                                Spacer(modifier = Modifier.height(16.dp))
                                Box(
                                    modifier = Modifier
                                        .clip(RoundedCornerShape(8.dp))
                                        .background(PrussianBlue.copy(alpha = 0.8f))
                                        .padding(horizontal = 12.dp, vertical = 6.dp)
                                ) {
                                    Text(
                                        text = "Berkas: ${state.fileName}",
                                        color = ElectricCyan,
                                        fontSize = 12.sp,
                                        fontFamily = FontFamily.Monospace
                                    )
                                }
                            }
                        }

                        // 2. ERROR / TIDAK MENEMUKAN TABEL
                        state.errorMessage != null -> {
                            Column(
                                horizontalAlignment = Alignment.CenterHorizontally,
                                verticalArrangement = Arrangement.Center,
                                modifier = Modifier.padding(20.dp)
                            ) {
                                Box(
                                    modifier = Modifier
                                        .size(56.dp)
                                        .clip(CircleShape)
                                        .background(CrimsonRed.copy(alpha = 0.15f)),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.ErrorOutline,
                                        contentDescription = null,
                                        tint = CrimsonRed,
                                        modifier = Modifier.size(32.dp)
                                    )
                                }
                                Spacer(modifier = Modifier.height(16.dp))
                                Text(
                                    text = "Tabel Tidak Ditemukan",
                                    color = AlabasterGrey,
                                    fontSize = 17.sp,
                                    fontWeight = FontWeight.Bold,
                                    textAlign = TextAlign.Center
                                )
                                Spacer(modifier = Modifier.height(8.dp))
                                Text(
                                    text = state.errorMessage ?: "Format berkas tidak sesuai atau tabel tidak dapat terbaca.",
                                    color = DustyDenim,
                                    fontSize = 13.sp,
                                    textAlign = TextAlign.Center,
                                    lineHeight = 18.sp
                                )
                                Spacer(modifier = Modifier.height(24.dp))
                                Button(
                                    onClick = { viewModel.dismissImportDialog() },
                                    colors = ButtonDefaults.buttonColors(containerColor = DuskBlue)
                                ) {
                                    Text("Tutup", color = AlabasterGrey)
                                }
                            }
                        }

                        // 3. SUKSES MENEMUKAN TABEL JADWAL
                        state.items.isNotEmpty() -> {
                            val allSelected = state.items.all { it.isSelected }
                            val selectedCount = state.items.count { it.isSelected }

                            Column(modifier = Modifier.fillMaxSize()) {
                                // Status bar ringkasan tabel
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .clip(RoundedCornerShape(12.dp))
                                        .background(PrussianBlue)
                                        .padding(horizontal = 14.dp, vertical = 10.dp),
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.SpaceBetween
                                ) {
                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        Icon(
                                            imageVector = Icons.Default.CheckCircle,
                                            contentDescription = null,
                                            tint = NeonEmerald,
                                            modifier = Modifier.size(18.dp)
                                        )
                                        Spacer(modifier = Modifier.width(8.dp))
                                        Text(
                                            text = "${state.items.size} Mata Kuliah Terdeteksi",
                                            color = AlabasterGrey,
                                            fontSize = 13.sp,
                                            fontWeight = FontWeight.SemiBold
                                        )
                                    }

                                    Row(
                                        verticalAlignment = Alignment.CenterVertically,
                                        modifier = Modifier.clickable {
                                            viewModel.toggleSelectAllImportItems(!allSelected)
                                        }
                                    ) {
                                        Checkbox(
                                            checked = allSelected,
                                            onCheckedChange = { viewModel.toggleSelectAllImportItems(it) },
                                            colors = CheckboxDefaults.colors(
                                                checkedColor = NeonEmerald,
                                                checkmarkColor = InkBlack
                                            ),
                                            modifier = Modifier.size(24.dp)
                                        )
                                        Spacer(modifier = Modifier.width(6.dp))
                                        Text(
                                            text = if (allSelected) "Batal Semua" else "Pilih Semua",
                                            color = DustyDenim,
                                            fontSize = 12.sp
                                        )
                                    }
                                }

                                Spacer(modifier = Modifier.height(10.dp))

                                // Daftar baris jadwal hasil ekstraksi tabel
                                LazyColumn(
                                    modifier = Modifier
                                        .weight(1f)
                                        .fillMaxWidth(),
                                    verticalArrangement = Arrangement.spacedBy(8.dp)
                                ) {
                                    itemsIndexed(state.items) { index, item ->
                                        ExtractedScheduleRowCard(
                                            item = item,
                                            onToggleSelect = { isChecked ->
                                                viewModel.toggleImportItemSelection(index, isChecked)
                                            }
                                        )
                                    }
                                }
                            }
                        }

                        else -> {
                            Text(
                                text = "Menunggu data...",
                                color = DustyDenim,
                                fontSize = 13.sp
                            )
                        }
                    }
                }

                // Footer tombol aksi (hanya saat hasil tersedia)
                if (state.items.isNotEmpty() && !state.isProcessing) {
                    val selectedCount = state.items.count { it.isSelected }

                    Spacer(modifier = Modifier.height(16.dp))
                    HorizontalDivider(color = DuskBlue.copy(alpha = 0.6f))
                    Spacer(modifier = Modifier.height(16.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        OutlinedButton(
                            onClick = { viewModel.dismissImportDialog() },
                            modifier = Modifier.weight(1f),
                            shape = RoundedCornerShape(12.dp),
                            border = androidx.compose.foundation.BorderStroke(1.dp, DuskBlue)
                        ) {
                            Text("Batal", color = DustyDenim)
                        }

                        Button(
                            onClick = { viewModel.applyImportedSchedules() },
                            modifier = Modifier
                                .weight(1.8f)
                                .testTag("apply_imported_schedules_button"),
                            enabled = selectedCount > 0,
                            colors = ButtonDefaults.buttonColors(
                                containerColor = NeonEmerald,
                                disabledContainerColor = DuskBlue.copy(alpha = 0.5f)
                            ),
                            shape = RoundedCornerShape(12.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.Check,
                                contentDescription = null,
                                tint = InkBlack,
                                modifier = Modifier.size(18.dp)
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = "Terapkan ($selectedCount Matkul)",
                                color = InkBlack,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun ExtractedScheduleRowCard(
    item: ParsedScheduleItem,
    onToggleSelect: (Boolean) -> Unit,
    modifier: Modifier = Modifier
) {
    val dayName = DateUtils.getDayName(item.dayOfWeek)
    val itemColor = try {
        Color(android.graphics.Color.parseColor(item.colorHex))
    } catch (_: Exception) {
        NeonEmerald
    }

    Card(
        modifier = modifier
            .fillMaxWidth()
            .clickable { onToggleSelect(!item.isSelected) },
        colors = CardDefaults.cardColors(
            containerColor = if (item.isSelected) PrussianBlue else SurfaceDarkVariant
        ),
        shape = RoundedCornerShape(12.dp),
        border = androidx.compose.foundation.BorderStroke(
            1.dp,
            if (item.isSelected) DuskBlue else Color.Transparent
        )
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Checkbox(
                checked = item.isSelected,
                onCheckedChange = onToggleSelect,
                colors = CheckboxDefaults.colors(
                    checkedColor = NeonEmerald,
                    checkmarkColor = InkBlack
                ),
                modifier = Modifier.size(24.dp)
            )

            Spacer(modifier = Modifier.width(10.dp))

            // Indikator warna kiri
            Box(
                modifier = Modifier
                    .width(4.dp)
                    .height(44.dp)
                    .clip(RoundedCornerShape(2.dp))
                    .background(itemColor)
            )

            Spacer(modifier = Modifier.width(10.dp))

            Column(modifier = Modifier.weight(1f)) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    if (item.courseCode.isNotBlank()) {
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(4.dp))
                                .background(DuskBlue)
                                .padding(horizontal = 6.dp, vertical = 2.dp)
                        ) {
                            Text(
                                text = item.courseCode,
                                color = ElectricCyan,
                                fontSize = 10.sp,
                                fontFamily = FontFamily.Monospace,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }

                    Text(
                        text = item.courseName,
                        color = AlabasterGrey,
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Bold,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }

                Spacer(modifier = Modifier.height(4.dp))

                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.Default.Schedule,
                            contentDescription = null,
                            tint = NeonEmerald,
                            modifier = Modifier.size(12.dp)
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            text = "$dayName, ${item.startTime} - ${item.endTime}",
                            color = AlabasterGrey,
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Medium
                        )
                    }

                    if (item.room.isNotBlank()) {
                        Text(
                            text = "• ${item.room}",
                            color = DustyDenim,
                            fontSize = 11.sp,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                    }
                }

                if (item.lecturer.isNotBlank()) {
                    Spacer(modifier = Modifier.height(2.dp))
                    Text(
                        text = "Dosen: ${item.lecturer}",
                        color = DustyDenim.copy(alpha = 0.8f),
                        fontSize = 10.sp,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }
            }
        }
    }
}
