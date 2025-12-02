package edu.adarko22.jdkcerts.core.execution

/**
 * Represents the result of executing a system command.
 *
 * @property stdout Standard output produced by the command.
 * @property stderr Standard error produced by the command.
 * @property exitCode Exit code of the command (0 typically means success).
 * @property dryRunOutput Optional output when the command is run in dry-run mode.
 */
data class ProcessResult(
    val stdout: String,
    val stderr: String,
    val exitCode: Int,
    val dryRunOutput: String,
)
