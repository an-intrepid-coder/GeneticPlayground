package prisonersDilemma

import core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material.Button
import androidx.compose.material.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import kotlinx.coroutines.*

/**
 * This app is very simple. The Playground is the back end and runs the simulation and does all the work,
 * while this functions displays what is happening. I have kept it simple for now, but I will
 * make this more complex in the future.
 */
@OptIn(ObsoleteCoroutinesApi::class)
@Composable
fun PrisonersDilemmaInterface(
    playground: PrisonersDilemmaPlayground,
    coroutineHandler: CoroutineHandler
) {
    LazyColumn(
        modifier = Modifier
            .background(Black)
            .fillMaxSize(),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.SpaceEvenly
    ) {
        item {
            Button(
                onClick = {
                    val coroutineScope = coroutineHandler.coroutineScope
                    if (!playground.started)
                        coroutineScope.launch {
                            playground.runSimulation(coroutineHandler)
                        }
                },
                content = {
                    when (playground.started) {
                        true -> when (playground.finished) {
                            true -> Text("Finished!")
                            else -> Text("...running...")
                        }
                        else -> Text("Start")
                    }
                }
            )
            WavyBorder()
            LabeledText("Time Step", playground.timeStep.toString())
            WavyBorder()
            LabeledText("Oldest Survivor", playground.mostSurvivorPoints.toString())
            LabeledText("Most Times Reproduced", playground.mostReproductionPoints.toString())
            LabeledText("# Reproduced This Step", playground.numReproduced.toString())
            WavyBorder()
            LabeledText(
                label = "Avg Score vs. Random",
                value = playground
                    .averageScoreAgainstRandom
                    .toString()
                    .let { trimStringForInterface(it) }
            )
            LabeledProgressBar(
                percentAsInt = playground.averageWinPercentAgainstRandom.toInt(),
                label = "Win % vs. Random",
                value = playground.averageWinPercentAgainstRandom.toString(),
            )
        }
    }
}