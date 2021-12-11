package core

/**
 * The Agent is primarily a list of Classifiers which each represent an adaptation to a rule-set. It is also
 * a mutable map of resources, represented as pairs of Strings and Ints (e.g. `Pair("food", 2)`). There are
 * functions to add / consume resources, as well as to combine the Agent (and its Classifiers, importantly) with
 * another Agent.
 */
class Agent(
    val agentName: String,
    val classifiers: List<Classifier>,
    private val resources: MutableMap<String, Int> = mutableMapOf()
) {
    /**
     * Clone's the Agent, but does not include its resources.
     */
    fun clone(): Agent {
        return Agent(agentName, classifiers)
    }

    /**
     * Returns the amount of a given resource that an Agent has.
     */
    fun numResources(resourceName: String): Int {
        return resources[resourceName] ?: 0
    }

    /**
     * Adds a given amount of a given resource to the Agent's
     * resources. Returns the amount of the given resource after the
     * operation.
     */
    fun addResources(resourceName: String, amount: Int): Int {
        return resources[resourceName].let { currentAmount ->
            resources[resourceName] = when (currentAmount) {
                null -> amount
                else -> currentAmount + amount
            }
            resources[resourceName]!!
        }
    }

    /**
     * Consumes a given amount of a given resource, if the Agent
     * has any (and enough). Returns the amount of the given resource
     * remaining after the operation, or null if there were
     * none of them in the MutableMap to begin with.
     */
    fun consumeResources(resourceName: String, amount: Int): Int? {
        return resources[resourceName]?.let { currentAmount ->
            if (currentAmount >= amount) {
                resources[resourceName] = currentAmount
                    .minus(amount)
                    .coerceAtLeast(0)
            }
            resources[resourceName]
        }
    }

    /**
     * Combines the Classifiers in each agent (assuming they have the same kinds of Classifiers)
     * and returns two new Agents each with recombinated child characteristics.
     */
    fun combine(other: Agent): List<Agent> {
        if (agentName != other.agentName)
            error("Mismatched Agents")

        // Take the List<Classifier> from each Agent, zip them up, and combine the resulting pairs:
        val recombinatedClassifiers = classifiers
            .zip(other.classifiers)
            .map { it.first.combine(it.second) }

        // Split the recombinated Classifiers up between the two offspring:
        return listOf(
            Agent(
                agentName = this.agentName,
                classifiers = recombinatedClassifiers
                    .map { it.first() }
            ),
            Agent(
                agentName = this.agentName,
                classifiers = recombinatedClassifiers
                    .map { it.last() }
            )
        )
    }

    /**
     * Attempts to apply the Classifier with ruleName, if it exists. Returns null if it does not exist,
     * otherwise it applies rule on the given dataBundle and returns the dataBundle returned by the Classifier's
     * ruleBehavior function. dataBundle should be anything that can handle some bundled data. The implementation
     * doesn't matter as long as it is compatible with ruleName's ruleBehavior.
     */
    fun applyRule(
        ruleName: String,
        dataBundle: Any,
    ): Any? {
        return classifiers
            .firstOrNull { it.ruleName == ruleName }
            ?.let { it.ruleBehavior(dataBundle, it) }
    }
}