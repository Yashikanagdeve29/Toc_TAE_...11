package com.example.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
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
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
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
import com.example.viewmodel.SolverStep

@Composable
fun StepProgressHeader(
    currentStep: SolverStep,
    onStepClick: (SolverStep) -> Unit,
    modifier: Modifier = Modifier
) {
    val steps = listOf(
        SolverStep.INPUT to "Input",
        SolverStep.VALIDATE to "Validate",
        SolverStep.PARSE_TREE to "Tree",
        SolverStep.LMD to "LMD",
        SolverStep.RMD to "RMD",
        SolverStep.COMPARE to "Compare",
        SolverStep.FINAL_RESULT to "Result"
    )

    val currentIdx = steps.indexOfFirst { it.first == currentStep }
    val scrollState = rememberScrollState()

    Surface(
        modifier = modifier.fillMaxWidth(),
        color = CardSurface,
        shadowElevation = 1.dp
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 10.dp)
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .horizontalScroll(scrollState)
                    .padding(horizontal = 16.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                steps.forEachIndexed { index, (step, label) ->
                    val isCurrent = step == currentStep
                    val isPast = index < currentIdx

                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier
                            .clip(RoundedCornerShape(20.dp))
                            .background(
                                when {
                                    isCurrent -> PrimaryBlue
                                    isPast -> PrimaryBlueLight
                                    else -> AcademicBackground
                                }
                            )
                            .clickable(enabled = isPast) { onStepClick(step) }
                            .padding(horizontal = 12.dp, vertical = 6.dp)
                            .testTag("step_pill_${step.name.lowercase()}")
                    ) {
                        Box(
                            modifier = Modifier
                                .size(18.dp)
                                .background(
                                    when {
                                        isCurrent -> Color.White
                                        isPast -> PrimaryBlue
                                        else -> TextMuted
                                    },
                                    CircleShape
                                ),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = "${index + 1}",
                                fontSize = 10.sp,
                                fontWeight = FontWeight.Bold,
                                color = when {
                                    isCurrent -> PrimaryBlue
                                    isPast -> Color.White
                                    else -> Color.White
                                }
                            )
                        }

                        Spacer(modifier = Modifier.width(6.dp))

                        Text(
                            text = label,
                            fontSize = 12.sp,
                            fontWeight = if (isCurrent) FontWeight.Bold else FontWeight.Medium,
                            color = when {
                                isCurrent -> Color.White
                                isPast -> PrimaryBlue
                                else -> TextSecondary
                            }
                        )
                    }

                    if (index < steps.size - 1) {
                        Box(
                            modifier = Modifier
                                .width(8.dp)
                                .height(2.dp)
                                .background(if (index < currentIdx) PrimaryBlue else SurfaceBorder)
                        )
                    }
                }
            }
        }
    }
}
