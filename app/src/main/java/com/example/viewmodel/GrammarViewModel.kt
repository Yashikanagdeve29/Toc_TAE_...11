package com.example.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.model.CFG
import com.example.model.DerivationStep
import com.example.model.EPSILON
import com.example.model.EducationalData
import com.example.model.GrammarExample
import com.example.model.ParseTreeNode
import com.example.model.ParsingOutcome
import com.example.model.Production
import com.example.parser.DerivationEngine
import com.example.parser.EarleyParser
import com.example.parser.GrammarParser
import com.example.parser.GrammarValidator
import com.example.parser.ValidationResult
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch

enum class InputMode {
    STRUCTURED,
    TEXT
}

enum class SolverStep {
    INPUT,
    VALIDATE,
    PARSE_TREE,
    LMD,
    RMD,
    COMPARE,
    FINAL_RESULT
}

data class GrammarUiState(
    val currentSolverStep: SolverStep = SolverStep.INPUT,
    val inputMode: InputMode = InputMode.STRUCTURED,
    val startSymbol: String = "S",
    val rulesText: String = "S -> AB\nA -> aA | a\nB -> bB | b",
    val structuredRules: List<Production> = listOf(
        Production(1, "S", listOf("A", "B")),
        Production(2, "A", listOf("a", "A")),
        Production(3, "A", listOf("a")),
        Production(4, "B", listOf("b", "B")),
        Production(5, "B", listOf("b"))
    ),
    val targetString: String = "aabb",
    val cfg: CFG? = null,
    val parsingOutcome: ParsingOutcome? = null,
    val selectedTreeIndex: Int = 0,
    val currentLmdStepIndex: Int = 0,
    val currentRmdStepIndex: Int = 0,
    val isAutoPlaying: Boolean = false,
    val playSpeed: Float = 1.0f,
    val showWhyBottomSheet: Boolean = false,
    val activeWhyExplanation: String = "",
    val showImageScannerDialog: Boolean = false,
    val ocrProcessing: Boolean = false,
    val ocrErrorMessage: String? = null
)

class GrammarViewModel : ViewModel() {

    private val _uiState = MutableStateFlow(GrammarUiState())
    val uiState: StateFlow<GrammarUiState> = _uiState.asStateFlow()

    private var autoPlayJob: Job? = null

    init {
        // Initial parse of default example so state is ready
        syncStructuredFromText()
    }

    fun setInputMode(mode: InputMode) {
        if (mode == InputMode.TEXT && _uiState.value.inputMode == InputMode.STRUCTURED) {
            // Sync text from structured
            val text = GrammarParser.productionsToText(_uiState.value.structuredRules)
            _uiState.update { it.copy(inputMode = mode, rulesText = text) }
        } else if (mode == InputMode.STRUCTURED && _uiState.value.inputMode == InputMode.TEXT) {
            syncStructuredFromText()
            _uiState.update { it.copy(inputMode = mode) }
        } else {
            _uiState.update { it.copy(inputMode = mode) }
        }
    }

    fun setStartSymbol(symbol: String) {
        _uiState.update { it.copy(startSymbol = symbol.trim()) }
    }

    fun setRulesText(text: String) {
        _uiState.update { it.copy(rulesText = text) }
    }

    fun setTargetString(str: String) {
        _uiState.update { it.copy(targetString = str) }
    }

    fun insertSymbolIntoRules(symbol: String) {
        val currentText = _uiState.value.rulesText
        _uiState.update { it.copy(rulesText = if (currentText.isEmpty()) symbol else "$currentText $symbol") }
    }

    fun insertSymbolIntoTarget(symbol: String) {
        _uiState.update { it.copy(targetString = it.targetString + symbol) }
    }

    fun addStructuredRule(lhs: String, rhsTokensString: String) {
        val trimmedLhs = lhs.trim()
        if (trimmedLhs.isEmpty()) return
        val tokens = GrammarParser.tokenizeRhs(rhsTokensString, setOf(trimmedLhs))
        val newProd = Production(
            id = (_uiState.value.structuredRules.maxOfOrNull { it.id } ?: 0) + 1,
            lhs = trimmedLhs,
            rhs = tokens
        )
        val updated = _uiState.value.structuredRules + newProd
        _uiState.update {
            it.copy(
                structuredRules = updated,
                rulesText = GrammarParser.productionsToText(updated)
            )
        }
    }

