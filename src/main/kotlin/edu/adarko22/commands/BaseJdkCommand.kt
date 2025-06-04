package edu.adarko22.commands

import com.github.ajalt.clikt.core.CliktCommand
import com.github.ajalt.clikt.parameters.options.convert
import com.github.ajalt.clikt.parameters.options.default
import com.github.ajalt.clikt.parameters.options.option
import edu.adarko22.utils.JdkDiscover
import java.nio.file.Path
import java.nio.file.Paths

abstract class BaseJdkCommand(
    name: String, help: String,
    protected open val print: (String) -> Unit = { msg -> println(msg) }
) : CliktCommand(name = name, help = help) {

    private val customJdkDirs: List<Path> by option("--custom-jdk-dirs", help = "Comma-separated Paths to the JDK dirs")
        .convert { it.toPaths() }
        .default(emptyList())

    protected fun discoverAndListJdks(jdkDiscover: JdkDiscover): List<Path> {
        val jdkPaths = jdkDiscover.discoverJdkHomes(customJdkDirs)
        if (jdkPaths.isEmpty()) {
            print("❌ No JDKs found. Try specifying a JDK path with the `--custom-jdk-dirs` option.".red())
            return emptyList()
        }

        print("🔍 Found JDKs:".green())
        jdkPaths.forEach { print("  - $it".green()) }
        return jdkPaths
    }


    private fun String.toPaths(): List<Path> = split(",")
        .map { it.trim() }
        .filter { it.isNotEmpty() }
        .map { it.toPath() }

    protected fun String.toPath(): Path = Paths.get(this.expandHome())

    private fun String.expandHome(): String = if (this.startsWith("~")) System.getProperty("user.home") + this.drop(1) else this

    protected fun String.red() = "\u001B[31m$this\u001B[0m"
    protected fun String.green() = "\u001B[32m$this\u001B[0m"
    protected fun String.yellow() = "\u001B[33m$this\u001B[0m"
    protected fun String.blue() = "\u001B[34m$this\u001B[0m"
}
