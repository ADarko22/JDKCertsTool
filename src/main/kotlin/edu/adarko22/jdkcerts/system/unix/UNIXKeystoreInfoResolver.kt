package edu.adarko22.jdkcerts.system.unix

import edu.adarko22.jdkcerts.core.jdk.JavaInfo
import edu.adarko22.jdkcerts.core.jdk.KeystoreInfo
import edu.adarko22.jdkcerts.system.KeystoreInfoResolver
import java.nio.file.Files
import java.nio.file.Path

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
