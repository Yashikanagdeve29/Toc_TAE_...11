package com.example.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.filled.FormatAlignLeft
import androidx.compose.material.icons.filled.FormatAlignRight
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.model.DerivationStep
import com.example.ui.components.AutoPlayControls
import com.example.ui.components.SententialFormView
import com.example.ui.theme.AcademicBackground
import com.example.ui.theme.AcademicTeal
import com.example.ui.theme.CardSurface
import com.example.ui.theme.PrimaryBlue
import com.example.ui.theme.PrimaryBlueLight
import com.example.ui.theme.SkyBlue
import com.example.ui.theme.SurfaceBorder
import com.example.ui.theme.TextMuted
import com.example.ui.theme.TextPrimary
import com.example.ui.theme.TextSecondary

@Composable
fun DerivationScreen(
    isLeftmost: Boolean,
    steps: List<DerivationStep>,
    currentStepIndex: Int,
    isPlaying: Boolean,
    playSpeed: Float,
    onPrevStep: () -> Unit,
    onNextStep: () -> Unit,
    onTogglePlay: () -> Unit,
    onRestart: () -> Unit,
    onSetSpeed: (Float) -> Unit,
    onWhyClick: (String) -> Unit,
    onProceedNext: () -> Unit,
    modifier: Modifier = Modifier
) {
    val scrollState = rememberScrollState()
    val activeStep = if (steps.isNotEmpty()) {
        steps[currentStepIndex.coerceIn(0, steps.size - 1)]
    } else null

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(AcademicBackground)
            .verticalScroll(scrollState)
            .padding(horizontal = 20.dp, vertical = 20.dp)
            .testTag(if (isLeftmost) "lmd_screen" else "rmd_screen")
    ) {
        // Title Bar
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Surface(
                    shape = RoundedCornerShape(10.dp),
                    color = if (isLeftmost) PrimaryBlueLight else SkyBlue.copy(alpha = 0.2f),
                    modifier = Modifier.size(38.dp)
                ) {
                    Box(contentAlignment = Alignment.Center) {
                        Icon(
                            if (isLeftmost) Icons.Default.FormatAlignLeft else Icons.Default.FormatAlignRight,
                            contentDescription = null,
                            tint = PrimaryBlue,
                            modifier = Modifier.size(20.dp)
                        )
                    }
                }
                Spacer(modifier = Modifier.width(12.dp))
                Column {
                    Text(
                        text = if (isLeftmost) "LEFTMOST DERIVATION" else "RIGHTMOST DERIVATION",
                        fontSize = 17.sp,
                        fontWeight = FontWeight.ExtraBold,
                        color = TextPrimary,
                        letterSpacing = 0.5.sp
                    )
                    Text(
                        text = if (isLeftmost) "Always expand leftmost variable" else "Always expand rightmost variable",
                        fontSize = 12.sp,
                        color = TextSecondary
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        if (activeStep != null) {
            // Sentential Form Card with selected symbol highlighted
            SententialFormView(
                step = activeStep,
                isLeftmost = isLeftmost
            )

            Spacer(modifier = Modifier.height(16.dp))

            // Playback and Step Navigation Controls
            AutoPlayControls(
                currentStepIndex = currentStepIndex,
                totalSteps = steps.size,
                isPlaying = isPlaying,
                playSpeed = playSpeed,
                onPrev = onPrevStep,
                onNext = onNextStep,
                onTogglePlay = onTogglePlay,
                onRestart = onRestart,
                onSetSpeed = onSetSpeed,
                onWhyClick = { onWhyClick(activeStep.explanation) }
            )

            Spacer(modifier = Modifier.height(20.dp))

            // Next phase action button
            Button(
                onClick = onProceedNext,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(50.dp)
                    .testTag(if (isLeftmost) "goto_rmd_button" else "goto_compare_button"),
                colors = ButtonDefaults.buttonColors(containerColor = PrimaryBlue),
                shape = RoundedCornerShape(14.dp)
            ) {
                Text(
                    text = if (isLeftmost) "Continue to Rightmost Derivation" else "Compare LMD vs RMD",
                    fontSize = 15.sp,
                    fontWeight = FontWeight.Bold
                )
                Spacer(modifier = Modifier.width(8.dp))
                Icon(Icons.AutoMirrored.Filled.ArrowForward, contentDescription = null, modifier = Modifier.size(18.dp))
            }
        } else {
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = CardSurface)
            ) {
                Box(modifier = Modifier.fillMaxWidth().padding(32.dp), contentAlignment = Alignment.Center) {
                    Text("No derivation steps available", color = TextSecondary)
                }
            }
        }
    }
}
