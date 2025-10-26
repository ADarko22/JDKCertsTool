package edu.adarko22.utils.system

import java.nio.file.Path
import java.nio.file.Paths

/**
 * Default implementation of SystemInfoProvider that delegates to actual System calls.
 *
 * This implementation provides access to system properties and environment variables
 * through the standard Java System class. It's suitable for production use and
 * provides a foundation for platform-specific implementations that may need
 * different behavior on different operating systems.
 */
class DefaultSystemInfoProvider : SystemInfoProvider {
    override fun getUserHome(): Path = Paths.get(System.getProperty("user.home"))

    override fun getOsName(): String = System.getProperty("os.name")

    override fun getProgramFilesEnv(): String? = System.getenv("ProgramFiles")

    override fun getProgramFilesX86Env(): String? = System.getenv("ProgramFiles(x86)")
}
