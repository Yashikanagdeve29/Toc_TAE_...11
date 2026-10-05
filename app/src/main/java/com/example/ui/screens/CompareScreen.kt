package com.example.ui.screens

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
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
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.CompareArrows
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.Surface
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRow
import androidx.compose.material3.TabRowDefaults
import androidx.compose.material3.TabRowDefaults.tabIndicatorOffset
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.model.DerivationStep
import com.example.model.EPSILON
import com.example.model.GrammarSymbol
import com.example.ui.theme.AcademicBackground
import com.example.ui.theme.AcademicSuccess
import com.example.ui.theme.AcademicSuccessBg
import com.example.ui.theme.AcademicTeal
import com.example.ui.theme.AcademicTealLight
import com.example.ui.theme.CardSurface
import com.example.ui.theme.PrimaryBlue
import com.example.ui.theme.PrimaryBlueLight
import com.example.ui.theme.SkyBlue
import com.example.ui.theme.SurfaceBorder
import com.example.ui.theme.TextMuted
import com.example.ui.theme.TextPrimary
import com.example.ui.theme.TextSecondary

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun CompareScreen(
    targetString: String,
    lmdSteps: List<DerivationStep>,
    rmdSteps: List<DerivationStep>,
    onProceedToFinalResult: () -> Unit,
    modifier: Modifier = Modifier
) {
    var selectedTab by remember { mutableIntStateOf(2) } // default to Compare tab
    val scrollState = rememberScrollState()

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(AcademicBackground)
            .verticalScroll(scrollState)
            .padding(horizontal = 20.dp, vertical = 20.dp)
            .testTag("compare_screen")
    ) {
        // Header
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Surface(
                shape = RoundedCornerShape(10.dp),
                color = PrimaryBlueLight,
                modifier = Modifier.size(38.dp)
            ) {
                Box(contentAlignment = Alignment.Center) {
                    Icon(
                        Icons.Default.CompareArrows,
                        contentDescription = null,
                        tint = PrimaryBlue,
                        modifier = Modifier.size(22.dp)
                    )
                }
            }
            Spacer(modifier = Modifier.width(12.dp))
            Column {
                Text(
                    text = "LMD vs RMD Comparison",
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold,
                    color = TextPrimary
                )
                Text(
                    text = "Observe order of variable expansions",
                    fontSize = 12.sp,
                    color = TextSecondary
                )
            }
        }

        Spacer(modifier = Modifier.height(14.dp))

        // Final String verification banner
        Surface(
            shape = RoundedCornerShape(14.dp),
            color = AcademicSuccessBg,
            border = BorderStroke(1.dp, AcademicSuccess.copy(alpha = 0.4f)),
            modifier = Modifier.fillMaxWidth()
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 12.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        Icons.Default.CheckCircle,
                        contentDescription = null,
                        tint = AcademicSuccess,
                        modifier = Modifier.size(20.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "Final String:",
                        fontSize = 13.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = Color(0xFF14532D)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = targetString.ifEmpty { EPSILON },
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Bold,
                        fontFamily = FontFamily.Monospace,
                        color = Color(0xFF14532D)
                    )
                }

                Surface(
                    shape = RoundedCornerShape(6.dp),
                    color = AcademicSuccess
                ) {
                    Text(
                        text = "ACCEPTED",
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color.White,
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp)
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(14.dp))

        // Tabs: [ Leftmost ] [ Rightmost ] [ Compare ]
        TabRow(
            selectedTabIndex = selectedTab,
            containerColor = CardSurface,
            contentColor = PrimaryBlue,
            indicator = { tabPositions ->
                TabRowDefaults.SecondaryIndicator(
                    modifier = Modifier.tabIndicatorOffset(tabPositions[selectedTab]),
                    color = PrimaryBlue
                )
            },
            modifier = Modifier
                .clip(RoundedCornerShape(12.dp))
                .border(1.dp, SurfaceBorder, RoundedCornerShape(12.dp))
        ) {
            Tab(
                selected = selectedTab == 0,
                onClick = { selectedTab = 0 },
                text = { Text("Leftmost", fontWeight = if (selectedTab == 0) FontWeight.Bold else FontWeight.Medium) },
                modifier = Modifier.testTag("tab_compare_lmd")
            )
            Tab(
                selected = selectedTab == 1,
                onClick = { selectedTab = 1 },
                text = { Text("Rightmost", fontWeight = if (selectedTab == 1) FontWeight.Bold else FontWeight.Medium) },
                modifier = Modifier.testTag("tab_compare_rmd")
            )
            Tab(
                selected = selectedTab == 2,
                onClick = { selectedTab = 2 },
                text = { Text("Compare", fontWeight = if (selectedTab == 2) FontWeight.Bold else FontWeight.Medium) },
                modifier = Modifier.testTag("tab_compare_dual")
            )
        }

        Spacer(modifier = Modifier.height(16.dp))

        when (selectedTab) {
            0 -> DerivationSequenceView(title = "LEFTMOST DERIVATION CHAIN", steps = lmdSteps, isLeftmost = true)
            1 -> DerivationSequenceView(title = "RIGHTMOST DERIVATION CHAIN", steps = rmdSteps, isLeftmost = false)
            2 -> DualComparisonView(lmdSteps = lmdSteps, rmdSteps = rmdSteps)
        }

        Spacer(modifier = Modifier.height(20.dp))

        // Proceed Button
        Button(
            onClick = onProceedToFinalResult,
            modifier = Modifier
                .fillMaxWidth()
                .height(50.dp)
                .testTag("goto_final_result_button"),
            colors = ButtonDefaults.buttonColors(containerColor = PrimaryBlue),
            shape = RoundedCornerShape(14.dp)
        ) {
            Text("View Final Result Summary", fontSize = 15.sp, fontWeight = FontWeight.Bold)
            Spacer(modifier = Modifier.width(8.dp))
            Icon(Icons.AutoMirrored.Filled.ArrowForward, contentDescription = null, modifier = Modifier.size(18.dp))
        }
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun DerivationSequenceView(
    title: String,
    steps: List<DerivationStep>,
    isLeftmost: Boolean
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(18.dp),
        colors = CardDefaults.cardColors(containerColor = CardSurface),
        border = BorderStroke(1.dp, SurfaceBorder)
    ) {
        Column(modifier = Modifier.padding(18.dp)) {
            Text(
                text = title,
                fontSize = 11.sp,
                fontWeight = FontWeight.Bold,
                color = TextSecondary,
                letterSpacing = 0.5.sp
            )
            Spacer(modifier = Modifier.height(12.dp))

            steps.forEachIndexed { index, step ->
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 4.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = if (index == 0) " " else "⇒",
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Bold,
                        color = PrimaryBlue,
                        modifier = Modifier.width(22.dp)
                    )

                    FlowRow(
                        modifier = Modifier
                            .weight(1f)
                            .clip(RoundedCornerShape(8.dp))
                            .background(AcademicBackground)
                            .padding(8.dp),
                        horizontalArrangement = Arrangement.spacedBy(4.dp),
                        verticalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        step.sententialForm.forEachIndexed { symIdx, sym ->
                            val isReplaced = symIdx == step.selectedIndex
                            Surface(
                                shape = RoundedCornerShape(4.dp),
                                color = if (isReplaced) PrimaryBlue else PrimaryBlueLight
                            ) {
                                Text(
                                    text = sym,
                                    fontSize = 13.sp,
                                    fontWeight = if (isReplaced) FontWeight.ExtraBold else FontWeight.Medium,
                                    fontFamily = FontFamily.Monospace,
                                    color = if (isReplaced) Color.White else PrimaryBlue,
                                    modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                                )
                            }
                        }
                    }

                    Spacer(modifier = Modifier.width(8.dp))

                    Text(
                        text = step.productionUsed.displayString,
                        fontSize = 11.sp,
                        fontFamily = FontFamily.Monospace,
                        color = TextSecondary
                    )
                }
            }

            // Final string row
            if (steps.isNotEmpty()) {
                val last = steps.last()
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 4.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "⇒",
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Bold,
                        color = AcademicSuccess,
                        modifier = Modifier.width(22.dp)
                    )
                    FlowRow(
                        modifier = Modifier
                            .weight(1f)
                            .clip(RoundedCornerShape(8.dp))
                            .background(AcademicSuccessBg)
                            .padding(8.dp),
                        horizontalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        last.resultSententialForm.forEach { sym ->
                            Surface(
                                shape = RoundedCornerShape(4.dp),
                                color = AcademicTealLight
                            ) {
                                Text(
                                    text = sym,
                                    fontSize = 13.sp,
                                    fontWeight = FontWeight.Bold,
                                    fontFamily = FontFamily.Monospace,
                                    color = AcademicTeal,
                                    modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun DualComparisonView(
    lmdSteps: List<DerivationStep>,
    rmdSteps: List<DerivationStep>
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(18.dp),
        colors = CardDefaults.cardColors(containerColor = CardSurface),
        border = BorderStroke(1.dp, SurfaceBorder)
    ) {
        Column(modifier = Modifier.padding(18.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    text = "LEFTMOST (LMD)",
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold,
                    color = PrimaryBlue,
                    letterSpacing = 0.5.sp,
                    modifier = Modifier.weight(1f)
                )
                Text(
                    text = "RIGHTMOST (RMD)",
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold,
                    color = SkyBlue,
                    letterSpacing = 0.5.sp,
                    modifier = Modifier.weight(1f)
                )
            }

            Spacer(modifier = Modifier.height(10.dp))

            val maxSteps = maxOf(lmdSteps.size, rmdSteps.size)
            for (i in 0 until maxSteps) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 4.dp),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    // Leftmost step column
                    Surface(
                        modifier = Modifier.weight(1f),
                        shape = RoundedCornerShape(8.dp),
                        color = AcademicBackground,
                        border = BorderStroke(1.dp, SurfaceBorder)
                    ) {
                        if (i < lmdSteps.size) {
                            val step = lmdSteps[i]
                            Column(modifier = Modifier.padding(8.dp)) {
                                Text(
                                    text = "${i + 1}. ${step.sententialFormDisplay}",
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.Bold,
                                    fontFamily = FontFamily.Monospace,
                                    color = TextPrimary
                                )
                                Text(
                                    text = step.productionUsed.displayString,
                                    fontSize = 10.sp,
                                    color = PrimaryBlue,
                                    fontFamily = FontFamily.Monospace
                                )
                            }
                        }
                    }

                    // Rightmost step column
                    Surface(
                        modifier = Modifier.weight(1f),
                        shape = RoundedCornerShape(8.dp),
                        color = AcademicBackground,
                        border = BorderStroke(1.dp, SurfaceBorder)
                    ) {
                        if (i < rmdSteps.size) {
                            val step = rmdSteps[i]
                            Column(modifier = Modifier.padding(8.dp)) {
                                Text(
                                    text = "${i + 1}. ${step.sententialFormDisplay}",
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.Bold,
                                    fontFamily = FontFamily.Monospace,
                                    color = TextPrimary
                                )
                                Text(
                                    text = step.productionUsed.displayString,
                                    fontSize = 10.sp,
                                    color = SkyBlue,
                                    fontFamily = FontFamily.Monospace
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}
