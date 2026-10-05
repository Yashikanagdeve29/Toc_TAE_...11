package com.example.parser

import com.example.model.CFG
import com.example.model.EPSILON

sealed class ValidationResult {
    data object Valid : ValidationResult()
    data class Invalid(val message: String, val suggestion: String? = null) : ValidationResult()
}

object GrammarValidator {

    /**
     * Validates that the CFG structure is well-formed.
     */
    fun validateGrammar(cfg: CFG): ValidationResult {
        if (cfg.startSymbol.isBlank()) {
            return ValidationResult.Invalid(
                message = "Start symbol is missing or blank.",
                suggestion = "Specify a start symbol, such as 'S'."
            )
        }

        if (cfg.productions.isEmpty()) {
            return ValidationResult.Invalid(
                message = "Grammar contains no production rules.",
                suggestion = "Add at least one rule like 'S -> a S | a'."
            )
        }

        val startRules = cfg.getProductionsFor(cfg.startSymbol)
        if (startRules.isEmpty()) {
            return ValidationResult.Invalid(
                message = "Start symbol '${cfg.startSymbol}' has no production rules.",
                suggestion = "Define at least one production starting with '${cfg.startSymbol} -> ...'."
            )
        }

        for (prod in cfg.productions) {
            if (prod.lhs.isBlank()) {
                return ValidationResult.Invalid(
                    message = "A production rule has an empty Left-Hand Side (LHS).",
                    suggestion = "Ensure every rule specifies a non-terminal on the left."
                )
            }
        }

        return ValidationResult.Valid
    }

    /**
     * Validates that the target string consists solely of recognized terminal symbols.
     * Also checks for epsilon/empty string acceptance eligibility.
     */
    fun validateTargetString(cfg: CFG, targetString: String): ValidationResult {
        val trimmed = targetString.trim()

        // Epsilon / empty string
        if (trimmed.isEmpty() || trimmed == EPSILON || trimmed.equals("epsilon", ignoreCase = true)) {
            return ValidationResult.Valid
        }

        // If grammar has no terminals yet target string is non-empty
        if (cfg.terminals.isEmpty() && trimmed.isNotEmpty()) {
            return ValidationResult.Invalid(
                message = "Grammar has no terminal symbols, but target string '$trimmed' is not empty.",
                suggestion = "Add productions producing terminal characters like 'a' or 'b'."
            )
        }

        // Check each character or token in target string against grammar terminals
        for (char in trimmed) {
            val charStr = char.toString()
            if (!cfg.terminals.contains(charStr)) {
                val availableTerminals = if (cfg.terminals.isEmpty()) "{none}" else cfg.terminals.sorted().joinToString(", ", "{", "}")
                return ValidationResult.Invalid(
                    message = "Symbol '$charStr' is not part of this grammar.",
                    suggestion = "Only use grammar terminals: $availableTerminals."
                )
            }
        }

        return ValidationResult.Valid
    }
}
