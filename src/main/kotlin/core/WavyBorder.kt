package core

import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp

@Composable
fun WavyBorder() {
    Text("~-~-~-~-~-~-~-~-", color = White)
    Spacer(Modifier.height(10.dp))
}