/**
 * Returns a random rule set of Characteristics for a PrisonersDilemmaPlayer. This is how the initial "gene pool"
 * is drawn.
 */
fun randomPrisonersDilemmaCharacteristics(): List<Characteristic> {
    return listOf(
        Characteristic("alwaysDefects"),
        Characteristic("alwaysCooperates"),
    )
}

/**
 * Represents a rule set which attempts to play Prisoner's Dilemma. In theory, this should work.
 */
class PrisonersDilemmaPlayer(
    characteristics: List<Characteristic> = randomPrisonersDilemmaCharacteristics(),
    var playerLabel: PrisonersDilemmaPlayerLabel? = null,
    var gameResult: PrisonersDilemmaGameResult? = null,
    var averageScore: Double = 0.0,
) : Classifier(characteristics) {

    override fun combine(other: Classifier): List<Classifier> {
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
            PrisonersDilemmaPlayer(childACharacteristics),
            PrisonersDilemmaPlayer(childBCharacteristics),
        )
    }

    // todo: test
    fun chooseMove(game: PrisonersDilemmaGame): DilemmaChoice {
        /*
            For this version I'm going to make mutually exclusive genes cancel out. That's the fairest way.
            Future versions will use the game variable to handle the memory aspect.
         */
        val alwaysDefects = characteristics.any { it.active && it.name == "alwaysDefects" }
        val alwaysCooperates = characteristics.any { it.active && it.name == "alwaysCooperates" }
        return when (Pair(alwaysDefects, alwaysCooperates)) {
            Pair(true, false) -> DilemmaChoice.DEFECT
            Pair(false, true) -> DilemmaChoice.COOPERATE
            Pair(true, true) -> DilemmaChoice.values().random()
            Pair(false, false) -> DilemmaChoice.values().random()
            else -> error("This should never happen.")
        }
    }
}
