package com.example.ui.screens

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
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
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.CameraAlt
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRow
import androidx.compose.material3.TabRowDefaults
import androidx.compose.material3.TabRowDefaults.tabIndicatorOffset
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import com.example.model.EPSILON
import com.example.model.Production
import com.example.ui.theme.AcademicBackground
import com.example.ui.theme.AcademicError
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
import com.example.viewmodel.GrammarUiState
import com.example.viewmodel.InputMode

@Composable
fun InputScreen(
    state: GrammarUiState,
    onInputModeChange: (InputMode) -> Unit,
    onStartSymbolChange: (String) -> Unit,
    onRulesTextChange: (String) -> Unit,
    onTargetStringChange: (String) -> Unit,
    onAddStructuredRule: (String, String) -> Unit,
    onUpdateStructuredRule: (Int, String, String) -> Unit,
    onRemoveStructuredRule: (Int) -> Unit,
    onInsertSymbol: (String) -> Unit,
    onInsertTargetSymbol: (String) -> Unit,
    onScanImageClick: () -> Unit,
    onValidateAndContinue: () -> Unit,
    onReset: () -> Unit,
    modifier: Modifier = Modifier
) {
    val scrollState = rememberScrollState()

    var showAddRuleDialog by remember { mutableStateOf(false) }
    var editingRuleIndex by remember { mutableStateOf<Int?>(null) }
    var dialogLhs by remember { mutableStateOf("") }
    var dialogRhs by remember { mutableStateOf("") }

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(AcademicBackground)
            .verticalScroll(scrollState)
            .padding(horizontal = 20.dp, vertical = 20.dp)
            .testTag("input_screen")
    ) {
        // Top Toolbar
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column {
                Text(
                    text = "Grammar Definition",
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Bold,
                    color = TextPrimary
                )
                Text(
                    text = "Specify CFG productions & target string",
                    fontSize = 12.sp,
                    color = TextSecondary
                )
            }

            Row {
                IconButton(
                    onClick = onScanImageClick,
                    modifier = Modifier.testTag("scan_image_header_button")
                ) {
                    Icon(
                        Icons.Default.CameraAlt,
                        contentDescription = "Scan Image",
                        tint = PrimaryBlue
                    )
                }

                IconButton(
                    onClick = onReset,
                    modifier = Modifier.testTag("reset_grammar_button")
                ) {
                    Icon(
                        Icons.Default.Refresh,
                        contentDescription = "Reset Form",
                        tint = TextSecondary
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Start Symbol Input Card
        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(containerColor = CardSurface),
            border = BorderStroke(1.dp, SurfaceBorder)
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = "START SYMBOL",
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        color = TextSecondary,
                        letterSpacing = 0.5.sp
                    )
                    Spacer(modifier = Modifier.height(2.dp))
                    Text(
                        text = "Initial root variable of derivation",
                        fontSize = 12.sp,
                        color = TextSecondary
                    )
                }

                OutlinedTextField(
                    value = state.startSymbol,
                    onValueChange = onStartSymbolChange,
                    modifier = Modifier
                        .width(76.dp)
                        .testTag("start_symbol_input"),
                    shape = RoundedCornerShape(10.dp),
                    textStyle = TextStyle(
                        fontFamily = FontFamily.Monospace,
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold,
                        color = PrimaryBlue
                    ),
                    singleLine = true,
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = PrimaryBlue,
                        unfocusedBorderColor = SurfaceBorder
                    )
                )
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Input Mode Tabs: Structured vs Text
        TabRow(
            selectedTabIndex = if (state.inputMode == InputMode.STRUCTURED) 0 else 1,
            containerColor = CardSurface,
            contentColor = PrimaryBlue,
            indicator = { tabPositions ->
                TabRowDefaults.SecondaryIndicator(
                    modifier = Modifier.tabIndicatorOffset(
                        tabPositions[if (state.inputMode == InputMode.STRUCTURED) 0 else 1]
                    ),
                    color = PrimaryBlue
                )
            },
            modifier = Modifier
                .clip(RoundedCornerShape(12.dp))
                .border(1.dp, SurfaceBorder, RoundedCornerShape(12.dp))
        ) {
            Tab(
                selected = state.inputMode == InputMode.STRUCTURED,
                onClick = { onInputModeChange(InputMode.STRUCTURED) },
                text = {
                    Text(
                        "Structured Mode",
                        fontWeight = if (state.inputMode == InputMode.STRUCTURED) FontWeight.Bold else FontWeight.Medium
                    )
                },
                modifier = Modifier.testTag("tab_structured_mode")
            )
            Tab(
                selected = state.inputMode == InputMode.TEXT,
                onClick = { onInputModeChange(InputMode.TEXT) },
                text = {
                    Text(
                        "Text Mode",
                        fontWeight = if (state.inputMode == InputMode.TEXT) FontWeight.Bold else FontWeight.Medium
                    )
                },
                modifier = Modifier.testTag("tab_text_mode")
            )
        }

        Spacer(modifier = Modifier.height(14.dp))

        // Quick Symbol Insertion Pills
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .horizontalScroll(rememberScrollState()),
            horizontalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            listOf("->", "|", EPSILON, "S", "A", "B", "a", "b").forEach { sym ->
                Surface(
                    shape = RoundedCornerShape(8.dp),
                    color = CardSurface,
                    border = BorderStroke(1.dp, SurfaceBorder),
                    modifier = Modifier
                        .clickable { onInsertSymbol(sym) }
                        .testTag("quick_symbol_$sym")
                ) {
                    Text(
                        text = sym,
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Bold,
                        fontFamily = FontFamily.Monospace,
                        color = PrimaryBlue,
                        modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp)
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(14.dp))

        // Productions Area
        if (state.inputMode == InputMode.STRUCTURED) {
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = CardSurface),
                border = BorderStroke(1.dp, SurfaceBorder)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "PRODUCTIONS (${state.structuredRules.size})",
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            color = TextSecondary,
                            letterSpacing = 0.5.sp
                        )

                        OutlinedButton(
                            onClick = {
                                dialogLhs = state.startSymbol
                                dialogRhs = ""
                                editingRuleIndex = null
                                showAddRuleDialog = true
                            },
                            modifier = Modifier
                                .height(32.dp)
                                .testTag("add_rule_button"),
                            shape = RoundedCornerShape(8.dp),
                            border = BorderStroke(1.dp, PrimaryBlue),
                            contentPadding = androidx.compose.foundation.layout.PaddingValues(horizontal = 10.dp)
                        ) {
                            Icon(Icons.Default.Add, contentDescription = null, tint = PrimaryBlue, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("Add Rule", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = PrimaryBlue)
                        }
                    }

                    Spacer(modifier = Modifier.height(10.dp))

                    if (state.structuredRules.isEmpty()) {
                        Text(
                            text = "No productions defined. Tap 'Add Rule' above.",
                            fontSize = 13.sp,
                            color = TextMuted,
                            modifier = Modifier.padding(vertical = 16.dp)
                        )
                    } else {
                        state.structuredRules.forEachIndexed { index, prod ->
                            Surface(
                                shape = RoundedCornerShape(10.dp),
                                color = AcademicBackground,
                                border = BorderStroke(1.dp, SurfaceBorder),
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(vertical = 4.dp)
                            ) {
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(horizontal = 12.dp, vertical = 8.dp),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Text(
                                        text = prod.displayString,
                                        fontSize = 15.sp,
                                        fontWeight = FontWeight.Bold,
                                        fontFamily = FontFamily.Monospace,
                                        color = TextPrimary
                                    )

                                    Row {
                                        IconButton(
                                            onClick = {
                                                dialogLhs = prod.lhs
                                                dialogRhs = prod.displayRhs
                                                editingRuleIndex = index
                                                showAddRuleDialog = true
                                            },
                                            modifier = Modifier.size(32.dp)
                                        ) {
                                            Icon(Icons.Default.Edit, contentDescription = "Edit Rule", tint = TextSecondary, modifier = Modifier.size(16.dp))
                                        }

                                        IconButton(
                                            onClick = { onRemoveStructuredRule(index) },
                                            modifier = Modifier.size(32.dp)
                                        ) {
                                            Icon(Icons.Default.Delete, contentDescription = "Delete Rule", tint = AcademicError, modifier = Modifier.size(16.dp))
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }
        } else {
            // Text Mode Input
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = CardSurface),
                border = BorderStroke(1.dp, SurfaceBorder)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(
                        text = "PRODUCTIONS (TEXT)",
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        color = TextSecondary,
                        letterSpacing = 0.5.sp
                    )
                    Spacer(modifier = Modifier.height(8.dp))

                    OutlinedTextField(
                        value = state.rulesText,
                        onValueChange = onRulesTextChange,
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(150.dp)
                            .testTag("rules_text_input"),
                        shape = RoundedCornerShape(12.dp),
                        textStyle = TextStyle(
                            fontFamily = FontFamily.Monospace,
                            fontSize = 15.sp,
                            lineHeight = 22.sp,
                            color = TextPrimary
                        ),
                        placeholder = {
                            Text("S -> AB\nA -> aA | a\nB -> bB | b")
                        },
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = PrimaryBlue,
                            unfocusedBorderColor = SurfaceBorder
                        )
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Target String Input Card
        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(containerColor = CardSurface),
            border = BorderStroke(1.dp, SurfaceBorder)
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Text(
                    text = "TARGET STRING",
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold,
                    color = TextSecondary,
                    letterSpacing = 0.5.sp
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = "String to derive using LMD and RMD",
                    fontSize = 12.sp,
                    color = TextSecondary
                )
                Spacer(modifier = Modifier.height(8.dp))

                OutlinedTextField(
                    value = state.targetString,
                    onValueChange = onTargetStringChange,
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("target_string_input"),
                    shape = RoundedCornerShape(12.dp),
                    textStyle = TextStyle(
                        fontFamily = FontFamily.Monospace,
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Bold,
                        color = TextPrimary
                    ),
                    placeholder = { Text("e.g. aabb or leave empty for ε") },
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = PrimaryBlue,
                        unfocusedBorderColor = SurfaceBorder
                    )
                )

                Spacer(modifier = Modifier.height(8.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    listOf("a", "b", "c", "ε", "+", "*").forEach { charSym ->
                        Surface(
                            shape = RoundedCornerShape(6.dp),
                            color = AcademicBackground,
                            border = BorderStroke(1.dp, SurfaceBorder),
                            modifier = Modifier
                                .clickable { onInsertTargetSymbol(charSym) }
                                .testTag("target_char_$charSym")
                        ) {
                            Text(
                                text = "+$charSym",
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold,
                                color = TextSecondary,
                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                            )
                        }
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(24.dp))

        // Validate & Continue Primary Action
        Button(
            onClick = onValidateAndContinue,
            modifier = Modifier
                .fillMaxWidth()
                .height(52.dp)
                .testTag("validate_and_continue_button"),
            colors = ButtonDefaults.buttonColors(containerColor = PrimaryBlue),
            shape = RoundedCornerShape(14.dp)
        ) {
            Text(
                text = "Validate & Continue",
                fontSize = 16.sp,
                fontWeight = FontWeight.Bold
            )
            Spacer(modifier = Modifier.width(8.dp))
            Icon(Icons.AutoMirrored.Filled.ArrowForward, contentDescription = null, modifier = Modifier.size(18.dp))
        }
    }

    // Add / Edit Rule Dialog
    if (showAddRuleDialog) {
        Dialog(onDismissRequest = { showAddRuleDialog = false }) {
            Surface(
                modifier = Modifier
                    .fillMaxWidth(0.92f)
                    .testTag("add_rule_dialog"),
                shape = RoundedCornerShape(18.dp),
                color = CardSurface,
                shadowElevation = 6.dp
            ) {
                Column(modifier = Modifier.padding(20.dp)) {
                    Text(
                        text = if (editingRuleIndex != null) "Edit Production Rule" else "Add Production Rule",
                        fontSize = 17.sp,
                        fontWeight = FontWeight.Bold,
                        color = TextPrimary
                    )
                    Spacer(modifier = Modifier.height(14.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        OutlinedTextField(
                            value = dialogLhs,
                            onValueChange = { dialogLhs = it },
                            label = { Text("LHS (Variable)") },
                            modifier = Modifier.weight(1f),
                            singleLine = true
                        )
                        Text(
                            text = "→",
                            fontSize = 20.sp,
                            fontWeight = FontWeight.Bold,
                            color = PrimaryBlue,
                            modifier = Modifier.padding(horizontal = 8.dp)
                        )
                        OutlinedTextField(
                            value = dialogRhs,
                            onValueChange = { dialogRhs = it },
                            label = { Text("RHS") },
                            modifier = Modifier.weight(2f),
                            singleLine = true
                        )
                    }

                    Spacer(modifier = Modifier.height(20.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.End
                    ) {
                        OutlinedButton(
                            onClick = { showAddRuleDialog = false },
                            shape = RoundedCornerShape(10.dp)
                        ) {
                            Text("Cancel")
                        }
                        Spacer(modifier = Modifier.width(10.dp))
                        Button(
                            onClick = {
                                if (editingRuleIndex != null) {
                                    onUpdateStructuredRule(editingRuleIndex!!, dialogLhs, dialogRhs)
                                } else {
                                    onAddStructuredRule(dialogLhs, dialogRhs)
                                }
                                showAddRuleDialog = false
                            },
                            colors = ButtonDefaults.buttonColors(containerColor = PrimaryBlue),
                            shape = RoundedCornerShape(10.dp)
                        ) {
                            Text("Save Rule")
                        }
                    }
                }
            }
        }
    }
}
