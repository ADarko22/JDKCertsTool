package edu.adarko22.jdkcerts.infra.execution

import edu.adarko22.jdkcerts.core.execution.ProcessResult
import edu.adarko22.jdkcerts.core.execution.ProcessRunner

/**
 * Default implementation of ProcessExecutor using ProcessBuilder for synchronous execution.
 *
 * This implementation executes commands synchronously and captures both stdout and
 * stderr streams. It's suitable for production use and provides a foundation for
 * more sophisticated execution strategies.
 */
class DefaultProcessRunner : ProcessRunner {
    override fun runCommand(
        command: List<String>,
        dryRun: Boolean,
    ): ProcessResult {
        if (dryRun) {
            val asText = command.joinToString(" ")
            val dryRunCommand = "Dry run: would run `$asText`"
            return ProcessResult("", "", 0, dryRunCommand)
        }

        try {
            val process =
                ProcessBuilder(command)
                    .redirectErrorStream(false)
                    .start()
            val stdout = process.inputStream.bufferedReader().readText()
            val stderr = process.errorStream.bufferedReader().readText()
            val exitCode = process.waitFor()
            return ProcessResult(stdout.trim(), stderr.trim(), exitCode, "")
        } catch (e: Exception) {
            val errorMessage = "Error executing command: ${e.message ?: "Unknown Error"}"
            return ProcessResult("", errorMessage, -1, "")
        }
    }
}
