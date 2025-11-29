package edu.adarko22.jdkcerts.core.jdk

import edu.adarko22.jdkcerts.core.execution.ProcessResult

/**
 * Represents the raw result of a keytool command execution on a specific JDK.
 *
 * @property jdk The JDK instance the command was executed against.
 * @property processResult The raw outcome including exit code, stdout, and stderr.
 */
data class KeytoolCommandResult(
    val jdk: Jdk,
    val processResult: ProcessResult,
)