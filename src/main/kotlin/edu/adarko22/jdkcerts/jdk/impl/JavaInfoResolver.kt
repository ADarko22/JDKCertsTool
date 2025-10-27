package edu.adarko22.jdkcerts.jdk.impl

import edu.adarko22.jdkcerts.process.DefaultProcessRunner
import edu.adarko22.jdkcerts.process.ProcessRunner
import java.nio.file.Path
import kotlin.io.path.absolutePathString

class JavaInfoResolver(
    val processRunner: ProcessRunner = DefaultProcessRunner,
    val javaInfoParser: JavaInfoParser = JavaInfoParser()
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
