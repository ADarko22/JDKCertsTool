package edu.adarko22.utils

import edu.adarko22.utils.system.DefaultSystemInfoProvider
import edu.adarko22.utils.system.SystemInfoProvider
import java.nio.file.Files
import java.nio.file.Path

class JdkDiscovery(
    private val systemInfoProvider: SystemInfoProvider = DefaultSystemInfoProvider()
) {

    private val defaultDirs = listOfNotNull(
        // macOS JDKs
        Path.of("/Library/Java/JavaVirtualMachines").takeIf(Files::isDirectory),
        // Linux
        Path.of("/usr/lib/jvm").takeIf(Files::isDirectory),
        Path.of("/usr/java").takeIf(Files::isDirectory),
        // Windows
        systemInfoProvider.getProgramFilesEnv()?.let { Path.of(it, "Java") }?.takeIf(Files::isDirectory),
        systemInfoProvider.getProgramFilesX86Env()?.let { Path.of(it, "Java") }?.takeIf(Files::isDirectory),
        // SDKMAN uses the injected userHome
        systemInfoProvider.getUserHome().resolve(".sdkman/candidates/java").takeIf(Files::isDirectory),
    )

    fun discoverJdkHomes(customJdkDirs: List<Path> = emptyList()): List<Path> {
        val allSearchRoots = defaultDirs + discoverJetBrainsRuntimeHomes() + customJdkDirs.filter(Files::isDirectory)

        return allSearchRoots
            .asSequence()
            .flatMap { Files.newDirectoryStream(it) }
            .map { toJavaHome(it) }
            .filter { isValidJavaHome(it) }
            .map { it.toRealPath() }
            .distinct()
            .toList()
    }

    // Now uses the class's userHome property
    internal fun discoverJetBrainsRuntimeHomes(): List<Path> {
        val jdkPaths = mutableListOf<Path>()

        // Detect IntelliJ .app installations in /Applications (macOS)
        val appsDir = systemInfoProvider.getUserHome().resolve("Applications")
        if (Files.isDirectory(appsDir)) {
            Files.newDirectoryStream(appsDir)
                .filter { it.fileName.toString().startsWith("IntelliJ IDEA") && it.toString().endsWith(".app") }
                .map { it.resolve("Contents/jbr/Contents/") }
                .filter(Files::isDirectory)
                .forEach { jdkPaths.add(it) }
        }

        // Detect JetBrains Toolbox installations (macOS/Linux)
        val toolboxRoots = listOf(
            systemInfoProvider.getUserHome().resolve("Library/Application Support/JetBrains/Toolbox/apps"), // macOS
            systemInfoProvider.getUserHome().resolve(".local/share/JetBrains/Toolbox/apps")                 // Linux
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
    internal fun toJavaHome(dir: Path): Path =
        if (dir.toString().endsWith(".jdk")) dir.resolve("Contents/Home") else dir

    /**
     * Validates a Java home by checking for both `bin/java` and `bin/keytool`
     */
    internal fun isValidJavaHome(dir: Path): Boolean {
        val isWindows = systemInfoProvider.getOsName().startsWith("Windows")
        val java = dir.resolve("bin").resolve(if (isWindows) "java.exe" else "java")
        val keytool = dir.resolve("bin").resolve(if (isWindows) "keytool.exe" else "keytool")
        return Files.isExecutable(java) && Files.isExecutable(keytool)
    }
}