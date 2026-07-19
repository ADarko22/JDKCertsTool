package edu.adarko22.jdkcerts.core.execution

import edu.adarko22.jdkcerts.core.jdk.Jdk
import edu.adarko22.jdkcerts.core.jdk.keytool.model.KeytoolOperation

/**
 * Executes a keytool operation across multiple JDKs and returns per-JDK, un-interpreted outcomes.
 *
 * Implementations must respect `dryRun` (no OS processes) by emitting
 * [KeytoolProcessResult.DryRun], and must not throw for non-zero exit codes — those are reported
 * as [KeytoolProcessResult.Executed] and interpreted by the core layer.
 */
interface KeytoolProcessRunner {
    /**
     * Executes the provided operation against the supplied JDKs concurrently.
     *
     * @param operation The keytool operation to execute (command or query).
     * @param jdks The JDK installations to target.
     * @param masterPassword The keystore password applied to each JDK's keystore.
     * @param dryRun If true, implementations must not start OS processes and should instead
     * return [KeytoolProcessResult.DryRun] previews.
     * @return A list of [KeytoolProcessResult], one per JDK.
     */
    suspend fun runConcurrently(
        operation: KeytoolOperation,
        jdks: List<Jdk>,
        masterPassword: String,
        dryRun: Boolean,
    ): List<KeytoolProcessResult>
}
