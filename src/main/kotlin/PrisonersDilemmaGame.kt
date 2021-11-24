/*
    Terminology for reward payoffs taken from Wikipedia. For simplicity's sake I've decided to use positive values
    instead of negative ones.
 */
const val rewardPayoff = 1
const val punishmentPayoff = 2
const val temptationPayoff = 0
const val suckersPayoff = 3

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
    val playerATotalScore: Int,
    val playerBTotalScore: Int,
)

/**
 * A rendition of the Prisoner's Dilemma game.
 *
 * TODO: Some unit tests.
 * TODO: Try to reproduce the results of Holland's paper, where the AIs rediscover the tit-for-tat strategy and beyond.
 * TODO: Migrate to its own package, as the Genetic Playground should be able to handle any number of games designed
 *  to use it.
 * TODO: Extended rule sets involving more players. I'm sure Wikipedia can offer a plethora of such things.
 */
class PrisonersDilemmaGame(
    val roundsToPlay: Int,
    private val interactiveMode: Boolean = false,
    private val playerA: PrisonersDilemmaPlayer,
    private val playerB: PrisonersDilemmaPlayer,
) {
    var roundsPassed = 0
    var previousRounds = listOf<PrisonersDilemmaRoundResult>()
    var playerAScoreTotal = 0
    var playerBScoreTotal = 0

    init {
        playerA.playerLabel = PrisonersDilemmaPlayerLabel.PLAYER_A
        playerB.playerLabel = PrisonersDilemmaPlayerLabel.PLAYER_B
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
                PrisonersDilemmaPlayerLabel.PLAYER_A -> playerA.chooseMove(this)
                PrisonersDilemmaPlayerLabel.PLAYER_B -> playerB.chooseMove(this)
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
        playerA.averageScore += roundResult.playerAScore
        playerB.averageScore += roundResult.playerBScore
        return roundResult
    }

    /**
     * Plays games until roundsToPlay has been reached, and returns the final result.
     */
    fun play(): PrisonersDilemmaGameResult {
        while (roundsPassed < roundsToPlay) {
            playRound().let { roundResult ->
                playerAScoreTotal += roundResult.playerAScore
                playerBScoreTotal += roundResult.playerBScore
                roundsPassed++
            }
        }

        playerA.averageScore /= roundsPassed
        playerB.averageScore /= roundsPassed

        return PrisonersDilemmaGameResult(
            roundResults = previousRounds,
            playerATotalScore = playerAScoreTotal,
            playerBTotalScore = playerBScoreTotal
        )
    }
}