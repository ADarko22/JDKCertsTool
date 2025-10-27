package edu.adarko22.jdkcerts.clickt.jdk.keytool

import edu.adarko22.jdkcerts.jdk.Jdk
import edu.adarko22.jdkcerts.jdk.discovery.JdkDiscovery
import edu.adarko22.jdkcerts.jdk.impl.keytool.KeytoolCommand
import edu.adarko22.jdkcerts.output.CommandPrinter
import edu.adarko22.jdkcerts.output.blue
import edu.adarko22.jdkcerts.output.green
import edu.adarko22.jdkcerts.output.red
import edu.adarko22.jdkcerts.output.yellow
import edu.adarko22.jdkcerts.process.DefaultProcessRunner
import edu.adarko22.jdkcerts.process.ProcessRunner
import java.nio.file.Path
import kotlin.io.path.absolutePathString

class KeytoolCommandExecutor(
    private val jdkDiscovery: JdkDiscovery,
    private val processRunner: ProcessRunner = DefaultProcessRunner,
    private val output: CommandPrinter,
) {
    fun execute(
        keytoolCommand: KeytoolCommand,
        customJdkDirs: List<Path>,
        dryRun: Boolean,
    ) {
        var successes = 0
        var failures = 0

        val jdks = jdkDiscovery.discoverJDKs(customJdkDirs)
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
                !keytoolCommand.shouldResolveKeystore() -> emptyList()
                keystoreInfo.cacertsShortcutEnabled -> listOf("-cacerts")
                else -> listOf("-keystore", keystoreInfo.keystorePath.absolutePathString())
            }

        return buildList {
            add(keytoolExecutable)
            addAll(keytoolCommand.toArgsList())
            addAll(keystoreArgs)
        }
    }

    private fun buildSummary(
        successes: Int,
        failures: Int,
        total: Int,
    ) = ("\nSummary: ").blue() + ("$successes/$total succeeded").green() + if (failures > 0) (", $failures/$total failed.").red() else ""
}
