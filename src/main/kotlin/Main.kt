import prisonersDilemma.PrisonersDilemmaInterface
import androidx.compose.material.MaterialTheme
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Window
import androidx.compose.ui.window.application
import androidx.compose.ui.window.rememberWindowState
import core.*
import kotlinx.coroutines.launch
import prisonersDilemma.PrisonersDilemmaPlayground

/**
 * Currently, runs the Prisoner's Dilemma demo/experiment. Will do more eventually.
 */
fun main(args: Array<String>) = application {
    val prisonersDilemmaPlayground = PrisonersDilemmaPlayground()
    val coroutineHandler = CoroutineHandler()
    Window(
        onCloseRequest = { coroutineHandler
            .coroutineScope
            .launch { coroutineHandler.cancel(exit = true) }
        },
        title = "Genetic Playground",
        state = rememberWindowState(width = 800.dp, height = 600.dp)
    ) {
        val interfaceMode = remember { mutableStateOf(InterfaceMode.MENU) }
        MaterialTheme {
            when (interfaceMode.value) {
                InterfaceMode.MENU -> MainMenu(interfaceMode)
                InterfaceMode.PRISONERS_DILEMMA -> PrisonersDilemmaInterface(prisonersDilemmaPlayground, coroutineHandler)
            }
        }
    }
}