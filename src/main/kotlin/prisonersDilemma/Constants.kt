package prisonersDilemma

// The number of rounds per game of Iterated Prisoner's Dilemma is chosen from a random range:
val prisonersDilemmaRoundRange = 100..200

// The static size of the "gene pool" from which Classifiers are drawn from and reproduce back in to:
const val defaultGenePoolSize = 3000

// The threshold at which a PrisonersDilemmaPlayer will reproduce:
const val prisonersDilemmaPlayerReproductionThreshold = 30

/*
    Only a few dozen generations are needed to demonstrate what is happening, but if curious then one could let it run
    indefinitely. There are some data structures which might not be optimized for that, although if so then that is
    an oversight on my part and will be fixed eventually -- it should be safe to run it for a very long time. This
    default limit is just a precaution.
 */
const val defaultGenerationLimit = 100000

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