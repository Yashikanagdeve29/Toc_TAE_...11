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
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.filled.WarningAmber
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.ScrollableTabRow
import androidx.compose.material3.Surface
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRowDefaults
import androidx.compose.material3.TabRowDefaults.tabIndicatorOffset
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
import com.example.model.EPSILON
import com.example.ui.components.ParseTreeCanvas
import com.example.ui.theme.AcademicBackground
import com.example.ui.theme.AcademicWarning
import com.example.ui.theme.AcademicWarningBg
import com.example.ui.theme.CardSurface
import com.example.ui.theme.PrimaryBlue
import com.example.ui.theme.SurfaceBorder
import com.example.ui.theme.TextPrimary
import com.example.ui.theme.TextSecondary
import com.example.viewmodel.GrammarUiState

@Composable
fun ParseTreeScreen(
    state: GrammarUiState,
    onSelectTree: (Int) -> Unit,
    onStartDerivation: () -> Unit,
    modifier: Modifier = Modifier
) {
    val outcome = state.parsingOutcome
    val trees = outcome?.parseTrees ?: emptyList()
    val activeTree = if (trees.isNotEmpty()) {
        trees[state.selectedTreeIndex.coerceIn(0, trees.size - 1)]
    } else null

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(AcademicBackground)
            .padding(16.dp)
            .testTag("parse_tree_screen")
    ) {
        // Header
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column {
                Text(
                    text = "Parse Tree Visualization",
                    fontSize = 19.sp,
                    fontWeight = FontWeight.Bold,
                    color = TextPrimary
                )
                Text(
                    text = "Concrete Syntax Tree for \"${state.targetString.ifEmpty { EPSILON }}\"",
                    fontSize = 12.sp,
                    fontFamily = FontFamily.Monospace,
                    color = TextSecondary
                )
            }
        }

        // Ambiguity Switcher Tabs if multiple parse trees exist
        if (trees.size > 1) {
            Spacer(modifier = Modifier.height(10.dp))

            Surface(
                shape = RoundedCornerShape(10.dp),
                color = AcademicWarningBg,
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(
                    modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        Icons.Default.WarningAmber,
                        contentDescription = null,
                        tint = AcademicWarning,
                        modifier = Modifier.size(16.dp)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = "Ambiguous Grammar: ${trees.size} parse trees detected",
                        fontSize = 11.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = Color(0xFF92400E)
                    )
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            ScrollableTabRow(
                selectedTabIndex = state.selectedTreeIndex,
                containerColor = CardSurface,
                contentColor = PrimaryBlue,
                edgePadding = 0.dp,
                indicator = { tabPositions ->
                    TabRowDefaults.SecondaryIndicator(
                        modifier = Modifier.tabIndicatorOffset(
                            tabPositions[state.selectedTreeIndex.coerceIn(0, tabPositions.size - 1)]
                        ),
                        color = PrimaryBlue
                    )
                },
                modifier = Modifier.fillMaxWidth()
            ) {
                trees.forEachIndexed { index, _ ->
                    Tab(
                        selected = state.selectedTreeIndex == index,
                        onClick = { onSelectTree(index) },
                        text = {
                            Text(
                                "Parse Tree ${index + 1}",
                                fontWeight = if (state.selectedTreeIndex == index) FontWeight.Bold else FontWeight.Medium
                            )
                        },
                        modifier = Modifier.testTag("tab_parse_tree_$index")
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(12.dp))

        // Tree Canvas Card
        Box(
            modifier = Modifier
                .weight(1f)
                .fillMaxWidth()
        ) {
            if (activeTree != null) {
                ParseTreeCanvas(
                    rootNode = activeTree,
                    modifier = Modifier.fillMaxSize()
                )
            } else {
                Card(
                    modifier = Modifier.fillMaxSize(),
                    colors = CardDefaults.cardColors(containerColor = CardSurface)
                ) {
                    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                        Text("No parse tree generated", color = TextSecondary)
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(14.dp))

        // Bottom Navigation Action: Start Derivation
        Button(
            onClick = onStartDerivation,
            modifier = Modifier
                .fillMaxWidth()
                .height(50.dp)
                .testTag("start_derivation_from_tree_button"),
            colors = ButtonDefaults.buttonColors(containerColor = PrimaryBlue),
            shape = RoundedCornerShape(14.dp)
        ) {
            Text("Start Derivations (LMD & RMD)", fontSize = 15.sp, fontWeight = FontWeight.Bold)
            Spacer(modifier = Modifier.width(8.dp))
            Icon(Icons.AutoMirrored.Filled.ArrowForward, contentDescription = null, modifier = Modifier.size(18.dp))
        }
    }
}
