package com.example.parser

import com.example.model.CFG
import com.example.model.EPSILON
import com.example.model.GrammarSymbol
import com.example.model.ParseTreeNode
import com.example.model.Production

/**
 * An Earley State (Item):
 * Represents a rule A -> alpha . beta with origin chart index 'origin'.
 */
data class EarleyItem(
    val production: Production,
    val dot: Int,
    val origin: Int
) {
    val isEpsilonRule: Boolean
        get() = production.isEpsilon

    val isComplete: Boolean
        get() = isEpsilonRule || dot >= production.rhs.size

    val nextSymbol: String?
        get() = if (isComplete) null else production.rhs[dot]

    override fun toString(): String {
        val rhsWithDot = if (isEpsilonRule) {
            "• ε"
        } else {
            val list = production.rhs.toMutableList()
            list.add(dot, "•")
            list.joinToString(" ")
        }
        return "${production.lhs} -> $rhsWithDot ($origin)"
    }
}

object EarleyParser {

    /**
     * Parses the target string using the CFG and Earley Algorithm.
     * Returns a pair: (isAccepted: Boolean, parseTrees: List<ParseTreeNode>)
     */
    fun parse(cfg: CFG, targetString: String): Pair<Boolean, List<ParseTreeNode>> {
        val inputTokens = tokenizeTargetString(targetString)
        val n = inputTokens.size

        // Augmented start production: S' -> S
        val augmentedStartSymbol = "${cfg.startSymbol}'"
        val augmentedProd = Production(
            id = 0,
            lhs = augmentedStartSymbol,
            rhs = listOf(cfg.startSymbol)
        )

        // Charts: S_0, S_1, ..., S_n
        val charts = Array(n + 1) { mutableListOf<EarleyItem>() }
        val chartSets = Array(n + 1) { mutableSetOf<EarleyItem>() }

        fun addItem(chartIndex: Int, item: EarleyItem): Boolean {
            if (chartSets[chartIndex].add(item)) {
                charts[chartIndex].add(item)
                return true
            }
            return false
        }

        // Initialize S_0 with augmented start rule
        addItem(0, EarleyItem(augmentedProd, 0, 0))

        for (k in 0..n) {
            var i = 0
            while (i < charts[k].size) {
                val item = charts[k][i]

                if (!item.isComplete) {
                    val next = item.nextSymbol!!
                    if (cfg.nonTerminals.contains(next)) {
                        // PREDICTION:
                        // For each rule B -> gamma, add (B -> . gamma, k)
                        for (prod in cfg.getProductionsFor(next)) {
                            val dot = if (prod.isEpsilon) 0 else 0
                            addItem(k, EarleyItem(prod, dot, k))
                        }
                    } else if (k < n && next == inputTokens[k]) {
                        // SCANNING: (for terminal matching next input token)
                        addItem(k + 1, EarleyItem(item.production, item.dot + 1, item.origin))
                    }
                } else {
                    // COMPLETION:
                    // For completed rule B -> gamma . with origin j, advance items in chart j waiting on B
                    val completedSymbol = item.production.lhs
                    val origin = item.origin
                    for (origItem in charts[origin]) {
                        if (!origItem.isComplete && origItem.nextSymbol == completedSymbol) {
                            addItem(
                                k,
                                EarleyItem(
                                    production = origItem.production,
                                    dot = origItem.dot + 1,
                                    origin = origItem.origin
                                )
                            )
                        }
                    }
                }
                i++
            }
        }

        // Check acceptance: augmented start rule completed in chart n with origin 0
        val isAccepted = charts[n].any {
            it.production.lhs == augmentedStartSymbol && it.isComplete && it.origin == 0
        }

        if (!isAccepted) {
            return false to emptyList()
        }

        // Construct parse trees from the start symbol spanning 0..n
        var nodeIdCounter = 0
        val trees = buildParseTrees(
            cfg = cfg,
            inputTokens = inputTokens,
            symbol = cfg.startSymbol,
            start = 0,
            end = n,
            callStack = mutableSetOf(),
            getNextId = { ++nodeIdCounter }
        )

        // Filter and ensure yield matches exactly
        val expectedYield = inputTokens.joinToString("")
        val validTrees = trees.filter { tree ->
            tree.getYield() == expectedYield
        }.distinctBy { treeToStructuralString(it) }

        return true to validTrees
    }

