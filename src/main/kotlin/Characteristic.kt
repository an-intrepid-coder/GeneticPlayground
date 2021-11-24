/**
 * The "gene" of the Classifier system. It has a name, an active/inactive boolean, and behavior which embodies the
 * rule/action system as described in Holland's paper.
 *
 * The behavior variable is an anonymous function which can take any class as input and returns any class as output.
 * When sub-classed, the idea is to have an "Input Package" and an "Output Package" class which correspond with these,
 * and are tailored to the problem in question.
 */
class Characteristic(
    val name: String,
    var active: Boolean = withChance(1, 1),
) {
    fun asBitString(): String { return if (active) "1" else "0" }

    fun applyMutationFrequency(mutationFrequency: Int) {
        if (withChance(mutationFrequency, 1))
            active = !active
    }
}