    fun updateStructuredRule(index: Int, lhs: String, rhsTokensString: String) {
        if (index !in _uiState.value.structuredRules.indices) return
        val trimmedLhs = lhs.trim()
        if (trimmedLhs.isEmpty()) return
        val tokens = GrammarParser.tokenizeRhs(rhsTokensString, setOf(trimmedLhs))
        val currentProd = _uiState.value.structuredRules[index]
        val updatedList = _uiState.value.structuredRules.toMutableList()
        updatedList[index] = currentProd.copy(lhs = trimmedLhs, rhs = tokens)
        _uiState.update {
            it.copy(
                structuredRules = updatedList,
                rulesText = GrammarParser.productionsToText(updatedList)
            )
        }
    }

    fun removeStructuredRule(index: Int) {
        if (index !in _uiState.value.structuredRules.indices) return
        val updated = _uiState.value.structuredRules.toMutableList()
        updated.removeAt(index)
        _uiState.update {
            it.copy(
                structuredRules = updated,
                rulesText = GrammarParser.productionsToText(updated)
            )
        }
    }

    private fun syncStructuredFromText() {
        val parsedCfg = GrammarParser.parseGrammarText(_uiState.value.startSymbol, _uiState.value.rulesText)
        _uiState.update {
            it.copy(
                structuredRules = parsedCfg.productions,
                startSymbol = parsedCfg.startSymbol
            )
        }
    }

    fun validateAndContinue() {
        stopAutoPlay()

        val state = _uiState.value
        val cfg = if (state.inputMode == InputMode.STRUCTURED) {
            val nonTerms = state.structuredRules.map { it.lhs }.toMutableSet()
            if (state.startSymbol.isNotEmpty()) nonTerms.add(state.startSymbol)
            val terms = mutableSetOf<String>()
            for (prod in state.structuredRules) {
                for (sym in prod.rhs) {
                    if (!nonTerms.contains(sym) && sym != EPSILON) {
                        terms.add(sym)
                    }
                }
            }
            CFG(
                startSymbol = state.startSymbol,
                productions = state.structuredRules,
                nonTerminals = nonTerms,
                terminals = terms
            )
        } else {
            GrammarParser.parseGrammarText(state.startSymbol, state.rulesText)
        }

        // Validate Grammar
        val grammarVal = GrammarValidator.validateGrammar(cfg)
        if (grammarVal is ValidationResult.Invalid) {
            _uiState.update {
                it.copy(
                    cfg = cfg,
                    currentSolverStep = SolverStep.VALIDATE,
                    parsingOutcome = ParsingOutcome(
                        isValidGrammar = false,
                        isAccepted = false,
                        targetString = state.targetString,
                        validationMessage = grammarVal.message + (grammarVal.suggestion?.let { s -> " Suggestion: $s" } ?: "")
                    )
                )
            }
            return
        }

        // Validate Target String Alphabet
        val stringVal = GrammarValidator.validateTargetString(cfg, state.targetString)
        if (stringVal is ValidationResult.Invalid) {
            _uiState.update {
                it.copy(
                    cfg = cfg,
                    currentSolverStep = SolverStep.VALIDATE,
                    parsingOutcome = ParsingOutcome(
                        isValidGrammar = true,
                        isAccepted = false,
                        targetString = state.targetString,
                        validationMessage = stringVal.message + (stringVal.suggestion?.let { s -> " Suggestion: $s" } ?: "")
                    )
                )
            }
            return
        }

        // Run Earley Parser
        val (isAccepted, parseTrees) = EarleyParser.parse(cfg, state.targetString)

        if (!isAccepted || parseTrees.isEmpty()) {
            _uiState.update {
                it.copy(
                    cfg = cfg,
                    currentSolverStep = SolverStep.VALIDATE,
                    parsingOutcome = ParsingOutcome(
                        isValidGrammar = true,
                        isAccepted = false,
                        targetString = state.targetString,
                        validationMessage = "No valid derivation from start symbol '${cfg.startSymbol}' produces this string."
                    )
                )
            }
            return
        }

        // Pre-generate LMD and RMD for all extracted parse trees
        val lmdMap = mutableMapOf<Int, List<DerivationStep>>()
        val rmdMap = mutableMapOf<Int, List<DerivationStep>>()
        for (i in parseTrees.indices) {
            lmdMap[i] = DerivationEngine.generateLeftmostDerivation(parseTrees[i])
            rmdMap[i] = DerivationEngine.generateRightmostDerivation(parseTrees[i])
        }

        _uiState.update {
            it.copy(
                cfg = cfg,
                currentSolverStep = SolverStep.VALIDATE,
                selectedTreeIndex = 0,
                currentLmdStepIndex = 0,
                currentRmdStepIndex = 0,
                parsingOutcome = ParsingOutcome(
                    isValidGrammar = true,
                    isAccepted = true,
                    targetString = state.targetString,
                    parseTrees = parseTrees,
                    lmdStepsMap = lmdMap,
                    rmdStepsMap = rmdMap,
                    isAmbiguous = parseTrees.size > 1
                )
            )
        }
    }

