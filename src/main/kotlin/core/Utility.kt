package core

import prisonersDilemma.interfaceDigits

/**
 * Makes sure an input string is no more than numDigits long, and at least numDigits long. If input string is
 * too small, then "0"s are added to the end.
 */
fun trimStringForInterface(
    string: String,
    numDigits: Int = interfaceDigits
): String {
    var trimmed = string
        .slice(0 until numDigits.coerceAtMost(string.length))

    while (trimmed.length < numDigits) {
        trimmed += "0"
    }

    return trimmed
}
