package edu.adarko22.jdkcerts.core.jdk.usecase

import edu.adarko22.jdkcerts.core.execution.ProcessRunner
import edu.adarko22.jdkcerts.core.jdk.Jdk
import edu.adarko22.jdkcerts.core.jdk.KeytoolCommand
import edu.adarko22.jdkcerts.core.jdk.KeytoolCommandResult
import java.nio.file.Path
import kotlin.io.path.absolutePathString

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
     * @param keytoolCommand The keytool command to execute.
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
            val command = buildProcessRunnerCommand(keytoolCommand, jdk)
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

    /**
     * Builds the full process command for a given JDK, including keytool path
     * and resolved keystore arguments.
     */
    private fun buildProcessRunnerCommand(
        keytoolCommand: KeytoolCommand,
        jdk: Jdk,
    ): List<String> {
        val keytoolExecutable = jdk.keytoolPath.absolutePathString()
        val keystoreInfo = jdk.keystoreInfo

        val keystoreArgs =
            when {
                !keytoolCommand.resolveKeystore -> emptyList()
                keystoreInfo.cacertsShortcutEnabled -> listOf("-cacerts")
                else -> listOf("-keystore", keystoreInfo.keystorePath.absolutePathString())
            }

        return buildList {
            add(keytoolExecutable)
            addAll(keytoolCommand.args)
            addAll(keystoreArgs)
        }
    }
}
