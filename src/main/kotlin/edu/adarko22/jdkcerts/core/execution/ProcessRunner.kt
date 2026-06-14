package edu.adarko22.jdkcerts.core.execution

/**
 * Defines the contract for executing native Operating System commands.
 *
 * This interface isolates the domain layer from the complexities of OS-level process management,
 * providing a clean, coroutine-friendly API for system interactions.
 *
 * **Implementation Contract:**
 * - **Thread Safety:** Implementations may be invoked concurrently by dozens of coroutines.
 * They must be fully thread-safe and free of race conditions.
 * - **Dispatcher Isolation:** Because executing native processes involves blocking I/O operations,
 * implementations are strictly responsible for shifting their internal workload to an appropriate
 * thread pool (e.g., `Dispatchers.IO`). Callers should not need to worry about blocking the main thread.
 * - **Resource Management:** Implementations should handle OS resource limits (like file descriptors
 * or thread exhaustion) safely, either through throttling or explicit error reporting.
 */
interface ProcessRunner {
    /**
     * Spawns and executes the specified system command.
     * * This is a suspending function. It will yield the caller's thread while the external
     * process runs, resuming only when the process exits, crashes, or is canceled.
     *
     * @param command A list containing the target executable and its arguments (e.g., `["ls", "-la"]`).
     * @param dryRun If `true`, the command must **not** be executed on the host system. Instead,
     * the implementation should return a simulated [ProcessResult] containing the command as a preview string.
     * @return A [ProcessResult] capturing the standard output (stdout), standard error (stderr),
     * and the integer exit code of the process.
     */
    suspend fun runCommand(
        command: List<String>,
        dryRun: Boolean,
    ): ProcessResult
}
