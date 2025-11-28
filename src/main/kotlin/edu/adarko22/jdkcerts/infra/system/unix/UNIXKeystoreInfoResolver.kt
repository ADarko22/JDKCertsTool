package edu.adarko22.jdkcerts.infra.system.unix

import edu.adarko22.jdkcerts.core.jdk.JavaInfo
import edu.adarko22.jdkcerts.core.jdk.KeystoreInfo
import edu.adarko22.jdkcerts.infra.system.KeystoreInfoResolver
import java.nio.file.Files
import java.nio.file.Path

/**
 * Unix/macOS implementation of [KeystoreInfoResolver].
 *
 * Determines the path to the JDK's `cacerts` file and whether the
 * `-cacerts` shortcut is supported (Java 9+).
 */
class UNIXKeystoreInfoResolver : KeystoreInfoResolver {
    override fun resolve(
        jdkPath: Path,
        javaInfo: JavaInfo,
    ): KeystoreInfo = KeystoreInfo(findCacertsFolder(jdkPath), javaInfo.major > 8)

    private fun findCacertsFolder(jdkPath: Path): Path =
        listOf(
            jdkPath.resolve("lib/security/cacerts"),
            jdkPath.resolve("jre/lib/security/cacerts"),
        ).firstOrNull { Files.exists(it) }
            .let { it ?: throw IllegalStateException("Could not find cacerts file in JDK at $jdkPath") }
}
