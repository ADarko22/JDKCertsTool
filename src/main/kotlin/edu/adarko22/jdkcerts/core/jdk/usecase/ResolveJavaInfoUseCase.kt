package edu.adarko22.jdkcerts.core.jdk.usecase

import edu.adarko22.jdkcerts.core.execution.ProcessRunner
import edu.adarko22.jdkcerts.core.jdk.JavaInfo
import edu.adarko22.jdkcerts.core.jdk.parser.JavaInfoParser
import java.nio.file.Path
import kotlin.io.path.absolutePathString

/**
 * Use case for resolving Java version and vendor information for a given JDK.
 *
 * It runs the `java -version` command on the specified JDK and parses the output
 * using [JavaInfoParser] to produce a [JavaInfo] object.
 *
 * @param processRunner Component responsible for running system commands.
 * @param javaInfoParser Component responsible for parsing Java version output.
 */
class ResolveJavaInfoUseCase(
    val processRunner: ProcessRunner,
    val javaInfoParser: JavaInfoParser,
) {
    /**
     * Resolves the Java version information for the given JDK path.
     *
     * @param jdkPath Path to the JDK installation.
     * @return A [JavaInfo] object containing vendor, full version, and major version.
     */
    fun resolve(jdkPath: Path): JavaInfo {
        val javaPath = jdkPath.resolve("bin/java")
        val command = listOf(javaPath.absolutePathString(), "-version")
        val output =
            processRunner
                .runCommand(command, dryRun = false)
                .let { it.stderr + "\n" + it.stdout }
        return javaInfoParser.parseVersionInfo(output)
    }
}
