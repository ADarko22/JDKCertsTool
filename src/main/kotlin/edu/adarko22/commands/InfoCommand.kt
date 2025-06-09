package edu.adarko22.commands

import com.github.ajalt.clikt.core.CliktCommand
import com.github.ajalt.clikt.core.Context
import edu.adarko22.utils.blue
import edu.adarko22.utils.bold
import edu.adarko22.utils.green
import edu.adarko22.utils.italic

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
