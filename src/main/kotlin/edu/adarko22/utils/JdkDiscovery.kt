package edu.adarko22.utils

import java.nio.file.Files
import java.nio.file.Path

class JdkDiscovery {

    fun discoverJdkHomes(customJdkDirs: List<Path> = emptyList()): List<Path> {
        val userHome = System.getProperty("user.home")
        val allSearchRoots = listOfNotNull(
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
        ) + discoverJetBrainsRuntimeHomes() + customJdkDirs.filter(Files::isDirectory)

        return allSearchRoots
            .asSequence()
            .flatMap { Files.newDirectoryStream(it) }
            .map { toJavaHome(it) }
            .filter { isValidJavaHome(it) }
            .map { it.toRealPath() }
            .distinct()
            .toList()
    }

    private fun discoverJetBrainsRuntimeHomes(): List<Path> {
        val userHome = System.getProperty("user.home")
        val jdkPaths = mutableListOf<Path>()

        // Detect IntelliJ .app installations in /Applications (macOS)
        val appsDir = Path.of(userHome, "/Applications")
        if (Files.isDirectory(appsDir)) {
            Files.newDirectoryStream(appsDir)
                .filter { it.fileName.toString().startsWith("IntelliJ IDEA") && it.toString().endsWith(".app") }
                .map { it.resolve("Contents/jbr/Contents/") }
                .filter(Files::isDirectory)
                .forEach { jdkPaths.add(it) }
        }

        // Detect JetBrains Toolbox installations (macOS/Linux)
        val toolboxRoots = listOf(
            Path.of(userHome, "Library/Application Support/JetBrains/Toolbox/apps"), // macOS
            Path.of(userHome, ".local/share/JetBrains/Toolbox/apps")                 // Linux
        )

        val ideaEditions = listOf("IDEA-U", "IDEA-C", "IDEA-E", "IDEA-P")
        toolboxRoots.filter(Files::isDirectory).forEach { toolboxBase ->
            ideaEditions.forEach { edition ->
                val editionRoot = toolboxBase.resolve(edition)
                if (Files.isDirectory(editionRoot)) {
                    Files.walk(editionRoot, 6)
                        .filter { it.fileName.toString() == "jbr" && Files.isDirectory(it) }
                        .map { it.resolve("Contents/") }
                        .filter(Files::isDirectory)
                        .forEach { jdkPaths.add(it) }
                }
            }
        }

        return jdkPaths.distinct()
    }


    /**
     * For macOS: convert `.jdk` to `Contents/Home`
     */
    private fun toJavaHome(dir: Path): Path =
        if (dir.toString().endsWith(".jdk")) dir.resolve("Contents/Home") else dir

    /**
     * Validates a Java home by checking for both `bin/java` and `bin/keytool`
     */
    private fun isValidJavaHome(dir: Path): Boolean {
        val isWindows = System.getProperty("os.name").startsWith("Windows")
        val java = dir.resolve("bin").resolve(if (isWindows) "java.exe" else "java")
        val keytool = dir.resolve("bin").resolve(if (isWindows) "keytool.exe" else "keytool")
        return Files.isExecutable(java) && Files.isExecutable(keytool)
    }
}