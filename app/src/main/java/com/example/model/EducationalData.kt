package com.example.model

/**
 * Built-in grammar example with preset rules and string.
 */
data class GrammarExample(
    val id: String,
    val title: String,
    val category: String,
    val description: String,
    val startSymbol: String,
    val rulesText: String,
    val targetString: String
)

/**
 * Educational flashcard for the Learn screen.
 */
data class LearnCard(
    val id: Int,
    val title: String,
    val subtitle: String,
    val summary: String,
    val formula: String? = null,
    val keyPoints: List<String>,
    val badge: String
)

object EducationalData {

    val EXAMPLES = listOf(
        GrammarExample(
            id = "basic",
            title = "Basic Concatenation",
            category = "Fundamental",
            description = "Simple two-symbol grammar demonstrating standard terminal generation.",
            startSymbol = "S",
            rulesText = """
                S -> AB
                A -> a
                B -> b
            """.trimIndent(),
            targetString = "ab"
        ),
        GrammarExample(
            id = "recursive",
            title = "Recursive Language aⁿbⁿ",
            category = "Recursive",
            description = "Classic context-free grammar generating matching counts of a's and b's.",
            startSymbol = "S",
            rulesText = """
                S -> AB
                A -> aA | a
                B -> bB | b
            """.trimIndent(),
            targetString = "aabb"
        ),
        GrammarExample(
            id = "epsilon",
            title = "Epsilon / Nullable Grammar",
            category = "Nullable",
            description = "Grammar containing an empty transition ε showing terminal evaporation.",
            startSymbol = "S",
            rulesText = """
                S -> aS | ε
            """.trimIndent(),
            targetString = "aaa"
        ),
        GrammarExample(
            id = "ambiguous",
            title = "Ambiguous Expression Grammar",
            category = "Ambiguity",
            description = "Produces multiple distinct parse trees for the same input string due to lack of operator precedence.",
            startSymbol = "E",
            rulesText = """
                E -> E + E | a
            """.trimIndent(),
            targetString = "a+a+a"
        ),
        GrammarExample(
            id = "palindrome",
            title = "Even Palindromes",
            category = "Symmetry",
            description = "Symmetric grammar generating strings of the form w w^R.",
            startSymbol = "S",
            rulesText = """
                S -> aSa | bSb | ε
            """.trimIndent(),
            targetString = "abba"
        )
    )

    val LEARN_CARDS = listOf(
        LearnCard(
            id = 1,
            title = "What is a CFG?",
            subtitle = "Context-Free Grammar 4-Tuple",
            summary = "A formal grammar defined by G = (V, Σ, R, S) where productions replace a single non-terminal variable regardless of context.",
            formula = "G = (V, Σ, R, S)",
            keyPoints = listOf(
                "V: Finite set of Non-Terminal variables (e.g., S, A, B)",
                "Σ: Finite set of Terminal symbols disjoint from V (e.g., a, b, +)",
                "R: Production rules of form A → α where A ∈ V and α ∈ (V ∪ Σ)*",
                "S: Distinct Start Symbol (S ∈ V)"
            ),
            badge = "Definition"
        ),
        LearnCard(
            id = 2,
            title = "What is a Derivation?",
            subtitle = "Stepwise Replacement Process",
            summary = "A sequence of production rule applications starting from the initial symbol S and terminating at the final string of terminals.",
            formula = "S ⇒ α₁ ⇒ α₂ ⇒ ... ⇒ w",
            keyPoints = listOf(
                "Each step replaces one non-terminal with the right-hand side of a rule",
                "The double arrow '⇒' denotes a single derivation step",
                "The reflexive transitive closure '⇒*' denotes zero or more steps"
            ),
            badge = "Process"
        ),
        LearnCard(
            id = 3,
            title = "What is Sentential Form?",
            subtitle = "Intermediate Derivation Strings",
            summary = "Any string α containing a mix of terminals and non-terminals derivable from the start symbol S.",
            formula = "S ⇒* α,  where α ∈ (V ∪ Σ)*",
            keyPoints = listOf(
                "Sentence: A sentential form containing only terminal symbols (α ∈ Σ*)",
                "Left-sentential form: Appears during leftmost derivation",
                "Right-sentential form: Appears during rightmost derivation"
            ),
            badge = "Structure"
        ),
        LearnCard(
            id = 4,
            title = "What is LMD?",
            subtitle = "Leftmost Derivation",
            summary = "At every step of the derivation, the leftmost non-terminal variable is strictly chosen and expanded before any other non-terminals.",
            formula = "uAv ⇒ uwv  where u ∈ Σ* (only terminals to left)",
            keyPoints = listOf(
                "Always scan left-to-right and pick the first non-terminal",
                "Crucial in LL(k) top-down compiler parsing",
                "All non-terminals to the right must wait until the left is fully resolved"
            ),
            badge = "Leftmost"
        ),
        LearnCard(
            id = 5,
            title = "What is RMD?",
            subtitle = "Rightmost Derivation (Canonical)",
            summary = "At every step of the derivation, the rightmost non-terminal variable is strictly chosen and expanded first.",
            formula = "uAv ⇒ uwv  where v ∈ Σ* (only terminals to right)",
            keyPoints = listOf(
                "Always scan right-to-left and pick the first non-terminal",
                "Crucial in LR(k) bottom-up shift-reduce compiler parsing",
                "The reverse of a rightmost derivation corresponds to a bottom-up reduction"
            ),
            badge = "Rightmost"
        ),
        LearnCard(
            id = 6,
            title = "What is a Parse Tree?",
            subtitle = "Concrete Syntax Tree (CST)",
            summary = "A hierarchical tree representation of the syntactic structure of a string according to a formal grammar.",
            formula = "Root = S, Leaves = Yield (w), Internal Nodes = Non-terminals",
            keyPoints = listOf(
                "Root node is always the Start Symbol S",
                "Leaf nodes read left-to-right produce the yield (target string)",
                "A single parse tree uniquely defines both its LMD and RMD"
            ),
            badge = "Tree"
        ),
        LearnCard(
            id = 7,
            title = "What is Ambiguity?",
            subtitle = "Multiple Parse Trees",
            summary = "A grammar is ambiguous if there exists at least one string in its language that possesses two or more distinct parse trees (or distinct LMDs).",
            formula = "∃ w ∈ L(G) : Count(ParseTrees(w)) > 1",
            keyPoints = listOf(
                "Causes ambiguity in compiler semantics (e.g. dangling else, arithmetic precedence)",
                "GrammarFlow detects when a string produces multiple trees",
                "Inherent ambiguity: some languages have no unambiguous CFG"
            ),
            badge = "Ambiguity"
        )
    )
}
