package edu.adarko22.jdkcerts.cli.command

import com.github.ajalt.clikt.core.CliktCommand
import com.github.ajalt.clikt.core.Context
import edu.adarko22.jdkcerts.cli.output.ToolOutputPrinter
import edu.adarko22.jdkcerts.cli.output.blue
import edu.adarko22.jdkcerts.cli.output.bold
import edu.adarko22.jdkcerts.cli.output.cyan
import edu.adarko22.jdkcerts.cli.output.green
import edu.adarko22.jdkcerts.cli.output.italic
import edu.adarko22.jdkcerts.cli.output.yellow

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
        output.print(
            """
   ▗▖▗▄▄▄ ▗▖ ▗▖ ▗▄▄▖▗▄▄▄▖▗▄▄▖▗▄▄▄▖▗▄▄▖
   ▐▌▐▌  █▐▌▗▞▘▐▌   ▐▌   ▐▌ ▐▌ █ ▐▌   
   ▐▌▐▌  █▐▛▚▖ ▐▌   ▐▛▀▀▘▐▛▀▚▖ █  ▝▀▚▖
▗▄▄▞▘▐▙▄▄▀▐▌ ▐▌▝▚▄▄▖▐▙▄▄▖▐▌ ▐▌ █ ▗▄▄▞▘
            """.blue().trimIndent(),
        )
        output.print(
            "Streamline your JDK certificate management workflow".blue().bold(),
        )
        output.print(
            "Install, find and remove CA certificates across all your JDK installations.".italic(),
        )
        output.print("")
        output.print("${"◆ Version:".bold()}       $version".green())
        output.print("${"◆ Author:".bold()}        ${"Angelo Buono".cyan()}".green())
        output.print("${"◆ License:".bold()}       ${"Apache License 2.0".yellow()}".green())
        output.print("")
        output.print("Explore more about JDK Certs Tool on ${"https://github.com/ADarko22/JDKCertsTool".cyan().bold()}")
    }
}
