package edu.adarko22.commands

import com.github.ajalt.clikt.core.CliktCommand
import edu.adarko22.utils.*

class InfoCommand(
    protected open val print: (String) -> Unit = { msg -> println(msg) }
) : CliktCommand(name = "info", help = "Display information about the tool") {
    private val version = this.javaClass.`package`.implementationVersion ?: "unknown"

    override fun run() {
        print("🔧 JDK Management Tool".blue().bold().italic())
        print("This tool helps you manage JDK installations, including listing JDKs, installing CA certificates, and removing them.".italic())
        print("Version: $version".green())
        print("Author: ${"Angelo Buono".bold().italic()}".green())
    }
}
