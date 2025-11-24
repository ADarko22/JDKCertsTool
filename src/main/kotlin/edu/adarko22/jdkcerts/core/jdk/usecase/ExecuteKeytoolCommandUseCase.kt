package edu.adarko22.jdkcerts.core.jdk.usecase

import edu.adarko22.jdkcerts.core.execution.ProcessResult
import edu.adarko22.jdkcerts.core.execution.ProcessRunner
import edu.adarko22.jdkcerts.core.jdk.Jdk
import edu.adarko22.jdkcerts.core.jdk.KeytoolCommand
import java.nio.file.Path
import kotlin.io.path.absolutePathString

class ExecuteKeytoolCommandUseCase(
    private val discoverJdks: DiscoverJdksUseCase,
    private val processRunner: ProcessRunner,
) {
    fun execute(
        keytoolCommand: KeytoolCommand,
        customJdkDirs: List<Path>,
        dryRun: Boolean,
    ): List<ProcessResult> =
        discoverJdks
            .discover(customJdkDirs)
            .map {
                val command = buildProcessRunnerCommand(keytoolCommand, it)
                processRunner.runCommand(command, dryRun)
            }

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
