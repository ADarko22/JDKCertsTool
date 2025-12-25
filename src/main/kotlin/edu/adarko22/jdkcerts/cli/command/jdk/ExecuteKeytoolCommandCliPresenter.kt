package edu.adarko22.jdkcerts.cli.command.jdk

import edu.adarko22.jdkcerts.cli.output.ToolOutputPrinter
import edu.adarko22.jdkcerts.cli.output.blue
import edu.adarko22.jdkcerts.cli.output.green
import edu.adarko22.jdkcerts.cli.output.red
import edu.adarko22.jdkcerts.cli.output.yellow
import edu.adarko22.jdkcerts.core.jdk.KeytoolCommandResult

/**
 * CLI presenter responsible for formatting the results for the console output.
 *
 * @param output Abstract printer used to emit formatted output.
 */
class ExecuteKeytoolCommandCliPresenter(
    private val output: ToolOutputPrinter,
) {
    fun present(
        results: List<KeytoolCommandResult>,
        dryRun: Boolean,
    ) {
        var successes = 0
        var failures = 0

        results.forEach { result ->
            output.print("--------------------------------------------------".blue())

            // 1. JDK Header
            output.print("JDK: ${result.jdk.javaInfo.fullVersion} (${result.jdk.javaInfo.vendor})".blue())
            output.print("Path: ${result.jdk.path}".blue())

            // 2. Status and Output
            when (result) {
                is KeytoolCommandResult.Success -> {
                    if (dryRun) {
                        output.print("Status: DRY RUN".yellow())
                        output.print("Command: ${result.processResult.dryRunOutput}")
                    } else {
                        output.print("Status: SUCCESS".green())
                        output.print("Output:\n${result.processResult.stdout}".green())
                    }
                    successes++
                }

                is KeytoolCommandResult.Failure -> {
                    output.print("Status: ERROR".red())
                    output.print("Message: ${result.errorMessage}".red())
                    output.print("Details:\n${result.processResult.stderr}".red())
                    failures++
                }
            }
        }

        output.print(buildSummary(successes, failures, results.size))
    }

    private fun buildSummary(
        successes: Int,
        failures: Int,
        total: Int,
    ): String {
        val label = "\nSummary: ".blue()
        val successPart = "$successes/$total succeeded".green()
        val failurePart = if (failures > 0) (", $failures/$total failed.").red() else "."
        return label + successPart + failurePart
    }
}
