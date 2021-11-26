import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

// TODO: Make an abstract class which this implements:

class PrisonersDilemmaPlayground {
    // TODO: Run the winners against some control groups to see how good they really are.

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
                    2 -> pair.first().combine(pair.last()).forEach { offspring ->
                        newGeneration.add(offspring as PrisonersDilemmaPlayer)
                    }
                }
            }

        // Fill in the remainder from the most fit of the previous generation:
        val sortedByScore = genePool.toMutableList().let { generation ->
            quickSortClassifiersByDescendingAverage(generation, 0, generation.size - 1)
            generation
        }
        for (survivor in sortedByScore) {
            if (newGeneration.size >= genePool.size) break
            newGeneration.add(survivor)
        }

        // Shuffling the new generation before returning helps to avoid getting stuck at
        //  sub-optimal local maximums.
        return newGeneration.shuffled()
    }

    enum class ProgressAlertType {
        LIGHT,
        VERBOSE
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
        progressAlerts: ProgressAlertType? = null
    ) {
        // Keep a running tally of generational metadata:
        val metadataList = mutableListOf<PrisonersDilemmaGenerationMetadata>()

        // Set up a gene pool:
        println("Setting up initial gene pool...")
        var genePool = newGenePool(genePoolSize)

        // The running average:
        var runningAverageScore = 0.0

        // Run the tests over a number of generations:
        println("Evolving...")
        repeat (numGenerations) { generationNumber ->

            if (progressAlerts != null)
                println("\t... beginning generation #$generationNumber ...")

            // Have the generation pair off and each play a game, collecting the average score:
            genePool
                .chunked(2)
                .forEach { pair ->
                    coroutineScope.launch {
                        val playerA = pair.first()
                        val playerB = pair.last()
                        PrisonersDilemmaGame(
                            roundsToPlay = numRounds,
                            interactiveMode = false,
                            playerA = playerA,
                            playerB = playerB
                        ).play().let { gameResult ->
                            playerA.gameResult = gameResult
                            playerB.gameResult = gameResult
                            runningAverageScore += gameResult.playerAAverageScore + gameResult.playerBAverageScore
                        }
                    }
                }

            // Wait for all coroutines to complete:
            delayUntilReady(genePool)

            // Compute the final average score:
            runningAverageScore /= genePoolSize

            // Add some metadata for the generation:
            metadataList.add(PrisonersDilemmaGenerationMetadata(
                generationNumber = generationNumber,
                generationSize = genePoolSize,
                averageScore = runningAverageScore,
                numAboveAverageScore = numAboveAverage(genePool, runningAverageScore),
                championString = championString(genePool),
                roundsPerGame = numRounds,
                activeGenePercentages = activeGenePercentages(genePool),
                genomesFrequency = genomePercentages(genePool)
            ))

            if (progressAlerts == ProgressAlertType.VERBOSE) {
                val last = metadataList.last()
                println(last.prettyPrint())
                println(last.printActiveGenePercentages())
            }

            // Produce a new generation:
            genePool = nextGeneration(genePool, runningAverageScore)
        }

        // add the final generation:
        metadataList.add(PrisonersDilemmaGenerationMetadata(
            generationNumber = numGenerations,
            generationSize = genePoolSize,
            averageScore = runningAverageScore,
            numAboveAverageScore = numAboveAverage(genePool, runningAverageScore),
            championString = championString(genePool),
            roundsPerGame = numRounds,
            activeGenePercentages = activeGenePercentages(genePool),
            genomesFrequency = genomePercentages(genePool)
        ))

        // Print metadata (will use much more complex analysis later on, and should probably go in files):
        println("Evolution complete!")
        println("Metadata for each generation:")
        metadataList.forEach { metadata ->
            println(metadata.prettyPrint())
            println("Active Gene Percentages:")
            println(metadata.printActiveGenePercentages())
        }
        println("The frequency of all genomes which exist in the final generation:")
        println(metadataList.last().printGenomeFrequencies())
        println("The Grand Champion:")
        println(metadataList.last().championString)
    }
}