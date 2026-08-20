package edu.adarko22.jdkcerts.core.system

import edu.adarko22.jdkcerts.core.jdk.java.model.JavaInfo
import edu.adarko22.jdkcerts.core.jdk.keytool.model.TruststoreInfo
import java.nio.file.Path

/**
 * Resolves truststore information for a given JDK installation.
 */
fun interface TruststoreInfoResolver {
    /**
     * Returns truststore information for the specified JDK.
     *
     * @param jdkPath Path to the JDK installation.
     * @param javaInfo Associated Java runtime information.
     * @return Resolved truststore information.
     */
    fun resolve(
        jdkPath: Path,
        javaInfo: JavaInfo,
    ): TruststoreInfo
}
