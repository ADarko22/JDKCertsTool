package edu.adarko22.jdkcerts.core.jdk

import java.nio.file.Path
import kotlin.io.path.absolutePathString

class Jdk(
    val path: Path,
    val javaInfo: JavaInfo,
    val keystoreInfo: KeystoreInfo,
) {
    val keytoolPath: Path = path.resolve("bin/keytool")

    override fun toString() = "${javaInfo.fullVersion} (${javaInfo.vendor}) JDK at ${path.absolutePathString()}"
}
