import kotlinx.coroutines.coroutineScope

/**
 * Runs a given number of generations, checking for fitness and reproducing the most fit every time.
 * Currently, only runs Prisoner's Dilemma.
 * Will do more eventually.
 *
 * todo: args & a front-end w/ a nice progress screen that shows the evolution of data over time. I'll use
 *  Compose Multiplatform for that eventually, but in the interim (for at least the next few weeks), I will mostly
 *  be printing either to screen (as of this writing) or to files which can be fed into graph viz software (next up).
 */
suspend fun main(args: Array<String>) = coroutineScope {
    /*
        Note that the number of generations can have a very large effect. Initially, the gene pool will be
        very random and so obviously good strategies will prevail. However, as it gets in to several hundred
        generations and more, surprising and unexpected things begin to happen. Because they are competing
        against each other (and lack a real control group), strategies which become favorable can depend heavily
        on the quality (and nature) of the competition even more than on the rules of the game. This is especially
        interesting when the number of rounds is high (100+, so that two players can really get the most out of each
        other) and the gene pool size very large (10k+).
     */
    PrisonersDilemmaPlayground().runExperiment(
        numGenerations = defaultGenerations,
        numRounds = defaultPrisonersDilemmaRounds,
        genePoolSize = defaultGenePoolSize,
        progressAlerts = PrisonersDilemmaPlayground.ProgressAlertType.LIGHT,
    )
}
