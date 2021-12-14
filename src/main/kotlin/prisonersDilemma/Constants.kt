package prisonersDilemma

// The number of digits (including the decimal point) to be included in certain GUI elements:
const val interfaceDigits = 8

// The number of rounds per game of Iterated Prisoner's Dilemma is chosen from a random range:
val prisonersDilemmaRoundRange = 100..200

// The static size of the "gene pool" from which Classifiers are drawn from and reproduce back in to:
const val defaultGenePoolSize = 3000

// The threshold at which a PrisonersDilemmaPlayer will reproduce. This was chosen pretty arbitrarily; will refine it.
const val prisonersDilemmaReproductionThreshold = 30

/*
    The depth of the decision tree represents how many turns "back" each player will attempt to account for.
    The default of 3 is what was mentioned in John Holland's paper, but one could use more depth if they wanted.
    I have built the tree to be able to handle it. Currently, at depths greater than 3, it is much harder to get the
    pool to evolve past a certain win-rate. Deeper trees are a work in progress, as they will probably be necessary
    for other games.
 */
const val decisionTreeDepth = 3

/*
    Terminology for reward payoffs taken from Wikipedia. For simplicity's sake I've decided to use positive values
    instead of negative ones.
 */
const val rewardPayoff = 1
const val punishmentPayoff = 2
const val temptationPayoff = 0
const val suckersPayoff = 3