/**
 * THe Characteristics for a PrisonersDilemmaPlayer each represent a possible combination of moves
 * during play, and an active or inactive gene at that point represents COOPERATION or DEFECTION. In this
 * case it is simplest to name the genes after the Index that it would have if this were an array of 1s and 0s.
 */
fun randomPrisonersDilemmaCharacteristics(): List<Characteristic> {
    val decisionTree = DecisionTree()

    return mutableListOf<Characteristic>().let { characteristics ->
        repeat (decisionTree.numNodes) { index ->
            characteristics.add(Characteristic((index + 1).toString()))
        }
        characteristics
    }
}

/**
 * Returns a new player with characteristics that match the given bit string.
 */
fun playerFromBitString(bitString: String): PrisonersDilemmaPlayer {
    fun indexActive(index: Int): Boolean {
        return bitString[index] == '1'
    }
    val characteristics = mutableListOf<Characteristic>()
    bitString.indices.forEach { bitIndex ->
        characteristics.add(
            when (indexActive(bitIndex)) {
                true -> Characteristic(bitIndex.toString(), active = true)
                else -> Characteristic(bitIndex.toString(), active = false)
            }
        )
    }
    return PrisonersDilemmaPlayer(characteristics)
}

/**
 * A list of control bots to test the gene pool against. titForTat is considered a good strategy for the game of
 * Prisoner's Dilemma. How a generation does on average against these archetypes will say a lot about how good
 * it is.
 */
val prisonersDilemmaBots = listOf(
    PrisonersDilemmaPlayer(
        botName = "alwaysDefects",
        botBehavior = {  _, _ ->
            DilemmaChoice.DEFECT
        }
    ),
    PrisonersDilemmaPlayer(
        botName = "alwaysCooperates",
        botBehavior = { _, _ ->
            DilemmaChoice.COOPERATE
        }
    ),
    PrisonersDilemmaPlayer(
        botName = "titForTat",
        botBehavior = { self, game ->
            if (game.roundsPassed == 0)
                DilemmaChoice.COOPERATE
            else {
                val lastRound = game.previousRounds.last()
                self.opponent!!.previousChoice(lastRound)
            }
        }
    )
)

/**
 * PrisonersDilemmaPlayer is a Classifier with one goal: Get the lowest score possible while playing Prisoner's
 * Dilemma. Most combinations of Characteristics will result in poor Classifiers, but what makes the Genetic Algorithm
 * so interesting is that it will hone in on relatively optimal strategies in less generations than one would
 * expect.
 */
class PrisonersDilemmaPlayer(
    characteristics: List<Characteristic> = randomPrisonersDilemmaCharacteristics(),
    var playerLabel: PrisonersDilemmaPlayerLabel? = null,
    var opponent: PrisonersDilemmaPlayer? = null,
    val botName: String? = null,
    val botBehavior: ((PrisonersDilemmaPlayer, PrisonersDilemmaGame) -> DilemmaChoice)? = null,
) : Classifier(characteristics) {

    /**
     * Returns a copy of the classifier that is one generation older.
     */
    override fun emitSurvivor(): Classifier {
        val survivor = playerFromBitString(this.asBinaryString())
        survivor.age = this.age + 1
        return survivor
    }

    /**
     * Following the recipe in John Holland's paper, this combines two Classifiers in to two offspring, using
     * a crossover technique and a chance of mutation.
     *
     * TODO: This should be in the super class, and I intend to put it there at some point.
     */
    override fun combine(other: Classifier): List<Classifier> {
        val childACharacteristics = mutableListOf<Characteristic>()
        val childBCharacteristics = mutableListOf<Characteristic>()

        // Pick random index:
        val crossoverIndex = characteristics.indices.random()

        // Swap all characteristics to the left of the index:
        repeat (crossoverIndex) { index ->
            childACharacteristics.add(other.characteristics[index])
            childBCharacteristics.add(characteristics[index])
        }

        // Leave characteristics to the right of the index:
        for (index in crossoverIndex until characteristics.size) {
            childACharacteristics.add(characteristics[index])
            childBCharacteristics.add(other.characteristics[index])
        }

        // Apply a mutation chance to each offspring characteristic:
        repeat (characteristics.size) { index ->
            childACharacteristics[index].applyMutationFrequency(defaultMutationFrequency)
            childBCharacteristics[index].applyMutationFrequency(defaultMutationFrequency)
        }

        return listOf(
            PrisonersDilemmaPlayer(childACharacteristics),
            PrisonersDilemmaPlayer(childBCharacteristics),
        )
    }

    /**
     * Grabs a slice of the previous rounds played in the game based on the depth of the DecisionTree
     * used for the players.
     */
    private fun turnMemory(game: PrisonersDilemmaGame): List<PrisonersDilemmaRoundResult> {
        val sliceRangeStart = (game.previousRounds.size - decisionTreeDepth).coerceAtLeast(0)
        return game.previousRounds.slice(sliceRangeStart until game.previousRounds.size)
    }

    /**
     * Parses a round result from the simulation in order to examine who did what.
     */
    fun previousChoice(
        roundResult: PrisonersDilemmaRoundResult,
    ): DilemmaChoice {
        return when (playerLabel) {
            PrisonersDilemmaPlayerLabel.PLAYER_A -> roundResult.playerAChoice
            else -> roundResult.playerBChoice
        }
    }

    /**
     * Returns the score the player got from the last round.
     */
    fun previousPayoff(
        roundResult: PrisonersDilemmaRoundResult,
    ): Int {
        return when (playerLabel) {
            PrisonersDilemmaPlayerLabel.PLAYER_A -> roundResult.playerAScore
            else -> roundResult.playerBScore
        }
    }

    /**
     * Returns true if the given previous round was a win.
     */
    fun previousWin(
        roundResult: PrisonersDilemmaRoundResult,
    ): Boolean {
        return when (playerLabel) {
            PrisonersDilemmaPlayerLabel.PLAYER_A -> roundResult.playerAScore == rewardPayoff || roundResult.playerAScore == temptationPayoff
            else -> roundResult.playerBScore == rewardPayoff || roundResult.playerBScore == temptationPayoff
        }
    }

    /**
     * Uses a map of all possible decisions within the given window and returns COOPERATE or DEFECT depending
     * on whether the indicated sequence of events is mapped to an active Characteristic.
     */
    fun chooseMove(
        game: PrisonersDilemmaGame,
        decisionTree: DecisionTree,
    ): DilemmaChoice {
        if (botBehavior != null)
            return botBehavior.invoke(this, game)

        val previousRounds = turnMemory(game)
        // If not a bot, it will do exactly as the decision tree mapped on to its genome dictates:
        val decisionIndex = decisionTree.returnIndexOfMoveSet(previousRounds)
        return when (hasActiveGene(decisionIndex.toString())) {
            true -> DilemmaChoice.COOPERATE
            else -> DilemmaChoice.DEFECT
        }
    }
}
