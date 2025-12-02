package edu.adarko22.jdkcerts.infra.system

import java.nio.file.Path

/**
 * Provides system-level information, such as OS name and user home directory.
 */
interface SystemInfoProvider {
    /** Returns the current user's home directory. */
    fun getUserHome(): Path

    /** Returns the operating system name. */
    fun getOsName(): String

    /** Returns the value of the 'ProgramFiles' environment variable, if available. */
    fun getProgramFilesEnv(): String?

    /** Returns the value of the 'ProgramFiles(x86)' environment variable, if available. */
    fun getProgramFilesX86Env(): String?
}
