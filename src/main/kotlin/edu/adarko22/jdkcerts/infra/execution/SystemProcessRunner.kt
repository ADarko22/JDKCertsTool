package edu.adarko22.jdkcerts.infra.execution

import edu.adarko22.jdkcerts.core.execution.ProcessResult
import edu.adarko22.jdkcerts.core.execution.ProcessRunner
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.TimeoutCancellationException
import kotlinx.coroutines.async
import kotlinx.coroutines.withContext
import kotlinx.coroutines.withTimeout
import kotlin.coroutines.cancellation.CancellationException
import kotlin.time.Duration.Companion.milliseconds

/**
 * A [ProcessRunner] implementation that executes commands as native Operating System processes.
 *
 * This class is designed to handle OS-level process invocation safely within a highly
 * concurrent coroutine architecture. It provides built-in protections against common
 * process execution pitfalls:
 *
 * - **Resource Exhaustion:** Uses a constrained [Dispatchers.IO] pool to throttle the number
 * of concurrent OS processes, preventing CPU spikes and OS-level file descriptor limits.
 * - **Stream Deadlocks:** Concurrently drains both standard output (stdout) and standard error (stderr)
 * streams to prevent the OS from pausing the process due to full I/O buffers.
 * - **Zombie Processes & Hangs:** Enforces a strict execution timeout. If the coroutine is canceled
 * or times out, the underlying OS process is forcefully destroyed.
 *
 * @param maxConcurrentProcesses The maximum number of OS processes allowed to run simultaneously.
 * Defaults to the number of available CPU cores (minimum 2).
 * @param timeoutMillis The maximum duration (in milliseconds) a process is permitted to run
 * before being forcefully terminated. Defaults to 10 seconds.
 */
class SystemProcessRunner(
    maxConcurrentProcesses: Int = Runtime.getRuntime().availableProcessors().coerceAtLeast(2),
    private val timeoutMillis: Long = 10_000L,
) : ProcessRunner {
    // Create a dedicated dispatcher to bottleneck the number of active OS processes
    private val processDispatcher = Dispatchers.IO.limitedParallelism(maxConcurrentProcesses)

    /**
     * Spawns and executes the given system command.
     * * This function suspends until the process exits, times out, or the parent coroutine is canceled.
     *
     * @param command A list containing the executable and its arguments (e.g., `["java", "-version"]`).
     * @param dryRun If `true`, the command is not executed and a textual preview is returned instead.
     * @return A [ProcessResult] containing the output, error streams, and exit code.
     * If the process times out or fails, the exit code will be -1 and the error message
     * will be populated in [ProcessResult.stderr].
     */
    override suspend fun runCommand(
        command: List<String>,
        dryRun: Boolean,
    ): ProcessResult =
        withContext(processDispatcher) {
            if (dryRun) {
                return@withContext ProcessResult("", "", 0, "[Dry run] ${command.joinToString(" ")}")
            }

            var process: Process? = null

            try {
                withTimeout(timeoutMillis.milliseconds) {
                    process =
                        ProcessBuilder(command)
                            .redirectErrorStream(false)
                            .start()

                    // Read streams concurrently to prevent buffer deadlocks
                    val stdoutDeferred = async { process.inputStream.bufferedReader().readText() }
                    val stderrDeferred = async { process.errorStream.bufferedReader().readText() }

                    val stdout = stdoutDeferred.await()
                    val stderr = stderrDeferred.await()
                    val exitCode = process!!.waitFor()

                    ProcessResult(stdout.trim(), stderr.trim(), exitCode, "")
                }
            } catch (_: TimeoutCancellationException) {
                ProcessResult("", "Error: Command timed out after ${timeoutMillis}ms", -1, "")
            } catch (e: CancellationException) {
                // Propagate cancellation signal
                throw e
            } catch (e: Exception) {
                val errorMessage = "Error executing command: ${e.message ?: "Unknown Error"}"
                ProcessResult("", errorMessage, -1, "")
            } finally {
                // Ensure the OS process is aggressively killed if the coroutine exits early (via timeout, cancellation, or crash).
                process?.destroyForcibly()
            }
        }
}
