enum class PrisonersDilemmaPlayerLabel {
    PLAYER_A,
    PLAYER_B
}

enum class DilemmaChoice {
    COOPERATE,
    DEFECT
}

data class PrisonersDilemmaRoundResult(
    val playerAScore: Int,
    val playerAChoice: DilemmaChoice,
    val playerBScore: Int,
    val playerBChoice: DilemmaChoice,
    val roundsPassed: Int,
)

data class PrisonersDilemmaGameResult(
    val roundResults: List<PrisonersDilemmaRoundResult>,
    val playerAAverageScore: Double,
    val playerATotalScore: Double,
    val playerBAverageScore: Double,
    val playerBTotalScore: Double,
)

/**
 * A rendition of the Prisoner's Dilemma game. Modeled after the rules on Wikipedia with the slight difference
 * that I am using positive numbers for the scoring (and therefore the best score is lower, like in golf), rather
 * than negative numbers. Technically this is a game of Iterated Prisoner's Dilemma.
 */
class PrisonersDilemmaGame(
    val roundsToPlay: Int,
    private val interactiveMode: Boolean = false,
    val playerA: PrisonersDilemmaPlayer,
    val playerB: PrisonersDilemmaPlayer,
) {
    private val decisionTree = DecisionTree()
    var roundsPassed = 0
    var previousRounds = mutableListOf<PrisonersDilemmaRoundResult>()

    init {
        playerA.playerLabel = PrisonersDilemmaPlayerLabel.PLAYER_A
        playerB.playerLabel = PrisonersDilemmaPlayerLabel.PLAYER_B
        playerA.opponent = playerB
        playerB.opponent = playerA
        playerA.score = 0.0
        playerB.score = 0.0
    }

    /**
     * Interactive mode exists for manual testing.
     */
    private fun makeChoice(playerLabel: PrisonersDilemmaPlayerLabel): DilemmaChoice {
        return when (interactiveMode) {
            true -> {
                println("(c)ooperate or (d)efect?")
                val input = readLine()
                when (input?.first()?.lowercase()) {
                    "c" -> DilemmaChoice.COOPERATE
                    "d" -> DilemmaChoice.DEFECT
                    else -> DilemmaChoice.values().random()
                }
            }
            else -> when (playerLabel) {
                PrisonersDilemmaPlayerLabel.PLAYER_A -> playerA.chooseMove(this, decisionTree)
                PrisonersDilemmaPlayerLabel.PLAYER_B -> playerB.chooseMove(this, decisionTree)
            }
        }
    }

    /**
     * Plays a single round and returns the result.
     */
    private fun playRound(): PrisonersDilemmaRoundResult {
        if (interactiveMode)
            println("Player A make your choice:")
        val playerAChoice = makeChoice(PrisonersDilemmaPlayerLabel.PLAYER_A)
        if (interactiveMode)
            println("Player B make your choice:")
        val playerBChoice = makeChoice(PrisonersDilemmaPlayerLabel.PLAYER_B)
        val roundResult = when (Pair(playerAChoice, playerBChoice)) {
            Pair(DilemmaChoice.COOPERATE, DilemmaChoice.COOPERATE) -> PrisonersDilemmaRoundResult(
                playerAScore = rewardPayoff,
                playerAChoice = DilemmaChoice.COOPERATE,
                playerBScore = rewardPayoff,
                playerBChoice = DilemmaChoice.COOPERATE,
                roundsPassed = roundsPassed
            )
            Pair(DilemmaChoice.DEFECT, DilemmaChoice.DEFECT) -> PrisonersDilemmaRoundResult(
                playerAScore = punishmentPayoff,
                playerAChoice = DilemmaChoice.DEFECT,
                playerBScore = punishmentPayoff,
                playerBChoice = DilemmaChoice.DEFECT,
                roundsPassed = roundsPassed
            )
            Pair(DilemmaChoice.COOPERATE, DilemmaChoice.DEFECT) -> PrisonersDilemmaRoundResult(
                playerAScore = suckersPayoff,
                playerAChoice = DilemmaChoice.COOPERATE,
                playerBScore = temptationPayoff,
                playerBChoice = DilemmaChoice.DEFECT,
                roundsPassed = roundsPassed
            )
            Pair(DilemmaChoice.DEFECT, DilemmaChoice.COOPERATE) -> PrisonersDilemmaRoundResult(
                playerAScore = temptationPayoff,
                playerAChoice = DilemmaChoice.DEFECT,
                playerBScore = suckersPayoff,
                playerBChoice = DilemmaChoice.COOPERATE,
                roundsPassed = roundsPassed
            )
            else -> error("This should never happen.")
        }
        return roundResult
    }

    /**
     * Plays games until roundsToPlay has been reached, and returns the final result.
     */
    fun play(): PrisonersDilemmaGameResult {
        while (roundsPassed < roundsToPlay) {
            playRound().let { roundResult ->
                playerA.score += roundResult.playerAScore
                playerB.score += roundResult.playerBScore
                roundsPassed++
                previousRounds.add(roundResult)
            }
        }

        val averageA = playerA.score / roundsToPlay
        val averageB = playerB.score / roundsToPlay

        val gameResult = PrisonersDilemmaGameResult(
            roundResults = previousRounds,
            playerAAverageScore = averageA,
            playerATotalScore = playerA.score,
            playerBAverageScore = averageB,
            playerBTotalScore = playerB.score
        )

        playerA.score /= roundsToPlay
        playerB.score /= roundsToPlay

        return gameResult
    }
}