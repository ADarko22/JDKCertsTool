package edu.adarko22.jdkcerts.jdk

import edu.adarko22.jdkcerts.jdk.impl.JavaInfoResolver
import edu.adarko22.jdkcerts.jdk.impl.keystore.KeystoreInfoResolver
import java.nio.file.Path

class JdkCreator(
    private val javaVersionResolver: JavaInfoResolver,
    private val keystoreInfoResolver: KeystoreInfoResolver,
) {
    fun createJdk(jdkPath: Path): Jdk {
        val javaInfo = javaVersionResolver.resolve(jdkPath)
        val keystoreInfo = keystoreInfoResolver.resolve(jdkPath, javaInfo)
        return Jdk(
            path = jdkPath,
            javaInfo = javaInfo,
            keystoreInfo = keystoreInfo,
        )
    }
}
