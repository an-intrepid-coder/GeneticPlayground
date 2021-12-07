package core

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue

/**
 * The blueprint for a "Playground", which is a vague work space in which to do "something" with the Classifier
 * system and genetic algorithms.
 */
abstract class Playground(
    val coroutineHandler: CoroutineHandler,
) {
    var started = false
    var finished = false

    var currentGeneration by mutableStateOf(-1)

    abstract suspend fun run()

    abstract suspend fun reset()
}