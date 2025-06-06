package edu.adarko22

import com.github.ajalt.clikt.core.CliktCommand
import com.github.ajalt.clikt.core.subcommands
import edu.adarko22.commands.InfoCommand
import edu.adarko22.commands.InstallCertsJdkCommand
import edu.adarko22.commands.ListJDKsCommand
import edu.adarko22.commands.RemoveCertJdkCommand
import edu.adarko22.utils.JdkDiscovery

fun main(args: Array<String>) {
    val jdkDiscovery = JdkDiscovery()

    object : CliktCommand() {
        override fun run() = Unit
    }.subcommands(
        InfoCommand(),
        ListJDKsCommand(jdkDiscovery),
        InstallCertsJdkCommand(jdkDiscovery),
        RemoveCertJdkCommand(jdkDiscovery)
    ).main(args)
}
