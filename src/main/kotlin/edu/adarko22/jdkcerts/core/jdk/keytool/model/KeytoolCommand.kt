package edu.adarko22.jdkcerts.core.jdk.keytool.model

import edu.adarko22.jdkcerts.core.jdk.Jdk
import kotlin.io.path.absolutePathString

/**
 * Represents a `keytool` command with arguments and automatic keystore resolution.
 *
 * @property alias The certificate alias (if applicable for this command).
 * @property keystorePassword The keystore password.
 */
interface KeytoolCommand {
    val alias: String
    val keystorePassword: String

    /**
     * Returns the command-line arguments for this keytool operation (without keytool executable or keystore args).
     */
    fun getArgs(): List<String>

    /**
     * Builds the full process command including keytool executable path and keystore resolution arguments.
     * This implementation is shared across all command types to avoid duplication.
     *
     * @param jdk The JDK instance providing keytool path and keystore information.
     * @return Complete command list ready for process execution.
     */
    fun buildCommand(jdk: Jdk): List<String> {
        val keytoolExecutable = jdk.keytoolPath.absolutePathString()
        val keystoreInfo = jdk.keystoreInfo

        val keystoreArgs =
            when {
                keystoreInfo.cacertsShortcutEnabled -> listOf("-cacerts")
                else -> listOf("-keystore", keystoreInfo.keystorePath.absolutePathString())
            }

        return buildList {
            add(keytoolExecutable)
            addAll(getArgs())
            addAll(keystoreArgs)
        }
    }
}
