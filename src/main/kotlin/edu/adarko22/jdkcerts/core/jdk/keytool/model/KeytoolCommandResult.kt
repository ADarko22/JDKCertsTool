package edu.adarko22.jdkcerts.core.jdk.keytool.model

import edu.adarko22.jdkcerts.core.jdk.Jdk

/**
 * CQRS **command**-side result: the outcome of a mutating keytool operation (import/delete) on a JDK.
 *
 * Infra-agnostic — it carries no exit codes or process streams, only semantic outcomes. Failures are
 * a typed taxonomy so the presentation layer can react to each case; the raw stderr is retained purely
 * for optional debugging.
 */
sealed class KeytoolCommandResult {
    abstract val jdk: Jdk

    /** The operation completed successfully. */
    data class Success(
        override val jdk: Jdk,
        val message: String = "Operation completed successfully.",
    ) : KeytoolCommandResult()

    /**
     * The operation was not executed because the run was a dry run.
     *
     * @property previewCommand The keytool command that would have been executed.
     */
    data class DryRun(
        override val jdk: Jdk,
        val previewCommand: String,
    ) : KeytoolCommandResult()

    /** The operation failed. */
    sealed class Failure : KeytoolCommandResult() {
        abstract val rawStderr: String

        /** The supplied keystore password was rejected. */
        data class WrongPassword(
            override val jdk: Jdk,
            override val rawStderr: String,
        ) : Failure()

        /** An import was rejected because [alias] is already taken. */
        data class AliasAlreadyExists(
            override val jdk: Jdk,
            val alias: String,
            override val rawStderr: String,
        ) : Failure()

        /** An import was rejected because the certificate already exists under [conflictingAlias]. */
        data class CertificateAlreadyExists(
            override val jdk: Jdk,
            val conflictingAlias: String?,
            override val rawStderr: String,
        ) : Failure()

        /** A delete was rejected because [alias] does not exist in the keystore. */
        data class AliasNotFound(
            override val jdk: Jdk,
            val alias: String,
            override val rawStderr: String,
        ) : Failure()

        /** Any failure that could not be classified into a specific case. */
        data class Unknown(
            override val jdk: Jdk,
            val exitCode: Int,
            override val rawStderr: String,
        ) : Failure()
    }
}
