package edu.adarko22.jdkcerts.core.jdk.keytool.model

import java.nio.file.Path

/**
 * Execution options for keytool operations.
 *
 * @property customJdkPaths Explicit JDK home paths to target; when non-empty, auto-discovery is bypassed.
 * @property masterPassword Keystore password used for all targeted JDKs.
 * @property dryRun If true, operations are simulated and no external processes are started.
 */
data class ExecutionContext(
    val customJdkPaths: List<Path> = emptyList(),
    val masterPassword: String,
    val dryRun: Boolean = false,
)