    fun selectParseTree(index: Int) {
        _uiState.update {
            it.copy(
                selectedTreeIndex = index,
                currentLmdStepIndex = 0,
                currentRmdStepIndex = 0
            )
        }
    }

    fun setSolverStep(step: SolverStep) {
        stopAutoPlay()
        _uiState.update { it.copy(currentSolverStep = step) }
    }

    // Step Navigation
    fun nextLmdStep() {
        val total = getCurrentLmdSteps().size
        _uiState.update {
            it.copy(currentLmdStepIndex = (it.currentLmdStepIndex + 1).coerceAtMost(total - 1))
        }
    }

    fun prevLmdStep() {
        _uiState.update {
            it.copy(currentLmdStepIndex = (it.currentLmdStepIndex - 1).coerceAtLeast(0))
        }
    }

    fun seekLmdStep(index: Int) {
        val total = getCurrentLmdSteps().size
        _uiState.update {
            it.copy(currentLmdStepIndex = index.coerceIn(0, (total - 1).coerceAtLeast(0)))
        }
    }

    fun nextRmdStep() {
        val total = getCurrentRmdSteps().size
        _uiState.update {
            it.copy(currentRmdStepIndex = (it.currentRmdStepIndex + 1).coerceAtMost(total - 1))
        }
    }

    fun prevRmdStep() {
        _uiState.update {
            it.copy(currentRmdStepIndex = (it.currentRmdStepIndex - 1).coerceAtLeast(0))
        }
    }

    fun seekRmdStep(index: Int) {
        val total = getCurrentRmdSteps().size
        _uiState.update {
            it.copy(currentRmdStepIndex = index.coerceIn(0, (total - 1).coerceAtLeast(0)))
        }
    }

    // Auto-Play
    fun toggleAutoPlay(isLmd: Boolean) {
        if (_uiState.value.isAutoPlaying) {
            stopAutoPlay()
        } else {
            startAutoPlay(isLmd)
        }
    }

    private fun startAutoPlay(isLmd: Boolean) {
        stopAutoPlay()
        _uiState.update { it.copy(isAutoPlaying = true) }
        autoPlayJob = viewModelScope.launch {
            while (isActive) {
                val delayMs = (1600L / _uiState.value.playSpeed).toLong()
                delay(delayMs)
                if (isLmd) {
                    val current = _uiState.value.currentLmdStepIndex
                    val max = getCurrentLmdSteps().size - 1
                    if (current < max) {
                        _uiState.update { it.copy(currentLmdStepIndex = current + 1) }
                    } else {
                        stopAutoPlay()
                        break
                    }
                } else {
                    val current = _uiState.value.currentRmdStepIndex
                    val max = getCurrentRmdSteps().size - 1
                    if (current < max) {
                        _uiState.update { it.copy(currentRmdStepIndex = current + 1) }
                    } else {
                        stopAutoPlay()
                        break
                    }
                }
            }
        }
    }

    fun stopAutoPlay() {
        autoPlayJob?.cancel()
        autoPlayJob = null
        _uiState.update { it.copy(isAutoPlaying = false) }
    }

    fun restartAutoPlay(isLmd: Boolean) {
        stopAutoPlay()
        if (isLmd) {
            _uiState.update { it.copy(currentLmdStepIndex = 0) }
        } else {
            _uiState.update { it.copy(currentRmdStepIndex = 0) }
        }
        startAutoPlay(isLmd)
    }

