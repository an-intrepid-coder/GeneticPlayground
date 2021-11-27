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
 * Returns the number of Classifiers within a generation which are above the average score.
 *
 * Note that for now an above average performer is actually one with a below average score, since Prisoner's Dilemma
 * is like Golf in that the higher points are for worse outcomes. This means that when/if I generalize this I need
 * to come up with a more generic solution, probably.
 */
fun numFit(
    generation: List<Classifier>,
    averageScore: Double,
): Int {
    return generation
        .filter { it.score < averageScore }
        .size
}

/**
 * Returns a string describing the most effective Classifier of the generation.
 */
fun championString(generation: List<Classifier>): String {
    val champion = generation.minByOrNull { it.score }!!
    val numOfChampion = generation.filter { it.asBinaryString() == champion.asBinaryString() }.size
    val percentGenePool = numOfChampion.toDouble() / generation.size * 100
    return "Champion: ${champion.asBinaryString()}" +
            "\n${champion.characteristics.map { it.prettyPrint() }}" +
            "\n\tScore: ${champion.score}" +
            "\n\t% of Gene Pool: $percentGenePool"
}

fun averageScoreForGeneration(generation: List<Classifier>): Double {
    return generation.sumOf { it.score } / generation.size
}

/**
 * Takes a List<Classifier> and returns how many of them contain the active gene.
 */
fun numWithActiveGene(generation: List<Classifier>, geneName: String): Int {
    return generation.asSequence()
        .map { it.characteristics }
        .filter { it.any { it.name == geneName && it.active } }
        .toList()
        .size
}

// TODO: I can probably combine the following two into something cleaner. The groupBy function, probably.

fun activeGenePercentages(generation: List<Classifier>): Map<String, Double> {
    val percentages = mutableMapOf<String, Double>()
    generation.forEach { classifier ->
        classifier.characteristics
            .filter { it.active }
            .forEach { characteristic ->
                when (characteristic.name in percentages.keys) {
                    true -> percentages[characteristic.name] = percentages[characteristic.name]!! + 1.0
                    else -> percentages[characteristic.name] = 1.0
                }
            }
    }
    percentages.forEach { entry ->
        percentages[entry.key] = entry.value / generation.size.toDouble() * 100.0
    }
    return percentages.toSortedMap()
}

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