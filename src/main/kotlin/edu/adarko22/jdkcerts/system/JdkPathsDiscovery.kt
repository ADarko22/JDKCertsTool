package edu.adarko22.jdkcerts.system

import java.nio.file.Path

interface JdkPathsDiscovery {
    fun discover(customJdkDirs: List<Path> = emptyList()): List<Path>
}
