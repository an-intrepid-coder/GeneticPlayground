import kotlinx.coroutines.*
import kotlin.math.pow
import kotlin.system.exitProcess

enum class PrisonersDilemmaPlaygroundPhase {
    SETUP,
    EVOLVING,
    FINISHED,
}

/**
 * This is the simulation which brings the game and the players together and runs the experiment.
 * It holds the data in a way that Compose can read while it is running.
 *
 * TODO: Make an abstract class which this implements:
 */
class PrisonersDilemmaPlayground {
    var metadata =  PrisonersDilemmaPlaygroundMetadata()
    var activeJobs = mutableListOf<Job>()
    var cancelled = false

    /**
     * Pauses execution until all coroutines have joined and then clears the list of active jobs.
     */
    private suspend fun joinAndClearActiveJobs() {
        activeJobs.forEach { it.join() }
        activeJobs = mutableListOf()
    }

    /**
     * Cancels and exits the simulation. Note that this is the safest way to shut it down for now, as
     * simply closing the window will not kill all the threads in the GlobalScope. This is because I am still
     * learning about concurrency and need to implement a more robust solution in the near future.
     */
    fun cancelAndExit() {
        cancelled = true
        activeJobs.forEach { it.cancel() }
        exitProcess(0)
    }

    /**
     * Returns a new gene pool with random Characteristics for each Classifier.
     */
    private fun newGenePool(
        // TODO: Make this more abstract
        genePoolSize: Int,
    ): List<PrisonersDilemmaPlayer> {
        val genePool = mutableListOf<PrisonersDilemmaPlayer>()
        repeat (genePoolSize) {
            genePool.add(PrisonersDilemmaPlayer())
        }
        return genePool
    }

