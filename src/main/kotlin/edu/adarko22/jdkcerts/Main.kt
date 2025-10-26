package edu.adarko22.jdkcerts

import com.github.ajalt.clikt.core.CliktCommand
import com.github.ajalt.clikt.core.main
import com.github.ajalt.clikt.core.subcommands
import edu.adarko22.jdkcerts.commands.InfoCommand
import edu.adarko22.jdkcerts.commands.InstallCertsJdkCommand
import edu.adarko22.jdkcerts.commands.ListJDKsCommand
import edu.adarko22.jdkcerts.commands.RemoveCertJdkCommand
import edu.adarko22.jdkcerts.jdk.JdkDiscovery

/**
 * Main entry point for the JDK Certificate Management Tool.
 *
 * This application provides a command-line interface for managing certificates
 * across multiple JDK installations. It automates the process of adding and
 * removing certificates from JDK truststores using the keytool utility.
 *
 * The application supports four main commands:
 * - info: Display tool information and version
 * - list-jdks: List all discovered JDK installations
 * - install-cert: Install certificates into JDK truststores
 * - remove-cert: Remove certificates from JDK truststores by alias
 *
 * @author Angelo Buono (adarko22)
 */
fun main(args: Array<String>) {
    val jdkDiscovery = JdkDiscovery()

    object : CliktCommand() {
        override fun run() = Unit
    }.subcommands(
        InfoCommand(),
        ListJDKsCommand(jdkDiscovery),
        InstallCertsJdkCommand(jdkDiscovery),
        RemoveCertJdkCommand(jdkDiscovery),
    ).main(args)
}
