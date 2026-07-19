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
     * - **Exclusive Mode (Non-Empty List):** Scan only the paths provided in [customJdkPaths].
     *
     * @param customJdkPaths Explicit JDK home paths provided by the runtime context; each is used directly as a JDK home.
     * @return A distinct, filtered list of validated Java home paths containing runnable binaries.
     */
    fun discover(customJdkPaths: List<Path>): List<Path>
}
