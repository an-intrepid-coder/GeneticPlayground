package prisonersDilemma

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import core.*
import kotlinx.coroutines.launch

// The four possible outcomes of a round:
val possibleOutcomes = listOf(
    Outcome(
        name = "bothCooperate",
        value = Pair(rewardPayoff, rewardPayoff),
    ),
    Outcome(
        name = "playerBWins",
        value = Pair(suckersPayoff, temptationPayoff),
    ),
    Outcome(
        name = "playerAWins",
        value = Pair(temptationPayoff, suckersPayoff),
    ),
    Outcome(
        name = "bothDefect",
        value = Pair(punishmentPayoff, punishmentPayoff)
    )
)

/*
    The decisionTree which acts as a map of all possible move-combinations in the game, going back to an
    arbitrary depth (by default 3 turns).
 */
val decisionTree = DecisionTree(decisionTreeDepth, possibleOutcomes)

/**
 * Returns a Classifier which represents a Strategy for the game Prisoner's Dilemma, with randomly-
 * generated Characteristics. A randomly-generated strategy is sure to be pretty bad, most of the time.
 */
fun randomPrisonersDilemmaStrategy(): Classifier {
    /**
     * Returns a random set of Characteristics representing a strategy for the game Prisoner's Dilemma.
     */
    fun randomCharacteristics(): List<Characteristic> {
        return mutableListOf<Characteristic>().let { list ->
            val characteristicIndexRange = 1 until decisionTree.numNodes
            for (index in characteristicIndexRange) {
                list.add(Characteristic(index.toString()))
            }
            list
        }
    }

    return Classifier(
        ruleName = "prisonersDilemmaStrategy",
        characteristics = randomCharacteristics(),
        ruleBehavior = { signal, self ->
            /*
                This Classifier takes a Signal to play a game of Prisoner's Dilemma against a specific
                opponent for a specific number of rounds. It returns the result of this exchange as an
                outgoing Signal.
             */
            signal as SignalToPlayPrisonersDilemma

            // Get the desired # of rounds from the signal:
            val numRounds = signal.numRounds

            // Get the opponent from the signal (for now just a random bot):
            val opposition = signal.opposition

            // Scores and score history for each player:
            var score = 0
            var oppositionScore = 0
            val outcomeHistory = mutableListOf<Outcome>()

            fun trimHistory(maxSize: Int) {
                while (outcomeHistory.size > maxSize)
                    outcomeHistory.removeFirst()
            }

            fun flippedHistoryPov(): List<Outcome> {
                return outcomeHistory
                    .map { outcome ->
                        Outcome(
                            name = outcome.name,
                            value = Pair((outcome.value as Pair<Int, Int>).second, outcome.value.first)
                        )
                    }
            }

            repeat (numRounds) {
                // Trim the history:
                trimHistory(decisionTreeDepth)

                // Decision for this Classifier to "defect" or not in the game of Prisoner's Dilemma:
                val defects = decisionTree
                    .idOfOutcomeChain(outcomeHistory.reversed())
                    .let { self.hasActiveGene(it.toString()) }

                // Decision for the opponent to "defect" or not in the game of Prisoner's Dilemma:
                val oppositionDefects = decisionTree
                    .idOfOutcomeChain(flippedHistoryPov().reversed())
                    .let { opposition.hasActiveGene(it.toString()) }

                // There are four possible ways the round can go:
                val outcome = when (Pair(defects, oppositionDefects)) {
                    Pair(true, true) -> Outcome("bothDefect", Pair(punishmentPayoff, punishmentPayoff))
                    Pair(true, false) -> Outcome("playerAWins", Pair(temptationPayoff, suckersPayoff))
                    Pair(false, true) -> Outcome("playerBWins", Pair(suckersPayoff, temptationPayoff))
                    else -> Outcome("bothCooperate", Pair(rewardPayoff, rewardPayoff))
                }

                // Update the scores and histories:
                score += (outcome.value as Pair<Int, Int>).first
                oppositionScore += outcome.value.second
                outcomeHistory.add(outcome)
            }

            // Return a signal that the game has been completed, and what the score was:
            SignalThatPrisonersDilemmaHasBeenPlayed(score, oppositionScore, numRounds)
        }
    )
}

