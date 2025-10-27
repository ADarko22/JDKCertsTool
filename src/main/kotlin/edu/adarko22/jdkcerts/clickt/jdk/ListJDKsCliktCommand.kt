package edu.adarko22.jdkcerts.clickt.jdk

import com.github.ajalt.clikt.core.CliktCommand
import com.github.ajalt.clikt.core.Context
import edu.adarko22.jdkcerts.clickt.customJdkDirsOption
import edu.adarko22.jdkcerts.jdk.discovery.JdkDiscovery
import edu.adarko22.jdkcerts.jdk.discovery.UNIXJdkDiscovery
import edu.adarko22.jdkcerts.output.CommandPrinter
import edu.adarko22.jdkcerts.output.green
import edu.adarko22.jdkcerts.output.red
import java.nio.file.Path

/**
 * Command that lists all discovered JDK installations on the system.
 *
 * This command searches for JDK installations in standard locations and any
 * custom directories specified by the user, then displays a formatted list
 * of all found JDKs. It's useful for verifying which JDKs the tool can
 * operate on before performing certificate operations.
 */
class ListJDKsCliktCommand(
    private val jdkDiscovery: JdkDiscovery = UNIXJdkDiscovery(),
    private val output: CommandPrinter = CommandPrinter(),
) : CliktCommand(name = "list-jdks") {
    private val customJdkDirs: List<Path> by customJdkDirsOption()

    override fun help(context: Context) = "List all discovered JDKs"

    override fun run() {
        val jdks = jdkDiscovery.discoverJDKs(customJdkDirs)

        if (jdks.isEmpty()) {
            output.print("No JDKs found. Use `--custom-jdk-dirs`.".red())
        }

        output.print("Found JDKs:".green())
        jdks.forEach { output.print("  - $it".green()) }
        output.print("\n")
    }
}
