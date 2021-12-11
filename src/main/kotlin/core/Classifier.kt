package core

/**
 * Classifiers represent an adaptation to a rule set. They have a ruleName, a set of Characteristics,
 * and some implementation-defined behavior which represents the reaction that happens when a valid rule
 * is applied to those characteristics. In the Prisoner's Dilemma example, the Classifier and its characteristics
 * represent a strategy for the game Prisoner's Dilemma, and ruleBehavior plays a game of Iterated Prisoner's Dilemma
 * according to that strategy. That's just one way to use this; I have left some of it open to interpretation.
 */
class Classifier(
    // The ruleName is used to determine whether an agent responds to a signal:
    val ruleName: String,
    // The characteristics are the individual "genes" which make up the rule set of the Classifier:
    val characteristics: List<Characteristic>,
    /*
        ruleBehavior should be a function that takes some kind of bundle of data and returns some kind of bundle of
        data, but I am leaving that implementation detail open to interpretation. What is important is that this
        is where the "effect" part of the Classifier should go. See the Prisoner's Dilemma package for a simple
        example. The Classifier parameter should be the calling Classifier itself.
     */
    val ruleBehavior: (Any, Classifier) -> Any,
) {
    /**
     * Combines two Classifiers, producing two new Classifiers, with a chance for mutation and with the use of
     * "crossover", as described in John Holland's paper.
     */
    fun combine(other: Classifier): List<Classifier> {
        if (ruleName != other.ruleName)
            error("Mismatched Classifiers")

        val childACharacteristics = mutableListOf<Characteristic>()
        val childBCharacteristics = mutableListOf<Characteristic>()

        // Pick random index:
        val crossoverIndex = characteristics.indices.random()

        // Swap all characteristics to the left of the index:
        repeat (crossoverIndex) { index ->
            childACharacteristics.add(other.characteristics[index])
            childBCharacteristics.add(characteristics[index])
        }

        // Leave characteristics to the right of the index:
        for (index in crossoverIndex until characteristics.size) {
            childACharacteristics.add(characteristics[index])
            childBCharacteristics.add(other.characteristics[index])
        }

        // Apply a mutation chance to each offspring characteristic:
        repeat (characteristics.size) { index ->
            childACharacteristics[index].applyMutationFrequency(defaultMutationFrequency)
            childBCharacteristics[index].applyMutationFrequency(defaultMutationFrequency)
        }

        return listOf(
            Classifier(ruleName, childACharacteristics, ruleBehavior),
            Classifier(ruleName, childBCharacteristics, ruleBehavior),
        )
    }

    /**
     * Returns whether the named "gene" is active.
     */
    fun hasActiveGene(geneName: String): Boolean {
        return characteristics.any { it.name == geneName && it.active }
    }
}
