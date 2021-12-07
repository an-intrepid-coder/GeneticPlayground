package core

import androidx.compose.material.Button
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import kotlinx.coroutines.launch

@Composable
fun StartStopButton(
    coroutineHandler: CoroutineHandler,
    playground: Playground,
) {
    val coroutineScope = coroutineHandler.coroutineScope
    Button(
        onClick = {
            when (playground.started) {
                true -> {
                    coroutineScope.launch {
                        playground.reset()
                    }
                }
                else -> {
                    coroutineScope.launch {
                        playground.run()
                    }
                }
            }
        },
        content = {
            when (playground.started) {
                true -> Text("Stop/Reset", color = White)
                else -> Text("Start", color = White)
            }
        }
    )
}