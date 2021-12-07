import prisonersDilemma.PrisonersDilemmaApp
import androidx.compose.material.MaterialTheme
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Window
import androidx.compose.ui.window.application
import androidx.compose.ui.window.rememberWindowState
import core.*
import kotlinx.coroutines.*

/**
 * Currently, runs the Prisoner's Dilemma demo/experiment. Will do more eventually.
 */
@OptIn(ObsoleteCoroutinesApi::class)
fun main(args: Array<String>) = application {
    val coroutineHandler = CoroutineHandler()
    Window(
        onCloseRequest = { coroutineHandler.coroutineScope.launch { coroutineHandler.cancel(true) } },
        title = "Genetic Playground",
        state = rememberWindowState(width = 1200.dp, height = 700.dp)
    ) {
        val interfaceMode = remember { mutableStateOf(InterfaceMode.MENU) }
        MaterialTheme {
            when (interfaceMode.value) {
                InterfaceMode.MENU -> MainMenu(interfaceMode)
                InterfaceMode.PRISONERS_DILEMMA -> PrisonersDilemmaApp(coroutineHandler)
            }
        }
    }
}
