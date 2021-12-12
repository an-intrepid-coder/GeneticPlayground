package core

import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.unit.dp

/**
 * A labeled percent-progress bar.
 */
@Composable
fun LabeledProgressBar(
    percentAsInt: Int,
    label: String,
    value: String,
) {
    fun barString(percentAsInt: Int): String {
        var barString = ""
        repeat (percentAsInt / 10) {
            barString += "=="
        }
        barString += ">"
        repeat (10 - percentAsInt / 10) {
            barString += "::"
        }
        return barString
    }

    Text(
        text = "$label: " + barString(percentAsInt) + "| " + trimStringForInterface(value),
        color = BrightGreen,
        fontFamily = FontFamily.Monospace
    )
    Spacer(Modifier.height(10.dp))
}