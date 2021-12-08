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
    averageScoreWithinPool: String,
    averageScoreAgainstAlwaysDefects: String,
    winPercentAgainstAlwaysDefects: String,
    averageScoreAgainstAlwaysCooperates: String,
    winPercentAgainstAlwaysCooperates: String,
    averageScoreAgainstTitForTat: String,
    winPercentAgainstTitForTat: String,
    averageScoreAgainstRandom: String,
    winPercentAgainstRandom: String,
    percentSolutionsExplored: String,
    numSolutionsExplored: String,
) {
    LabeledText("Started", started)
    LabeledText("Finished", finished)
    LabeledText("# Solutions Explored", numSolutionsExplored)
    LabeledText("% Solutions Explored", percentSolutionsExplored)
    WavyBorder()
    LabeledText("Current Generation", generation)
    LabeledText("Average Score Within Pool", averageScoreWithinPool)
    WavyBorder()
    LabeledText("Average Score of Pool vs. alwaysDefects", averageScoreAgainstAlwaysDefects)
    LabeledText("Win % vs. alwaysDefects", winPercentAgainstAlwaysDefects)
    WavyBorder()
    LabeledText("Average Score of Pool vs. alwaysCooperates", averageScoreAgainstAlwaysCooperates)
    LabeledText("Win % vs. alwaysCooperates", winPercentAgainstAlwaysCooperates)
    WavyBorder()
    LabeledText("Average Score of Pool vs. titForTat", averageScoreAgainstTitForTat)
    LabeledText("Win % vs. titForTat", winPercentAgainstTitForTat)
    WavyBorder()
    LabeledText("Average Score of Pool vs. random", averageScoreAgainstRandom)
    LabeledText("Win % vs. random", winPercentAgainstRandom)
    WavyBorder()
}