package edu.adarko22.jdkcerts.core.jdk.keytool.usecase

import edu.adarko22.jdkcerts.core.execution.KeytoolProcessRunner
import edu.adarko22.jdkcerts.core.execution.ProcessRunner
import edu.adarko22.jdkcerts.core.jdk.DiscoverJdksUseCase
import edu.adarko22.jdkcerts.core.jdk.keytool.model.ExecutionContext
import edu.adarko22.jdkcerts.core.jdk.keytool.model.KeytoolCommand
import edu.adarko22.jdkcerts.core.jdk.keytool.model.KeytoolOperationResult

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
 * @param keytoolProcessRunner Engine running KeytoolCommands.
 */
class ExecuteKeytoolCommandUseCase(
    val jdkDiscoverJdksUseCase: DiscoverJdksUseCase,
    private val keytoolProcessRunner: KeytoolProcessRunner,
) {
    /**
     * Executes the given keytool command against all discovered JDKs concurrently.
     *
     * Standard process failures (e.g., non-zero exit codes) are gracefully caught and
     * wrapped in a [KeytoolOperationResult.Failure] object. They do **not** throw exceptions
     * or cancel the concurrent execution of other JDKs.
     *
     * @param keytoolCommand The keytool command configuration (responsible for building its own arguments).
     * @param executionContext The context for executing the keytool command on the system.
     * @return List of [KeytoolOperationResult] objects, representing the isolated outcome for each JDK.
     */
    suspend fun execute(
        keytoolCommand: KeytoolCommand,
        executionContext: ExecutionContext,
    ): List<KeytoolOperationResult> {
        val jdks = jdkDiscoverJdksUseCase.discover(executionContext.customJdkDirs)
        return keytoolProcessRunner.runConcurrently(keytoolCommand, jdks, executionContext.masterPassword, executionContext.dryRun)
    }
}
