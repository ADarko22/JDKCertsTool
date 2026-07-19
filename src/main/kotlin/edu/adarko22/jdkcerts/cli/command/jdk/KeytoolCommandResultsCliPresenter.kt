package edu.adarko22.jdkcerts.cli.command.jdk

import edu.adarko22.jdkcerts.cli.output.ToolOutputPrinter
import edu.adarko22.jdkcerts.cli.output.blue
import edu.adarko22.jdkcerts.cli.output.green
import edu.adarko22.jdkcerts.cli.output.red
import edu.adarko22.jdkcerts.cli.output.yellow
import edu.adarko22.jdkcerts.core.jdk.keytool.model.KeytoolCommandResult

/**
 * CLI presenter responsible for formatting keytool **command** results for the console output.
 *
 * Consumes only the domain [KeytoolCommandResult]; it has no knowledge of exit codes or raw process
 * streams (beyond optional debug stderr carried on failures).
 *
 * @param output Abstract printer used to emit formatted output.
 */
class KeytoolCommandResultsCliPresenter(
    private val output: ToolOutputPrinter,
) {
    fun present(results: List<KeytoolCommandResult>) {
        var successes = 0
        var failures = 0

        results.forEach { result ->
            output.print("--------------------------------------------------".blue())
            output.print("JDK: ${result.jdk.javaInfo.fullVersion} (${result.jdk.javaInfo.vendor})".blue())
            output.print("Path: ${result.jdk.path}".blue())

            when (result) {
                is KeytoolCommandResult.Success -> {
                    output.print("Status: SUCCESS".green())
                    output.print(result.message.green())
                    successes++
                }

                is KeytoolCommandResult.DryRun -> {
                    output.print("Status: DRY RUN".yellow())
                    output.print("Command: ${result.previewCommand}")
                }

                is KeytoolCommandResult.Failure -> {
                    output.print("Status: ERROR".red())
                    output.print("Message: ${result.describe()}".red())
                    failures++
                }
            }
        }

        output.print(buildSummary(successes, failures, results.size))
    }

    private fun KeytoolCommandResult.Failure.describe(): String =
        when (this) {
            is KeytoolCommandResult.Failure.WrongPassword -> {
                "Incorrect keystore password."
            }

            is KeytoolCommandResult.Failure.AliasAlreadyExists -> {
                "Alias `$alias` already exists in the keystore."
            }

            is KeytoolCommandResult.Failure.CertificateAlreadyExists -> {
                "Certificate already exists" + (conflictingAlias?.let { " under alias `$it`." } ?: ".")
            }

            is KeytoolCommandResult.Failure.AliasNotFound -> {
                "Alias `$alias` does not exist in the keystore."
            }

            is KeytoolCommandResult.Failure.Unknown -> {
                "Keytool failed (exit code $exitCode): $rawStderr"
            }
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