/**
 * Returns a prisonersDilemmaPlayer Agent a randomPrisonersDilemmaStrategy() as its lone Classifier.
 */
fun randomPrisonersDilemmaPlayer(): Agent {
    return Agent(
        agentName = "prisonersDilemmaPlayer",
        classifiers = listOf(randomPrisonersDilemmaStrategy()),
    )
}

/**
 * A "signal" which instructs a prisonersDilemmaPlayer to play a game of Iterated Prisoner's Dilemma for a given
 * number of rounds against a given opponent (for now, a random bot).
 */
data class SignalToPlayPrisonersDilemma (
    val numRounds: Int = prisonersDilemmaRoundRange.random(),
    val opposition: Classifier = randomPrisonersDilemmaStrategy()
)

/**
 * A "signal" which indicates that a game of Iterated Prisoner's Dilemma has been played, and what the score was.
 */
data class SignalThatPrisonersDilemmaHasBeenPlayed (
    val score: Int,
    val oppositionScore: Int,
    val numRounds: Int,
)

/**
 * A concurrent simulation which sets up a population of prisonersDilemmaPlayers and evolves them against
 * randomly-generated bots until they are mature. It is a place for the simulation to run "out of the way" of the
 * Compose front-end.
 *
 * Tentatively, they are considered "mature" when a win-rate of 95%+ is achieved by the whole pool against
 * random bots. I will likely change this in the future to be more nuanced.
 */
class PrisonersDilemmaPlayground {
    var timeStep by mutableStateOf(0)
    var started by mutableStateOf(false)
    var finished by mutableStateOf(false)
    var averageScoreAgainstRandom by mutableStateOf(0.0)
    var averageWinPercentAgainstRandom by mutableStateOf(0.0)

    // A population of Prisoner's Dilemma Players:
    private var population: List<Agent> = mutableListOf<Agent>().let { list ->
        repeat (defaultGenePoolSize) {
            list.add(randomPrisonersDilemmaPlayer())
        }
        list
    }

