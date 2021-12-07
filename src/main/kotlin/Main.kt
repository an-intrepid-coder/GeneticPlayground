import Core.CoroutineHandler
import PrisonersDilemma.PrisonersDilemmaPlaygroundApp
import androidx.compose.material.MaterialTheme
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Window
import androidx.compose.ui.window.application
import androidx.compose.ui.window.rememberWindowState
import kotlinx.coroutines.*

/**
 * Currently, runs the Prisoner's Dilemma demo/experiment. Will do more eventually.
 *
 * TODO: Time to make this have a proper title screen, multiple options, etc etc.
 */
@OptIn(ObsoleteCoroutinesApi::class)
fun main(args: Array<String>) = application {
    val coroutineHandler = CoroutineHandler()
    Window(
        onCloseRequest = { coroutineHandler.coroutineScope.launch { coroutineHandler.cancelAndExit() } },
        title = "Genetic Playground",
        state = rememberWindowState(width = 1200.dp, height = 700.dp)
    ) {
        MaterialTheme {
            PrisonersDilemmaPlaygroundApp(coroutineHandler)
        }
    }
}