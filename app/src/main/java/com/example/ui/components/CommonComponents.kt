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
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.theme.AlabasterGrey
import com.example.ui.theme.CrimsonRed
import com.example.ui.theme.DuskBlue
import com.example.ui.theme.DustyDenim
import com.example.ui.theme.ElectricCyan
import com.example.ui.theme.InkBlack
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
    val parsedColor = try {
        if (!colorHex.isNullOrBlank()) Color(android.graphics.Color.parseColor(colorHex))
        else ElectricCyan
    } catch (e: Exception) {
        ElectricCyan
    }

    Row(
        modifier = modifier
            .clip(RoundedCornerShape(6.dp))
            .background(PrussianBlue)
            .border(1.dp, parsedColor.copy(alpha = 0.6f), RoundedCornerShape(6.dp))
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
        val displayText = if (!code.isNullOrBlank()) "$code • $name" else name
        Text(
            text = displayText,
            color = AlabasterGrey,
            fontSize = 11.sp,
            fontWeight = FontWeight.Medium,
            maxLines = 1
        )
    }
}

@Composable
fun StatusOverrideBadge(status: String, modifier: Modifier = Modifier) {
    val (bg, fg, text) = when (status) {
        "CANCELED" -> Triple(CrimsonRed.copy(alpha = 0.25f), CrimsonRed, "🚫 Kelas Diliburkan")
        "CHANGED_TIME" -> Triple(SunsetAmber.copy(alpha = 0.25f), SunsetAmber, "⏱ Jam Berubah")
        "CHANGED_ROOM" -> Triple(DuskBlue.copy(alpha = 0.35f), ElectricCyan, "📍 Pindah Ruangan")
        "ONLINE" -> Triple(NeonEmerald.copy(alpha = 0.25f), NeonEmerald, "💻 Kuliah Online")
        else -> Triple(DustyDenim.copy(alpha = 0.2f), AlabasterGrey, status)
    }

    Box(
        modifier = modifier
            .clip(RoundedCornerShape(6.dp))
            .background(bg)
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
