package com.example.ui.screens

import androidx.compose.foundation.BorderStroke
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
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccountTree
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.CompareArrows
import androidx.compose.material.icons.filled.FormatAlignLeft
import androidx.compose.material.icons.filled.FormatAlignRight
import androidx.compose.material.icons.filled.RestartAlt
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.model.DerivationStep
import com.example.model.EPSILON
import com.example.ui.theme.AcademicBackground
import com.example.ui.theme.AcademicSuccess
import com.example.ui.theme.AcademicSuccessBg
import com.example.ui.theme.AcademicTeal
import com.example.ui.theme.CardSurface
import com.example.ui.theme.PrimaryBlue
import com.example.ui.theme.PrimaryBlueLight
import com.example.ui.theme.SurfaceBorder
import com.example.ui.theme.TextMuted
import com.example.ui.theme.TextPrimary
import com.example.ui.theme.TextSecondary

@Composable
fun FinalResultScreen(
    targetString: String,
    lmdSteps: List<DerivationStep>,
    rmdSteps: List<DerivationStep>,
    onViewLmd: () -> Unit,
    onViewRmd: () -> Unit,
    onViewParseTree: () -> Unit,
    onViewCompare: () -> Unit,
    onNewProblem: () -> Unit,
    modifier: Modifier = Modifier
) {
    val scrollState = rememberScrollState()

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(AcademicBackground)
            .verticalScroll(scrollState)
            .padding(horizontal = 20.dp, vertical = 24.dp)
            .testTag("final_result_screen")
    ) {
        // Success Header
        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(20.dp),
            colors = CardDefaults.cardColors(containerColor = CardSurface),
            border = BorderStroke(1.dp, AcademicSuccess.copy(alpha = 0.3f)),
            elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(22.dp)
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier = Modifier
                            .size(48.dp)
                            .background(AcademicSuccessBg, CircleShape),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            Icons.Default.CheckCircle,
                            contentDescription = null,
                            tint = AcademicSuccess,
                            modifier = Modifier.size(28.dp)
                        )
                    }

                    Spacer(modifier = Modifier.width(14.dp))

                    Column {
                        Text(
                            text = "DERIVATION COMPLETE",
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            color = AcademicSuccess,
                            letterSpacing = 1.sp
                        )
                        Text(
                            text = "String Successfully Derived",
                            fontSize = 18.sp,
                            fontWeight = FontWeight.Bold,
                            color = TextPrimary
                        )
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                Surface(
                    shape = RoundedCornerShape(12.dp),
                    color = AcademicBackground,
                    border = BorderStroke(1.dp, SurfaceBorder),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(14.dp),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Column {
                            Text(
                                text = "TARGET STRING",
                                fontSize = 10.sp,
                                fontWeight = FontWeight.Bold,
                                color = TextSecondary
                            )
                            Spacer(modifier = Modifier.height(2.dp))
                            Text(
                                text = targetString.ifEmpty { EPSILON },
                                fontSize = 16.sp,
                                fontWeight = FontWeight.Bold,
                                fontFamily = FontFamily.Monospace,
                                color = PrimaryBlue
                            )
                        }

                        Column(horizontalAlignment = Alignment.End) {
                            Text(
                                text = "DERIVATION LENGTH",
                                fontSize = 10.sp,
                                fontWeight = FontWeight.Bold,
                                color = TextSecondary
                            )
                            Spacer(modifier = Modifier.height(2.dp))
                            Text(
                                text = "${lmdSteps.size} steps",
                                fontSize = 15.sp,
                                fontWeight = FontWeight.Bold,
                                color = TextPrimary
                            )
                        }
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Full LMD Sequence Card
        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(18.dp),
            colors = CardDefaults.cardColors(containerColor = CardSurface),
            border = BorderStroke(1.dp, SurfaceBorder)
        ) {
            Column(modifier = Modifier.padding(18.dp)) {
                Text(
                    text = "FULL LEFTMOST DERIVATION (LMD)",
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold,
                    color = PrimaryBlue,
                    letterSpacing = 0.5.sp
                )
                Spacer(modifier = Modifier.height(8.dp))

                val lmdChain = buildDerivationChain(lmdSteps, targetString)
                Text(
                    text = lmdChain,
                    fontSize = 14.sp,
                    lineHeight = 22.sp,
                    fontFamily = FontFamily.Monospace,
                    fontWeight = FontWeight.SemiBold,
                    color = TextPrimary
                )
            }
        }

        Spacer(modifier = Modifier.height(14.dp))

        // Full RMD Sequence Card
        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(18.dp),
            colors = CardDefaults.cardColors(containerColor = CardSurface),
            border = BorderStroke(1.dp, SurfaceBorder)
        ) {
            Column(modifier = Modifier.padding(18.dp)) {
                Text(
                    text = "FULL RIGHTMOST DERIVATION (RMD)",
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold,
                    color = AcademicTeal,
                    letterSpacing = 0.5.sp
                )
                Spacer(modifier = Modifier.height(8.dp))

                val rmdChain = buildDerivationChain(rmdSteps, targetString)
                Text(
                    text = rmdChain,
                    fontSize = 14.sp,
                    lineHeight = 22.sp,
                    fontFamily = FontFamily.Monospace,
                    fontWeight = FontWeight.SemiBold,
                    color = TextPrimary
                )
            }
        }

        Spacer(modifier = Modifier.height(20.dp))

        // Navigation Actions Grid
        Text(
            text = "EXPLORE VIEWS",
            fontSize = 11.sp,
            fontWeight = FontWeight.Bold,
            color = TextSecondary,
            letterSpacing = 1.sp
        )

        Spacer(modifier = Modifier.height(10.dp))

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            OutlinedButton(
                onClick = onViewLmd,
                modifier = Modifier
                    .weight(1f)
                    .height(44.dp)
                    .testTag("result_view_lmd_button"),
                shape = RoundedCornerShape(12.dp),
                border = BorderStroke(1.dp, PrimaryBlue)
            ) {
                Icon(Icons.Default.FormatAlignLeft, contentDescription = null, tint = PrimaryBlue, modifier = Modifier.size(16.dp))
                Spacer(modifier = Modifier.width(6.dp))
                Text("View LMD", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = PrimaryBlue)
            }

            OutlinedButton(
                onClick = onViewRmd,
                modifier = Modifier
                    .weight(1f)
                    .height(44.dp)
                    .testTag("result_view_rmd_button"),
                shape = RoundedCornerShape(12.dp),
                border = BorderStroke(1.dp, PrimaryBlue)
            ) {
                Icon(Icons.Default.FormatAlignRight, contentDescription = null, tint = PrimaryBlue, modifier = Modifier.size(16.dp))
                Spacer(modifier = Modifier.width(6.dp))
                Text("View RMD", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = PrimaryBlue)
            }
        }

        Spacer(modifier = Modifier.height(10.dp))

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            OutlinedButton(
                onClick = onViewParseTree,
                modifier = Modifier
                    .weight(1f)
                    .height(44.dp)
                    .testTag("result_view_tree_button"),
                shape = RoundedCornerShape(12.dp),
                border = BorderStroke(1.dp, PrimaryBlue)
            ) {
                Icon(Icons.Default.AccountTree, contentDescription = null, tint = PrimaryBlue, modifier = Modifier.size(16.dp))
                Spacer(modifier = Modifier.width(6.dp))
                Text("Parse Tree", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = PrimaryBlue)
            }

            OutlinedButton(
                onClick = onViewCompare,
                modifier = Modifier
                    .weight(1f)
                    .height(44.dp)
                    .testTag("result_view_compare_button"),
                shape = RoundedCornerShape(12.dp),
                border = BorderStroke(1.dp, PrimaryBlue)
            ) {
                Icon(Icons.Default.CompareArrows, contentDescription = null, tint = PrimaryBlue, modifier = Modifier.size(16.dp))
                Spacer(modifier = Modifier.width(6.dp))
                Text("Compare", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = PrimaryBlue)
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // New Problem Button
        Button(
            onClick = onNewProblem,
            modifier = Modifier
                .fillMaxWidth()
                .height(50.dp)
                .testTag("new_problem_button"),
            colors = ButtonDefaults.buttonColors(containerColor = PrimaryBlue),
            shape = RoundedCornerShape(14.dp)
        ) {
            Icon(Icons.Default.RestartAlt, contentDescription = null, modifier = Modifier.size(20.dp))
            Spacer(modifier = Modifier.width(8.dp))
            Text("Solve Another Problem", fontSize = 15.sp, fontWeight = FontWeight.Bold)
        }
    }
}

private fun buildDerivationChain(steps: List<DerivationStep>, target: String): String {
    if (steps.isEmpty()) return target
    val forms = steps.map { it.sententialFormDisplay } + steps.last().resultSententialForm.joinToString("")
    return forms.joinToString(" ⇒ ")
}
