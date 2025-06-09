package edu.adarko22.process

import edu.adarko22.runner.JavaRunner
import java.nio.file.Files
import java.nio.file.Path
import kotlin.io.path.absolutePathString

class KeystoreResolver(
    private val javaRunner: JavaRunner = JavaRunner(),
) {
    fun resolve(jdk: Path): List<String>? {
        val version = javaRunner.getMajorVersion(jdk)

        return if (version != null && version > 8) {
            listOf("-cacerts")
        } else {
            findJava8Cacerts(jdk)?.let { listOf("-keystore", it.absolutePathString()) }
        }
    }

    private fun findJava8Cacerts(jdk: Path): Path? =
        listOf(
            jdk.resolve("lib/security/cacerts"),
            jdk.resolve("jre/lib/security/cacerts"),
        ).firstOrNull { Files.exists(it) }
}
