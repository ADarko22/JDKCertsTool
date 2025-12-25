package edu.adarko22.jdkcerts.core.jdk

import edu.adarko22.jdkcerts.core.execution.ProcessResult

/**
 * Represents the outcome of a keytool process execution on a specific JDK.
 *
 * This sealed hierarchy distinguishes between commands that finished with a success exit code
 * and those that encountered system or execution failures.
 */
sealed class KeytoolCommandResult {
    abstract val jdk: Jdk

    /**
     * Represents a command that finished successfully (typically exit code 0).
     *
     * @property jdk The JDK instance where the command was executed.
     * @property processResult The raw outcome containing stdout and stderr.
     */
    data class Success(
        override val jdk: Jdk,
        val processResult: ProcessResult,
    ) : KeytoolCommandResult()

    /**
     * Represents a command that failed to execute correctly (typically a non-zero exit code).
     *
     * @property jdk The JDK instance where the execution failed.
     * @property processResult The raw outcome including the error stream and exit code.
     * @property errorMessage A human-readable description of the failure.
     */
    data class Failure(
        override val jdk: Jdk,
        val processResult: ProcessResult,
        val errorMessage: String,
    ) : KeytoolCommandResult()
}
