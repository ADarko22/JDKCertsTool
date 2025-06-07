package edu.adarko22.utils.system

import java.nio.file.Path

/**
 * Provides access to system-related information, abstracting away static calls to java.lang.System.
 */
interface SystemInfoProvider {
    fun getUserHome(): Path
    fun getOsName(): String
    fun getProgramFilesEnv(): String?
    fun getProgramFilesX86Env(): String?
}