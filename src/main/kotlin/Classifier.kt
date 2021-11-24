/**
 * The "chromosome" of the Classifier system. It consists of a list of Characteristics and the means to "reproduce".
 * It can be sub-classed for more specific purposes.
 */
abstract class Classifier( // todo: test
    val characteristics: List<Characteristic>,
) {
    // todo: test
    abstract fun combine(other: Classifier): List<Classifier>

    // todo: test
    fun asBinaryString(): String {
        return characteristics
            .map { it.asBitString() }
            .toString()
    }
}

// todo: test
fun numWithActiveGene(generation: List<Classifier>, geneName: String): Int {
    return generation.asSequence()
        .map { it.characteristics }
        .filter { it.any { it.name == geneName && it.active } }
        .toList()
        .size
}

// todo: test
fun genePercentages(generation: List<Classifier>): Map<String, Double> { // oooh generics
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