    /**
     * Returns a new generation containing the offspring of the most fit in the previous generation who replace
     * the least fit.
     */
    private fun nextGeneration(
        // TODO: Make this more abstract
        genePool: List<PrisonersDilemmaPlayer>,
        averageScore: Double,
    ): List<PrisonersDilemmaPlayer> {
        val newGeneration = mutableListOf<PrisonersDilemmaPlayer>()

        // Above Average players reproduce:
        genePool
            .filter { it.score < averageScore }
            .chunked(2)
            .forEach { pair ->
                when (pair.size) {
                    1 -> newGeneration.add(pair.first().emitSurvivor() as PrisonersDilemmaPlayer)
                    2 -> pair.first().combine(pair.last()).forEach { offspring ->
                        newGeneration.add(offspring as PrisonersDilemmaPlayer)
                    }
                }
            }

        // Fill in the remainder from the most fit of the previous generation:
        val sortedByScore = genePool.sortedBy { it.score }
        for (survivor in sortedByScore) {
            if (newGeneration.size >= genePool.size) break
            newGeneration.add(survivor.emitSurvivor() as PrisonersDilemmaPlayer)
        }

        return newGeneration.shuffled()
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
     * Note that there are other things which could be chosen for the selective pressure. Selecting against totally
     * random opponents each generation is an interesting one, for example. Also, this simulation was built to be very
     * demonstrative, and it could be sped up by removing some fluff which exists only to make it easier to
     * track numbers during runtime. However, as my goal was to demonstrate something from the paper, I am okay
     * with this tradeoff.
     */
    suspend fun runExperiment(
        // TODO: Make this more abstract
        numGenerations: Int = defaultGenerations,
        numRoundsRange: IntRange = prisonersDilemmaRoundRange,
        genePoolSize: Int = defaultGenePoolSize,
    ) = coroutineScope {

        // Set up a gene pool:
        var genePool = newGenePool(genePoolSize)

        activeJobs = mutableListOf()

        // Set up the set of all seen genomes:
        val seenGenomes = mutableSetOf<String>()

        /**
         * Tracks each observed permutation of the gene pool and reports on how many have been seen,
         * and what percentage of the total possible genomes this represents. Although this one would
         * probably not be included in a performance-conscious app, it helps to illustrate why the
         * genetic algorithm is so good. By the time optimal solutions have been found against titForTat and
         * alwaysCooperates, often *far* less than 1% of the total "search space" will have been explored. It is a
         * neat demonstration of how these algorithms work.
         */
        fun observeGenomes() {
            genePool.asSequence()
                .map { it.asBinaryString() }
                .filter { it !in seenGenomes }
                .forEach { seenGenomes.add(it) }

            metadata.numSolutionsExplored = seenGenomes.size

            val possibleSolutions = (2.0).pow(genePool.first().characteristics.size)

            metadata.percentSolutionsExplored = seenGenomes
                .size
                .toDouble()
                .div(possibleSolutions)
                .times(100.0)
        }

        // Run the tests over a number of generations:
        metadata.currentPlaygroundPhase = PrisonersDilemmaPlaygroundPhase.EVOLVING
        metadata.generationSize = genePoolSize

        for (generationNumber in 0..numGenerations) {
            metadata.currentGeneration = generationNumber

            // Pair off members of generation and have them play each other.
            genePool
                .chunked(2)
                .forEach { pair ->
                    if (pair.size != 2)
                        error("Players not paired off correctly.")

                    activeJobs.add(launch {
                        PrisonersDilemmaGame(
                            roundsToPlay = numRoundsRange.random(),
                            interactiveMode = false,
                            playerA = pair.first(),
                            playerB = pair.last(),
                        ).play()
                    })
                }

            // Wait for all coroutines to complete:
            joinAndClearActiveJobs()

            // Collect the average scores:
            metadata.averageScoreWithinPool = averageScoreForGeneration(genePool)

            // Wait for all coroutines to complete:
            joinAndClearActiveJobs()

            // Collect the average age:
            metadata.averageAge = genePool.sumOf { it.age }.toDouble() / genePool.size.toDouble()

            // Collect the eldest:
            val eldest = genePool.maxByOrNull { it.age }!!
            metadata.currentEldest = eldest.asBinaryString()
            metadata.currentEldestAge = eldest.age

            // Collect # of species:
            metadata.numSpecies = genomePercentages(genePool).size

            // Produce a new generation using the average within the pool as the selective pressure:
            val nextGeneration = nextGeneration(genePool, metadata.averageScoreWithinPool)

            // Collect test data for the generation: // TODO: This isn't quite there yet. Close though!
            prisonersDilemmaBots.forEach { bot ->
                genePool.forEach { player ->
                    activeJobs.add(launch {
                        PrisonersDilemmaGame(
                            roundsToPlay = numRoundsRange.random(),
                            interactiveMode = false,
                            playerA = player,
                            playerB = bot,
                        ).play()
                    })
                }

                // Wait for all coroutines to complete:
                joinAndClearActiveJobs()

                when (bot.botName) {
                    // Add average score against the type of bot:
                    "alwaysDefects" -> metadata.averageScoreAgainstAlwaysDefects = averageScoreForGeneration(genePool)
                    "alwaysCooperates" -> metadata.averageScoreAgainstAlwaysCooperates = averageScoreForGeneration(genePool)
                    "titForTat" -> metadata.averageScoreAgainstTitForTat = averageScoreForGeneration(genePool)
                }
            }

            // Test against pure random bots:
            genePool.forEach { player ->
                activeJobs.add(launch {
                    PrisonersDilemmaGame(
                        roundsToPlay = numRoundsRange.random(),
                        interactiveMode = false,
                        playerA = player,
                        playerB = PrisonersDilemmaPlayer(),
                    ).play()
                })
            }

            // Wait for all coroutines to complete:
            joinAndClearActiveJobs()

            // Add average score against pure random bots:
            metadata.averageScoreAgainstRandom = averageScoreForGeneration(genePool)

            // Observe current genomes:
            observeGenomes()

            // Set the new generation:
            genePool = nextGeneration

            if (cancelled) {
                joinAndClearActiveJobs()
                delay(1000)
                break
            }
        }

        metadata.currentPlaygroundPhase = PrisonersDilemmaPlaygroundPhase.FINISHED
    }
}