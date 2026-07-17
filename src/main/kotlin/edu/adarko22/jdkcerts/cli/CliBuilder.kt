package edu.adarko22.jdkcerts.cli

import com.github.ajalt.clikt.core.CliktCommand
import com.github.ajalt.clikt.core.main
import com.github.ajalt.clikt.core.subcommands
import edu.adarko22.jdkcerts.cli.command.InfoCliCommand
import edu.adarko22.jdkcerts.cli.command.jdk.FindCertCliCommand
import edu.adarko22.jdkcerts.cli.command.jdk.FindCertCliPresenter
import edu.adarko22.jdkcerts.cli.command.jdk.InstallCertCliCommand
import edu.adarko22.jdkcerts.cli.command.jdk.KeytoolCommandResultsCliPresenter
import edu.adarko22.jdkcerts.cli.command.jdk.ListJDKsCliCommand
import edu.adarko22.jdkcerts.cli.command.jdk.RemoveCertCliCommand
import edu.adarko22.jdkcerts.cli.output.DefaultToolOutputPrinter
import edu.adarko22.jdkcerts.cli.output.ToolOutputPrinter
import edu.adarko22.jdkcerts.core.jdk.DiscoverJdksUseCase
import edu.adarko22.jdkcerts.core.jdk.keytool.usecase.ExecuteKeytoolCommandUseCase
import edu.adarko22.jdkcerts.core.jdk.keytool.usecase.FindKeytoolCertificateUseCase

/**
 * Builder for assembling the JDKCertsTool CLI application.
 *
 * This class constructs the CLI by registering subcommands (`info`, `list-jdks`, `install-cert`, `remove-cert`)
 * and wires them with the necessary use cases and output printer.
 *
 * @param discoverJdks Use case for discovering installed JDKs.
 * @param installKeytoolCertificateUseCase Use case for installing certificates.
 * @param removeKeytoolCertificateUseCase Use case for removing certificates.
 * @param findKeytoolCertificateUseCase Use case for finding certificates.
 * @param toolOutputPrinter Output printer used for CLI messages (default is [DefaultToolOutputPrinter]).
 */
class CliBuilder(
    private val discoverJdks: DiscoverJdksUseCase,
    private val executeKeytoolCommandUseCase: ExecuteKeytoolCommandUseCase,
    private val findKeytoolCertificateUseCase: FindKeytoolCertificateUseCase,
    private val toolOutputPrinter: ToolOutputPrinter = DefaultToolOutputPrinter(),
) : CliEntryPoint {
    // Internal list to hold the selected subcommands
    private val subcommands = mutableListOf<CliktCommand>()

    private val keytoolCommandResultsCliPresenter by lazy {
        KeytoolCommandResultsCliPresenter(toolOutputPrinter)
    }

    private val findCertCliPresenter by lazy {
        FindCertCliPresenter(toolOutputPrinter)
    }

    /**
     * Adds the 'info' subcommand to the CLI.
     */
    fun withInfo(): CliBuilder =
        apply {
            subcommands.add(InfoCliCommand(toolOutputPrinter))
        }

    /**
     * Adds the 'list-jdks' subcommand to the CLI.
     */
    fun withListJdks(): CliBuilder =
        apply {
            subcommands.add(
                ListJDKsCliCommand(
                    discoverJdks,
                    output = toolOutputPrinter,
                ),
            )
        }

    /**
     * Adds the 'install-cert' subcommand to the CLI.
     */
    fun withInstallCert(): CliBuilder =
        apply {
            subcommands.add(
                InstallCertCliCommand(
                    executeKeytoolCommandUseCase,
                    keytoolCommandResultsCliPresenter,
                ),
            )
        }

    /**
     * Adds the 'remove-cert' subcommand to the CLI.
     */
    fun withRemoveCert(): CliBuilder =
        apply {
            subcommands.add(
                RemoveCertCliCommand(
                    executeKeytoolCommandUseCase,
                    keytoolCommandResultsCliPresenter,
                ),
            )
        }

    /**
     * Adds the `find-cert` subcommand to the CLI
     */
    fun withFindCert(): CliBuilder =
        apply {
            subcommands.add(
                FindCertCliCommand(
                    findKeytoolCertificateUseCase,
                    findCertCliPresenter,
                ),
            )
        }

    /**
     * Returns the finalized CLI entry point instance, which is the Builder itself.
     */
    fun build(): CliEntryPoint = this

    /**
     * Executes the built CLI application using the registered subcommands.
     */
    override fun run(args: Array<String>) {
        // Create the root Clikt command internally
        val rootCommand: CliktCommand =
            object : CliktCommand(name = "jdkcerts") {
                override fun run() = Unit
            }.subcommands(subcommands)

        // Run the Clikt command
        rootCommand.main(args)
    }
}
