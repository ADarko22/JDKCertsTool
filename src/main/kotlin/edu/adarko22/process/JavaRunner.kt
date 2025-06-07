package edu.adarko22.runner

import java.nio.file.Path

class JavaRunner(private val executor: ProcessExecutor = DefaultProcessExecutor()) {

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
            else -> versionString.takeWhile { it.isDigit() }.toIntOrNull()               // 11, 17, etc.
        }
    }
}
