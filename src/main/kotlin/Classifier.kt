/**
 * The "chromosome" of the Classifier system. It consists of a list of Characteristics and the means to "reproduce".
 * It can be sub-classed for more specific purposes.
 *
 * Currently, it is assuming that whatever it is classifying is going to use a relatively simple system of measuring
 * the average score over a number of trials, as is the case in Iterated Prisoner's Dilemma, and using the system
 * of fitness selection and reproduction described in John Holland's paper. Right now this is unlikely to change,
 * but as the system gets more advanced I may depart from this.
 *
 * TODO: I may implement a "Generation" class, as I have found a frequent need to write functions for List<Classifier>.
 */
abstract class Classifier(
    val characteristics: List<Characteristic>,
    var score: Double = 0.0,
) {
    var age = 0

    /**
     * Returns a copy of the classifier that is one generation older.
     */
    abstract fun emitSurvivor(): Classifier

    /**
     * Combines two Classifiers, producing two new Classifiers, with a chance for mutation and with the use of
     * "crossover", as described in John Holland's paper.
     *
     * TODO: Right now this is implemented in a sub-class, but I am going to move it up here eventually.
     */
    abstract fun combine(other: Classifier): List<Classifier>

    /**
     * Returns a binary string of all the possible characteristics (which should be in a consistent order among
     * all instances of the Classifier -- e.g. alphabetical) represented as 0s for Inactive and 1st for Active, as
     * suggested in John Holland's paper.
     */
    fun asBinaryString(): String {
        var bitString = ""
        characteristics.forEach { bitString += it.asBitString() }
        return bitString
    }

    /**
     * Returns how many instances of a given gene are active in a Classifier:
     */
    fun hasActiveGene(geneName: String): Boolean {
        return characteristics.any { it.name == geneName && it.active }
    }
}

/**
 * Returns the average score of a generation.
 */
fun averageScoreForGeneration(generation: List<Classifier>): Double {
    return generation.sumOf { it.score } / generation.size
}

/**
 * Returns the surviving gene pool as a set of bit strings and the % of the population that they make up.
 */
fun genomePercentages(generation: List<Classifier>): Map<String, Double> {
    val percentages = mutableMapOf<String, Double>()
    generation.forEach { classifier ->
        val asBitString = classifier.asBinaryString()
        when (asBitString in percentages.keys) {
            true -> percentages[asBitString] = percentages[asBitString]!! + 1.0
            else -> percentages[asBitString] = 1.0
        }
    }
    percentages.forEach { entry ->
        percentages[entry.key] = entry.value / generation.size.toDouble() * 100.0
    }
    return percentages.toSortedMap()
}