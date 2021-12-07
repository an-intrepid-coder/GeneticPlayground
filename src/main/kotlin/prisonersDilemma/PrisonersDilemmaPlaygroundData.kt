package prisonersDilemma

import core.LabeledText
import core.WavyBorder
import androidx.compose.runtime.Composable

/**
 * Displays all the data for the Prisoner's Dilemma experiment.
 */
@Composable
fun PrisonersDilemmaPlaygroundData(
    started: String,
    finished: String,
    generation: String,
    averageAge: String,
    numSpecies: String,
    eldest: String,
    eldestAge: String,
    averageScoreWithinPool: String,
    averageScoreAgainstAlwaysDefects: String,
    averageScoreAgainstAlwaysCooperates: String,
    averageScoreAgainstTitForTat: String,
    averageScoreAgainstRandom: String,
    percentSolutionsExplored: String,
    numSolutionsExplored: String,
) {
    LabeledText("Started", started)
    LabeledText("Finished", finished)
    LabeledText("# Solutions Explored", numSolutionsExplored)
    LabeledText("% Solutions Explored", percentSolutionsExplored)
    WavyBorder()
    LabeledText("Current Generation", generation)
    LabeledText("Average Age", averageAge)
    LabeledText("# of Distinct Genomes in Pool", numSpecies)
    WavyBorder()
    LabeledText("Eldest", eldest)
    LabeledText("Eldest Age", eldestAge)
    WavyBorder()
    LabeledText("Average Score Within Pool", averageScoreWithinPool)
    LabeledText("Average Score of Pool vs. alwaysDefects", averageScoreAgainstAlwaysDefects)
    LabeledText("Average Score of Pool vs. alwaysCooperates", averageScoreAgainstAlwaysCooperates)
    LabeledText("Average Score of Pool vs. titForTat", averageScoreAgainstTitForTat)
    LabeledText("Average Score of Pool vs. random", averageScoreAgainstRandom)
    WavyBorder()
}