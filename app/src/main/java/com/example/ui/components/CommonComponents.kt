package com.example.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.theme.AlabasterGrey
import com.example.ui.theme.CrimsonRed
import com.example.ui.theme.DuskBlue
import com.example.ui.theme.DustyDenim
import com.example.ui.theme.ElectricCyan
import com.example.ui.theme.InkBlack
import com.example.ui.theme.MahaTheme
import com.example.ui.theme.NeonEmerald
import com.example.ui.theme.PrussianBlue
import com.example.ui.theme.SunsetAmber

@Composable
fun PriorityBadge(priority: String, modifier: Modifier = Modifier) {
    val (bgColor, textColor, label) = when (priority.uppercase()) {
        "TINGGI" -> Triple(CrimsonRed.copy(alpha = 0.2f), CrimsonRed, "Prioritas Tinggi")
        "SEDANG" -> Triple(SunsetAmber.copy(alpha = 0.2f), SunsetAmber, "Prioritas Sedang")
        else -> Triple(NeonEmerald.copy(alpha = 0.2f), NeonEmerald, "Prioritas Rendah")
    }

    Box(
        modifier = modifier
            .clip(RoundedCornerShape(6.dp))
            .background(bgColor)
            .border(1.dp, textColor.copy(alpha = 0.4f), RoundedCornerShape(6.dp))
            .padding(horizontal = 8.dp, vertical = 3.dp)
    ) {
        Text(
            text = label,
            color = textColor,
            fontSize = 11.sp,
            fontWeight = FontWeight.SemiBold
        )
    }
}

@Composable
fun CourseTag(
    name: String,
    code: String? = null,
    colorHex: String? = null,
    modifier: Modifier = Modifier
) {
    val colors = MahaTheme.colors
    val parsedColor = try {
        if (!colorHex.isNullOrBlank()) Color(android.graphics.Color.parseColor(colorHex))
        else colors.accentPrimary
    } catch (e: Exception) {
        colors.accentPrimary
    }

    val isCategoryCode = code.equals("Teori", ignoreCase = true) ||
                         code.equals("Praktik", ignoreCase = true) ||
                         code.equals("Praktikum", ignoreCase = true) ||
                         code.equals(name, ignoreCase = true)

    val displayText = if (!code.isNullOrBlank() && !isCategoryCode) "$code • $name" else name

    Row(
        modifier = modifier
            .clip(RoundedCornerShape(6.dp))
            .background(colors.surfaceVariant)
            .border(1.dp, parsedColor.copy(alpha = 0.5f), RoundedCornerShape(6.dp))
            .padding(horizontal = 8.dp, vertical = 3.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            modifier = Modifier
                .size(7.dp)
                .clip(CircleShape)
                .background(parsedColor)
        )
        Spacer(modifier = Modifier.width(6.dp))
        Text(
            text = displayText,
            color = colors.textPrimary,
            fontSize = 11.sp,
            fontWeight = FontWeight.Medium,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis
        )
    }
}

@Composable
fun StatusOverrideBadge(status: String, modifier: Modifier = Modifier) {
    val colors = MahaTheme.colors
    val (bg, fg, text) = when (status) {
        "CANCELED" -> Triple(CrimsonRed.copy(alpha = 0.15f), CrimsonRed, "🚫 Kelas Diliburkan")
        "CHANGED_TIME" -> Triple(SunsetAmber.copy(alpha = 0.15f), SunsetAmber, "⏱ Jam Berubah")
        "CHANGED_ROOM" -> Triple(colors.accentPrimary.copy(alpha = 0.15f), colors.accentPrimary, "📍 Pindah Ruangan")
        "ONLINE" -> Triple(NeonEmerald.copy(alpha = 0.15f), NeonEmerald, "💻 Kuliah Online")
        else -> Triple(colors.surfaceVariant, colors.textPrimary, status)
    }

    Box(
        modifier = modifier
            .clip(RoundedCornerShape(6.dp))
            .background(bg)
            .border(1.dp, fg.copy(alpha = 0.35f), RoundedCornerShape(6.dp))
            .padding(horizontal = 8.dp, vertical = 3.dp)
    ) {
        Text(
            text = text,
            color = fg,
            fontSize = 11.sp,
            fontWeight = FontWeight.Bold
        )
    }
}

