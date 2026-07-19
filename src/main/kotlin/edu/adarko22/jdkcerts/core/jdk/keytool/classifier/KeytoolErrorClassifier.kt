package edu.adarko22.jdkcerts.core.jdk.keytool.classifier

/**
 * Translates the raw signals of a failed keytool execution (exit code + output streams) into a
 * neutral, semantic [KeytoolFailure].
 *
 * This is the **single home** for keytool output string-matching. Both the command and the query
 * use cases delegate here instead of sniffing stderr themselves, keeping interpretation consistent
 * and testable in isolation.
 */
class KeytoolErrorClassifier {
    /**
     * Classifies a non-successful keytool execution.
     *
     * @param exitCode The process exit code (expected to be non-zero; retained for [KeytoolFailure.Unknown]).
     * @param stdout Standard output captured from keytool (some keytool errors print here).
     * @param stderr Standard error captured from keytool.
     */
    fun classify(
        exitCode: Int,
        stdout: String,
        stderr: String,
    ): KeytoolFailure {
        val combined = "$stdout\n$stderr"

        return when {
            combined.containsIgnoreCase("password was incorrect") ||
                combined.containsIgnoreCase("keystore was tampered with") -> {
                KeytoolFailure.WrongPassword(stderr)
            }

            combined.containsIgnoreCase("already exists in keystore under alias") -> {
                KeytoolFailure.CertificateAlreadyExists(extractConflictingAlias(combined), stderr)
            }

            combined.containsIgnoreCase("already exists") -> {
                KeytoolFailure.AliasAlreadyExists(stderr)
            }

            combined.containsIgnoreCase("does not exist") -> {
                KeytoolFailure.AliasNotFound(stderr)
            }

            else -> {
                KeytoolFailure.Unknown(exitCode, stderr)
            }
        }
    }

    private fun extractConflictingAlias(output: String): String? =
        CONFLICTING_ALIAS_REGEX
            .find(output)
            ?.groupValues
            ?.get(1)
            ?.trim()

    private fun String.containsIgnoreCase(needle: String): Boolean = contains(needle, ignoreCase = true)

    private companion object {
        // Matches keytool's "... already exists in keystore under alias <the-alias>".
        val CONFLICTING_ALIAS_REGEX = Regex("under alias\\s+<([^>]+)>", RegexOption.IGNORE_CASE)
    }
}
