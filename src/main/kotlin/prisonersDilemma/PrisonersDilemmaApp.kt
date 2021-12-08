package prisonersDilemma

import core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier

/**
 * This app is very simple. The Playground is the back end and runs the simulation and does all the work,
 * while this functions acts as a bridge to the Compose front-end. I have kept it simple for now but I will
 * make this more complex in the future.
 */
@Composable
fun PrisonersDilemmaApp(
    coroutineHandler: CoroutineHandler,
) {
    val playground = remember { PrisonersDilemmaPlayground(coroutineHandler) }
    LazyColumn(
        modifier = Modifier
            .background(Black)
            .fillMaxSize(),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        item {
            StartStopButton(coroutineHandler, playground)
            PrisonersDilemmaPlaygroundData(
                started = playground.started.toString(),
                finished = playground.finished.toString(),
                generation = playground.currentGeneration.toString(),
                averageScoreWithinPool = playground.averageScoreWithinPool.toString(),
                averageScoreAgainstAlwaysDefects = playground.averageScoreAgainstAlwaysDefects.toString(),
                winPercentAgainstAlwaysDefects = playground.winPercentAgainstAlwaysDefects.toString(),
                averageScoreAgainstAlwaysCooperates = playground.averageScoreAgainstAlwaysCooperates.toString(),
                winPercentAgainstAlwaysCooperates = playground.winPercentAgainstAlwaysCooperates.toString(),
                averageScoreAgainstTitForTat = playground.averageScoreAgainstTitForTat.toString(),
                winPercentAgainstTitForTat = playground.winPercentAgainstTitForTat.toString(),
                averageScoreAgainstRandom = playground.averageScoreAgainstRandom.toString(),
                winPercentAgainstRandom = playground.winPercentAgainstRandom.toString(),
                percentSolutionsExplored = playground.percentSolutionsExplored.toString(),
                numSolutionsExplored = playground.numSolutionsExplored.toString(),
            )
        }
    }
}
