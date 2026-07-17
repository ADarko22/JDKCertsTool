package edu.adarko22.jdkcerts.core.jdk.keytool.model

import java.nio.file.Path

/**
 * @property customJdkDirs  Optional user-provided directories to include in the JDK discovery phase.
 * @property masterPassword Password for all the keystores accessed on the JDKs.
 * @property dryRun If `true`, the OS process is bypassed and the command returns a simulated preview.
 */
data class ExecutionContext(
    val customJdkDirs: List<Path> = emptyList(),
    val masterPassword: String,
    val dryRun: Boolean = false,
)
