package edu.adarko22.jdkcerts.jdk

import edu.adarko22.jdkcerts.jdk.impl.JavaInfo
import edu.adarko22.jdkcerts.jdk.impl.keystore.KeystoreInfo
import java.nio.file.Path
import kotlin.io.path.absolutePathString

class Jdk(
    val path: Path,
    val javaInfo: JavaInfo,
    val keystoreInfo: KeystoreInfo,
) {
    val keytoolPath: Path by lazy(LazyThreadSafetyMode.NONE) { path.resolve("bin/keytool") }

    override fun toString() = "${javaInfo.fullVersion} (${javaInfo.vendor}) JDK at ${path.absolutePathString()}"
}
