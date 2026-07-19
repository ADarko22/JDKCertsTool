package edu.adarko22.jdkcerts.core.jdk.keytool.classifier

/**
 * Neutral, infra-agnostic classification of a failed keytool execution.
 *
 * This is the single currency produced by [KeytoolErrorClassifier] from raw process signals
 * (exit code + stderr/stdout). It is deliberately independent of the CQRS result types: each use
 * case translates a [KeytoolFailure] into its own side-specific result
 * (`KeytoolCommandResult.Failure.*` / `KeytoolQueryResult.Failure.*` / `KeytoolQueryResult.NotFound`),
 * so the raw keytool string-matching lives in exactly one place.
 *
 * @property rawStderr The raw error stream, retained only for debugging/verbose output.
 */
sealed interface KeytoolFailure {
    val rawStderr: String

    /** The supplied keystore password was rejected. */
    data class WrongPassword(
        override val rawStderr: String,
    ) : KeytoolFailure

    /** The targeted alias is not present in the keystore. */
    data class AliasNotFound(
        override val rawStderr: String,
    ) : KeytoolFailure

    /** An import was rejected because the alias is already taken. */
    data class AliasAlreadyExists(
        override val rawStderr: String,
    ) : KeytoolFailure

    /**
     * An import was rejected because the certificate is already present under another alias.
     *
     * @property conflictingAlias The existing alias holding the certificate, if keytool reported it.
     */
    data class CertificateAlreadyExists(
        val conflictingAlias: String?,
        override val rawStderr: String,
    ) : KeytoolFailure

    /** Any failure that could not be classified into a specific case. */
    data class Unknown(
        val exitCode: Int,
        override val rawStderr: String,
    ) : KeytoolFailure
}
