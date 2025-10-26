package edu.adarko22.process

import edu.adarko22.utils.blue
import edu.adarko22.utils.green
import edu.adarko22.utils.red
import edu.adarko22.utils.yellow
import java.nio.file.Path
import kotlin.io.path.absolutePathString

/**
 * Service for executing keytool commands across multiple JDK installations.
 *
 * This class handles the execution of keytool operations (like importing and deleting
 * certificates) across all discovered JDK installations. It provides consistent
 * error handling, progress reporting, and supports dry-run mode for previewing
 * operations without making actual changes.
 *
 * The service automatically resolves the correct keystore location for different
 * JDK versions and provides detailed feedback on operation results.
 */
class KeytoolRunner(
    private val printer: (String) -> Unit = ::println,
    private val executor: ProcessExecutor = DefaultProcessExecutor(),
    private val keystoreResolver: KeystoreResolver = KeystoreResolver(),
) {
    fun runCommandWithCacertsResolution(
        commandName: String,
        jdkPaths: List<Path>,
        commandArgs: List<String>,
        dryRun: Boolean,
    ) {
        var successes = 0
        var failures = 0

        for (jdk in jdkPaths) {
            val keytoolPath = jdk.resolve("bin/keytool").absolutePathString()
            val baseCommand = listOf(keytoolPath) + commandArgs

            printer("Running $commandName on $jdk ...")

            val success = runForJdk(jdk, baseCommand, dryRun)
            if (success) successes++ else failures++
        }

        printer("\nSummary: $successes succeeded, $failures failed.".blue())
    }

    private fun runForJdk(
        jdk: Path,
        command: List<String>,
        dryRun: Boolean,
    ): Boolean {
        val keystoreArgs = keystoreResolver.resolve(jdk)

        if (keystoreArgs == null) {
            printer("❌ No cacerts found. Skipping.".red())
            return false
        }

        val fullCommand = command + keystoreArgs

        if (dryRun) {
            printer("🛑 Dry run: would run `${fullCommand.joinToString(" ")}`".blue())
            return true
        }

        return try {
            val result = executor.runCommand(fullCommand)
            handleResult(result)
        } catch (e: Exception) {
            printer("❌ Error executing command: ${e.message}".red())
            false
        }
    }

    private fun handleResult(result: ProcessResult): Boolean =
        if (result.exitCode == 0) {
            printer("✅ Success!".green())
            true
        } else {
            printer("❌ Failure (exit code ${result.exitCode})".red())

            result.stdout.lines().filter { it.isNotBlank() }.forEach {
                printer("\t${it.trim()}".yellow())
            }
            result.stderr.lines().filter { it.isNotBlank() }.forEach {
                printer("\t${it.trim()}".yellow())
            }
            false
        }
}
