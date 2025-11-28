package edu.adarko22.jdkcerts.infra.system

import edu.adarko22.jdkcerts.core.jdk.JavaInfo
import edu.adarko22.jdkcerts.core.jdk.KeystoreInfo
import java.nio.file.Path

/**
 * Resolves keystore information for a given JDK installation.
 */
interface KeystoreInfoResolver {
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
