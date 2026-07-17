package edu.adarko22.jdkcerts.core.jdk.keytool.model

import edu.adarko22.jdkcerts.core.jdk.Jdk
import kotlin.collections.List
import kotlin.io.path.absolutePathString

sealed interface KeytoolOperation {
    val alias: String

    /**
     * Returns the command-line arguments for this keytool operation (without keytool executable or keystore args).
     */
    fun getArgs(): List<String>

    /**
     * Builds the full process command including keytool executable path and keystore resolution arguments.
     * This implementation is shared across all command types to avoid duplication.
     *
     * @param jdk The JDK instance providing keytool path and keystore information.
     * @param keystorePassword The keystore password.
     * @return Complete command list ready for process execution.
     */
    fun buildCommand(
        jdk: Jdk,
        keystorePassword: String,
    ): List<String> {
        val keytoolExecutable = jdk.keytoolPath.absolutePathString()
        val keystoreInfo = jdk.keystoreInfo

        val keystoreArgs =
            when {
                keystoreInfo.cacertsShortcutEnabled -> listOf("-cacerts", "-storepass", keystorePassword)
                else -> listOf("-keystore", keystoreInfo.keystorePath.absolutePathString(), "-storepass", keystorePassword)
            }

        return buildList {
            add(keytoolExecutable)
            addAll(getArgs())
            addAll(keystoreArgs)
        }
    }
}

// QRS Separations
sealed interface KeytoolCommand : KeytoolOperation

sealed interface KeytoolQuery : KeytoolOperation
