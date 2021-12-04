import androidx.compose.material.MaterialTheme
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Window
import androidx.compose.ui.window.application
import androidx.compose.ui.window.rememberWindowState
import kotlinx.coroutines.*

/**
 * Runs a given number of generations, checking for fitness and reproducing the most fit every time. Currently, only
 * runs Prisoner's Dilemma. Will do more eventually.
 */
@OptIn(DelicateCoroutinesApi::class)
fun main(args: Array<String>) = application {
    // It is not recommended to use Global Scope. I will look in to alternatives soon:
    val coroutineScope = GlobalScope

    Window(
        onCloseRequest = ::exitApplication,
        title = "Genetic Playground",
        state = rememberWindowState(width = 1200.dp, height = 700.dp)
    ) {
        MaterialTheme {
            PrisonersDilemmaPlaygroundApp(coroutineScope)
        }
    }
}