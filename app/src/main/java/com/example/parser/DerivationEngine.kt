package com.example.parser

import com.example.model.DerivationStep
import com.example.model.EPSILON
import com.example.model.GrammarSymbol
import com.example.model.ParseTreeNode
import com.example.model.Production

object DerivationEngine {

    /**
     * Generates Leftmost Derivation (LMD) steps from the given parse tree.
     */
    fun generateLeftmostDerivation(parseTree: ParseTreeNode): List<DerivationStep> {
        val steps = mutableListOf<DerivationStep>()

        // Maintain a list of current active tree nodes in the sentential form
        var currentNodes = listOf(parseTree)
        var stepCount = 1

        while (true) {
            // Find the LEFTMOST non-terminal node in current sentential form
            val leftmostIndex = currentNodes.indexOfFirst { !it.isTerminal }
            if (leftmostIndex == -1) {
                // All nodes are terminals (or epsilon), derivation complete!
                break
            }

            val targetNode = currentNodes[leftmostIndex]
            val prod = targetNode.productionUsed ?: Production(
                lhs = targetNode.symbol,
                rhs = targetNode.children.map { it.symbol }
            )

            val beforeSymbols = currentNodes.map { it.symbol }

            // Replace targetNode with its children in the list
            val newNodes = mutableListOf<ParseTreeNode>()
            for (i in currentNodes.indices) {
                if (i == leftmostIndex) {
                    newNodes.addAll(targetNode.children)
                } else {
                    newNodes.add(currentNodes[i])
                }
            }

            val afterSymbols = newNodes.map { it.symbol }

            val childSymbolsStr = if (targetNode.children.isEmpty() || (targetNode.children.size == 1 && targetNode.children[0].isEpsilon)) {
                EPSILON
            } else {
                targetNode.children.joinToString(" ") { it.symbol }
            }

            val explanation = buildString {
                append("In Leftmost Derivation, the leftmost non-terminal '${targetNode.symbol}' ")
                if (beforeSymbols.size > 1) {
                    append("at position ${leftmostIndex + 1} of [${beforeSymbols.joinToString(" ")}] ")
                }
                append("is expanded first. ")
                append("Rule applied: ${prod.displayString}. ")
                append("'${targetNode.symbol}' is replaced with '$childSymbolsStr'.")
            }

            steps.add(
                DerivationStep(
                    stepNumber = stepCount++,
                    sententialForm = beforeSymbols,
                    selectedIndex = leftmostIndex,
                    selectedSymbol = targetNode.symbol,
                    productionUsed = prod,
                    resultSententialForm = afterSymbols,
                    explanation = explanation
                )
            )

            currentNodes = newNodes
        }

        return steps
    }

    /**
     * Generates Rightmost Derivation (RMD) steps from the given parse tree.
     */
    fun generateRightmostDerivation(parseTree: ParseTreeNode): List<DerivationStep> {
        val steps = mutableListOf<DerivationStep>()

        var currentNodes = listOf(parseTree)
        var stepCount = 1

        while (true) {
            // Find the RIGHTMOST non-terminal node in current sentential form
            val rightmostIndex = currentNodes.indexOfLast { !it.isTerminal }
            if (rightmostIndex == -1) {
                // All nodes are terminals, derivation complete!
                break
            }

            val targetNode = currentNodes[rightmostIndex]
            val prod = targetNode.productionUsed ?: Production(
                lhs = targetNode.symbol,
                rhs = targetNode.children.map { it.symbol }
            )

            val beforeSymbols = currentNodes.map { it.symbol }

            // Replace targetNode with its children in the list
            val newNodes = mutableListOf<ParseTreeNode>()
            for (i in currentNodes.indices) {
                if (i == rightmostIndex) {
                    newNodes.addAll(targetNode.children)
                } else {
                    newNodes.add(currentNodes[i])
                }
            }

            val afterSymbols = newNodes.map { it.symbol }

            val childSymbolsStr = if (targetNode.children.isEmpty() || (targetNode.children.size == 1 && targetNode.children[0].isEpsilon)) {
                EPSILON
            } else {
                targetNode.children.joinToString(" ") { it.symbol }
            }

            val explanation = buildString {
                append("In Rightmost Derivation, the rightmost non-terminal '${targetNode.symbol}' ")
                if (beforeSymbols.size > 1) {
                    append("at position ${rightmostIndex + 1} of [${beforeSymbols.joinToString(" ")}] ")
                }
                append("is expanded first. ")
                append("Rule applied: ${prod.displayString}. ")
                append("'${targetNode.symbol}' is replaced with '$childSymbolsStr'. Non-terminals to the left wait.")
            }

            steps.add(
                DerivationStep(
                    stepNumber = stepCount++,
                    sententialForm = beforeSymbols,
                    selectedIndex = rightmostIndex,
                    selectedSymbol = targetNode.symbol,
                    productionUsed = prod,
                    resultSententialForm = afterSymbols,
                    explanation = explanation
                )
            )

            currentNodes = newNodes
        }

        return steps
    }

    /**
     * Helper to clean up final sentential form for display (removing epsilons when displaying final string)
     */
    fun formatFinalString(symbols: List<String>): String {
        return symbols.filter { !GrammarSymbol.isEpsilonSymbol(it) }.joinToString("")
    }
}
