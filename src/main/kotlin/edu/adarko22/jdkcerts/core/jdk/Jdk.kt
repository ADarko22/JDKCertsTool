package edu.adarko22.jdkcerts.core.jdk

import edu.adarko22.jdkcerts.core.jdk.java.model.JavaInfo
import edu.adarko22.jdkcerts.core.jdk.keytool.model.TruststoreInfo
import java.nio.file.Path
import kotlin.io.path.absolutePathString

/**
 * Represents a Java Development Kit (JDK) installation.
 *
 * @property path Path to the root of the JDK installation.
 * @property javaInfo Version and vendor information for this JDK.
 * @property truststoreInfo Information about the JDK's truststore.
 */
class Jdk(
    val path: Path,
    val javaInfo: JavaInfo,
    val truststoreInfo: TruststoreInfo,
) {
    /** Path to the `keytool` executable inside this JDK. */
    val keytoolPath: Path = path.resolve("bin/keytool")

    override fun toString() = "${javaInfo.fullVersion} (${javaInfo.vendor}) JDK at ${path.absolutePathString()}"
}
