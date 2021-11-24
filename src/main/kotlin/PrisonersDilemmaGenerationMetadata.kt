/**
 * Represents the metadata for an entire generation of PrisonersDilemmaPlayers.
 */
data class PrisonersDilemmaGenerationMetadata(
    val generationNumber: Int,
    val generationSize: Int,
    val averageScore: Double,
    val numAboveAverageScore: Int,
    val roundsPerGame: Int,
    val genePercentages: Map<String, Double>,
) {
    fun printGenePercentages(): String {
        var genePercentagesString = "Gene Percentages for Generation #$generationNumber:"
        genePercentages.forEach { entry ->
            genePercentagesString += "\n\t${entry.key}: ${entry.value}%"
        }
        return genePercentagesString
    }
}