package edu.adarko22.jdkcerts.commands

import com.github.ajalt.clikt.core.CliktCommand
import com.github.ajalt.clikt.core.Context
import com.github.ajalt.clikt.parameters.options.convert
import com.github.ajalt.clikt.parameters.options.default
import com.github.ajalt.clikt.parameters.options.flag
import com.github.ajalt.clikt.parameters.options.option
import edu.adarko22.jdkcerts.jdk.JdkDiscovery
import edu.adarko22.jdkcerts.jdk.keytool.KeytoolRunner
import edu.adarko22.jdkcerts.util.green
import edu.adarko22.jdkcerts.util.red
import java.nio.file.Path
import java.nio.file.Paths

/**
 * Abstract base class for JDK-related commands that provides common functionality
 * for discovering JDKs and managing certificates.
 *
 * This class encapsulates shared behavior including JDK discovery, dry-run mode support,
 * custom JDK directory specification, and keystore password handling. All concrete
 * command implementations should extend this class to inherit these common features.
 *
 * Key features:
 * - Automatic JDK discovery across standard locations
 * - Support for custom JDK directories via --custom-jdk-dirs option
 * - Dry-run mode for previewing changes without execution
 * - Configurable keystore password with sensible defaults
 */
abstract class BaseJdkCommand(
    name: String,
    private val help: String,
    protected open val printer: (String) -> Unit = ::println,
    protected open val keytoolRunner: KeytoolRunner = KeytoolRunner(),
) : CliktCommand(name = name) {
    private val customJdkDirs: List<Path>
        by option("--custom-jdk-dirs", help = "Comma-separated paths to JDK dirs")
            .convert { it.toPaths() }
            .default(emptyList())

    protected val dryRun: Boolean by option("--dry-run", help = "Preview changes only").flag()
    protected val keystorePassword: String
        by option("--keystore-password", help = "Keystore password").default("changeit")

    override fun help(context: Context) = help

    protected fun discoverAndListJdks(jdkDiscovery: JdkDiscovery): List<Path> {
        val jdkPaths = jdkDiscovery.discoverJdkHomes(customJdkDirs)
        if (jdkPaths.isEmpty()) {
            printer("❌ No JDKs found. Use `--custom-jdk-dirs`.".red())
            return emptyList()
        }

        printer("🔍 Found JDKs:".green())
        jdkPaths.forEach { printer("  - $it".green()) }
        println()
        return jdkPaths
    }

    private fun String.toPaths(): List<Path> = split(",").map { it.trim().toPath() }

    fun String.toPath(): Path = Paths.get(this.expandHome())

    private fun String.expandHome(): String = if (startsWith("~")) System.getProperty("user.home") + drop(1) else this
}
