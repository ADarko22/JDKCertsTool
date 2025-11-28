package edu.adarko22.jdkcerts.infra.system.unix

import edu.adarko22.jdkcerts.infra.system.SystemInfoProvider
import java.nio.file.Path
import java.nio.file.Paths

/**
 * Unix/macOS implementation of [SystemInfoProvider].
 *
 * Provides standard system properties such as user home and OS name.
 */
class UNIXSystemInfoProvider : SystemInfoProvider {
    override fun getUserHome(): Path = Paths.get(System.getProperty("user.home"))

    override fun getOsName(): String = System.getProperty("os.name")

    override fun getProgramFilesEnv(): String? = System.getenv("ProgramFiles")

    override fun getProgramFilesX86Env(): String? = System.getenv("ProgramFiles(x86)")
}
