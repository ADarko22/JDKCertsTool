package edu.adarko22.jdkcerts.core.execution

/**
 * Interface for executing external processes with consistent error handling and result reporting.
 *
 * This abstraction allows for easy testing and different execution strategies
 * (e.g., synchronous vs asynchronous execution). It provides a clean separation
 * between process execution logic and the rest of the application.
 */
interface ProcessRunner {
    fun runCommand(
        command: List<String>,
        dryRun: Boolean,
    ): ProcessResult
}
