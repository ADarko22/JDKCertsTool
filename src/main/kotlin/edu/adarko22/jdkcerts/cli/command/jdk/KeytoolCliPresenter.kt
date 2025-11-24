package edu.adarko22.jdkcerts.cli.command.jdk

import edu.adarko22.jdkcerts.cli.output.ToolOutputPrinter
import edu.adarko22.jdkcerts.cli.output.blue
import edu.adarko22.jdkcerts.cli.output.green
import edu.adarko22.jdkcerts.cli.output.red
import edu.adarko22.jdkcerts.cli.output.yellow
import edu.adarko22.jdkcerts.core.jdk.Jdk
import edu.adarko22.jdkcerts.core.jdk.KeytoolCommand
import edu.adarko22.jdkcerts.core.jdk.usecase.ExecuteKeytoolCommandUseCase
import java.nio.file.Path
import kotlin.io.path.absolutePathString

class KeytoolCliPresenter(
    private val executeKeytoolCommandUseCase: ExecuteKeytoolCommandUseCase,
    private val output: ToolOutputPrinter,
) {
    fun present(
        keytoolCommand: KeytoolCommand,
        customJdkDirs: List<Path>,
        dryRun: Boolean,
    ) {
        var successes = 0
        var failures = 0
        executeKeytoolCommandUseCase
            .execute(keytoolCommand, customJdkDirs, dryRun)
            .forEach {
                if (dryRun) {
                    output.print("\t${it.dryRunOutput}".yellow())
                    successes++
                } else if (it.exitCode == 0) {
                    output.print("\t${it.stdout}".green())
                    successes++
                } else {
                    output.print("\t${it.stderr}".red())
                    failures++
                }
            }
        output.print(buildSummary(successes, failures, successes + failures))
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
