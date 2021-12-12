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
    I have built the tree to be able to handle it. The biggest effect of doing so would be a need to increase the
    size of the "binary string" which represents the "genome". 3 turns back is already 84 different possibilities
    when you consider the first 3 turns as well as all turns after the first 3. That makes it an 84-bit search
    space, which is already enormous. I am confident that there is still a better way to handle the decision tree,
    and I will experiment with more depth at some point.
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

// Resource reward values:
const val majorWinReward = 2
const val minorWinReward = 1

// The four possible outcomes of a round:
val possibleOutcomes = listOf(
    Pair(rewardPayoff, rewardPayoff),
    Pair(suckersPayoff, temptationPayoff),
    Pair(temptationPayoff, suckersPayoff),
    Pair(punishmentPayoff, punishmentPayoff)
)