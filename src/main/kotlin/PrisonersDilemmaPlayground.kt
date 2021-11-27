import kotlinx.coroutines.Job
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.launch

class PrisonersDilemmaPlayground {
    // TODO: Make an abstract class which this implements:

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
            .filter { it.score < runningAverageScore }
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
        val sortedByScore = genePool.sortedBy { it.score }
        for (survivor in sortedByScore) {
            if (newGeneration.size >= genePool.size) break
            newGeneration.add(survivor)
        }

        return newGeneration.shuffled()
    }

    /**
     * Each winner plays a given number of control games (of a given number of rounds each) against
     * totally randomly generated opponents. This is to more objectively measure the "winners" in order
     * to see how much of their prowess is objective and not the result of some equilibrium.
     */
    private suspend fun controlTestWinners(
        generation: List<PrisonersDilemmaPlayer>,
        numRounds: Int,
    ): Map<String, Double>  {
        val winners = generation
            .map { it.asBinaryString() }
            .toSet()
            .map { playerFromBitString(it) }

        coroutineScope {
            repeat(numControlGames) {

                val jobs = mutableListOf<Job>()

                winners.forEach { player ->
                    jobs.add(this.launch {
                        PrisonersDilemmaGame(
                            roundsToPlay = numRounds,
                            interactiveMode = false,
                            // The one being tested will always be player A:
                            playerA = player,
                            playerB = PrisonersDilemmaPlayer(),
                        ).play()
                    })
                }

                jobs.forEach { it.join() }
            }
        }

        return winners
            .map { it.asBinaryString() }
            .zip(winners.map { it.score })
            .toMap()
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
        numGenerations: Int = defaultGenerations,
        numRounds: Int = defaultPrisonersDilemmaRounds,
        genePoolSize: Int = defaultGenePoolSize,
        progressAlerts: ProgressAlertType? = null
    ) = coroutineScope {
        // Keep a running tally of generational metadata:
        val metadataList = mutableListOf<PrisonersDilemmaGenerationMetadata>()

        // Set up a gene pool:
        if (progressAlerts != null)
            println("Setting up initial gene pool...")

        var genePool = newGenePool(genePoolSize)

        // Run the tests over a number of generations:
        if (progressAlerts != null)
            println("Evolving...")

        repeat (numGenerations) { generationNumber ->
            if (progressAlerts != null)
                println("\t... beginning generation #$generationNumber ...")

            val jobs = mutableListOf<Job>()

            // Have the generation pair off and each play a game, collecting the average score:
            genePool
                .chunked(2)
                .forEach { pair ->
                    jobs.add(this.launch {
                        val playerA = pair.first()
                        val playerB = pair.last()
                        PrisonersDilemmaGame(
                            roundsToPlay = numRounds,
                            interactiveMode = false,
                            playerA = playerA,
                            playerB = playerB
                        ).play().let { gameResult ->
                            playerA.gameResults.add(gameResult)
                            playerB.gameResults.add(gameResult)
                        }
                     })
                }

            // Wait for all coroutines to complete:
            jobs.forEach { it.join() }

            // Add some metadata for the generation:
            val averageScoreForGeneration = averageScoreForGeneration(genePool)
            metadataList.add(PrisonersDilemmaGenerationMetadata(
                generationNumber = generationNumber,
                generationSize = genePoolSize,
                averageScore = averageScoreForGeneration,
                numFit = numFit(genePool, averageScoreForGeneration),
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
            genePool = nextGeneration(genePool, averageScoreForGeneration)
        }

        // add the final generation:
        metadataList.add(PrisonersDilemmaGenerationMetadata(
            generationNumber = numGenerations,
            generationSize = genePoolSize,
            averageScore = averageScoreForGeneration(genePool),
            numFit = numFit(genePool, averageScoreForGeneration(genePool)),
            championString = championString(genePool),
            roundsPerGame = numRounds,
            activeGenePercentages = activeGenePercentages(genePool),
            genomesFrequency = genomePercentages(genePool)
        ))

        if (progressAlerts != null)
            println("Evolution complete!")

        // Compare the winners against a control group:
        if (progressAlerts != null)
            println("Testing the winners against control group...")

        val controlTestResults = controlTestWinners(
            generation = genePool,
            numRounds = numRounds
        )

        // Print metadata (will use much more complex analysis later on, and should probably go in files):
        println("Metadata for each generation:")
        metadataList.forEach { metadata ->
            println(metadata.prettyPrint())
            println("Active Gene Percentages:")
            println(metadata.printActiveGenePercentages())
        }
        println("The frequency of all genomes which exist in the final generation:")
        println(metadataList.last().printGenomeFrequencies())
        println("The surviving genomes acquitted themselves thus against control groups:")
        controlTestResults.forEach { entry ->
            println("\tGenome: ${entry.key} | Avg. Score: ${entry.value}")
        }
    }
}