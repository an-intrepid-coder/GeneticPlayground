/**
 * Represents the metadata for an entire generation of PrisonersDilemmaPlayers.
 */
data class PrisonersDilemmaGenerationMetadata(
    val generationNumber: Int,
    val generationSize: Int,
    val averageScore: Double,
    val numAboveAverageScore: Int,
    val roundsPerGame: Int,
    val activeGenePercentages: Map<String, Double>,
    val genomesFrequency: Map<String, Double>
) {
    fun printActiveGenePercentages(): String {
        var percentagesString = "Active Gene Percentages for Generation #$generationNumber:"
        activeGenePercentages.forEach { entry ->
            percentagesString += "\n\t${entry.key}: ${entry.value}%"
        }
        return percentagesString
    }

    fun printGenomeFrequencies(): String {
        var percentagesString = "Genome Frequencies for Generation #$generationNumber:"
        genomesFrequency.forEach { entry ->
            percentagesString += "\n\t${entry.key}: ${entry.value}%"
        }
        return percentagesString
    }
}