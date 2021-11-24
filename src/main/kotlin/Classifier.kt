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
        var bitString = ""
        characteristics.forEach { characteristic ->
            bitString += when (characteristic.active) {
                true -> "1"
                else -> "0"
            }
        }
        return bitString
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
