package edu.adarko22.jdkcerts.infra.execution

import edu.adarko22.jdkcerts.core.execution.ProcessResult
import edu.adarko22.jdkcerts.core.execution.ProcessRunner

/**
 * Default implementation of [ProcessRunner] that executes system commands.
 *
 * Supports a "dry run" mode where the command is not executed, but returned
 * as a textual representation for preview purposes.
 */
class DefaultProcessRunner : ProcessRunner {
    /**
     * Runs the given system command.
     *
     * @param command List of command-line arguments, including the executable.
     * @param dryRun If true, the command is not executed and only a preview is returned.
     * @return A [ProcessResult] containing stdout, stderr, exit code, and dry-run output.
     */
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
