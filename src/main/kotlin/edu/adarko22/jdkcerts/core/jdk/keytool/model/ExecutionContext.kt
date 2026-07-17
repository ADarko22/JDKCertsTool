package edu.adarko22.jdkcerts.core.jdk.keytool.model

import java.nio.file.Path

/**
 * Execution options for keytool operations.
 *
 * @property customJdkDirs Directories to include when discovering JDKs.
 * @property masterPassword Keystore password used for all targeted JDKs.
 * @property dryRun If true, operations are simulated and no external processes are started.
 */
data class ExecutionContext(
    val customJdkDirs: List<Path> = emptyList(),
    val masterPassword: String,
    val dryRun: Boolean = false,
)
