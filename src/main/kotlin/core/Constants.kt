package core

import androidx.compose.ui.graphics.Color

enum class InterfaceMode {
    MENU,
    PRISONERS_DILEMMA,
}

/*
    Holland's paper suggests a mutation frequency of 1/10,000, which will be the default. This is pretty
    arbitrary though.
 */
const val defaultMutationFrequency = 10000

// Some simple placeholder colors for the GUI.
val White = Color(255, 255, 255)
val Black = Color(0, 0, 0)
val BrightGreen = Color(0, 255, 0)
val BrightRed = Color(255, 0, 0)