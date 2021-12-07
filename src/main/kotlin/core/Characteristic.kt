package core

/**
 * The "gene" of the Core.Classifier system. It has a name, an active/inactive boolean, and behavior which embodies the
 * rule/action system as described in Holland's paper.
 */
class Characteristic(
    val name: String,
    var active: Boolean = coinFlip(),
) {
    fun prettyPrint(): String {
        return "$name = $active"
    }

    fun asBitString(): String { return if (active) "1" else "0" }

    fun applyMutationFrequency(mutationFrequency: Int) {
        if (withChance(mutationFrequency, 1))
            active = !active
    }
}