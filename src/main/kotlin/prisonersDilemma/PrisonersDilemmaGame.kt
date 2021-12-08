package prisonersDilemma

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
    val playerTotalScore: Int,
    val botTotalScore: Int,
    val win: Boolean,
)

/**
 * A rendition of the Prisoner's Dilemma game. Modeled after the rules on Wikipedia with the slight difference
 * that I am using positive numbers for the scoring (and therefore the best score is lower, like in golf), rather
 * than negative numbers. Technically this is a game of Iterated Prisoner's Dilemma.
 */
class PrisonersDilemmaGame(
    val roundsToPlay: Int,
    val playerA: PrisonersDilemmaPlayer,
    val playerB: PrisonersDilemmaPlayer,
    private val interactiveMode: Boolean = false,
    private val countsTowardsWins: Boolean = true,
) {
    private val decisionTree = PrisonersDilemmaDecisionTree()
    var roundsPassed = 0
    var previousRounds = mutableListOf<PrisonersDilemmaRoundResult>()

    init {
        playerA.playerLabel = PrisonersDilemmaPlayerLabel.PLAYER_A
        playerB.playerLabel = PrisonersDilemmaPlayerLabel.PLAYER_B
        playerA.opponent = playerB
        playerB.opponent = playerA
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
     * Plays games until roundsToPlay has been reached, and returns the final result. For the purposes of this
     * program, a "win" in Prisoner's Dilemma is when you get less than or equal to the score of the opponent.
     * This is to reward the potential for emergent cooperation. However, I have also chosen to give extra
     * resources (which will speed evolution) for getting strictly less than an opponent as opposed to a tie
     * (so that players are inclined to try and beat titForTat rather than just be competitive with it).
     */
    fun play(): PrisonersDilemmaGameResult {
        var playerAScore = 0
        var playerBScore = 0

        while (roundsPassed < roundsToPlay) {
            playRound().let { roundResult ->
                playerAScore += roundResult.playerAScore
                playerBScore += roundResult.playerBScore
                roundsPassed++
                previousRounds.add(roundResult)
            }
        }

        val gameResult = PrisonersDilemmaGameResult(
            roundResults = previousRounds,
            playerTotalScore = playerAScore,
            botTotalScore = playerBScore,
            /*
                In the event of a tie, both win. This supports the cooperative nature of Prisoner's Dilemma,
                and may not translate to other experiments or models. In order to reward more competitive
                Classifiers, they will get 1 point for ties and 2 points for outright wins.
             */
            win = playerAScore <= playerBScore,
        )

        if (countsTowardsWins && gameResult.win)
            playerA.wins += if (playerAScore < playerBScore) 2 else 1

        return gameResult
    }
}