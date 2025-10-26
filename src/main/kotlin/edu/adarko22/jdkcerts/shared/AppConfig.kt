package edu.adarko22.jdkcerts.shared

import edu.adarko22.jdkcerts.commands.InfoCommand
import edu.adarko22.jdkcerts.commands.InstallCertsJdkCommand
import edu.adarko22.jdkcerts.commands.ListJDKsCommand
import edu.adarko22.jdkcerts.commands.RemoveCertJdkCommand
import edu.adarko22.jdkcerts.jdk.JdkDiscovery
import edu.adarko22.jdkcerts.jdk.keytool.KeytoolRunner

/**
 * Central configuration object.
 *
 * This class serves as a dependency injection container for the application,
 * providing all services and dependencies needed by CLI commands. It ensures
 * consistent configuration across all commands.
 *
 * The configuration uses sensible defaults:
 * - Standard output printer (println)
 * - System-based JDK discovery
 * - Default keytool execution runner
 */
data class AppConfig(
    val printer: (String) -> Unit = ::println,
    val jdkDiscovery: JdkDiscovery = JdkDiscovery(),
    val keytoolRunner: KeytoolRunner = KeytoolRunner(),
) {
    // Convenience methods
    fun createInfoCommand() = InfoCommand(printer)

    fun createListCommand() = ListJDKsCommand(jdkDiscovery, printer)

    fun createInstallCommand() = InstallCertsJdkCommand(jdkDiscovery, printer, keytoolRunner)

    fun createRemoveCommand() = RemoveCertJdkCommand(jdkDiscovery, printer, keytoolRunner)
}
