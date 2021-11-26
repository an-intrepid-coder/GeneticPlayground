/**
 * I found a need to implement Quick Sort for this project rather than use the built-in sortedByDescending function.
 * 
 * This implementation of Quick Sort is based on the Wikipedia Article:
 * https://en.wikipedia.org/wiki/Quicksort
 *
 * I have not bothered to make it too generic here, as it is only being used for this project where the nature of
 * the input is well known.
 *
 *  TODO: Make this generic enough to handle all Classifiers.
 */

/**
 * The quick sort function.
 */
fun quickSortClassifiersByDescendingAverage(
    list: MutableList<PrisonersDilemmaPlayer>,
    lowIndex: Int,
    highIndex: Int,
) {
    if (lowIndex >= 0 && highIndex >= 0 && lowIndex < highIndex) {
        val pivotIndex = partitionClassifiersByDescendingAverage(list, lowIndex, highIndex)
        quickSortClassifiersByDescendingAverage(list, lowIndex, pivotIndex)
        quickSortClassifiersByDescendingAverage(list, pivotIndex + 1, highIndex)
    }
}

/**
 * The partition function on which the quick sort function depends.
 */
fun partitionClassifiersByDescendingAverage(
    list: MutableList<PrisonersDilemmaPlayer>,
    lowIndex: Int,
    highIndex: Int
): Int {
    val pivotValue = list[(lowIndex + highIndex) / 2].averageScore
    var leftIndex = lowIndex - 1
    var rightIndex = highIndex + 1
    while (true) {
        do { leftIndex++ }
        while (list[leftIndex].averageScore > pivotValue)

        do { rightIndex-- }
        while (list[rightIndex].averageScore < pivotValue)

        if (leftIndex >= rightIndex)
            return rightIndex

        val temp = list[leftIndex]
        list[leftIndex] = list[rightIndex]
        list[rightIndex] = temp
    }
}