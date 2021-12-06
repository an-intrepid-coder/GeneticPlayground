/**
 * A "gene pool" of Prisoner's Dilemma Players.
 */
class PrisonersDilemmaPlayerPool(
    poolSize: Int
) : ClassifierPool(poolSize) {

    /**
     * Returns a new gene pool of Prisoner's Dilemma Players.
     */
    override fun newClassifierPool(): List<Classifier> {
        return mutableListOf<PrisonersDilemmaPlayer>().let { list ->
            repeat (poolSize) {
                list.add(PrisonersDilemmaPlayer())
            }
            list
        }
    }

    /**
     * Returns the next generation of Prisoner's Dilemma Players, using the lowest average score
     * as the criteria for selection.
     */
    override fun nextGeneration(): List<Classifier> {
        return mutableListOf<PrisonersDilemmaPlayer>().let { newGeneration ->
            // Above Average players reproduce:
            val averageScore = averageScore()
            pool
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
            val sortedByScore = pool.sortedBy { it.score }
            for (survivor in sortedByScore) {
                if (newGeneration.size >= pool.size) break
                newGeneration.add(survivor.emitSurvivor() as PrisonersDilemmaPlayer)
            }

            newGeneration
        }
    }

    init {
        pool = newClassifierPool()
    }
}