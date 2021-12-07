package PrisonersDilemma

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue

/**
 * This class is a mutable record which holds information about the state of the simulation.
 * It is read by the Compose front-end.
 */
class PrisonersDilemmaPlaygroundMetadata {
    var currentGeneration by mutableStateOf(-1)
    var generationSize: Int by mutableStateOf(0)
    var currentPlaygroundPhase by mutableStateOf(PrisonersDilemmaPlaygroundPhase.SETUP)
    var poolEvolutionMode by mutableStateOf(PoolEvolutionMode.GRADUAL)
    var averageAge by mutableStateOf(0.0)
    var averageScoreWithinPool by mutableStateOf(0.0)
    var averageScoreAgainstAlwaysDefects by mutableStateOf(0.0)
    var averageScoreAgainstAlwaysCooperates by mutableStateOf(0.0)
    var averageScoreAgainstTitForTat by mutableStateOf(0.0)
    var averageScoreAgainstRandom by mutableStateOf(0.0)
    var currentEldest by mutableStateOf("No Champion Yet")
    var currentEldestAge by mutableStateOf(0)
    var numSpecies by mutableStateOf(0)
    var percentSolutionsExplored by mutableStateOf(0.0)
    var numSolutionsExplored by mutableStateOf(0)
}