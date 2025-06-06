package edu.adarko22.commands

import com.github.ajalt.clikt.core.CliktCommand
import com.github.ajalt.clikt.parameters.options.convert
import com.github.ajalt.clikt.parameters.options.default
import com.github.ajalt.clikt.parameters.options.option
import edu.adarko22.utils.JdkDiscovery
import edu.adarko22.utils.*
import java.nio.file.Files
import java.nio.file.Path
import java.nio.file.Paths
import kotlin.io.path.absolutePathString

abstract class BaseJdkCommand(
    name: String, help: String,
    protected open val print: (String) -> Unit = { msg -> println(msg) }
) : CliktCommand(name = name, help = help) {

    private val customJdkDirs: List<Path> by option("--custom-jdk-dirs", help = "Comma-separated Paths to the JDK dirs")
        .convert { it.toPaths() }
        .default(emptyList())

    protected fun discoverAndListJdks(jdkDiscovery: JdkDiscovery): List<Path> {
        val jdkPaths = jdkDiscovery.discoverJdkHomes(customJdkDirs)
        if (jdkPaths.isEmpty()) {
            print("❌ No JDKs found. Try specifying a JDK path with the `--custom-jdk-dirs` option.".red())
            return emptyList()
        }

        print("🔍 Found JDKs:".green())
        jdkPaths.forEach { print("  - $it".green()) }
        println()
        return jdkPaths
    }

    private fun String.toPaths(): List<Path> = split(",")
        .map { it.trim() }
        .filter { it.isNotEmpty() }
        .map { it.toPath() }

    protected fun String.toPath(): Path = Paths.get(this.expandHome())

    private fun String.expandHome() = if (this.startsWith("~")) System.getProperty("user.home") + this.drop(1) else this

    protected fun runKeyToolCommandWithCacertsResolution(
        commandName: String,
        jdkPaths: List<Path>,
        commandOptions: List<String>,
        dryRun: Boolean
    ) {
        var successes = 0
        var failures = 0

        jdkPaths.forEach { jdk ->
            val cmd = listOf("$jdk/bin/keytool") + commandOptions

            print("Running $commandName on $jdk ...")

            val success = runKeyToolCommandWithCacertsResolution(jdk, cmd, dryRun)
            if (success) successes++ else failures++
        }

        print("\nSummary: $successes succeeded, $failures failed.".blue())
    }

    private fun runKeyToolCommandWithCacertsResolution(jdk: Path, command: List<String>, dryRun: Boolean): Boolean {
        val keystoreArg = computeKeystoreArgument(jdk)
        if (keystoreArg == null) {
            print("❌ No cacerts found. Skipping.".red())
            return false
        }

        val fullCommand = command + keystoreArg
        return runKeytoolCommand(fullCommand, dryRun)
    }

    private fun runKeytoolCommand(command: List<String>, dryRun: Boolean): Boolean {
        if (dryRun) {
            print("🛑 Dry run: would run `${command.joinToString(" ")}`".blue())
            return true
        }

        try {
            val process = ProcessBuilder(command).start()
            val stdout = process.inputStream.bufferedReader().readText().trimEnd()
            val stderr = process.errorStream.bufferedReader().readText().trimEnd()
            val exitCode = process.waitFor()

            if (exitCode == 0) {
                print("✅ Success!".green())
            } else {
                print("❌ Failure (exit code $exitCode)".red())

                if (stdout.isNotBlank())
                    stdout.split("\n").forEach { print("\t${it.trimEnd()}".yellow()) }
                if (stderr.isNotBlank())
                    stderr.split("\n").forEach { print("\t${it.trimEnd()}".yellow()) }

                return false
            }
        } catch (e: Exception) {
            print("❌ Error executing command: ${e.message}".red())
            return false
        }
        return true
    }

    private fun computeKeystoreArgument(jdk: Path): List<String>? {
        val version = getJdkMajorVersionFromJava(jdk)

        if (version != null && version > 8)
            return listOf("-cacerts")

        val cacertsPath = findJava8TruststorePath(jdk) ?: return null
        return listOf("-keystore", cacertsPath.absolutePathString())
    }

    private fun getJdkMajorVersionFromJava(jdk: Path): Int? {
        return try {
            val process = ProcessBuilder("$jdk/bin/java", "-version")
                .redirectErrorStream(true)
                .start()
            val output = process.inputStream.bufferedReader().readText()
            process.waitFor()

            // Output example: 'java version "1.8.0_202"' or 'openjdk version "11.0.2"'
            val versionLine = output.lineSequence().firstOrNull { it.contains("version") } ?: return null
            val versionString = Regex("\"([^\"]+)\"").find(versionLine)?.groupValues?.get(1) ?: return null
            when {
                versionString.startsWith("1.") -> versionString.substring(2, 3).toIntOrNull() // 1.8.x
                else -> versionString.takeWhile { it.isDigit() }.toIntOrNull() // 11, 17, 21, etc.
            }
        } catch (e: Exception) {
            null
        }
    }

    private fun findJava8TruststorePath(jdk: Path): Path? {
        val possiblePaths = listOf(
            jdk.resolve("lib/security/cacerts"),
            jdk.resolve("jre/lib/security/cacerts")
        )
        return possiblePaths.firstOrNull { Files.exists(it) }
    }
}
