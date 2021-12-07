package PrisonersDilemma

import Core.Black
import Core.BrightGreen
import Core.BrightRed
import Core.CoroutineHandler
import Core.White
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyListScope
import androidx.compose.material.Button
import androidx.compose.material.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.launch

@Composable
fun LabeledText(
    label: String,
    value: String,
) {
    Text("$label: $value", color = BrightGreen)
    Spacer(Modifier.height(10.dp))
}

@Composable
fun WavyBorder() {
    Text("~-~-~-~-~-~-~-~-", color = White)
    Spacer(Modifier.height(10.dp))
}

/**
 * This Composable takes all the GUI strings and places them in the main LazyList. It is nothing fancy.
 * I will eventually write a more complex GUI for this program.
 */
fun prisonersDilemmaPlaygroundData(
    lazyListScope: LazyListScope,
    playgroundPhase: String,
    generation: String,
    averageAge: String,
    numSpecies: String,
    eldest: String,
    eldestAge: String,
    poolMode: String,
    averageScoreWithinPool: String,
    averageScoreAgainstAlwaysDefects: String,
    averageScoreAgainstAlwaysCooperates: String,
    averageScoreAgainstTitForTat: String,
    averageScoreAgainstRandom: String,
    percentSolutionsExplored: String,
    numSolutionsExplored: String,
) {
    lazyListScope.item {
        LabeledText("Current Phase", playgroundPhase)
        LabeledText("# Solutions Explored", numSolutionsExplored)
        LabeledText("% Solutions Explored", percentSolutionsExplored)
        WavyBorder()
        LabeledText("Current Generation", generation)
        LabeledText("Average Age", averageAge)
        LabeledText("# of Distinct Genomes in Pool", numSpecies)
        LabeledText("Pool Evolution Mode", poolMode)
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
}

@Composable
fun StartStopButton(
    coroutineHandler: CoroutineHandler,
    playground: PrisonersDilemmaPlayground
) {
    val coroutineScope = coroutineHandler.coroutineScope
    Button(
        onClick = {
            when (playground.metadata.currentPlaygroundPhase) {
                PrisonersDilemmaPlaygroundPhase.SETUP -> {
                    coroutineScope.launch {
                        /*
                            For optimized performance, turn off demoMode.
                            Note that this will disable many of the GUI elements.
                         */
                        playground.runExperiment(demoMode = true)
                    }
                }
                else -> {
                    coroutineScope.launch {
                        coroutineHandler.cancelAndExit()
                    }
                }
            }
        },
        content = {
            when (playground.metadata.currentPlaygroundPhase) {
                PrisonersDilemmaPlaygroundPhase.SETUP -> Text("Start", color = White)
                else -> Text("Cancel and Exit", color = BrightRed)
            }
        }
    )
}

/**
 * This app is very simple. The Playground is the back end and runs the simulation and does all the work,
 * while this functions acts as a bridge to the Compose front-end. I have kept it simple for now but I will
 * make this more complex in the future.
 */
@Composable
fun PrisonersDilemmaPlaygroundApp(
    coroutineHandler: CoroutineHandler,
) {
    val playground = remember { PrisonersDilemmaPlayground(coroutineHandler) }

    LazyColumn(
        modifier = Modifier
            .background(Black)
            .fillMaxSize(),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        item { StartStopButton(coroutineHandler, playground) }

        prisonersDilemmaPlaygroundData(
            lazyListScope = this,
            generation = playground.metadata.currentGeneration.toString(),
            averageAge = playground.metadata.averageAge.toString(),
            averageScoreWithinPool = playground.metadata.averageScoreWithinPool.toString(),
            averageScoreAgainstAlwaysDefects = playground.metadata.averageScoreAgainstAlwaysDefects.toString(),
            averageScoreAgainstAlwaysCooperates = playground.metadata.averageScoreAgainstAlwaysCooperates.toString(),
            averageScoreAgainstTitForTat = playground.metadata.averageScoreAgainstTitForTat.toString(),
            averageScoreAgainstRandom = playground.metadata.averageScoreAgainstRandom.toString(),
            eldest = playground.metadata.currentEldest,
            eldestAge = playground.metadata.currentEldestAge.toString(),
            poolMode = playground.metadata.poolEvolutionMode.toString(),
            numSpecies = playground.metadata.numSpecies.toString(),
            playgroundPhase = playground.metadata.currentPlaygroundPhase.toString(),
            percentSolutionsExplored = playground.metadata.percentSolutionsExplored.toString(),
            numSolutionsExplored = playground.metadata.numSolutionsExplored.toString(),
        )
    }
}
