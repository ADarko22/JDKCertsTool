package edu.adarko22

import com.github.ajalt.clikt.core.CliktCommand
import com.github.ajalt.clikt.core.subcommands
import edu.adarko22.commands.InstallCaCerts
import edu.adarko22.commands.ListJDKs
import edu.adarko22.commands.RemoveCaCert
import edu.adarko22.utils.JdkDiscover

fun main(args: Array<String>) {
    val jdkDiscover = JdkDiscover()

    object : CliktCommand() {
        override fun run() = Unit
    }.subcommands(
        ListJDKs(jdkDiscover),
        InstallCaCerts(jdkDiscover),
        RemoveCaCert(jdkDiscover)
    ).main(args)
}
