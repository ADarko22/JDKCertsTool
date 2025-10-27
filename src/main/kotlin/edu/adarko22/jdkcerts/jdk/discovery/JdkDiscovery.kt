package edu.adarko22.jdkcerts.jdk.discovery

import edu.adarko22.jdkcerts.jdk.Jdk
import java.nio.file.Path

interface JdkDiscovery {
    fun discoverJDKs(customJdkDirs: List<Path> = emptyList()): List<Jdk>
}
