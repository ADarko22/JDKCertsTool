package edu.adarko22.jdkcerts.cli.command.jdk.keytool

import edu.adarko22.jdkcerts.cli.output.ToolOutputPrinter
import edu.adarko22.jdkcerts.cli.output.blue
import edu.adarko22.jdkcerts.cli.output.green
import edu.adarko22.jdkcerts.cli.output.red
import edu.adarko22.jdkcerts.cli.output.yellow
import edu.adarko22.jdkcerts.core.jdk.Jdk
import edu.adarko22.jdkcerts.core.jdk.usecase.DiscoverJdksUseCase
import edu.adarko22.jdkcerts.core.process.ProcessRunner
import java.nio.file.Path
import kotlin.io.path.absolutePathString

class KeytoolCommandExecutor(
    private val discoverJdks: DiscoverJdksUseCase,
    private val processRunner: ProcessRunner,
    private val output: ToolOutputPrinter,
) {
    fun execute(
        keytoolCommand: KeytoolCommand,
        customJdkDirs: List<Path>,
        dryRun: Boolean,
    ) {
        var successes = 0
        var failures = 0

        val jdks = discoverJdks.discover(customJdkDirs)
        for (jdk in jdks) {
            output.print("Running on ${jdk.toString().blue()} ...")

            val command = buildProcessRunnerCommand(keytoolCommand, jdk)
            val result = processRunner.runCommand(command, dryRun)

            if (dryRun) {
                output.print("\t${result.dryRunOutput}".yellow())
                successes++
            } else if (result.exitCode == 0) {
                output.print("\t${result.stdout}".green())
                successes++
            } else {
                output.print("\t${result.stderr}".red())
                failures++
            }
        }

        output.print(buildSummary(successes, failures, jdks.size))
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

    private fun buildSummary(
        successes: Int,
        failures: Int,
        total: Int,
    ) = ("\nSummary: ").blue() + ("$successes/$total succeeded").green() + if (failures > 0) (", $failures/$total failed.").red() else ""
}
