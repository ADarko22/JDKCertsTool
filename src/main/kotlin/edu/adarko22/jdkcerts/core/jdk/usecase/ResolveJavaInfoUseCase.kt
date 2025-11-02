package edu.adarko22.jdkcerts.core.jdk.usecase

import edu.adarko22.jdkcerts.core.jdk.JavaInfo
import edu.adarko22.jdkcerts.core.jdk.parser.JavaInfoParser
import edu.adarko22.jdkcerts.core.process.ProcessRunner
import java.nio.file.Path
import kotlin.io.path.absolutePathString

class ResolveJavaInfoUseCase(
    val processRunner: ProcessRunner,
    val javaInfoParser: JavaInfoParser,
) {
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
