package core

/**
 * A loose wrapper which represents a distinct possible "outcome" of some kind. It does not need a value, but it
 * can have one, and it may be useful to have it store one. It is important that there be distinct names for each
 * type of outcome. See Prisoner's Dilemma for a simple example.
 */
data class Outcome(
    val name: String,
    val value: Any? = null,
) {
    fun matches(other: Outcome): Boolean {
        return this.name == other.name
    }
}

/**
 * A tree which contains all possible combinations of a set of outcomes, to a pre-determined depth. Its primary use
 * is for indexing and retrieving specific combinations of outcomes.
 */
class DecisionTree(
    val depth: Int,
    val allOutcomes: List<Outcome>,
) {
    var numNodes = 0

    /**
     * Unless root, contains an outcome and, if not the bottom level of the tree, then also children for each outcome.
     * Each node has an id, and that id corresponds to a unique chain of outcomes.
     */
    class DecisionTreeNode(
        val id: Int,
        val currentDepth: Int,
        val maxDepth: Int,
        val allOutcomes: List<Outcome>,
        val parentTree: DecisionTree,
        val outcome: Outcome? = null,
        val parentNode: DecisionTreeNode? = null,
        val children: MutableList<DecisionTreeNode> = mutableListOf()
    ) {
        init {
            if (currentDepth < maxDepth) {
                allOutcomes.forEach { outcome ->
                    children.add(
                        DecisionTreeNode(
                            id = parentTree.numNodes++,
                            currentDepth = currentDepth + 1,
                            maxDepth = maxDepth,
                            allOutcomes = allOutcomes,
                            parentTree = parentTree,
                            outcome = outcome,
                            parentNode = this,
                        )
                    )
                }
            }
        }
    }

    private val root = DecisionTreeNode(
        id = numNodes++,
        currentDepth = 0,
        maxDepth = depth,
        allOutcomes = allOutcomes,
        parentTree = this,
    )

    /**
     * Finds the element of the DecisionTree which matches the sequence in outcomeChain, and returns its id. This
     * assumes outcomeChain is to be read from the front of the list to the back.
     */
    fun idOfOutcomeChain(outcomeChain: List<Outcome>): Int {
        if (outcomeChain.size > depth)
            error("outcomeChain.size > depth: outcomeChain.size=${outcomeChain.size}, depth=$depth")

        var currentTreePosition = root
        var found = 0
        outcomeChain
            .forEach { outcome ->
            currentTreePosition.children.forEach { childNode ->
                if (childNode.outcome!!.matches(outcome)) {
                    currentTreePosition = childNode
                    found++
                }
            }
        }
        if (found < outcomeChain.size)
            error("Incomplete search. Bad data. This should never happen.")
        return currentTreePosition.id
    }
}