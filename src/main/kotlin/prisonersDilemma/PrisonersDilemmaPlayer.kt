package prisonersDilemma

import core.Characteristic
import core.Classifier

/**
 * THe Characteristics for a PrisonersDilemma.PrisonersDilemmaPlayer each represent a possible combination of moves
 * during play, and an active or inactive gene at that point represents COOPERATION or DEFECTION. In this
 * case it is simplest to name the genes after the Index that it would have if this were an array of 1s and 0s.
 */
fun randomPrisonersDilemmaCharacteristics(): List<Characteristic> {
    val decisionTree = PrisonersDilemmaDecisionTree()

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
fun prisonersDilemmaPlayerFromBitString(bitString: String): PrisonersDilemmaPlayer {
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
val prisonersDilemmaBots = mapOf(
    Pair(
        "alwaysDefects",
        PrisonersDilemmaPlayer(
            botBehavior = {  _, _ ->
                DilemmaChoice.DEFECT
            }
        )
    ),
    Pair(
        "alwaysCooperates",
        PrisonersDilemmaPlayer(
            botBehavior = { _, _ ->
                DilemmaChoice.COOPERATE
            }
        )
    ),
    Pair(
        "titForTat",
        PrisonersDilemmaPlayer(
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
)

/**
 * PrisonersDilemma.PrisonersDilemmaPlayer is a Core.Classifier with one goal: Get the lowest score possible while playing Prisoner's
 * Dilemma. Most combinations of Characteristics will result in poor Classifiers, but what makes the Genetic Algorithm
 * so interesting is that it will hone in on relatively optimal strategies in less generations than one would
 * expect.
 */
class PrisonersDilemmaPlayer(
    characteristics: List<Characteristic> = randomPrisonersDilemmaCharacteristics(),
    var playerLabel: PrisonersDilemmaPlayerLabel? = null,
    var opponent: PrisonersDilemmaPlayer? = null,
    val botBehavior: ((PrisonersDilemmaPlayer, PrisonersDilemmaGame) -> DilemmaChoice)? = null,
) : Classifier(characteristics) {


    /**
     * Returns a copy of the classifier that is one generation older.
     */
    override fun emitSurvivor(): Classifier {
        val survivor = prisonersDilemmaPlayerFromBitString(this.asBinaryString())
        survivor.age = this.age + 1
        return survivor
    }

    /**
     * Grabs a slice of the previous rounds played in the game based on the depth of the Core.DecisionTree
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
     * on whether the indicated sequence of events is mapped to an active Core.Characteristic.
     */
    fun chooseMove(
        game: PrisonersDilemmaGame,
        decisionTree: PrisonersDilemmaDecisionTree,
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
