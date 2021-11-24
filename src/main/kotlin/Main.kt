import kotlinx.coroutines.coroutineScope

/**
 * Runs a given number of generations, checking for fitness and reproducing the most fit every time.
 * Currently, only runs Prisoner's Dilemma.
 * Will do more eventually.
 *
 * todo: args & a front-end
 */
suspend fun main(args: Array<String>) = coroutineScope {
    PrisonersDilemmaPlayground().runExperiment(this)
}
