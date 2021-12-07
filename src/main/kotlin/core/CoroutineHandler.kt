package core

import kotlinx.coroutines.*
import kotlin.system.exitProcess

/**
 * This class is my attempt at a rudimentary coroutine handler. Will improve it as I learn more about how to do
 * coroutines right. What it does is hold all active jobs and some functions to shut everything down
 * gracefully if needed. Currently, this is just a home-rolled replacement for GlobalScope, and a temporary
 * measure until I learn more.
 */
@OptIn(ObsoleteCoroutinesApi::class)
class CoroutineHandler {
    val coroutineScope = CoroutineScope(newFixedThreadPoolContext(8, "appThreads"))
    var activeJobs = mutableListOf<Job>()
    var cancelled = false

    fun addJob(job: Job) {
        activeJobs.add(job)
    }

    /**
     * Pauses execution until all coroutines have joined and then clears the list of active jobs.
     */
    suspend fun joinAndClearActiveJobs() {
        activeJobs.forEach { it.join() }
        activeJobs = mutableListOf()
    }

    /**
     * This attempts to cancel all coroutines before exiting the application. It works, most of the time.
     * It could be better and I will improve it.
     */
    suspend fun cancel(exit: Boolean = false) {
        cancelled = true
        delay(1000)
        activeJobs.forEach { it.cancelAndJoin() }
        activeJobs = mutableListOf()
        if (exit)
            exitProcess(0)
    }
}