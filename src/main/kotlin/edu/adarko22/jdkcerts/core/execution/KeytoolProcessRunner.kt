package edu.adarko22.jdkcerts.core.execution

import edu.adarko22.jdkcerts.core.jdk.Jdk
import edu.adarko22.jdkcerts.core.jdk.keytool.model.KeytoolOperation
import edu.adarko22.jdkcerts.core.jdk.keytool.model.KeytoolOperationResult

/**
 * Executes a Keytool operation across multiple JDKs and returns per-JDK results.
 *
 * Implementations must respect `dryRun` (no OS processes) and represent failures using
 * [KeytoolOperationResult.Failure].
 */
interface KeytoolProcessRunner {
    /**
     * Executes the provided operation against the supplied JDKs concurrently.
     *
     * @param operation The keytool operation to execute (command or query).
     * @param jdks The JDK installations to target.
     * @param masterPassword The keystore password applied to each JDK's keystore.
     * @param dryRun If true, implementations must not start OS processes and should instead
     * return dry-run/process preview results.
     * @return A list of [KeytoolOperationResult], one per JDK, encapsulating success or failure.
     */
    suspend fun runConcurrently(
        operation: KeytoolOperation,
        jdks: List<Jdk>,
        masterPassword: String,
        dryRun: Boolean,
    ): List<KeytoolOperationResult>
}
