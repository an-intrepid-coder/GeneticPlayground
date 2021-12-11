package prisonersDilemma

/**
 * This is a tree which maps out all the possible combinations of moves in a game of Prisoner's Dilemma
 * and maps each one to an index. It is a pretty generic tree, and I intend to make this a more abstract data structure
 * down the road. For now, it is just for the purpose of mapping out the game of Prisoner's Dilemma.
 */
class PrisonersDilemmaDecisionTree(
    val depth: Int = decisionTreeDepth,
) {
    var numNodes = 0

    /**
     * The tree works by having each node contain its children, starting from the root on down.
     */
    class DecisionTreeNode(
        val currentDepth: Int,
        val maxDepth: Int,
        val parentTree: PrisonersDilemmaDecisionTree,
        val parentNode: DecisionTreeNode?,
        val id: Int,
        val root: Boolean = false,
        val playerAPayoff: Int,
        val playerBPayoff: Int,
        val children: MutableList<DecisionTreeNode> = mutableListOf()
    ) {
        init {
            if (currentDepth < maxDepth) {
                possibleOutcomes.forEach { resultPair ->
                    children.add(
                        DecisionTreeNode(
                            parentTree = parentTree,
                            parentNode = this,
                            currentDepth = currentDepth + 1,
                            maxDepth = maxDepth,
                            id = parentTree.numNodes++,
                            playerAPayoff = resultPair.first,
                            playerBPayoff = resultPair.second,
                        )
                    )
                }
            }
        }
    }

    private val root = DecisionTreeNode(
        parentTree = this,
        parentNode = null,
        currentDepth = 0,
        maxDepth = 3,
        id = numNodes++,
        root = true,
        playerAPayoff = 0,
        playerBPayoff = 0
    )

    /**
     * returnIndexOfMoveSet takes a list of past round results, and traverses the tree by matching the moves
     * against the projections. When it reaches the bottom level of the tree, it returns the index associated with that
     * possible chain of events.
     */
    fun returnIndexOfMoveSet(mutualPayoffHistory: List<Pair<Int, Int>>): Int {
        if (mutualPayoffHistory.size > decisionTreeDepth)
            error("Invalid moveSet size: ${mutualPayoffHistory.size}. This should never happen.")

        var currentTreePosition = root
        mutualPayoffHistory.forEach { previousRound ->
            // The first player / player A is assumed to be the one looking back.
            val rememberedMovePair = Pair(previousRound.first, previousRound.second)
            currentTreePosition.children.forEach { childNode ->
                val childNodePayoffPair = Pair(childNode.playerAPayoff, childNode.playerBPayoff)
                if (rememberedMovePair == childNodePayoffPair) {
                    currentTreePosition = childNode
                }
            }
        }
        return currentTreePosition.id
    }
}