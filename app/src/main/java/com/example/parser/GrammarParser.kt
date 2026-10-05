package com.example.parser

import com.example.model.CFG
import com.example.model.EPSILON
import com.example.model.GrammarSymbol
import com.example.model.Production

object GrammarParser {

    /**
     * Parses textual grammar input into a CFG object.
     * Supports arrows: "->", "→", "::=", ":"
     * Supports alternatives: "|"
     * Supports epsilons: "ε", "epsilon", "eps", "λ"
     */
    fun parseGrammarText(startSymbolInput: String, rulesText: String): CFG {
        val lines = rulesText.lines()
            .map { it.trim() }
            .filter { it.isNotEmpty() && !it.startsWith("#") && !it.startsWith("//") }

        val rawRules = mutableListOf<Pair<String, String>>()
        val discoveredLhs = mutableSetOf<String>()

        val arrowRegex = Regex("->|→|::=|:")

        for (line in lines) {
            val arrowMatch = arrowRegex.find(line) ?: continue
            val lhs = line.substring(0, arrowMatch.range.first).trim()
            val rhsPart = line.substring(arrowMatch.range.last + 1).trim()
            if (lhs.isNotEmpty()) {
                discoveredLhs.add(lhs)
                rawRules.add(lhs to rhsPart)
            }
        }

        val startSymbol = startSymbolInput.trim().ifEmpty {
            discoveredLhs.firstOrNull() ?: "S"
        }

        // Collect all non-terminals (all LHS symbols plus start symbol)
        val nonTerminals = discoveredLhs.toMutableSet()
        if (startSymbol.isNotEmpty()) {
            nonTerminals.add(startSymbol)
        }

        val productions = mutableListOf<Production>()
        var prodIdCounter = 1

        for ((lhs, rhsPart) in rawRules) {
            val alternatives = rhsPart.split("|").map { it.trim() }
            for (alt in alternatives) {
                val tokens = tokenizeRhs(alt, nonTerminals)
                val isEps = tokens.isEmpty() || (tokens.size == 1 && GrammarSymbol.isEpsilonSymbol(tokens[0]))
                val finalTokens = if (isEps) listOf(EPSILON) else tokens
                productions.add(
                    Production(
                        id = prodIdCounter++,
                        lhs = lhs,
                        rhs = finalTokens
                    )
                )
            }
        }

        // Determine terminals: any symbol appearing in RHS that is not in nonTerminals and not epsilon
        val terminals = mutableSetOf<String>()
        for (prod in productions) {
            for (token in prod.rhs) {
                if (!nonTerminals.contains(token) && !GrammarSymbol.isEpsilonSymbol(token)) {
                    terminals.add(token)
                }
            }
        }

        return CFG(
            startSymbol = startSymbol,
            productions = productions,
            nonTerminals = nonTerminals,
            terminals = terminals
        )
    }

    /**
     * Splits an RHS alternative into individual symbol tokens.
     * Respects space-separation if present, otherwise splits smartly using known non-terminals.
     */
    fun tokenizeRhs(rhs: String, knownNonTerminals: Set<String>): List<String> {
        val trimmed = rhs.trim()
        if (trimmed.isEmpty() || GrammarSymbol.isEpsilonSymbol(trimmed)) {
            return listOf(EPSILON)
        }

        // If user used spaces, split by whitespace
        if (trimmed.contains(" ") || trimmed.contains("\t")) {
            val parts = trimmed.split(Regex("\\s+")).filter { it.isNotEmpty() }
            return parts.map { part ->
                if (GrammarSymbol.isEpsilonSymbol(part)) EPSILON else part
            }
        }

        // If no spaces, parse character by character or known multi-char non-terminals
        val result = mutableListOf<String>()
        var i = 0
        while (i < trimmed.length) {
            // Check if a known non-terminal starts at i (longest match first)
            var matchedNt: String? = null
            for (nt in knownNonTerminals.sortedByDescending { it.length }) {
                if (trimmed.startsWith(nt, i)) {
                    matchedNt = nt
                    break
                }
            }

            if (matchedNt != null) {
                result.add(matchedNt)
                i += matchedNt.length
            } else {
                // Single character terminal or token
                val ch = trimmed[i].toString()
                if (GrammarSymbol.isEpsilonSymbol(ch)) {
                    result.add(EPSILON)
                } else {
                    result.add(ch)
                }
                i++
            }
        }

        return result
    }

    /**
     * Converts a list of Productions back to a standard text representation.
     */
    fun productionsToText(productions: List<Production>): String {
        val grouped = productions.groupBy { it.lhs }
        return buildString {
            for ((lhs, prods) in grouped) {
                val alts = prods.joinToString(" | ") { it.displayRhs }
                appendLine("$lhs -> $alts")
            }
        }.trim()
    }
}
