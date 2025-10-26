package edu.adarko22.jdkcerts.jdk.java

import edu.adarko22.jdkcerts.shared.DefaultProcessExecutor
import edu.adarko22.jdkcerts.shared.ProcessExecutor
import java.nio.file.Path

/**
 * Service for executing Java runtime commands and extracting version information.
 *
 * This class provides functionality to interact with Java installations, primarily
 * for determining the Java version which affects keystore handling. It executes
 * 'java -version' and parses the output to extract the major version number.
 *
 * Version detection is crucial for determining the correct keystore location
 * and format (Java 8 vs Java 9+ differences).
 */
class JavaRunner(
    private val executor: ProcessExecutor = DefaultProcessExecutor(),
) {
    fun getMajorVersion(jdk: Path): Int? {
        val javaPath = jdk.resolve("bin/java").toAbsolutePath().toString()
        val command = listOf(javaPath, "-version")
        val result = executor.runCommand(command)
        // `java -version` writes to stderr
        val output = result.stderr + "\n" + result.stdout

        val versionLine = output.lineSequence().firstOrNull { "version" in it } ?: return null
        val versionString = Regex("\"([^\"]+)\"").find(versionLine)?.groupValues?.get(1) ?: return null

        return when {
            versionString.startsWith("1.") -> versionString.substring(2, 3).toIntOrNull() // 1.8
            else -> versionString.takeWhile { it.isDigit() }.toIntOrNull() // 11, 17, etc.
        }
    }
}