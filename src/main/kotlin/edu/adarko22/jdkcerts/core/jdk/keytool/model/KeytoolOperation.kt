package edu.adarko22.jdkcerts.core.jdk.keytool.model

import edu.adarko22.jdkcerts.core.jdk.Jdk
import kotlin.io.path.absolutePathString

/**
 * Represents a keytool operation and composes the full CLI invocation.

 */
sealed interface KeytoolOperation {
    val alias: String
}

/**
 * A [KeytoolOperation] for mutating keytool operations (commands) such as import or delete.
 */
sealed interface KeytoolCommand : KeytoolOperation

/**
 * Represents a [KeytoolCommand] for installing certificates.
 *
 * @property alias The certificate alias.
 * @property certificateAbsolutePath The absolute path to the certificate file.
 */
data class InstallCertKeytoolCommand(
    override val alias: String,
    val certificateAbsolutePath: String,
) : KeytoolCommand

/**
 * Represents a [KeytoolCommand] for removing certificates.
 *
 * @property alias The certificate alias.
 */
data class RemoveCertKeytoolCommand(
    override val alias: String,
) : KeytoolCommand

/**
 *  A [KeytoolOperation] for read-only keytool operations (queries) such as listing certificates.
 */
sealed interface KeytoolQuery : KeytoolOperation

/**
 * Represents a [KeytoolQuery] for finding certificates.
 *
 * @property alias The certificate alias or search pattern.
 * @property searchStrategy The search strategy (EXACT_MATCH, CLOSEST_MATCH, REGEX).
 */
data class FindCertKeytoolQuery(
    override val alias: String,
    val searchStrategy: SearchStrategy,
) : KeytoolQuery
