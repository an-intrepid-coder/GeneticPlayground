import androidx.compose.ui.graphics.Color

/*
    The number of generations for the whole experiment. This is pretty arbitrary and you only need a few dozen to
    see how effective it is. It can be fun to see how far it will optimize over hundreds of generations, though.
 */
const val defaultGenerations = 1000

// The number of rounds per game of Iterated Prisoner's Dilemma is chosen from a random range:
val prisonersDilemmaRoundRange = 100..200

// The static size of the "gene pool" from which Classifiers are drawn from and reproduce back in to:
const val defaultGenePoolSize = 8000

/*
    The depth of the decision tree represents how many turns "back" each player will attempt to account for.
    The default of 3 is what was mentioned in John Holland's paper, but one could use more depth if they wanted.
    I have built the tree to be able to handle it. The biggest effect of doing so would be a need to increase the
    size of the "binary string" which represents the "genome". 3 turns back is already 84 different possibilities
    when you consider the first 3 turns as well as all turns after the first 3. That makes it an 84-bit search
    space, which is already enormous.
 */
const val decisionTreeDepth = 3

/*
    Holland's paper suggests a mutation frequency of 1/10,000, which will be the default. This is pretty
    arbitrary though.
 */
const val defaultMutationFrequency = 10000

/*
    Terminology for reward payoffs taken from Wikipedia. For simplicity's sake I've decided to use positive values
    instead of negative ones.
 */
const val rewardPayoff = 1
const val punishmentPayoff = 2
const val temptationPayoff = 0
const val suckersPayoff = 3

// The four possible outcomes of a round:
val possibleOutcomes = listOf(
    Pair(rewardPayoff, rewardPayoff),
    Pair(suckersPayoff, temptationPayoff),
    Pair(temptationPayoff, suckersPayoff),
    Pair(punishmentPayoff, punishmentPayoff)
)

// Some simple placeholder colors for the GUI.
val White = Color(255, 255, 255)
val Black = Color(0, 0, 0)
val BrightGreen = Color(0, 255, 0)
val BrightRed = Color(255, 0, 0)