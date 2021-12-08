package prisonersDilemma

import core.Classifier
import core.ClassifierPool

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
     * Returns the next generation of Prisoner's Dilemma Players, using number of wins as the
     * criteria for selection.
     */
    override fun nextGeneration(): List<Classifier> {
        val nextGeneration = mutableListOf<PrisonersDilemmaPlayer>()

        // Collect a list of those fit enough to reproduce:
        val mostFit = pool.asSequence()
            .map { it as PrisonersDilemmaPlayer }
            .filter { it.wins > prisonersDilemmaPlayerReproductionThreshold }
            .toList()
            .shuffled()

        // The least fit, sorted in descending order by # of wins:
        val sortedRemainder = pool.asSequence()
            .map { it as PrisonersDilemmaPlayer }
            .filter { it.wins <= prisonersDilemmaPlayerReproductionThreshold }
            .sortedByDescending { it.wins }
            .toList()

        // Pair off mostFit and add their "children" to nextGeneration:
        mostFit
            .chunked(2)
            .forEach { pair ->
                /*
                    In the case of an odd-numbered mostFit with a single lone leftover, I have
                    chosen to have it clone itself.
                 */
                if (pair.size < 2)
                    nextGeneration.add(pair.first().emitSurvivor() as PrisonersDilemmaPlayer)
                else
                    pair.first()
                        .combine(pair.last())
                        .forEach { nextGeneration.add(it as PrisonersDilemmaPlayer) }
            }

        // Fill in the remainder using the most fit from the previous generation:
        var mostFitIndex = mostFit.indices.first
        var sortedRemainderIndex = sortedRemainder.indices.first
        while (nextGeneration.size < poolSize) {
            nextGeneration.add(
                if (mostFitIndex <= mostFit.indices.last)
                    mostFit[mostFitIndex++]
                else if (sortedRemainderIndex <= sortedRemainder.indices.last)
                    sortedRemainder[sortedRemainderIndex++]
                else
                    error("This should never happen")
            )
        }

        return nextGeneration
    }

    init {
        pool = newClassifierPool()
    }
}