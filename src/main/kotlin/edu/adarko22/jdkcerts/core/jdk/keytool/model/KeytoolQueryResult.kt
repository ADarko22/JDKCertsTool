package edu.adarko22.jdkcerts.core.jdk.keytool.model

import edu.adarko22.jdkcerts.core.jdk.Jdk

/**
 * Represents the high-level result of searching for a specific certificate in a JDK.
 */

sealed class KeytoolQueryResult {
    abstract val jdk: Jdk

    data class Found(
        override val jdk: Jdk,
        val certificateInfos: List<CertificateInfo>,
    ) : KeytoolQueryResult()

    data class NotFound(
        override val jdk: Jdk,
        val reason: String,
    ) : KeytoolQueryResult()

    /**
     * The query was not executed because the run was a dry run.
     *
     * @property previewCommand The keytool command that would have been executed.
     */
    data class DryRun(
        override val jdk: Jdk,
        val previewCommand: String,
    ) : KeytoolQueryResult()

    sealed class Failure : KeytoolQueryResult() {
        abstract val rawStderr: String

        data class WrongPassword(
            override val jdk: Jdk,
            override val rawStderr: String,
        ) : Failure()

        data class ParseError(
            override val jdk: Jdk,
            val message: String,
            val rawStdout: String,
            override val rawStderr: String,
            val cause: Throwable? = null,
        ) : Failure()

        /** The user-supplied search pattern (regex) was invalid. */
        data class InvalidPattern(
            override val jdk: Jdk,
            val pattern: String,
            val message: String,
            override val rawStderr: String = "",
        ) : Failure()

        data class Unknown(
            override val jdk: Jdk,
            val exitCode: Int,
            override val rawStderr: String,
        ) : Failure()
    }
}
