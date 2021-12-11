package core

import androidx.compose.ui.graphics.Color

enum class InterfaceMode {
    MENU,
    PRISONERS_DILEMMA,
}

/*
    Holland's paper *Genetic Algorithms* suggests a mutation frequency of 1/10,000, which will be the default. This is
    pretty arbitrary though. In *Signals and Boundaries* a more vague number is given. It remains to do some
    testing to see how much different mutation frequencies can affect the results.
 */
const val defaultMutationFrequency = 10000

// Some simple placeholder colors for the GUI.
val White = Color(255, 255, 255)
val Black = Color(0, 0, 0)
val BrightGreen = Color(0, 255, 0)
val BrightRed = Color(255, 0, 0)