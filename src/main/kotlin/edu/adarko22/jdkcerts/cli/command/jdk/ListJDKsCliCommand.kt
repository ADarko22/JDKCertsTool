package edu.adarko22.jdkcerts.cli.command.jdk

import com.github.ajalt.clikt.core.CliktCommand
import com.github.ajalt.clikt.core.Context
import edu.adarko22.jdkcerts.cli.command.customJdkDirsOption
import edu.adarko22.jdkcerts.cli.output.ToolOutputPrinter
import edu.adarko22.jdkcerts.cli.output.green
import edu.adarko22.jdkcerts.cli.output.red
import edu.adarko22.jdkcerts.core.jdk.usecase.DiscoverJdksUseCase
import java.nio.file.Path

/**
 * Command that lists all discovered JDK installations on the system.
 *
 * This command searches for JDK installations in standard locations and any
 * custom directories specified by the user, then displays a formatted list
 * of all found JDKs. It's useful for verifying which JDKs the tool can
 * operate on before performing certificate operations.
 */
class ListJDKsCliCommand(
    private val discoverJdks: DiscoverJdksUseCase,
    private val output: ToolOutputPrinter,
) : CliktCommand(name = "list-jdks") {
    private val customJdkDirs: List<Path> by customJdkDirsOption()

    override fun help(context: Context) = "List all discovered JDKs"

    override fun run() {
        val jdks = discoverJdks.discover(customJdkDirs)

        if (jdks.isEmpty()) {
            output.print("No JDKs found. Use `--custom-jdk-dirs`.".red())
        }

        output.print("Found JDKs:".green())
        jdks.forEach { output.print("  - $it".green()) }
        output.print("\n")
    }
}
