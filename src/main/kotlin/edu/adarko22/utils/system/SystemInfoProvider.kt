package edu.adarko22.utils.system

import java.nio.file.Path

/**
 * Interface for accessing system-related information in a platform-independent way.
 *
 * This abstraction allows for easy testing and platform-specific implementations
 * by removing direct dependencies on System properties and environment variables.
 * It provides access to user home directory, OS information, and platform-specific
 * environment variables like Program Files on Windows.
 */
interface SystemInfoProvider {
    fun getUserHome(): Path

    fun getOsName(): String

    fun getProgramFilesEnv(): String?

    fun getProgramFilesX86Env(): String?
}
