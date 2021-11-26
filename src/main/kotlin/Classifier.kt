/**
 * The "chromosome" of the Classifier system. It consists of a list of Characteristics and the means to "reproduce".
 * It can be sub-classed for more specific purposes.
 */
abstract class Classifier(
    val characteristics: List<Characteristic>,
    var averageScore: Double = 0.0,
) {
    abstract fun combine(other: Classifier): List<Classifier>
    // TODO: ^ This is better off as an open fun if possible. There is definitely a default way this should work.

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
 */
fun numAboveAverage(
    generation: List<Classifier>,
    averageScore: Double,
): Int {
    return generation
        .filter { it.averageScore > averageScore }
        .size
}

/**
 * Returns a string describing the most effective Classifier of the generation.
 */
fun championString(generation: List<Classifier>): String {
    val champion = generation.maxByOrNull { it.averageScore }!!
    val numOfChampion = generation.filter { it.asBinaryString() == champion.asBinaryString() }.size
    val percentGenePool = numOfChampion.toDouble() / generation.size * 100
    return "Champion: ${champion.asBinaryString()}" +
            "\n${champion.characteristics.map { it.prettyPrint() }}" +
            "\n\tScore: ${champion.averageScore}" +
            "\n\t% of Gene Pool: $percentGenePool"
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

// TODO: I can probably combine the following two into something cleaner:

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