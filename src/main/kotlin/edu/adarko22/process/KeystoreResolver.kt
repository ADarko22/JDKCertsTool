package edu.adarko22.process

import java.nio.file.Files
import java.nio.file.Path
import kotlin.io.path.absolutePathString

/**
 * Service for resolving the correct keystore location and arguments for different JDK versions.
 *
 * This class handles the differences between Java 8 and Java 9+ keystore handling:
 * - Java 8: Uses explicit keystore path (-keystore /path/to/cacerts)
 * - Java 9+: Uses the -cacerts flag which automatically locates the keystore
 *
 * The service searches for keystores in standard locations and returns the
 * appropriate command-line arguments for keytool operations.
 */
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
