package edu.adarko22.jdkcerts.core.jdk.keytool.usecase

import edu.adarko22.jdkcerts.core.execution.ProcessRunner
import edu.adarko22.jdkcerts.core.jdk.DiscoverJdksUseCase
import edu.adarko22.jdkcerts.core.jdk.keytool.model.KeytoolCommand
import edu.adarko22.jdkcerts.core.jdk.keytool.model.KeytoolCommandResult
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.coroutineScope
import java.nio.file.Path

/**
 * Use case for executing [KeytoolCommand] on all discovered JDKs.
 *
 * This use case leverages Kotlin Coroutines to fan-out external processes concurrently,
 * significantly reducing execution time when scanning multiple JDK installations.
 *
 * **Architectural Notes:**
 * - **Concurrency Delegation:** It relies entirely on the injected [ProcessRunner] to safely handle
 * OS-level process throttling and resource limits.
 * - **Non-Deterministic Ordering:** Because tasks run concurrently, the temporal execution order
 * is non-deterministic. However, the final list of results is reliably mapped back to the discovered JDKs.
 * - **Structured Concurrency:** Wrapped in a `coroutineScope`, ensuring that if the parent job
 * is canceled, all parallel OS process tasks are safely notified.
 *
 * @param discoverJdks Component responsible for finding and assembling [Jdk] instances.
 * @param processRunner Component responsible for running system commands safely.
 */
class ExecuteKeytoolCommandUseCase(
    private val discoverJdks: DiscoverJdksUseCase,
    private val processRunner: ProcessRunner,
) {
    /**
     * Executes the given keytool command against all discovered JDKs concurrently.
     *
     * Standard process failures (e.g., non-zero exit codes) are gracefully caught and
     * wrapped in a [KeytoolCommandResult.Failure] object. They do **not** throw exceptions
     * or cancel the concurrent execution of other JDKs.
     *
     * @param keytoolCommand The keytool command configuration (responsible for building its own arguments).
     * @param customJdkDirs Optional user-provided directories to include in the JDK discovery phase.
     * @param dryRun If `true`, the OS process is bypassed and the command returns a simulated preview.
     * @return List of [KeytoolCommandResult] objects, representing the isolated outcome for each JDK.
     */
    suspend fun execute(
        keytoolCommand: KeytoolCommand,
        customJdkDirs: List<Path>,
        dryRun: Boolean,
    ): List<KeytoolCommandResult> =
        coroutineScope {
            val jdks = discoverJdks.discover(customJdkDirs)

            val deferredResults =
                jdks.map { jdk ->
                    async {
                        val command = keytoolCommand.buildCommand(jdk)
                        val result = processRunner.runCommand(command, dryRun)

                        if (result.exitCode == 0) {
                            KeytoolCommandResult.Success(jdk, result)
                        } else {
                            KeytoolCommandResult.Failure(
                                jdk,
                                result,
                                "Keytool failed with exit code ${result.exitCode}: ${result.stderr}",
                            )
                        }
                    }
                }

            deferredResults.awaitAll()
        }
}
