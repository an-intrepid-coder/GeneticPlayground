package PrisonersDilemma

import Core.CoroutineHandler
import Core.defaultGenePoolSize
import kotlinx.coroutines.*
import Core.prisonersDilemmaRoundRange
import kotlin.math.pow

enum class PrisonersDilemmaPlaygroundPhase {
    SETUP,
    EVOLVING,
    FINISHED,
}

enum class PoolEvolutionMode {
    GRADUAL,
    RAPID,
    STABLE
}

/**
 * This is the simulation which brings the game and the players together and runs the experiment.
 * It holds the data in a way that Compose can read while it is running.
 *
 * TODO: The next big task is to abstract this class out. runExperiment() is probably a bit of a run-on
 *  function, but that's okay in this case because of the demo-nature of it. I still need to make an
 *  abstract Playground.kt class.
 */
class PrisonersDilemmaPlayground(val coroutineHandler: CoroutineHandler) {
    var metadata =  PrisonersDilemmaPlaygroundMetadata()

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
     * demoMode is off, most cycle-wasting actions are locked behind an if-statement.
     */
    suspend fun runExperiment(
        numRoundsRange: IntRange = prisonersDilemmaRoundRange,
        genePoolSize: Int = defaultGenePoolSize,
        demoMode: Boolean = false,
    ) {
        val coroutineScope = coroutineHandler.coroutineScope

        // Set up a gene pool:
        val classifierPool = PrisonersDilemmaPlayerPool(genePoolSize)

        /**
         * Gives a rough indication of how fast the pool is evolving based on the average age.
         * This is "demo fluff" and is only active when demoMode is enabled.
         */
        fun poolMode(): PoolEvolutionMode {
            val stableCutoff = 10.0
            val gradualCutoff = 0.005
            return if (metadata.averageAge > stableCutoff)
                PoolEvolutionMode.STABLE
            else if (metadata.averageAge > gradualCutoff)
                PoolEvolutionMode.GRADUAL
            else
                PoolEvolutionMode.RAPID
        }

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

            metadata.numSolutionsExplored = seenGenomes.size

            val possibleSolutions = (2.0).pow(classifierPool.pool.first().characteristics.size)

            metadata.percentSolutionsExplored = seenGenomes
                .size
                .toDouble()
                .div(possibleSolutions)
                .times(100.0)
        }

        // Set up the metadata:
        metadata.currentPlaygroundPhase = PrisonersDilemmaPlaygroundPhase.EVOLVING
        metadata.generationSize = genePoolSize

        /**
         * For this example, the simulation is considered "over" when the pool reaches a stable equilibrium
         * that involves having a 1.0 average score against itself, against alwaysCooperates, and against titForTat,
         * while doing much better than a random pool against alwaysDefects and purely random bots. At this point
         * the pool also stops evolving, because it has figured out a little niche where everybody "wins" every
         * generation (which also happens to contain optimal strategies against titForTat, for example).
         *
         * There are other criteria which could be used to end the simulation instead. The fact that this demonstration
         * is using the average score within the pool while paired off each generation leads to different
         * behavior than if, say, average score against a titForTat bot were used each generation.
         */
        fun simulationOver(): Boolean {
            return metadata.poolEvolutionMode == PoolEvolutionMode.STABLE || metadata.averageScoreWithinPool == 1.0
        }

        var generationNumber = 0
        while (!simulationOver()) {
            metadata.currentGeneration = generationNumber++

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
            metadata.averageScoreWithinPool = classifierPool.averageScore()

            // Wait for all coroutines to complete:
            coroutineHandler.joinAndClearActiveJobs()

            if (demoMode) {
                // Collect the average age:
                metadata.averageAge = classifierPool.pool.sumOf { it.age }.toDouble() / genePoolSize

                // Collect the eldest:
                val eldest = classifierPool.pool.maxByOrNull { it.age }!!
                metadata.currentEldest = eldest.asBinaryString()
                metadata.currentEldestAge = eldest.age

                // Pool evolution mode (how fast is it evolving?):
                metadata.poolEvolutionMode = poolMode()

                // Collect # of species:
                metadata.numSpecies = classifierPool.numDistinctClassifiers()
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
                        "alwaysDefects" -> metadata.averageScoreAgainstAlwaysDefects = classifierPool.averageScore()
                        "alwaysCooperates" -> metadata.averageScoreAgainstAlwaysCooperates = classifierPool.averageScore()
                        "titForTat" -> metadata.averageScoreAgainstTitForTat = classifierPool.averageScore()
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
                metadata.averageScoreAgainstRandom = classifierPool.averageScore()

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

        metadata.currentPlaygroundPhase = PrisonersDilemmaPlaygroundPhase.FINISHED
    }
}