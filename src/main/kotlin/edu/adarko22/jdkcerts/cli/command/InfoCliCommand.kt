package edu.adarko22.jdkcerts.cli.command

import com.github.ajalt.clikt.core.CliktCommand
import com.github.ajalt.clikt.core.Context
import edu.adarko22.jdkcerts.cli.output.ToolOutputPrinter
import edu.adarko22.jdkcerts.cli.output.blue
import edu.adarko22.jdkcerts.cli.output.bold
import edu.adarko22.jdkcerts.cli.output.green
import edu.adarko22.jdkcerts.cli.output.italic

/**
 * Command that displays information about the JDK Certificate Management Tool.
 *
 * @param output Abstract printer used to emit formatted output to the terminal or other destinations.
 */
class InfoCliCommand(
    private val output: ToolOutputPrinter,
) : CliktCommand(name = "info") {
    private val version = this.javaClass.`package`.implementationVersion ?: "unknown"

    override fun help(context: Context) = "Display information about the tool"

    override fun run() {
        output.print("🔧 JDK Management Tool".blue().bold().italic())
        output.print(
            "This tool helps you manage JDK installations, including listing JDKs, installing CA certificates, finding and removing them."
                .italic(),
        )
        output.print("Version: $version".green())
        output.print("Author: ${"Angelo Buono".bold().italic()}".green())
    }
}
