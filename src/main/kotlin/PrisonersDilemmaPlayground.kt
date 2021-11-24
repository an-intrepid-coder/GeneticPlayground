import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

// TODO: Make an abstract class which this implements:

const val defaultGenerations = 200
const val defaultPrisonersDilemmaRounds = 100
const val defaultGenePoolSize = 10000

class PrisonersDilemmaPlayground {

    /**
     * While any game is still in progress, delays execution.
     */
    private suspend fun delayUntilReady(classifiers: List<PrisonersDilemmaPlayer>) {
        while (classifiers.any { it.gameResult == null }) {
            delay(100) // tentative
        }
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
        runningAverageScore: Double,
    ): List<PrisonersDilemmaPlayer> {
        val newGeneration = mutableListOf<PrisonersDilemmaPlayer>()

        // Above Average players reproduce:
        genePool
            .filter { it.averageScore > runningAverageScore }
            .chunked(2)
            .forEach { pair ->
                when (pair.size) {
                    1 -> newGeneration.add(pair.first())
                    2 -> pair.first().combine(pair.last()).forEach {
                        newGeneration.add(it as PrisonersDilemmaPlayer)
                    }
                }
            }

        // Fill in the remainder from the most fit of the previous generation:
        genePool.sortedByDescending { it.averageScore }
        for (index in 0 until (genePool.size - newGeneration.size)) {
            newGeneration.add(genePool[index])
        }

        return newGeneration.shuffled()
    }

    /**
     * Runs a single game of Iterated Prisoner's Dilemma over a given number of rounds between two players.
     */
    private fun simulateGame(
        // TODO: Make this more abstract
        playerA: PrisonersDilemmaPlayer,
        playerB: PrisonersDilemmaPlayer,
        numRounds: Int,
    ): Double {
        var combinedScore: Double?
        PrisonersDilemmaGame(
            roundsToPlay = numRounds,
            interactiveMode = false,
            playerA = playerA,
            playerB = playerB
        ).play().let { gameResult ->
            playerA.gameResult = gameResult
            playerB.gameResult = gameResult
            combinedScore = playerA.averageScore + playerB.averageScore
        }
        return combinedScore!!
    }

    /**
     * Runs the Prisoner's Dilemma experiment.
     */
    suspend fun runExperiment(
        // TODO: Make this more abstract
        coroutineScope: CoroutineScope,
        numGenerations: Int = defaultGenerations,
        numRounds: Int = defaultPrisonersDilemmaRounds,
        genePoolSize: Int = defaultGenePoolSize,
    ) {
        // Keep a running tally of generational metadata:
        val metadata = mutableListOf<PrisonersDilemmaGenerationMetadata>()

        // Set up a gene pool:
        println("Setting up initial gene pool...")
        var genePool = newGenePool(genePoolSize)

        // Run the tests over a number of generations:
        println("Evolving...")
        repeat (numGenerations) { generationNumber ->

            // The running average:
            var runningAverageScore = 0.0

            // Have the generation pair off and each play a game, collecting the average score:
            genePool
                .chunked(2)
                .forEach { pair ->
                    coroutineScope.launch {
                        runningAverageScore += simulateGame(
                            playerA = pair.first(),
                            playerB = pair.last(),
                            numRounds = numRounds
                        )
                    }
                }

            // Wait for all coroutines to complete:
            delayUntilReady(genePool)

            // Compute the final average score:
            runningAverageScore /= genePool.size

            // Add some metadata for the generation:
            metadata.add(PrisonersDilemmaGenerationMetadata(
                generationNumber = generationNumber,
                generationSize = genePoolSize,
                averageScore = runningAverageScore,
                numAboveAverageScore = genePool.asSequence()
                    .map { it.averageScore }
                    .filter { it > runningAverageScore }
                    .toList()
                    .size,
                roundsPerGame = numRounds,
                genePercentages = genePercentages(genePool)
            ))

            // Produce a new generation:
            genePool = nextGeneration(genePool, runningAverageScore)
        }

        // Print metadata (will use much more complex analysis later on):
        println("Evolution complete. Results: ")
        metadata.forEach { println(it.printGenePercentages()) }
    }
}