    /**
     * Runs the whole simulation from start to finish. It ends when the population has a 95%+ win-rate against
     * random bots.
     */
    suspend fun runSimulation(coroutineHandler: CoroutineHandler) {
        val coroutineScope = coroutineHandler.coroutineScope
        started = true
        while(!finished) {
            // A small cost-of-living adjustment to push bad builds out of the pool:
            population.forEach { it.consumeResources("prisonersDilemmaPoints", 1) }

            // The total score and # of wins for this time step against random bots:
            var totalScoreAgainstRandom = 0.0
            var totalWinsAgainstRandom = 0.0

            // Run the population through a number of tests which reward for success:
            population.forEach { agent ->
                // Test against the outside population. There is a penalty for losing here:
                coroutineHandler.addJob(coroutineScope.launch {
                    val signal = agent.applyRule(
                        ruleName = "prisonersDilemmaStrategy",
                        dataBundle = SignalToPlayPrisonersDilemma()
                    ) as SignalThatPrisonersDilemmaHasBeenPlayed

                    totalScoreAgainstRandom += signal.score

                    // Players get 2 food for scoring less than the opponent, 1 food for tying, and 0 for scoring more.
                    if (signal.score < signal.oppositionScore)
                        agent.addResources("prisonersDilemmaPoints", 2)
                    else if (signal.score == signal.oppositionScore)
                        agent.addResources("prisonersDilemmaPoints", 1)
                    else
                        agent.consumeResources("prisonersDilemmaPoints", 1)

                    // Currently, counting less than or equal to as a win:
                    if (signal.score <= signal.oppositionScore)
                        totalWinsAgainstRandom++
                })

                // Test against the best Agent in the pool. (note: will use more nuanced criteria later) This is
                // to make sure that the whole pool has a chance to face the best possible opposition every timeStep.
                // There is no penalty for doing poorly in this one.
                coroutineHandler.addJob(coroutineScope.launch {
                    val signal = agent.applyRule(
                        ruleName = "prisonersDilemmaStrategy",
                        dataBundle = SignalToPlayPrisonersDilemma(
                            opposition = population
                                .minByOrNull { it.numResources("prisonersDilemmaPoints") }!!
                                .getClassifierOrNull("prisonersDilemmaStrategy")!!
                        )
                    ) as SignalThatPrisonersDilemmaHasBeenPlayed

                    // Players get 2 food for scoring less than the opponent, 1 food for tying, and 0 for scoring more.
                    agent.addResources(
                        resourceName = "prisonersDilemmaPoints",
                        amount = if (signal.score < signal.oppositionScore)
                            majorWinReward
                        else if (signal.score == signal.oppositionScore)
                            minorWinReward
                        else
                            0
                    )
                })

                // Test against random Agents in the pool. This helps to keep the pool strong even after it has
                // become much better than average. There is a penalty for all Agents who lose here.
                coroutineHandler.addJob(coroutineScope.launch {
                    val signal = agent.applyRule(
                        ruleName = "prisonersDilemmaStrategy",
                        dataBundle = SignalToPlayPrisonersDilemma(
                            opposition = population
                                .random()
                                .getClassifierOrNull("prisonersDilemmaStrategy")!!
                        )
                    ) as SignalThatPrisonersDilemmaHasBeenPlayed

                    // Players get 2 food for scoring less than the opponent, 1 food for tying, and lose a resource
                    // for scoring more:
                    if (signal.score < signal.oppositionScore)
                        agent.addResources("prisonersDilemmaPoints", 2)
                    else if (signal.score == signal.oppositionScore)
                        agent.addResources("prisonersDilemmaPoints", 1)
                    else
                        agent.consumeResources("prisonersDilemmaPoints", 1)
                })
            }

            // Wait for all coroutines to complete:
            coroutineHandler.joinAndClearActiveJobs()

            // Collect scores:
            averageScoreAgainstRandom = totalScoreAgainstRandom
                .div(population.size)

            // Collect win %:
            averageWinPercentAgainstRandom = totalWinsAgainstRandom
                .div(population.size)
                .times(100.0)

            // Check for finished-condition:
            if (averageWinPercentAgainstRandom >= 95.0)
                finished = true

            // Preparing a new population:
            val newPopulation = mutableListOf<Agent>()

            // All Agents above the reproduction threshold pair off at random:
            val reproducingAgents = population
                .filter { it.numResources("prisonersDilemmaPoints") >= prisonersDilemmaReproductionThreshold }
                .shuffled()

            // The whole population (including those reproducing) in descending order by prisonersDilemmaPoints
            // (before they've been spent):
            val orderedByPoints = population
                .sortedByDescending { it.numResources("prisonersDilemmaPoints") }

            // All reproducing pairs contribute their "offspring" to the newPopulation:
            reproducingAgents
                .chunked(2)
                .forEach { pair ->
                if (pair.size == 1)
                    newPopulation.add(pair.first().clone())
                else
                    pair.first()
                        .combine(pair.last())
                        .forEach { newPopulation.add(it) }
            }

            // Survivors are added from most-to-least fit until the newPopulation is full, leaving the least fit
            // to fall out of the pool:
            for (survivor in orderedByPoints) {
                if (newPopulation.size >= population.size)
                    break
                if (survivor in reproducingAgents)
                    survivor.consumeResources("prisonersDilemmaPoints", prisonersDilemmaReproductionThreshold)
                newPopulation.add(survivor)
            }

            // Set the new population:
            population = newPopulation

            // Advance the time-step
            timeStep++
        }
    }
}