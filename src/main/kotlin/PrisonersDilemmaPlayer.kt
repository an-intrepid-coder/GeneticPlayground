/**
 * Returns a random rule set of Characteristics for a PrisonersDilemmaPlayer. This is how the initial "gene pool"
 * is drawn.
 */
fun randomPrisonersDilemmaCharacteristics(): List<Characteristic> {
    return listOf(
        Characteristic("aggressive"),
        Characteristic("competitive"),
        Characteristic("contrarian"),
        Characteristic("copycat"),
        Characteristic("grateful"),
        Characteristic("kind"),
        Characteristic("memory"),
        Characteristic("opportunistic"),
        Characteristic("spontaneous"),
        Characteristic("teamPlayer"),
        Characteristic("vindictive"),
    )
    // ^^^ More to be sure, but this is a good start. ^^^
}

/**
 * Returns a new player with characteristics that match the given bit string.
 */
fun playerFromBitString(bitString: String): PrisonersDilemmaPlayer {
    fun indexActive(index: Int): Boolean {
        return bitString[index] == '1'
    }
    return PrisonersDilemmaPlayer(characteristics = listOf(
        Characteristic("aggressive", indexActive(0)),
        Characteristic("competitive", indexActive(1)),
        Characteristic("contrarian", indexActive(2)),
        Characteristic("copycat", indexActive(3)),
        Characteristic("grateful", indexActive(4)),
        Characteristic("kind", indexActive(5)),
        Characteristic("memory", indexActive(6)),
        Characteristic("opportunistic", indexActive(7)),
        Characteristic("spontaneous", indexActive(8)),
        Characteristic("teamPlayer", indexActive(9)),
        Characteristic("vindictive", indexActive(10)),
    ))
}

/**
 * Represents a rule set which attempts to play Prisoner's Dilemma. In theory, this should work.
 */
class PrisonersDilemmaPlayer(
    characteristics: List<Characteristic> = randomPrisonersDilemmaCharacteristics(),
    var playerLabel: PrisonersDilemmaPlayerLabel? = null,
    var gameResults: MutableList<PrisonersDilemmaGameResult> = mutableListOf(),
    var opponent: PrisonersDilemmaPlayer? = null
    // TODO: Track the "age" of the player, as not all Classifiers perish every generation, and that would be
    //  cool to know.
) : Classifier(characteristics) {

    /**
     * Following the recipe in John Holland's paper, this combines two Classifiers in to two offspring, using
     * a crossover technique and a chance of mutation.
     *
     * TODO: This should be in the super class, and I intend to put it there at some point.
     */
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

    /**
     * Default memory size is 3, while those with the memory characteristic have 15.
     */
    private fun turnMemory(game: PrisonersDilemmaGame): List<PrisonersDilemmaRoundResult> {
        val memorySize = defaultMemorySize + if (hasActiveGene("memory")) memoryModifier else 0
        val sliceRangeStart = (game.previousRounds.size - memorySize).coerceAtLeast(0)
        return game.previousRounds.slice(sliceRangeStart until game.previousRounds.size)
    }

    /**
     * Parses a round result from the simulation in order to examine who did what.
     */
    fun previousChoice(
        roundResult: PrisonersDilemmaRoundResult,
    ): DilemmaChoice {
        return when (playerLabel) {
            PrisonersDilemmaPlayerLabel.PLAYER_A -> roundResult.playerAChoice
            else -> roundResult.playerBChoice
        }
    }

    /**
     * There is a base 50/50 chance to DEFECT or COOPERATE, and through individual Characteristics and interactive
     * combinations of them, this chance is raised or lowered. At the end it is a roll of the dice based on these
     * interactions.
     */
    fun chooseMove(game: PrisonersDilemmaGame): DilemmaChoice {
        // Base chance to defect is 50/50:
        var defectChance = defaultDefectChance

        // If aggressive, then is more likely to defect.
        if (hasActiveGene("aggressive"))
            defectChance += aggressionModifier

        // If kind, then is less likely to defect.
        if (hasActiveGene("kindness"))
            defectChance += kindnessModifier

        // Set the memory:
        val turnMemory = turnMemory(game)

        // Vindictive players are more likely to defect for every time in recent memory that the other has defected.
        if (hasActiveGene("vindictive")) {
            turnMemory.forEach { roundResult ->
                if (opponent!!.previousChoice(roundResult) == DilemmaChoice.DEFECT)
                    defectChance += vindictiveModifier
            }
        }

        // Grateful players are more likely to cooperate for every time the other has cooperated in recent memory.
        if (hasActiveGene("grateful")) {
            turnMemory.forEach { roundResult ->
                if (opponent!!.previousChoice(roundResult) == DilemmaChoice.COOPERATE)
                    defectChance += gratefulModifier
            }
        }

        // If the player has the "competitive" gene then they are more likely to defect if they are behind:
        if (hasActiveGene("competitive")) {
            if (score < opponent!!.score)
                defectChance += competitiveModifier
        }

        // If the player has the "spontaneous" gene then there is a chance to abandon the plan and choose randomly:
        if (hasActiveGene("spontaneous")) {
            if (withChance(diceMax, spontaneityChance)) {
                return when (coinFlip()) {
                    true -> DilemmaChoice.DEFECT
                    else -> DilemmaChoice.COOPERATE
                }
            }
        }

        // If the player has the "opportunistic" gene then it will always choose to defect if the player has
        // made a majority of COOPERATE moves in recent memory.
        if (hasActiveGene("opportunistic")) {
            val numTimesEnemyDefected = turnMemory.filter { roundResult ->
                opponent!!.previousChoice(roundResult) == DilemmaChoice.DEFECT
            }.size
            val numTimesEnemyCooperated = turnMemory.filter { roundResult ->
                opponent!!.previousChoice(roundResult) == DilemmaChoice.COOPERATE
            }.size
            if (numTimesEnemyCooperated > numTimesEnemyDefected)
                return DilemmaChoice.DEFECT
        }

        // If the player has the "copycat" gene then it is more likely to do whatever the player did on the
        // last round.
        if (hasActiveGene("copycat") && turnMemory.isNotEmpty()) {
            val lastRound = turnMemory.last()
            defectChance += when (opponent!!.previousChoice(lastRound)) {
                DilemmaChoice.DEFECT -> copycatModifier
                DilemmaChoice.COOPERATE -> -copycatModifier
            }
        }

        // If the player has the "teamPlayer" characteristic and the opponent has the "teamPlayer" characteristic
        //  then it is more likely to cooperate:
        if (hasActiveGene("teamPlayer") && opponent!!.hasActiveGene("teamPlayer"))
            defectChance += teamPlayerModifier

        // Bound the defectChance:
        defectChance = defectChance
            .coerceAtLeast(0)
            .coerceAtMost(diceMax)

        // If the player has the "contrarian" characteristic then the defectChance is sometimes inverted before rolling
        //  (making it something of a wildcard):
        if (hasActiveGene("contrarian") && withChance(diceMax, contrarianChance))
            defectChance = diceMax - defectChance

        // Roll the dice and make a choice:
        return when (withChance(diceMax, defectChance)) {
            true -> DilemmaChoice.DEFECT
            else -> DilemmaChoice.COOPERATE
        }
    }
}
