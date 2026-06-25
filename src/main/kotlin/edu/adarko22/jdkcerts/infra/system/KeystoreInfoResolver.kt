package edu.adarko22.jdkcerts.infra.system

import edu.adarko22.jdkcerts.core.jdk.java.model.JavaInfo
import edu.adarko22.jdkcerts.core.jdk.keytool.model.KeystoreInfo
import java.nio.file.Path

/**
 * Resolves keystore information for a given JDK installation.
 */
fun interface KeystoreInfoResolver {
    /**
     * Returns keystore information for the specified JDK.
     *
     * @param jdkPath Path to the JDK installation.
     * @param javaInfo Associated Java runtime information.
     * @return Resolved keystore information.
     */
    fun resolve(
        jdkPath: Path,
        javaInfo: JavaInfo,
    ): KeystoreInfo
}
