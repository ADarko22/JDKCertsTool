package edu.adarko22.jdkcerts.core.jdk.keytool.model

import edu.adarko22.jdkcerts.core.jdk.Jdk
import kotlin.collections.List
import kotlin.io.path.absolutePathString

/**
 * Represents a keytool operation and composes the full CLI invocation.
 *
 * Implementations provide operation-specific args via [getArgs]. [buildCommand]
 * prefixes the keytool executable from the target [Jdk] and appends keystore args.
 */
sealed interface KeytoolOperation {
    /**
     * The alias associated with the operation (if applicable).
     */
    val alias: String

    /**
     * Returns the keytool-specific command-line arguments for this operation (excluding the
     * keytool executable and keystore selection arguments).
     */
    fun getArgs(): List<String>

    /**
     * Builds the full process command including keytool executable path and keystore resolution arguments.
     *
     * @param jdk The JDK instance providing keytool path and keystore information.
     * @param keystorePassword The keystore password to pass as `-storepass`.
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

/**
 * Marker for mutating keytool operations (commands) such as import or delete.
 */
sealed interface KeytoolCommand : KeytoolOperation

/**
 * Marker for read-only keytool operations (queries) such as listing certificates.
 */
sealed interface KeytoolQuery : KeytoolOperation
