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
    var averageAge by mutableStateOf(0.0)
    var averageScoreWithinPool by mutableStateOf(0.0)
    var currentEldest by mutableStateOf("No Eldest Yet")
    var currentEldestAge by mutableStateOf(0)
    var averageScoreAgainstAlwaysDefects by mutableStateOf(0.0)
    var averageScoreAgainstAlwaysCooperates by mutableStateOf(0.0)
    var averageScoreAgainstTitForTat by mutableStateOf(0.0)
    var averageScoreAgainstRandom by mutableStateOf(0.0)
    var numSpecies by mutableStateOf(0)
    var percentSolutionsExplored by mutableStateOf(0.0)
    var numSolutionsExplored by mutableStateOf(0)

    /**
     * Resets the app.
     */
    override suspend fun reset() {
        coroutineHandler.cancel()
        averageAge = 0.0
        averageScoreWithinPool = 0.0
        currentEldest = "No Eldest Yet"
        currentEldestAge = 0
        averageScoreAgainstAlwaysDefects = 0.0
        averageScoreAgainstAlwaysCooperates = 0.0
        averageScoreAgainstTitForTat = 0.0
        averageScoreAgainstRandom = 0.0
        numSpecies = 0
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

        var generationNumber = 0
        while (!simulationOver()) {
            currentGeneration = generationNumber++

            // Pair off members of generation and have them play each other.
            classifierPool.pool
                .chunked(2)
                .forEach { pair ->
                    if (pair.size != 2)
                        error("Players not paired off correctly.")

                   coroutineHandler.addJob(coroutineScope.launch {
                        PrisonersDilemmaGame(
                            roundsToPlay = numRoundsRange.random(),
                            interactiveMode = false,
                            playerA = pair.first() as PrisonersDilemmaPlayer,
                            playerB = pair.last() as PrisonersDilemmaPlayer,
                        ).play()
                    })
                }

            // Wait for all coroutines to complete:
            coroutineHandler.joinAndClearActiveJobs()

            // Collect the average scores:
            averageScoreWithinPool = classifierPool.averageScore()

            // Wait for all coroutines to complete:
            coroutineHandler.joinAndClearActiveJobs()

            if (demoMode) {
                // Collect the average age:
                averageAge = classifierPool.pool.sumOf { it.age }.toDouble() / genePoolSize

                // Collect the eldest:
                val eldest = classifierPool.pool.maxByOrNull { it.age }!!
                currentEldest = eldest.asBinaryString()
                currentEldestAge = eldest.age

                // Collect # of species:
                numSpecies = classifierPool.numDistinctClassifiers()
            }

            // Produce a new generation using the average within the pool as the selective pressure:
            val nextGeneration = classifierPool.nextGeneration()

            if (demoMode) {
                // Collect test data for the generation:
                prisonersDilemmaBots.forEach { entry ->
                    classifierPool.pool.forEach { player ->
                        coroutineHandler.addJob(coroutineScope.launch {
                            PrisonersDilemmaGame(
                                roundsToPlay = numRoundsRange.random(),
                                interactiveMode = false,
                                playerA = player as PrisonersDilemmaPlayer,
                                playerB = entry.value,
                            ).play()
                        })
                    }

                    // Wait for all coroutines to complete:
                    coroutineHandler.joinAndClearActiveJobs()

                    when (entry.key) {
                        // Add average score against the type of bot:
                        "alwaysDefects" -> averageScoreAgainstAlwaysDefects = classifierPool.averageScore()
                        "alwaysCooperates" -> averageScoreAgainstAlwaysCooperates = classifierPool.averageScore()
                        "titForTat" -> averageScoreAgainstTitForTat = classifierPool.averageScore()
                    }
                }

                // Test against pure random bots:
                classifierPool.pool.forEach { player ->
                    coroutineHandler.addJob(coroutineScope.launch {
                        PrisonersDilemmaGame(
                            roundsToPlay = numRoundsRange.random(),
                            interactiveMode = false,
                            playerA = player as PrisonersDilemmaPlayer,
                            playerB = PrisonersDilemmaPlayer(),
                        ).play()
                    })
                }

                // Wait for all coroutines to complete:
                coroutineHandler.joinAndClearActiveJobs()

                // Add average score against pure random bots:
                averageScoreAgainstRandom = classifierPool.averageScore()

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