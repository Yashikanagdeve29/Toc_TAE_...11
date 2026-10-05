package com.example.ui.components

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
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
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.filled.HelpOutline
import androidx.compose.material.icons.filled.Pause
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Replay
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.FilledIconButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButtonDefaults
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
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
import com.example.ui.theme.AcademicBackground
import com.example.ui.theme.CardSurface
import com.example.ui.theme.PrimaryBlue
import com.example.ui.theme.PrimaryBlueLight
import com.example.ui.theme.SurfaceBorder
import com.example.ui.theme.TextMuted
import com.example.ui.theme.TextPrimary
import com.example.ui.theme.TextSecondary

@Composable
fun AutoPlayControls(
    currentStepIndex: Int,
    totalSteps: Int,
    isPlaying: Boolean,
    playSpeed: Float,
    onPrev: () -> Unit,
    onNext: () -> Unit,
    onTogglePlay: () -> Unit,
    onRestart: () -> Unit,
    onSetSpeed: (Float) -> Unit,
    onWhyClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier
            .fillMaxWidth()
            .testTag("autoplay_controls_card"),
        shape = RoundedCornerShape(18.dp),
        colors = CardDefaults.cardColors(containerColor = CardSurface),
        border = CardDefaults.outlinedCardBorder().copy(brush = androidx.compose.ui.graphics.SolidColor(SurfaceBorder)),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        ) {
            // Step counter and Progress bar
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Step ${currentStepIndex + 1} of $totalSteps",
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Bold,
                    color = TextPrimary
                )

                // Why button
                OutlinedButton(
                    onClick = onWhyClick,
                    modifier = Modifier
                        .height(34.dp)
                        .testTag("why_button"),
                    shape = RoundedCornerShape(8.dp),
                    border = BorderStroke(1.dp, PrimaryBlue),
                    contentPadding = androidx.compose.foundation.layout.PaddingValues(horizontal = 10.dp)
                ) {
                    Icon(
                        Icons.Default.HelpOutline,
                        contentDescription = "Why?",
                        tint = PrimaryBlue,
                        modifier = Modifier.size(16.dp)
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        text = "Why?",
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Bold,
                        color = PrimaryBlue
                    )
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            val progress = if (totalSteps > 1) {
                (currentStepIndex.toFloat() / (totalSteps - 1).toFloat()).coerceIn(0f, 1f)
            } else 1f

            LinearProgressIndicator(
                progress = { progress },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(6.dp)
                    .clip(RoundedCornerShape(3.dp)),
                color = PrimaryBlue,
                trackColor = PrimaryBlueLight
            )

            Spacer(modifier = Modifier.height(16.dp))

            // Playback controls
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Restart button
                FilledIconButton(
                    onClick = onRestart,
                    modifier = Modifier
                        .size(42.dp)
                        .testTag("restart_playback_button"),
                    colors = IconButtonDefaults.filledIconButtonColors(
                        containerColor = AcademicBackground,
                        contentColor = TextPrimary
                    ),
                    shape = RoundedCornerShape(10.dp)
                ) {
                    Icon(Icons.Default.Replay, contentDescription = "Restart", modifier = Modifier.size(20.dp))
                }

                // Prev Step
                FilledIconButton(
                    onClick = onPrev,
                    enabled = currentStepIndex > 0,
                    modifier = Modifier
                        .size(44.dp)
                        .testTag("prev_step_button"),
                    colors = IconButtonDefaults.filledIconButtonColors(
                        containerColor = AcademicBackground,
                        contentColor = TextPrimary,
                        disabledContainerColor = AcademicBackground.copy(alpha = 0.5f),
                        disabledContentColor = TextMuted
                    ),
                    shape = RoundedCornerShape(10.dp)
                ) {
                    Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Previous Step", modifier = Modifier.size(20.dp))
                }

                // Play / Pause main button
                FilledIconButton(
                    onClick = onTogglePlay,
                    modifier = Modifier
                        .size(52.dp)
                        .testTag("toggle_play_pause_button"),
                    colors = IconButtonDefaults.filledIconButtonColors(
                        containerColor = PrimaryBlue,
                        contentColor = Color.White
                    ),
                    shape = CircleShape
                ) {
                    Icon(
                        if (isPlaying) Icons.Default.Pause else Icons.Default.PlayArrow,
                        contentDescription = if (isPlaying) "Pause" else "Play",
                        modifier = Modifier.size(28.dp)
                    )
                }

                // Next Step
                FilledIconButton(
                    onClick = onNext,
                    enabled = currentStepIndex < totalSteps - 1,
                    modifier = Modifier
                        .size(44.dp)
                        .testTag("next_step_button"),
                    colors = IconButtonDefaults.filledIconButtonColors(
                        containerColor = AcademicBackground,
                        contentColor = TextPrimary,
                        disabledContainerColor = AcademicBackground.copy(alpha = 0.5f),
                        disabledContentColor = TextMuted
                    ),
                    shape = RoundedCornerShape(10.dp)
                ) {
                    Icon(Icons.AutoMirrored.Filled.ArrowForward, contentDescription = "Next Step", modifier = Modifier.size(20.dp))
                }

                // Speed Selector (0.5x, 1x, 1.5x)
                Row(
                    modifier = Modifier
                        .clip(RoundedCornerShape(10.dp))
                        .background(AcademicBackground)
                        .padding(2.dp)
                ) {
                    listOf(0.5f to "0.5x", 1.0f to "1x", 1.5f to "1.5x").forEach { (speed, label) ->
                        val isSelected = playSpeed == speed
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(8.dp))
                                .background(if (isSelected) PrimaryBlue else Color.Transparent)
                                .clickable { onSetSpeed(speed) }
                                .padding(horizontal = 6.dp, vertical = 6.dp)
                                .testTag("speed_${label}"),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = label,
                                fontSize = 11.sp,
                                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                                color = if (isSelected) Color.White else TextSecondary
                            )
                        }
                    }
                }
            }
        }
    }
}
