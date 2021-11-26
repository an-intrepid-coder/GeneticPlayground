const val defaultGenerations = 200
const val defaultPrisonersDilemmaRounds = 100
const val defaultGenePoolSize = 10000

const val diceMax = 99
// TODO: ^ Increase this to a number with more digits, for more granularity.
const val defaultDefectChance = 50
const val aggressionModifier = 15
const val kindnessModifier = -15
const val defaultMemorySize = 3
const val memoryModifier = 12
const val vindictiveModifier = 3
const val gratefulModifier = -3
const val competitiveModifier = 25
const val copycatModifier = 15
const val teamPlayerModifier = -15
const val spontaneityChance = 10
const val contrarianChance = 15
// ^ all tentative numbers

// Holland's paper suggests a mutation frequency of 1/10,000, which will be the default.
const val defaultMutationFrequency = 10000

/*
    Terminology for reward payoffs taken from Wikipedia. For simplicity's sake I've decided to use positive values
    instead of negative ones.
 */
const val rewardPayoff = 1
const val punishmentPayoff = 2
const val temptationPayoff = 0
const val suckersPayoff = 3