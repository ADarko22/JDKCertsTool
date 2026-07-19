package edu.adarko22.jdkcerts.core.execution

/**
 * Represents the raw result of executing a system command.
 *
 * @property stdout Standard output produced by the command.
 * @property stderr Standard error produced by the command.
 * @property exitCode Exit code of the command (0 typically means success).
 */
data class ProcessResult(
    val stdout: String,
    val stderr: String,
    val exitCode: Int,
)
