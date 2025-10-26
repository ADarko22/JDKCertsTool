package edu.adarko22.jdkcerts.commands

import com.github.ajalt.clikt.core.CliktCommand
import com.github.ajalt.clikt.core.Context
import edu.adarko22.jdkcerts.util.blue
import edu.adarko22.jdkcerts.util.bold
import edu.adarko22.jdkcerts.util.green
import edu.adarko22.jdkcerts.util.italic

/**
 * Command that displays information about the JDK Certificate Management Tool.
 *
 * This command provides users with basic information about the tool including
 * version number, author details, and a brief description of its capabilities.
 * It serves as a simple way for users to verify the tool is working correctly
 * and to get basic information about the installation.
 */
class InfoCommand(
    private val print: (String) -> Unit = { msg -> println(msg) },
) : CliktCommand(name = "info") {
    private val version = this.javaClass.`package`.implementationVersion ?: "unknown"

    override fun help(context: Context) = "Display information about the tool"

    override fun run() {
        print("🔧 JDK Management Tool".blue().bold().italic())
        print(
            "This tool helps you manage JDK installations, including listing JDKs, installing CA certificates, and removing them.".italic(),
        )
        print("Version: $version".green())
        print("Author: ${"Angelo Buono".bold().italic()}".green())
    }
}
