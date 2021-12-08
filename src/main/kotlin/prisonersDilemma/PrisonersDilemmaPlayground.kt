package prisonersDilemma

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import core.CoroutineHandler
import core.Playground
import kotlinx.coroutines.*
import kotlin.math.pow

/**
 * This is the simulation which brings the game and the players together and runs the experiment.
 * It holds the data in a way that Compose can read while it is running.
 */
class PrisonersDilemmaPlayground(
    coroutineHandler: CoroutineHandler,
) : Playground(coroutineHandler) {
    // These are "demoMode fluff" data points which help to illustrate what is happening:
    var percentSolutionsExplored by mutableStateOf(0.0)
    var numSolutionsExplored by mutableStateOf(0)
   /*
        Although the book "Signals and Boundaries" suggests using a resource-acquisition-based system instead
        of averages when it comes to testing for fitness, these metrics are still useful for demoMode (if somewhat
        non-performant to collect).
    */
    var averageScoreWithinPool by mutableStateOf(0.0)
    var averageScoreAgainstAlwaysDefects by mutableStateOf(0.0)
    var winPercentAgainstAlwaysDefects by mutableStateOf(0.0)
    var averageScoreAgainstAlwaysCooperates by mutableStateOf(0.0)
    var winPercentAgainstAlwaysCooperates by mutableStateOf(0.0)
    var averageScoreAgainstTitForTat by mutableStateOf(0.0)
    var winPercentAgainstTitForTat by mutableStateOf(0.0)
    var averageScoreAgainstRandom by mutableStateOf(0.0)
    var winPercentAgainstRandom by mutableStateOf(0.0)

    /**
     * Resets the app.
     */
    override suspend fun reset() {
        coroutineHandler.cancel()
        winPercentAgainstAlwaysDefects = 0.0
        winPercentAgainstAlwaysCooperates = 0.0
        winPercentAgainstTitForTat = 0.0
        winPercentAgainstRandom = 0.0
        averageScoreWithinPool = 0.0
        averageScoreAgainstAlwaysDefects = 0.0
        averageScoreAgainstAlwaysCooperates = 0.0
        averageScoreAgainstTitForTat = 0.0
        averageScoreAgainstRandom = 0.0
        percentSolutionsExplored = 0.0
        numSolutionsExplored = 0
        currentGeneration = -1
        started = false
        finished = false
        coroutineHandler.cancelled = false
    }

    /**
     * The experiment is pretty straightforward: The size of the gene pool never changes. The most fit reproduce
     * using combination and crossover, the most fit survivors (including the parents) fill in the remainder of the
     * next generation, and the least fit from a previous generation fall off. At each generation, the pool as a
     * whole will pair off and play each other -- this is the score which will act as a sort of
     * selective "pressure". Then,the whole pool is tested against a group of control bots in order to see how
     * much progress they have made as a whole relative to known archetypes. They will rather quickly reach a
     * sort of equilibrium which is in general pretty effective (even against good bots such as titForTat).
     *
     * When the experiment is in demoMode, it displays a lot of additional information in the front-end which
     * allows the user to observe how this works. This stuff takes a lot of extra cycles to calculate and display,
     * and it is possible to turn off demoMode in order to see how fast this can be when more optimized. When
     * demoMode is off, most cycle-wasting actions are locked behind an if-statement. If it weren't for all the demo
     * fluff and testing, this would be a pretty short function. It runs very fast when demoMode = false.
     */
    override suspend fun run() {
        val numRoundsRange = prisonersDilemmaRoundRange
        val genePoolSize = defaultGenePoolSize
        val demoMode = true
        val coroutineScope = coroutineHandler.coroutineScope

        // Set up a gene pool:
        val classifierPool = PrisonersDilemmaPlayerPool(genePoolSize)

        // Ensure there are no active jobs:
        coroutineHandler.joinAndClearActiveJobs()

        // Set up the set of all seen genomes:
        val seenGenomes = mutableSetOf<String>()

        /**
         * Tracks each observed permutation of the gene pool and reports on how many have been seen,
         * and what percentage of the total possible genomes this represents. Although this one would
         * probably not be included in a performance-conscious app, it helps to illustrate why the
         * genetic algorithm is so good. By the time optimal solutions have been found against titForTat and
         * alwaysCooperates, often *far* less than 1% of the total "search space" will have been explored. It is a
         * neat demonstration of how these algorithms work.
         *
         * This is "demo fluff" and is only used during demoMode.
         */
        fun observeGenomes() {
            classifierPool.pool.asSequence()
                .map { it.asBinaryString() }
                .filter { it !in seenGenomes }
                .forEach { seenGenomes.add(it) }

            numSolutionsExplored = seenGenomes.size

            val possibleSolutions = (2.0).pow(classifierPool.pool.first().characteristics.size)

            percentSolutionsExplored = seenGenomes
                .size
                .toDouble()
                .div(possibleSolutions)
                .times(100.0)
        }

        started = true

        /**
         * For this example, the simulation is considered "over" when the pool reaches a stable equilibrium
         * that involves having a 1.0 average score against itself, which happens to coincide with a 1.0
         * average against alwaysCooperates as well as against titForTat. That is the "goal" of the
         * experiment: it reaches a stable equilibrium that is also competitive.
         */
        fun simulationOver(): Boolean {
            return averageScoreWithinPool == 1.0
        }

        /**
         * Runs the whole ClassifierPool against a given kind of bot and returns the average score.
         * Can be instructed to countTowardsWins for individual Classifiers or not. Can also be instructed to
         * compete within the generation only.
         *
         * botType is a string. The acceptable kinds of bots can be found in PrisonersDilemmaPlayer.kt. "random"
         * will generate wholly random players. "withinPool" will pick opponents randomly from within the
         * ClassifierPool.
         */
        suspend fun playGames(
            botType: String,
            countsTowardsWins: Boolean,
        ): Pair<Double, Double> { // TODO: Maybe a class instead of this tuple?

            // Play the games, tracking the scores:
            var scoreSum = 0.0
            var numWins = 0.0
            classifierPool.pool.forEach { player ->
                coroutineHandler.addJob(coroutineScope.launch {
                    PrisonersDilemmaGame(
                        roundsToPlay = numRoundsRange.random(),
                        playerA = player as PrisonersDilemmaPlayer,
                        playerB = when (botType) {
                            "withinPool" -> classifierPool.pool.random()
                            "random" -> PrisonersDilemmaPlayer()
                            "alwaysDefects" -> prisonersDilemmaBots["alwaysDefects"]
                            "alwaysCooperates" -> prisonersDilemmaBots["alwaysCooperates"]
                            "titForTat" -> prisonersDilemmaBots["titForTat"]
                            else -> error("Invalid botType: $botType")
                        } as PrisonersDilemmaPlayer,
                        interactiveMode = false,
                        countsTowardsWins = countsTowardsWins
                    ).play().let { gameResult ->
                        scoreSum += gameResult.playerTotalScore
                        if (gameResult.win)
                            numWins += 1 //todo: verify ++ is the same for Doubles?
                    }
                })
            }

            // Wait for all coroutines to complete:
            coroutineHandler.joinAndClearActiveJobs()

            // Collect the result in the form of Pair<averageScore, winPercent>:
            return Pair(scoreSum / genePoolSize, numWins / genePoolSize * 100.0)
        }

        // Run the experiment for a number of generations:
        for (generationNumber in 0 until defaultGenerationLimit) {
            currentGeneration = generationNumber

            // Members of generation play each other for the wins which count:
            playGames(
                botType = "withinPool",
                countsTowardsWins = true
            ).let { averageScoreWithinPool = it.first }

            // Wait for all coroutines to complete:
            coroutineHandler.joinAndClearActiveJobs() // <-- May be able to delete this line. todo

            // Produce a new generation using the average within the pool as the selective pressure:
            val nextGeneration = classifierPool.nextGeneration() // todo: this

            /*
                demoMode is where the expensive fluff lives. These metrics are useful but they slow things down.
                Turning off demoMode can demonstrate how fast genetic algorithms can be.
             */
            if (demoMode) {
                // Collect test data for the generation:
                prisonersDilemmaBots.keys.forEach { botType ->
                    playGames(
                        botType = botType,
                        countsTowardsWins = false
                    ).let { pair ->
                        when (botType) {
                            // Add average score against the type of bot:
                            "alwaysDefects" -> {
                                averageScoreAgainstAlwaysDefects = pair.first
                                winPercentAgainstAlwaysDefects = pair.second
                            }
                            "alwaysCooperates" -> {
                                averageScoreAgainstAlwaysCooperates = pair.first
                                winPercentAgainstAlwaysCooperates = pair.second
                            }
                            "titForTat" -> {
                                averageScoreAgainstTitForTat = pair.first
                                winPercentAgainstTitForTat = pair.second
                            }
                        }
                    }
                }

                // Test against pure random bots:
                playGames(
                    botType = "random",
                    countsTowardsWins = false
                ).let { pair ->
                    averageScoreAgainstRandom = pair.first
                    winPercentAgainstRandom = pair.second
                }

                // Observe current genomes:
                observeGenomes()
            }

            // Set the new generation:
            classifierPool.pool = nextGeneration

            if (coroutineHandler.cancelled) {
                coroutineHandler.joinAndClearActiveJobs()
                delay(1000)
                break
            }
        }

        finished = true
    }
}