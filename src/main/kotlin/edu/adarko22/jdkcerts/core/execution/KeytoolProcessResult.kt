package edu.adarko22.jdkcerts.core.execution

import edu.adarko22.jdkcerts.core.jdk.Jdk

/**
 * Neutral, verdict-free outcome of running a keytool operation against a single JDK.
 *
 * This is the boundary type returned by the [KeytoolProcessRunner] port. It deliberately carries
 * **no** success/failure judgement and **no** infra type ([ProcessResult]): keytool exit codes are
 * ambiguous (a non-zero exit can mean a legitimate "alias does not exist" as well as a real error),
 * so interpretation is left to the core layer (a classifier + the CQRS use cases).
 */
sealed interface KeytoolProcessResult {
    val jdk: Jdk

    /**
     * The operation was actually executed against [jdk]; exposes the raw process signals for the
     * core layer to interpret.
     *
     * @property exitCode The process exit code (not yet interpreted as success/failure).
     * @property stdout Standard output captured from keytool.
     * @property stderr Standard error captured from keytool.
     */
    data class Executed(
        override val jdk: Jdk,
        val exitCode: Int,
        val stdout: String,
        val stderr: String,
    ) : KeytoolProcessResult

    /**
     * The operation was **not** executed because the run was a dry run.
     *
     * @property previewCommand The fully composed keytool command that would have been executed.
     */
    data class DryRun(
        override val jdk: Jdk,
        val previewCommand: String,
    ) : KeytoolProcessResult
}
