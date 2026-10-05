package com.example

import com.example.parser.DerivationEngine
import com.example.parser.EarleyParser
import com.example.parser.GrammarParser
import com.example.parser.GrammarValidator
import com.example.parser.ValidationResult
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Test

class GrammarEngineTest {

    @Test
    fun testGrammarParserAndValidator() {
        val grammarText = """
            S -> AB
            A -> aA | a
            B -> bB | b
        """.trimIndent()

        val cfg = GrammarParser.parseGrammarText("S", grammarText)
        val valResult = GrammarValidator.validateGrammar(cfg)

        assertTrue(valResult is ValidationResult.Valid)
        assertEquals("S", cfg.startSymbol)
        assertEquals(5, cfg.productions.size)
        assertTrue(cfg.nonTerminals.contains("S"))
        assertTrue(cfg.nonTerminals.contains("A"))
        assertTrue(cfg.nonTerminals.contains("B"))
        assertTrue(cfg.terminals.contains("a"))
        assertTrue(cfg.terminals.contains("b"))
    }

    @Test
    fun testEarleyParserAcceptAndReject() {
        val grammarText = """
            S -> AB
            A -> aA | a
            B -> bB | b
        """.trimIndent()

        val cfg = GrammarParser.parseGrammarText("S", grammarText)

        // Valid strings
        val (accepted1, trees1) = EarleyParser.parse(cfg, "aabb")
        assertTrue(accepted1)
        assertTrue(trees1.isNotEmpty())
        assertEquals("aabb", trees1[0].getYield())

        val (accepted2, trees2) = EarleyParser.parse(cfg, "ab")
        assertTrue(accepted2)
        assertTrue(trees2.isNotEmpty())
        assertEquals("ab", trees2[0].getYield())

        // Invalid strings
        val (acceptedInvalid1, _) = EarleyParser.parse(cfg, "ba")
        assertFalse(acceptedInvalid1)

        val (acceptedInvalid2, _) = EarleyParser.parse(cfg, "aab")
        // "aab" has 2 a's and 1 b. Wait: in S -> AB, A -> aA | a, B -> bB | b,
        // A generates a^n (n>=1), B generates b^m (m>=1).
        // So aab is 2 a's and 1 b: A -> aA -> aa, B -> b. So aab is in L(G).
        assertTrue(acceptedInvalid2)

        // String with unknown symbol or empty
        val (acceptedEmpty, _) = EarleyParser.parse(cfg, "")
        assertFalse(acceptedEmpty)
    }

    @Test
    fun testDerivationEngineLmdAndRmd() {
        val grammarText = """
            S -> AB
            A -> aA | a
            B -> bB | b
        """.trimIndent()

        val cfg = GrammarParser.parseGrammarText("S", grammarText)
        val (accepted, trees) = EarleyParser.parse(cfg, "aabb")
        assertTrue(accepted)
        assertTrue(trees.isNotEmpty())

        val tree = trees[0]
        val lmdSteps = DerivationEngine.generateLeftmostDerivation(tree)
        val rmdSteps = DerivationEngine.generateRightmostDerivation(tree)

        // Initial sentential form must be S
        assertEquals(listOf("S"), lmdSteps[0].sententialForm)
        assertEquals(listOf("S"), rmdSteps[0].sententialForm)

        // Final result must produce target string "aabb"
        val lmdFinal = lmdSteps.last().resultSententialForm.joinToString("")
        val rmdFinal = rmdSteps.last().resultSententialForm.joinToString("")
        assertEquals("aabb", lmdFinal)
        assertEquals("aabb", rmdFinal)

        // Verify Leftmost rule: each step replaces the FIRST non-terminal
        for (step in lmdSteps) {
            val nonTermIndex = step.sententialForm.indexOfFirst { it.firstOrNull()?.isUpperCase() == true }
            assertEquals(nonTermIndex, step.selectedIndex)
        }

        // Verify Rightmost rule: each step replaces the LAST non-terminal
        for (step in rmdSteps) {
            val nonTermIndex = step.sententialForm.indexOfLast { it.firstOrNull()?.isUpperCase() == true }
            assertEquals(nonTermIndex, step.selectedIndex)
        }
    }

    @Test
    fun testAmbiguityDetection() {
        val ambiguousGrammar = """
            E -> E + E | a
        """.trimIndent()

        val cfg = GrammarParser.parseGrammarText("E", ambiguousGrammar)
        val (accepted, trees) = EarleyParser.parse(cfg, "a+a+a")
        assertTrue(accepted)
        // Expression grammar with no associativity on "a+a+a" must produce 2 parse trees: (a+a)+a and a+(a+a)
        assertTrue(trees.size >= 2)
    }
}
