package com.example.jobpilot.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
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
import com.example.jobpilot.data.model.ApplicationStatus
import com.example.jobpilot.data.model.SeniorityLevel
import com.example.jobpilot.data.model.VerificationState
import com.example.jobpilot.data.model.WorkArrangement
import com.example.ui.theme.DarkBorder
import com.example.ui.theme.DarkSurface
import com.example.ui.theme.Graphite
import com.example.ui.theme.LightBorder
import com.example.ui.theme.LightSurface
import com.example.ui.theme.PrimaryWhite
import com.example.ui.theme.PureBlack

@Composable
fun VerificationBadge(state: VerificationState, modifier: Modifier = Modifier) {
    val (bgColor, textColor, borderColor) = when (state) {
        VerificationState.VERIFIED_SOURCE -> Triple(
            Color(0xFF0A2E17),
            Color(0xFF4ADE80),
            Color(0xFF15803D)
        )
        VerificationState.CORROBORATED -> Triple(
            Color(0xFF0F172A),
            Color(0xFF60A5FA),
            Color(0xFF1D4ED8)
        )
        VerificationState.SOURCE_REPORTED -> Triple(
            Color(0xFF1E1E1E),
            Color(0xFFD4D4D4),
            Color(0xFF404040)
        )
        VerificationState.AI_INFERRED -> Triple(
            Color(0xFF2E1C0A),
            Color(0xFFFBBF24),
            Color(0xFFB45309)
        )
        VerificationState.UNAVAILABLE -> Triple(
            Color(0xFF1A1A1A),
            Color(0xFF888888),
            Color(0xFF333333)
        )
        VerificationState.NOT_CHECKED -> Triple(
            Color(0xFF18181B),
            Color(0xFFA1A1AA),
            Color(0xFF3F3F46)
        )
        VerificationState.CONFLICTING -> Triple(
            Color(0xFF2E0A0A),
            Color(0xFFF87171),
            Color(0xFFB91C1C)
        )
    }

    Box(
        modifier = modifier
            .testTag("badge_verification_${state.name.lowercase()}")
            .clip(RoundedCornerShape(4.dp))
            .background(bgColor)
            .border(0.75.dp, borderColor, RoundedCornerShape(4.dp))
            .padding(horizontal = 6.dp, vertical = 2.dp),
        contentAlignment = Alignment.Center
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Box(
                modifier = Modifier
                    .size(5.dp)
                    .clip(CircleShape)
                    .background(textColor)
            )
            Text(
                text = " " + state.badgeText,
                color = textColor,
                fontSize = 9.sp,
                fontWeight = FontWeight.Bold,
                letterSpacing = 0.5.sp
            )
        }
    }
}

@Composable
fun SubtleTag(text: String, modifier: Modifier = Modifier) {
    Box(
        modifier = modifier
            .clip(RoundedCornerShape(4.dp))
            .background(MaterialTheme.colorScheme.surfaceVariant)
            .border(0.5.dp, MaterialTheme.colorScheme.outline, RoundedCornerShape(4.dp))
            .padding(horizontal = 7.dp, vertical = 3.dp),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = text,
            color = MaterialTheme.colorScheme.onSurface,
            fontSize = 10.sp,
            fontWeight = FontWeight.Medium
        )
    }
}

@Composable
fun MatchScoreBadge(score: Int, modifier: Modifier = Modifier) {
    Box(
        modifier = modifier
            .clip(RoundedCornerShape(6.dp))
            .background(PureBlack)
            .border(1.dp, PrimaryWhite, RoundedCornerShape(6.dp))
            .padding(horizontal = 8.dp, vertical = 4.dp),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = "$score% MATCH",
            color = PrimaryWhite,
            fontSize = 10.sp,
            fontWeight = FontWeight.Bold,
            letterSpacing = 0.5.sp
        )
    }
}

@Composable
fun ApplicationStatusBadge(status: ApplicationStatus, modifier: Modifier = Modifier) {
    Box(
        modifier = modifier
            .clip(RoundedCornerShape(4.dp))
            .background(Graphite)
            .border(0.5.dp, DarkBorder, RoundedCornerShape(4.dp))
            .padding(horizontal = 8.dp, vertical = 3.dp),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = status.label.uppercase(),
            color = PrimaryWhite,
            fontSize = 9.sp,
            fontWeight = FontWeight.SemiBold,
            letterSpacing = 0.5.sp
        )
    }
}