    /**
     * Splits target string into terminal tokens.
     */
    fun tokenizeTargetString(target: String): List<String> {
        val trimmed = target.trim()
        if (trimmed.isEmpty() || GrammarSymbol.isEpsilonSymbol(trimmed)) {
            return emptyList()
        }
        return trimmed.map { it.toString() }
    }

    /**
     * Recursively extracts all valid parse trees for a given symbol spanning start..end.
     */
    private fun buildParseTrees(
        cfg: CFG,
        inputTokens: List<String>,
        symbol: String,
        start: Int,
        end: Int,
        callStack: MutableSet<Triple<String, Int, Int>>,
        getNextId: () -> Int,
        maxTrees: Int = 5
    ): List<ParseTreeNode> {
        val key = Triple(symbol, start, end)
        if (callStack.contains(key)) {
            // Cycle detected (e.g. A -> A)
            return emptyList()
        }

        // Terminal or Epsilon symbol check
        if (!cfg.nonTerminals.contains(symbol)) {
            if (GrammarSymbol.isEpsilonSymbol(symbol)) {
                if (start == end) {
                    return listOf(
                        ParseTreeNode(
                            id = getNextId(),
                            symbol = EPSILON,
                            isTerminal = true,
                            children = emptyList(),
                            spanStart = start,
                            spanEnd = end
                        )
                    )
                }
                return emptyList()
            }

            // Normal terminal: spans exactly 1 token
            if (end == start + 1 && start < inputTokens.size && inputTokens[start] == symbol) {
                return listOf(
                    ParseTreeNode(
                        id = getNextId(),
                        symbol = symbol,
                        isTerminal = true,
                        children = emptyList(),
                        spanStart = start,
                        spanEnd = end
                    )
                )
            }
            return emptyList()
        }

        callStack.add(key)
        val candidateTrees = mutableListOf<ParseTreeNode>()

        for (prod in cfg.getProductionsFor(symbol)) {
            if (candidateTrees.size >= maxTrees) break

            if (prod.isEpsilon) {
                if (start == end) {
                    val epsLeaf = ParseTreeNode(
                        id = getNextId(),
                        symbol = EPSILON,
                        isTerminal = true,
                        children = emptyList(),
                        spanStart = start,
                        spanEnd = end
                    )
                    candidateTrees.add(
                        ParseTreeNode(
                            id = getNextId(),
                            symbol = symbol,
                            isTerminal = false,
                            children = listOf(epsLeaf),
                            productionUsed = prod,
                            spanStart = start,
                            spanEnd = end
                        )
                    )
                }
                continue
            }

            // For non-epsilon RHS: find all partitions of start..end across prod.rhs symbols
            val partitions = findPartitions(prod.rhs, start, end, inputTokens, cfg)
            for (partition in partitions) {
                if (candidateTrees.size >= maxTrees) break

                val childTreeOptions = mutableListOf<List<ParseTreeNode>>()
                var allValid = true

                for (idx in prod.rhs.indices) {
                    val childSym = prod.rhs[idx]
                    val (cStart, cEnd) = partition[idx]
                    val childTrees = buildParseTrees(
                        cfg = cfg,
                        inputTokens = inputTokens,
                        symbol = childSym,
                        start = cStart,
                        end = cEnd,
                        callStack = callStack,
                        getNextId = getNextId,
                        maxTrees = maxTrees
                    )
                    if (childTrees.isEmpty()) {
                        allValid = false
                        break
                    }
                    childTreeOptions.add(childTrees)
                }

                if (allValid && childTreeOptions.size == prod.rhs.size) {
                    // Cartesian product of child options
                    val combinations = cartesianProduct(childTreeOptions, maxTrees - candidateTrees.size)
                    for (comb in combinations) {
                        candidateTrees.add(
                            ParseTreeNode(
                                id = getNextId(),
                                symbol = symbol,
                                isTerminal = false,
                                children = comb,
                                productionUsed = prod,
                                spanStart = start,
                                spanEnd = end
                            )
                        )
                        if (candidateTrees.size >= maxTrees) break
                    }
                }
            }
        }

        callStack.remove(key)
        return candidateTrees
    }

