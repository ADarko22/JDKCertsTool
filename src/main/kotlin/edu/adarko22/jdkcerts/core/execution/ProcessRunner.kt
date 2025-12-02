package edu.adarko22.jdkcerts.core.execution

/**
 * Generic contract for executing system commands.
 *
 * Implementations are responsible for running the provided command,
 * optionally in dry-run mode, and returning a [ProcessResult].
 */
interface ProcessRunner {
    /**
     * Executes a command on the system.
     *
     * @param command The command and arguments to execute.
     * @param dryRun If true, the command should not be actually executed but simply printed.
     * @return [ProcessResult].
     */
    fun runCommand(
        command: List<String>,
        dryRun: Boolean,
    ): ProcessResult
}
