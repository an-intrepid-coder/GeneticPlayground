/*
    Terminology for reward payoffs taken from Wikipedia. For simplicity's sake I've decided to use positive values
    instead of negative ones.
 */
const val rewardPayoff = 1
const val punishmentPayoff = 2
const val temptationPayoff = 0
const val suckersPayoff = 3

enum class DilemmaChoice {
    COOPERATE,
    DEFECT
}

data class PrisonersDilemmaGameResult(
    val playerAScore: Int,
    val playerBScore: Int,
    val roundsPassed: Int,
    val final: Boolean = false
)

/**
 * A rendition of the Prisoner's Dilemma game.
 *
 * TODO: Some unit tests.
 * TODO: A means of inputting AI players with custom rule sets.
 * TODO: Try to reproduce the results of Holland's paper, where the AIs rediscover the tit-for-tat strategy and beyond.
 * TODO: Migrate to its own package, as the Genetic Playground should be able to handle any number of games designed
 *  to use it.
 * TODO: Extended rule sets involving more players. I'm sure Wikipedia can offer a plethora of such things.
 */
class PrisonersDilemma(
    private val roundsToPlay: Int,
    private val interactiveMode: Boolean = false
    // TODO: Behavior rule sets will go here, in this game and others.
) {
    private var roundsPassed = 0
    private var playerAScoreTotal = 0
    private var playerBScoreTotal = 0

    /**
     * Interactive mode exists for manual testing.
     */
    private fun makeChoice(): DilemmaChoice {
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
            else -> DilemmaChoice.values().random() // placeholder for rule sets describing behavior
        }
    }

    /**
     * Plays a single round and returns the result.
     */
    private fun playRound(): PrisonersDilemmaGameResult {
        if (interactiveMode)
            println("Player A make your choice:")
        val playerAChoice = makeChoice()
        if (interactiveMode)
            println("Player B make your choice:")
        val playerBChoice = makeChoice()
        return when (Pair(playerAChoice, playerBChoice)) {
            Pair(DilemmaChoice.COOPERATE, DilemmaChoice.COOPERATE) -> PrisonersDilemmaGameResult(
                playerAScore = rewardPayoff,
                playerBScore = rewardPayoff,
                roundsPassed = roundsPassed
            )
            Pair(DilemmaChoice.DEFECT, DilemmaChoice.DEFECT) -> PrisonersDilemmaGameResult(
                playerAScore = punishmentPayoff,
                playerBScore = punishmentPayoff,
                roundsPassed = roundsPassed
            )
            Pair(DilemmaChoice.COOPERATE, DilemmaChoice.DEFECT) -> PrisonersDilemmaGameResult(
                playerAScore = suckersPayoff,
                playerBScore = temptationPayoff,
                roundsPassed = roundsPassed
            )
            Pair(DilemmaChoice.DEFECT, DilemmaChoice.COOPERATE) -> PrisonersDilemmaGameResult(
                playerAScore = temptationPayoff,
                playerBScore = suckersPayoff,
                roundsPassed = roundsPassed
            )
            else -> error("This should never happen.")
            // ^ seems to be a bug. It complained that my conditions weren't exhaustive when they were. Perhaps
            //  this is due to it not taking the nature of the Pair() in to account.
        }
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
        return PrisonersDilemmaGameResult(
            playerAScore = playerAScoreTotal,
            playerBScore = playerBScoreTotal,
            roundsPassed = roundsPassed,
            final = true
        )
    }
}