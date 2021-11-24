// Holland's paper suggests a mutation frequency of 1/10,000, which will be the default.
const val defaultMutationFrequency = 10000

/**
 * Returns true with a frequency of chanceOf/outOf.
 */
fun withChance(outOf: Int, chanceOf: Int): Boolean {
    return (0..outOf).random() < chanceOf
}

