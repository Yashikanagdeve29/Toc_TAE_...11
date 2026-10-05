package com.example

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Scaffold
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.model.EducationalData
import com.example.ui.components.AppBottomNav
import com.example.ui.components.AppTab
import com.example.ui.components.ImageScannerDialog
import com.example.ui.components.StepProgressHeader
import com.example.ui.components.WhyExplanationBottomSheet
import com.example.ui.screens.CompareScreen
import com.example.ui.screens.DerivationScreen
import com.example.ui.screens.ExamplesScreen
import com.example.ui.screens.FinalResultScreen
import com.example.ui.screens.HomeScreen
import com.example.ui.screens.InputScreen
import com.example.ui.screens.LearnScreen
import com.example.ui.screens.ParseTreeScreen
import com.example.ui.screens.ValidationScreen
import com.example.ui.theme.AcademicBackground
import com.example.ui.theme.GrammarFlowTheme
import com.example.viewmodel.GrammarViewModel
import com.example.viewmodel.SolverStep

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            GrammarFlowTheme {
                GrammarFlowApp()
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun GrammarFlowApp(
    viewModel: GrammarViewModel = viewModel()
) {
    val uiState by viewModel.uiState.collectAsState()
    var currentTab by remember { mutableStateOf(AppTab.HOME) }
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)

    Scaffold(
        modifier = Modifier.fillMaxSize(),
        bottomBar = {
            AppBottomNav(
                currentTab = currentTab,
                onTabSelected = { currentTab = it }
            )
        }
    ) { innerPadding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(AcademicBackground)
                .padding(innerPadding)
        ) {
            when (currentTab) {
                AppTab.HOME -> {
                    HomeScreen(
                        onStartDerivation = {
                            currentTab = AppTab.SOLVER
                            viewModel.setSolverStep(SolverStep.INPUT)
                        },
                        onTryDemoProblem = {
                            viewModel.loadExample(EducationalData.EXAMPLES[1])
                            viewModel.validateAndContinue()
                            currentTab = AppTab.SOLVER
                            viewModel.setSolverStep(SolverStep.PARSE_TREE)
                        },
                        onFeatureCardClick = { feature ->
                            currentTab = AppTab.SOLVER
                            when (feature) {
                                "tree" -> {
                                    if (uiState.parsingOutcome?.isAccepted == true) {
                                        viewModel.setSolverStep(SolverStep.PARSE_TREE)
                                    } else {
                                        viewModel.validateAndContinue()
                                        viewModel.setSolverStep(SolverStep.PARSE_TREE)
                                    }
                                }
                                "lmd" -> {
                                    if (uiState.parsingOutcome?.isAccepted == true) {
                                        viewModel.setSolverStep(SolverStep.LMD)
                                    } else {
                                        viewModel.validateAndContinue()
                                        viewModel.setSolverStep(SolverStep.LMD)
                                    }
                                }
                                "rmd" -> {
                                    if (uiState.parsingOutcome?.isAccepted == true) {
                                        viewModel.setSolverStep(SolverStep.RMD)
                                    } else {
                                        viewModel.validateAndContinue()
                                        viewModel.setSolverStep(SolverStep.RMD)
                                    }
                                }
                                "compare" -> {
                                    if (uiState.parsingOutcome?.isAccepted == true) {
                                        viewModel.setSolverStep(SolverStep.COMPARE)
                                    } else {
                                        viewModel.validateAndContinue()
                                        viewModel.setSolverStep(SolverStep.COMPARE)
                                    }
                                }
                            }
                        }
                    )
                }

                AppTab.SOLVER -> {
                    Column(modifier = Modifier.fillMaxSize()) {
                        // Progression Stepper
                        StepProgressHeader(
                            currentStep = uiState.currentSolverStep,
                            onStepClick = { step -> viewModel.setSolverStep(step) }
                        )

                        // Solver Screens based on current step
                        Box(modifier = Modifier.weight(1f)) {
                            when (uiState.currentSolverStep) {
                                SolverStep.INPUT -> {
                                    InputScreen(
                                        state = uiState,
                                        onInputModeChange = { viewModel.setInputMode(it) },
                                        onStartSymbolChange = { viewModel.setStartSymbol(it) },
                                        onRulesTextChange = { viewModel.setRulesText(it) },
                                        onTargetStringChange = { viewModel.setTargetString(it) },
                                        onAddStructuredRule = { lhs, rhs -> viewModel.addStructuredRule(lhs, rhs) },
                                        onUpdateStructuredRule = { idx, lhs, rhs -> viewModel.updateStructuredRule(idx, lhs, rhs) },
                                        onRemoveStructuredRule = { idx -> viewModel.removeStructuredRule(idx) },
                                        onInsertSymbol = { viewModel.insertSymbolIntoRules(it) },
                                        onInsertTargetSymbol = { viewModel.insertSymbolIntoTarget(it) },
                                        onScanImageClick = { viewModel.openImageScannerDialog() },
                                        onValidateAndContinue = {
                                            viewModel.validateAndContinue()
                                        },
                                        onReset = { viewModel.resetAll() }
                                    )
                                }

                                SolverStep.VALIDATE -> {
                                    ValidationScreen(
                                        state = uiState,
                                        onStartDerivation = { viewModel.setSolverStep(SolverStep.LMD) },
                                        onViewParseTree = { viewModel.setSolverStep(SolverStep.PARSE_TREE) },
                                        onEditGrammar = { viewModel.setSolverStep(SolverStep.INPUT) },
                                        onChangeString = { viewModel.setSolverStep(SolverStep.INPUT) }
                                    )
                                }

                                SolverStep.PARSE_TREE -> {
                                    ParseTreeScreen(
                                        state = uiState,
                                        onSelectTree = { viewModel.selectParseTree(it) },
                                        onStartDerivation = { viewModel.setSolverStep(SolverStep.LMD) }
                                    )
                                }

                                SolverStep.LMD -> {
                                    DerivationScreen(
                                        isLeftmost = true,
                                        steps = viewModel.getCurrentLmdSteps(),
                                        currentStepIndex = uiState.currentLmdStepIndex,
                                        isPlaying = uiState.isAutoPlaying,
                                        playSpeed = uiState.playSpeed,
                                        onPrevStep = { viewModel.prevLmdStep() },
                                        onNextStep = { viewModel.nextLmdStep() },
                                        onTogglePlay = { viewModel.toggleAutoPlay(isLmd = true) },
                                        onRestart = { viewModel.restartAutoPlay(isLmd = true) },
                                        onSetSpeed = { viewModel.setPlaySpeed(it) },
                                        onWhyClick = { explanation -> viewModel.showWhyExplanation(explanation) },
                                        onProceedNext = { viewModel.setSolverStep(SolverStep.RMD) }
                                    )
                                }

                                SolverStep.RMD -> {
                                    DerivationScreen(
                                        isLeftmost = false,
                                        steps = viewModel.getCurrentRmdSteps(),
                                        currentStepIndex = uiState.currentRmdStepIndex,
                                        isPlaying = uiState.isAutoPlaying,
                                        playSpeed = uiState.playSpeed,
                                        onPrevStep = { viewModel.prevRmdStep() },
                                        onNextStep = { viewModel.nextRmdStep() },
                                        onTogglePlay = { viewModel.toggleAutoPlay(isLmd = false) },
                                        onRestart = { viewModel.restartAutoPlay(isLmd = false) },
                                        onSetSpeed = { viewModel.setPlaySpeed(it) },
                                        onWhyClick = { explanation -> viewModel.showWhyExplanation(explanation) },
                                        onProceedNext = { viewModel.setSolverStep(SolverStep.COMPARE) }
                                    )
                                }

                                SolverStep.COMPARE -> {
                                    CompareScreen(
                                        targetString = uiState.targetString,
                                        lmdSteps = viewModel.getCurrentLmdSteps(),
                                        rmdSteps = viewModel.getCurrentRmdSteps(),
                                        onProceedToFinalResult = { viewModel.setSolverStep(SolverStep.FINAL_RESULT) }
                                    )
                                }

                                SolverStep.FINAL_RESULT -> {
                                    FinalResultScreen(
                                        targetString = uiState.targetString,
                                        lmdSteps = viewModel.getCurrentLmdSteps(),
                                        rmdSteps = viewModel.getCurrentRmdSteps(),
                                        onViewLmd = { viewModel.setSolverStep(SolverStep.LMD) },
                                        onViewRmd = { viewModel.setSolverStep(SolverStep.RMD) },
                                        onViewParseTree = { viewModel.setSolverStep(SolverStep.PARSE_TREE) },
                                        onViewCompare = { viewModel.setSolverStep(SolverStep.COMPARE) },
                                        onNewProblem = {
                                            viewModel.resetAll()
                                            viewModel.setSolverStep(SolverStep.INPUT)
                                        }
                                    )
                                }
                            }
                        }
                    }
                }

                AppTab.EXAMPLES -> {
                    ExamplesScreen(
                        onSelectExample = { example ->
                            viewModel.loadExample(example)
                            viewModel.validateAndContinue()
                            currentTab = AppTab.SOLVER
                            viewModel.setSolverStep(SolverStep.PARSE_TREE)
                        }
                    )
                }

                AppTab.LEARN -> {
                    LearnScreen()
                }
            }
        }

        // Educational "Why?" Bottom Sheet
        if (uiState.showWhyBottomSheet) {
            WhyExplanationBottomSheet(
                explanation = uiState.activeWhyExplanation,
                onDismiss = { viewModel.dismissWhyBottomSheet() },
                sheetState = sheetState
            )
        }

        // Image / Camera OCR Scanner Dialog
        if (uiState.showImageScannerDialog) {
            ImageScannerDialog(
                onDismiss = { viewModel.dismissImageScannerDialog() },
                onConfirmGrammar = { scannedText ->
                    viewModel.processScannedGrammarText(scannedText)
                },
                errorMessage = uiState.ocrErrorMessage
            )
        }
    }
}