    fun setPlaySpeed(speed: Float) {
        _uiState.update { it.copy(playSpeed = speed) }
    }

    // Why? Explanation
    fun showWhyExplanation(explanation: String) {
        _uiState.update {
            it.copy(
                showWhyBottomSheet = true,
                activeWhyExplanation = explanation
            )
        }
    }

    fun dismissWhyBottomSheet() {
        _uiState.update { it.copy(showWhyBottomSheet = false) }
    }

    // OCR / Image scan
    fun openImageScannerDialog() {
        _uiState.update { it.copy(showImageScannerDialog = true, ocrErrorMessage = null) }
    }

    fun dismissImageScannerDialog() {
        _uiState.update { it.copy(showImageScannerDialog = false, ocrErrorMessage = null) }
    }

    fun processScannedGrammarText(scannedText: String) {
        // Parse detected grammar text
        val lines = scannedText.lines().map { it.trim() }.filter { it.isNotEmpty() }
        var detectedStart = "S"
        val ruleLines = mutableListOf<String>()
        var detectedTarget = ""

        for (line in lines) {
            if (line.startsWith("target:", ignoreCase = true) || line.startsWith("string:", ignoreCase = true)) {
                detectedTarget = line.substringAfter(":").trim()
            } else if (line.contains("->") || line.contains("→") || line.contains("::=")) {
                ruleLines.add(line)
                if (ruleLines.size == 1) {
                    detectedStart = line.substringBefore("->").substringBefore("→").substringBefore("::=").trim()
                }
            }
        }

        if (ruleLines.isEmpty()) {
            _uiState.update { it.copy(ocrErrorMessage = "CFG Not Detected in scanned image. Please check image clarity.") }
            return
        }

        val rulesJoined = ruleLines.joinToString("\n")
        val parsedCfg = GrammarParser.parseGrammarText(detectedStart, rulesJoined)

        _uiState.update {
            it.copy(
                startSymbol = detectedStart,
                rulesText = rulesJoined,
                structuredRules = parsedCfg.productions,
                targetString = detectedTarget.ifEmpty { it.targetString },
                showImageScannerDialog = false,
                ocrErrorMessage = null
            )
        }
    }

    // Preloaded Example Loader
    fun loadExample(example: GrammarExample) {
        stopAutoPlay()
        val parsedCfg = GrammarParser.parseGrammarText(example.startSymbol, example.rulesText)
        _uiState.update {
            it.copy(
                startSymbol = example.startSymbol,
                rulesText = example.rulesText,
                structuredRules = parsedCfg.productions,
                targetString = example.targetString,
                currentSolverStep = SolverStep.INPUT,
                parsingOutcome = null,
                selectedTreeIndex = 0,
                currentLmdStepIndex = 0,
                currentRmdStepIndex = 0
            )
        }
    }

    // Reset All State
    fun resetAll() {
        stopAutoPlay()
        _uiState.update {
            GrammarUiState(
                currentSolverStep = SolverStep.INPUT,
                inputMode = InputMode.STRUCTURED,
                startSymbol = "S",
                rulesText = "",
                structuredRules = emptyList(),
                targetString = "",
                cfg = null,
                parsingOutcome = null,
                selectedTreeIndex = 0,
                currentLmdStepIndex = 0,
                currentRmdStepIndex = 0
            )
        }
    }

    fun getCurrentLmdSteps(): List<DerivationStep> {
        val outcome = _uiState.value.parsingOutcome ?: return emptyList()
        return outcome.lmdStepsMap[_uiState.value.selectedTreeIndex] ?: emptyList()
    }

    fun getCurrentRmdSteps(): List<DerivationStep> {
        val outcome = _uiState.value.parsingOutcome ?: return emptyList()
        return outcome.rmdStepsMap[_uiState.value.selectedTreeIndex] ?: emptyList()
    }

    fun getSelectedParseTree(): ParseTreeNode? {
        val outcome = _uiState.value.parsingOutcome ?: return null
        val trees = outcome.parseTrees
        if (trees.isEmpty()) return null
        val idx = _uiState.value.selectedTreeIndex.coerceIn(0, trees.size - 1)
        return trees[idx]
    }
}
