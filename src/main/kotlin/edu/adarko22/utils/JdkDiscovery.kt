package edu.adarko22.utils

import java.nio.file.Files
import java.nio.file.Path

class JdkDiscover {

    fun discoverJdkHomes(customJdkDirs: List<Path> = emptyList()): List<Path> {
        val userHome = System.getProperty("user.home")
        val allDirs = listOfNotNull(
            // macOS JDKs
            Path.of("/Library/Java/JavaVirtualMachines").takeIf(Files::isDirectory),
            // Linux
            Path.of("/usr/lib/jvm").takeIf(Files::isDirectory),
            Path.of("/usr/java").takeIf(Files::isDirectory),
            // Windows
            System.getenv("ProgramFiles")?.let { Path.of(it, "Java") }?.takeIf(Files::isDirectory),
            System.getenv("ProgramFiles(x86)")?.let { Path.of(it, "Java") }?.takeIf(Files::isDirectory),
            // SDKMAN
            Path.of(userHome, ".sdkman/candidates/java").takeIf(Files::isDirectory),
        ) + customJdkDirs.filter(Files::isDirectory)

        return allDirs
            .asSequence()
            .flatMap { Files.newDirectoryStream(it) }
            .filter { Files.isDirectory(it) && !Files.isSymbolicLink(it) }
            .map { toJavaHome(it) }
            .filter { Files.isDirectory(it) }
            .map { it.toRealPath() }
            .distinct()
            .toList()
    }

    // For macOS, JDKs are under Contents/Home
    private fun toJavaHome(it: Path): Path = if (it.toString().endsWith(".jdk")) it.resolve("Contents/Home") else it
}