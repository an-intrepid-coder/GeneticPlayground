package core

import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp

@Composable
fun LabeledText(
    label: String,
    value: String,
) {
    Text("$label: $value", color = BrightGreen)
    Spacer(Modifier.height(10.dp))
}

