package edu.adarko22.jdkcerts

import com.github.ajalt.clikt.core.CliktCommand
import com.github.ajalt.clikt.core.main
import com.github.ajalt.clikt.core.subcommands
import edu.adarko22.jdkcerts.clickt.info.InfoCliktCommand
import edu.adarko22.jdkcerts.clickt.jdk.ListJDKsCliktCommand
import edu.adarko22.jdkcerts.clickt.jdk.keytool.InstallCertCliktCommand
import edu.adarko22.jdkcerts.clickt.jdk.keytool.RemoveCertCliktCommand

/**
 * Main entry point for the JDK Certificate Management Tool.
 *
 * This application provides a command-line interface for managing certificates
 * across multiple JDK installations. It automates the process of adding and
 * removing certificates from JDK keystores using the keytool utility.
 *
 * The application supports four main commands:
 * - info: Display tool information and version
 * - list-jdks: List all discovered JDK installations
 * - install-cert: Install certificates into JDK keystores
 * - remove-cert: Remove certificates from JDK keystores by alias
 *
 * @author Angelo Buono (adarko22)
 */
fun main(args: Array<String>) {
    object : CliktCommand() {
        override fun run() = Unit
    }.subcommands(
        InfoCliktCommand(),
        ListJDKsCliktCommand(),
        InstallCertCliktCommand(),
        RemoveCertCliktCommand(),
    ).main(args)
}
