package edu.adarko22.jdkcerts.infra.system

import java.nio.file.Path

/**
 * Discovers JDK installation paths on the host system.
 */
interface JdkPathsDiscovery {
    /**
     * Provides JDK installation paths on the host system.
     *
     * @param customJdkDirs Optional list of additional directories to search.
     * @return List of discovered JDK home paths.
     */
    fun discover(customJdkDirs: List<Path> = emptyList()): List<Path>
}
