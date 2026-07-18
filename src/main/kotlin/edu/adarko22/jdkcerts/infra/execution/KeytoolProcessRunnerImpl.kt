package edu.adarko22.jdkcerts.infra.execution

import edu.adarko22.jdkcerts.core.execution.KeytoolProcessRunner
import edu.adarko22.jdkcerts.core.execution.ProcessRunner
import edu.adarko22.jdkcerts.core.jdk.Jdk
import edu.adarko22.jdkcerts.core.jdk.keytool.model.FindCertKeytoolQuery
import edu.adarko22.jdkcerts.core.jdk.keytool.model.InstallCertKeytoolCommand
import edu.adarko22.jdkcerts.core.jdk.keytool.model.KeytoolOperation
import edu.adarko22.jdkcerts.core.jdk.keytool.model.KeytoolOperationResult
import edu.adarko22.jdkcerts.core.jdk.keytool.model.RemoveCertKeytoolCommand
import edu.adarko22.jdkcerts.core.jdk.keytool.model.SearchStrategy
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.coroutineScope
import kotlin.io.path.absolutePathString

/**
 * Implementation of [KeytoolProcessRunner] which leverages Kotlin Coroutines
 * to fan-out external processes concurrently, when scanning multiple JDK installations.
 *
 * **Architectural Notes:**
 * - **Concurrency Delegation:** It relies entirely on the injected [ProcessRunner] to safely handle
 * OS-level process throttling and resource limits.
 * - **Non-Deterministic Ordering:** Because tasks run concurrently, the temporal execution order
 * is non-deterministic. However, the final list of results is reliably mapped back to the discovered JDKs.
 * - **Structured Concurrency:** Wrapped in a `coroutineScope`, ensuring that if the parent job
 * is canceled, all parallel OS process tasks are safely notified.
 *
 * @param processRunner Component responsible for running system commands safely.
 */
class KeytoolProcessRunnerImpl(
    private val processRunner: ProcessRunner,
) : KeytoolProcessRunner {
    /**
     * Executes the given keytool command against all discovered JDKs concurrently.
     *
     * Standard process failures (e.g., non-zero exit codes) are gracefully caught and
     * wrapped in a [KeytoolOperationResult.Failure] object. They do **not** throw exceptions
     * or cancel the concurrent execution of other JDKs.
     *
     * @param operation The keytool command configuration (responsible for building its own arguments).
     * @return List of [KeytoolOperationResult] objects, representing the isolated outcome for each JDK.
     */
    override suspend fun runConcurrently(
        operation: KeytoolOperation,
        jdks: List<Jdk>,
        masterPassword: String,
        dryRun: Boolean,
    ): List<KeytoolOperationResult> =
        coroutineScope {
            jdks
                .map { jdk ->
                    async {
                        val command = buildCommand(operation, jdk, masterPassword)
                        runCommand(command, dryRun, jdk)
                    }
                }.awaitAll()
        }

    private fun buildCommand(
        operation: KeytoolOperation,
        jdk: Jdk,
        masterPassword: String,
    ): List<String> {
        val info = jdk.keystoreInfo

        val keytoolCommandPath = listOf(jdk.keytoolPath.absolutePathString())

        val operationArgs =
            when (operation) {
                is InstallCertKeytoolCommand -> {
                    listOf("-importcert", "-noprompt", "-alias", operation.alias, "-file", operation.certificateAbsolutePath)
                }

                is RemoveCertKeytoolCommand -> {
                    listOf("-delete", "-alias", operation.alias)
                }

                is FindCertKeytoolQuery -> {
                    listOf("-list", "-v") +
                        if (operation.searchStrategy == SearchStrategy.EXACT_MATCH) listOf("-alias", operation.alias) else emptyList()
                }
            }

        val keystoreArgs =
            when {
                info.cacertsShortcutEnabled -> listOf("-cacerts")
                else -> listOf("-keystore", info.keystorePath.absolutePathString())
            }

        return keytoolCommandPath + operationArgs + keystoreArgs + listOf("-storepass", masterPassword)
    }

    private suspend fun runCommand(
        command: List<String>,
        dryRun: Boolean,
        jdk: Jdk,
    ): KeytoolOperationResult {
        val result = processRunner.runCommand(command, dryRun)

        return if (result.exitCode == 0) {
            KeytoolOperationResult.Success(jdk, result)
        } else {
            KeytoolOperationResult.Failure(
                jdk,
                result,
                "Keytool failed with exit code ${result.exitCode}: ${result.stderr}",
            )
        }
    }
}