    /**
     * Finds partitions of span start..end for the RHS symbols list.
     */
    private fun findPartitions(
        rhs: List<String>,
        start: Int,
        end: Int,
        inputTokens: List<String>,
        cfg: CFG
    ): List<List<Pair<Int, Int>>> {
        val result = mutableListOf<List<Pair<Int, Int>>>()

        fun search(
            symbolIndex: Int,
            currentStart: Int,
            currentSplits: MutableList<Pair<Int, Int>>
        ) {
            if (symbolIndex == rhs.size - 1) {
                val nextSym = rhs[symbolIndex]
                val lengthNeeded = end - currentStart
                if (isValidSpanForSymbol(nextSym, lengthNeeded, currentStart, end, inputTokens, cfg)) {
                    currentSplits.add(Pair(currentStart, end))
                    result.add(ArrayList(currentSplits))
                    currentSplits.removeAt(currentSplits.size - 1)
                }
                return
            }

            val sym = rhs[symbolIndex]
            val maxLen = end - currentStart
            val minLen = if (isNullable(sym, cfg)) 0 else (if (!cfg.nonTerminals.contains(sym)) 1 else 0)

            for (len in minLen..maxLen) {
                val nextStart = currentStart + len
                if (isValidSpanForSymbol(sym, len, currentStart, nextStart, inputTokens, cfg)) {
                    currentSplits.add(Pair(currentStart, nextStart))
                    search(symbolIndex + 1, nextStart, currentSplits)
                    currentSplits.removeAt(currentSplits.size - 1)
                }
            }
        }

        search(0, start, mutableListOf())
        return result
    }

    private fun isValidSpanForSymbol(
        sym: String,
        len: Int,
        start: Int,
        end: Int,
        inputTokens: List<String>,
        cfg: CFG
    ): Boolean {
        if (GrammarSymbol.isEpsilonSymbol(sym)) {
            return len == 0
        }
        if (!cfg.nonTerminals.contains(sym)) {
            // Must be single terminal matching input token
            return len == 1 && start < inputTokens.size && inputTokens[start] == sym
        }
        // Non-terminal: can span 0 or more
        if (len == 0) {
            return isNullable(sym, cfg)
        }
        return true
    }

    private fun isNullable(sym: String, cfg: CFG): Boolean {
        if (GrammarSymbol.isEpsilonSymbol(sym)) return true
        if (!cfg.nonTerminals.contains(sym)) return false
        // Quick nullable check
        return cfg.getProductionsFor(sym).any { it.isEpsilon }
    }

    private fun cartesianProduct(
        lists: List<List<ParseTreeNode>>,
        limit: Int
    ): List<List<ParseTreeNode>> {
        var current = listOf(emptyList<ParseTreeNode>())
        for (list in lists) {
            val next = mutableListOf<List<ParseTreeNode>>()
            for (acc in current) {
                for (item in list) {
                    val combined = acc + item
                    next.add(combined)
                    if (next.size >= limit) break
                }
                if (next.size >= limit) break
            }
            current = next
        }
        return current
    }

    private fun treeToStructuralString(node: ParseTreeNode): String {
        if (node.isTerminal) return "${node.symbol}:${node.spanStart}-${node.spanEnd}"
        val prodStr = node.productionUsed?.displayString ?: node.symbol
        val kids = node.children.joinToString(",") { treeToStructuralString(it) }
        return "[$prodStr($kids)]"
    }
}
