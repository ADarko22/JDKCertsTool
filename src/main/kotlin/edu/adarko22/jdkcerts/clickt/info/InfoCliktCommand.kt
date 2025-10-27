package edu.adarko22.jdkcerts.clickt.info

import com.github.ajalt.clikt.core.CliktCommand
import com.github.ajalt.clikt.core.Context
import edu.adarko22.jdkcerts.output.CommandPrinter
import edu.adarko22.jdkcerts.output.blue
import edu.adarko22.jdkcerts.output.bold
import edu.adarko22.jdkcerts.output.green
import edu.adarko22.jdkcerts.output.italic

/**
 * Command that displays information about the JDK Certificate Management Tool.
 *
 * This command provides users with basic information about the tool including
 * version number, author details, and a brief description of its capabilities.
 * It serves as a simple way for users to verify the tool is working correctly
 * and to get basic information about the installation.
 */
class InfoCliktCommand(
    private val output: CommandPrinter = CommandPrinter(),
) : CliktCommand(name = "info") {
    private val version = this.javaClass.`package`.implementationVersion ?: "unknown"

    override fun help(context: Context) = "Display information about the tool"

    override fun run() {
        output.print("🔧 JDK Management Tool".blue().bold().italic())
        output.print(
            "This tool helps you manage JDK installations, including listing JDKs, installing CA certificates, and removing them.".italic(),
        )
        output.print("Version: $version".green())
        output.print("Author: ${"Angelo Buono".bold().italic()}".green())
    }
}
