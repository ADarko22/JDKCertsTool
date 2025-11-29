package edu.adarko22.jdkcerts.cli.command.jdk

import edu.adarko22.jdkcerts.cli.output.ToolOutputPrinter
import edu.adarko22.jdkcerts.cli.output.blue
import edu.adarko22.jdkcerts.cli.output.green
import edu.adarko22.jdkcerts.cli.output.red
import edu.adarko22.jdkcerts.cli.output.yellow
import edu.adarko22.jdkcerts.core.jdk.KeytoolCommand
import edu.adarko22.jdkcerts.core.jdk.usecase.ExecuteKeytoolCommandUseCase
import java.nio.file.Path

/**
 * CLI presenter responsible for executing a [edu.adarko22.jdkcerts.core.jdk.KeytoolCommand]
 * across all JDK installations and formatting the results for terminal output.
 *
 * @param executeKeytoolCommandUseCase Use case that executes the keytool command on all JDKs.
 * @param output Abstract printer used to emit formatted output to the terminal or other destinations.
 */
class ExecuteKeytoolCommandCliPresenter(
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
                    output.print("\t${it.processResult.dryRunOutput}".yellow())
                    successes++
                } else if (it.processResult.exitCode == 0) {
                    output.print("\t${it.processResult.stdout}".green())
                    successes++
                } else {
                    output.print("\t${it.processResult.stderr}".red())
                    failures++
                }
            }
        output.print(buildSummary(successes, failures, successes + failures))
    }

    private fun buildSummary(
        successes: Int,
        failures: Int,
        total: Int,
    ) = ("\nSummary: ").blue() + ("$successes/$total succeeded").green() + if (failures > 0) (", $failures/$total failed.").red() else ""
}
