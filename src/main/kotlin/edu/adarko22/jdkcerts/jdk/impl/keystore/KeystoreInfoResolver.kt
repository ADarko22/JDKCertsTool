package edu.adarko22.jdkcerts.jdk.impl.keystore

import edu.adarko22.jdkcerts.jdk.impl.JavaInfo
import java.nio.file.Path

interface KeystoreInfoResolver {
    fun resolve(
        jdkPath: Path,
        javaInfo: JavaInfo,
    ): KeystoreInfo
}
