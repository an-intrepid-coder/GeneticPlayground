package core

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.material.Button
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.MutableState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp

@Composable
fun MainMenu(interfaceMode: MutableState<InterfaceMode>) {
    Column(
        modifier = Modifier
            .background(Black)
            .fillMaxSize(),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        Text("Welcome to the Genetic Playground", color = BrightGreen)
        Spacer(Modifier.height(30.dp))
        Button(
            onClick = {
                interfaceMode.value = InterfaceMode.PRISONERS_DILEMMA
            },
            content = {
                Text("Prisoner's Dilemma Playground")
            }
        )
    }
}