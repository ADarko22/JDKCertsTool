package edu.adarko22.jdkcerts.core.jdk.keytool.usecase

import edu.adarko22.jdkcerts.core.execution.ProcessRunner
import edu.adarko22.jdkcerts.core.jdk.DiscoverJdksUseCase
import edu.adarko22.jdkcerts.core.jdk.keytool.model.KeytoolCommand
import edu.adarko22.jdkcerts.core.jdk.keytool.model.KeytoolCommandResult
import java.nio.file.Path

/**
 * Use case for executing [KeytoolCommand] on all discovered JDKs.
 *
 * It discovers all JDKs (optionally including custom directories), builds
 * the full keytool command including keystore resolution, and executes it via a [ProcessRunner].
 *
 * @param discoverJdks Component responsible for finding and assembling [Jdk] instances.
 * @param processRunner Component responsible for running system commands.
 */
class ExecuteKeytoolCommandUseCase(
    private val discoverJdks: DiscoverJdksUseCase,
    private val processRunner: ProcessRunner,
) {
    /**
     * Executes the given keytool command across all discovered JDKs.
     *
     * @param keytoolCommand The keytool command to execute (responsible for building its own command line).
     * @param customJdkDirs Optional custom JDK directories to include in discovery.
     * @param dryRun If true, commands are not actually executed but simulated.
     * @return List of [KeytoolCommandResult] objects representing the outcome for each JDK.
     */
    fun execute(
        keytoolCommand: KeytoolCommand,
        customJdkDirs: List<Path>,
        dryRun: Boolean,
    ): List<KeytoolCommandResult> =
        discoverJdks.discover(customJdkDirs).map { jdk ->
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
