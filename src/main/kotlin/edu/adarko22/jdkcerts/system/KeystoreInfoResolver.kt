package edu.adarko22.jdkcerts.system

import edu.adarko22.jdkcerts.core.jdk.JavaInfo
import edu.adarko22.jdkcerts.core.jdk.KeystoreInfo
import java.nio.file.Path

interface KeystoreInfoResolver {
    fun resolve(
        jdkPath: Path,
        javaInfo: JavaInfo,
    ): KeystoreInfo
}
