package edu.adarko22.jdkcerts.infra.system

import java.nio.file.Path

/**
 * Strategy boundary for identifying JDK installation paths on the host system.
 */
fun interface JdkPathsDiscovery {
    /**
     * Resolves and validates absolute home paths of available JDK installations.
     *
     * ### Discovery Modes:
     * - **Automatic Mode (Empty List):** The scanner falls back to standard platform locations .
     * - **Exclusive Mode (Non-Empty List):** Scan only the paths provided in [customJdkDirs].
     *
     * @param customJdkDirs Explicit root directories provided by the runtime context to restrict searching.
     * @return A distinct, filtered list of validated Java home paths containing runnable binaries.
     */
    fun discover(customJdkDirs: List<Path>): List<Path>
}
