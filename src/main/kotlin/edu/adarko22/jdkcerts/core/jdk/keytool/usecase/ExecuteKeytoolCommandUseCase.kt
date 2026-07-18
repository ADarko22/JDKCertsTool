package edu.adarko22.jdkcerts.core.jdk.keytool.usecase

import edu.adarko22.jdkcerts.core.execution.KeytoolProcessRunner
import edu.adarko22.jdkcerts.core.jdk.DiscoverJdksUseCase
import edu.adarko22.jdkcerts.core.jdk.keytool.model.ExecutionContext
import edu.adarko22.jdkcerts.core.jdk.keytool.model.KeytoolCommand
import edu.adarko22.jdkcerts.core.jdk.keytool.model.KeytoolOperationResult

/**
 * Orchestrates executing keytool operations across discovered JDKs.
 *
 * Discovers JDKs via [jdkDiscoverJdksUseCase] and delegates execution to [KeytoolProcessRunner].
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
