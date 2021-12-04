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
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch

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
    averageScoreWithinPool: String,
    averageScoreAgainstAlwaysDefects: String,
    averageScoreAgainstAlwaysCooperates: String,
    averageScoreAgainstTitForTat: String,
    averageScoreAgainstRandom: String,
    percentSolutionsExplored: String,
    numSolutionsExplored: String,
) {
    lazyListScope.item {
        Text("Current Phase: $playgroundPhase", color = BrightGreen)
        Spacer(Modifier.height(10.dp))
        Text("# Solutions Explored: $numSolutionsExplored", color = BrightGreen)
        Spacer(Modifier.height(10.dp))
        Text("% Solutions Explored: $percentSolutionsExplored", color = BrightGreen)
        Spacer(Modifier.height(10.dp))
        Text("~-~-~-~-~-~-~-~-", color = White)
        Spacer(Modifier.height(10.dp))
        Text("Current Generation: $generation", color = BrightGreen)
        Spacer(Modifier.height(10.dp))
        Text("Average Age: $averageAge", color = BrightGreen)
        Spacer(Modifier.height(10.dp))
        Text("# of Distinct Genomes in Pool: $numSpecies", color = BrightGreen)
        Spacer(Modifier.height(10.dp))
        Text("~-~-~-~-~-~-~-~-", color = White)
        Text("Eldest: $eldest", color = BrightGreen)
        Spacer(Modifier.height(10.dp))
        Text("Eldest Age: $eldestAge", color = BrightGreen)
        Spacer(Modifier.height(10.dp))
        Text("~-~-~-~-~-~-~-~-", color = White)
        Spacer(Modifier.height(10.dp))
        Spacer(Modifier.height(10.dp))
        Text("Average Score Within Pool: $averageScoreWithinPool", color = BrightGreen)
        Spacer(Modifier.height(10.dp))
        Text("Average Score of Pool vs. alwaysDefects: $averageScoreAgainstAlwaysDefects", color = BrightGreen)
        Spacer(Modifier.height(10.dp))
        Text("Average Score of Pool vs. alwaysCooperates: $averageScoreAgainstAlwaysCooperates", color = BrightGreen)
        Spacer(Modifier.height(10.dp))
        Text("Average Score of Pool vs. titForTat: $averageScoreAgainstTitForTat", color = BrightGreen)
        Spacer(Modifier.height(10.dp))
        Text("Average Score of Pool vs. random: $averageScoreAgainstRandom", color = BrightGreen)
        Spacer(Modifier.height(10.dp))
        Text("~-~-~-~-~-~-~-~-", color = White)
        Spacer(Modifier.height(10.dp))
    }
}

/**
 * This app is very simple. The Playground is the back end and runs the simulation and does all the work,
 * while this functions acts as a bridge to the Compose front-end. I have kept it simple for now but I will
 * make this more complex in the future.
 */
@Composable
fun PrisonersDilemmaPlaygroundApp(
    coroutineScope: CoroutineScope,
) {
    val playground = remember { PrisonersDilemmaPlayground() }

    LazyColumn(
        modifier = Modifier
            .background(Black)
            .fillMaxSize(),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        item {
            Button(
                onClick = {
                    when (playground.metadata.currentPlaygroundPhase) {
                        PrisonersDilemmaPlaygroundPhase.SETUP -> {
                            coroutineScope.launch {
                                playground.runExperiment()
                            }
                        }
                        else -> {
                            coroutineScope.launch {
                                playground.cancelAndExit()
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
            numSpecies = playground.metadata.numSpecies.toString(),
            playgroundPhase = playground.metadata.currentPlaygroundPhase.toString(),
            percentSolutionsExplored = playground.metadata.percentSolutionsExplored.toString(),
            numSolutionsExplored = playground.metadata.numSolutionsExplored.toString(),
        )
    }
}
