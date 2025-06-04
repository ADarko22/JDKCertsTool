package edu.adarko22

import com.github.ajalt.clikt.core.CliktCommand
import com.github.ajalt.clikt.core.subcommands
import edu.adarko22.commands.InfoCommand
import edu.adarko22.commands.InstallCaCertsCommand
import edu.adarko22.commands.ListJDKsCommand
import edu.adarko22.commands.RemoveCaCertCommand
import edu.adarko22.utils.JdkDiscover

fun main(args: Array<String>) {
    val jdkDiscover = JdkDiscover()

    object : CliktCommand() {
        override fun run() = Unit
    }.subcommands(
        InfoCommand(),
        ListJDKsCommand(jdkDiscover),
        InstallCaCertsCommand(jdkDiscover),
        RemoveCaCertCommand(jdkDiscover)
    ).main(args)
}
