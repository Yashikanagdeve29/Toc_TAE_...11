package com.example.model

const val EPSILON = "ε"

/**
 * Represents a single grammar symbol (terminal or non-terminal).
 */
data class GrammarSymbol(
    val value: String,
    val isTerminal: Boolean
) {
    val isEpsilon: Boolean
        get() = isEpsilonSymbol(value)

    companion object {
        fun isEpsilonSymbol(str: String): Boolean {
            val trimmed = str.trim()
            return trimmed == "ε" || trimmed.equals("epsilon", ignoreCase = true) || trimmed.equals("eps", ignoreCase = true) || trimmed == "λ"
        }
    }
}

/**
 * A Context-Free Grammar production rule: LHS -> RHS
 * e.g., A -> a A or A -> ε
 */
data class Production(
    val id: Int = 0,
    val lhs: String,
    val rhs: List<String>
) {
    val isEpsilon: Boolean
        get() = rhs.isEmpty() || (rhs.size == 1 && GrammarSymbol.isEpsilonSymbol(rhs[0]))

    val displayRhs: String
        get() = if (isEpsilon) EPSILON else rhs.joinToString(" ")

    val displayString: String
        get() = "$lhs → $displayRhs"
}

/**
 * Complete Context-Free Grammar definition.
 */
data class CFG(
    val startSymbol: String,
    val productions: List<Production>,
    val nonTerminals: Set<String>,
    val terminals: Set<String>
) {
    fun getProductionsFor(nonTerminal: String): List<Production> {
        return productions.filter { it.lhs == nonTerminal }
    }
}

/**
 * Node in the concrete syntax/parse tree.
 */
data class ParseTreeNode(
    val id: Int,
    val symbol: String,
    val isTerminal: Boolean,
    val children: List<ParseTreeNode> = emptyList(),
    val productionUsed: Production? = null,
    val spanStart: Int = 0,
    val spanEnd: Int = 0
) {
    val isEpsilon: Boolean
        get() = GrammarSymbol.isEpsilonSymbol(symbol)

    /**
     * Reconstruct the terminal yield of this parse tree node.
     */
    fun getYield(): String {
        if (isTerminal) {
            return if (isEpsilon) "" else symbol
        }
        return children.joinToString("") { it.getYield() }
    }
}

/**
 * Represents a single step in a derivation (Leftmost or Rightmost).
 */
data class DerivationStep(
    val stepNumber: Int,
    val sententialForm: List<String>,
    val selectedIndex: Int,
    val selectedSymbol: String,
    val productionUsed: Production,
    val resultSententialForm: List<String>,
    val explanation: String
) {
    val sententialFormDisplay: String
        get() = if (sententialForm.isEmpty()) EPSILON else sententialForm.joinToString(" ")

    val resultSententialFormDisplay: String
        get() = if (resultSententialForm.isEmpty()) EPSILON else resultSententialForm.joinToString(" ")
}

/**
 * Represents the complete result of parsing and deriving a string.
 */
data class ParsingOutcome(
    val isValidGrammar: Boolean,
    val isAccepted: Boolean,
    val targetString: String,
    val parseTrees: List<ParseTreeNode> = emptyList(),
    val lmdStepsMap: Map<Int, List<DerivationStep>> = emptyMap(), // treeIndex -> LMD steps
    val rmdStepsMap: Map<Int, List<DerivationStep>> = emptyMap(), // treeIndex -> RMD steps
    val validationMessage: String = "",
    val isAmbiguous: Boolean = false
)
