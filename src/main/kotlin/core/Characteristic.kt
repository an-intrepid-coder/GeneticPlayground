package core

/**
 * Characteristics are primarily a name and a boolean representing an on/off state. This is important for the
 * rest of the Genetic Algorithm. New Characteristics start randomly on/off except as a result of combination
 * between Classifiers.
 */
class Characteristic(
    val name: String,
    var active: Boolean = coinFlip(),
) {
    /**
     * During Classifier combination, there is a small chance that a Characteristic will "mutate" and result in
     * the opposite active state.
     */
    fun applyMutationFrequency(mutationFrequency: Int) {
        if (withChance(mutationFrequency, 1))
            active = !active
    }
}