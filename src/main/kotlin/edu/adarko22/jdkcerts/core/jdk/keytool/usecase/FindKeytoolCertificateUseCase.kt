package edu.adarko22.jdkcerts.core.jdk.keytool.usecase

import edu.adarko22.jdkcerts.core.algorithms.strings.FuzzyMatcher
import edu.adarko22.jdkcerts.core.algorithms.strings.LevenshteinDistanceFuzzyMatcher
import edu.adarko22.jdkcerts.core.jdk.keytool.model.FindCertKeytoolCommand
import edu.adarko22.jdkcerts.core.jdk.keytool.model.KeytoolCommandResult
import edu.adarko22.jdkcerts.core.jdk.keytool.model.KeytoolFindCertResult
import edu.adarko22.jdkcerts.core.jdk.keytool.model.SearchStrategy
import edu.adarko22.jdkcerts.core.jdk.keytool.parser.CertificateInfoParser
import java.nio.file.Path
import java.util.regex.PatternSyntaxException

/**
 * Finds certificates across all JDK keystores using configurable search strategies.
 *
 * Executes keytool on each discovered JDK, parses results, and applies strategy-specific logic.
 *
 * @param executeKeytoolCommandUseCase Executes keytool commands
 * @param certificateInfoParser Parses keytool output into certificate objects
 */
class FindKeytoolCertificateUseCase(
    val executeKeytoolCommandUseCase: ExecuteKeytoolCommandUseCase,
    val certificateInfoParser: CertificateInfoParser,
    val fuzzyMatcher: FuzzyMatcher = LevenshteinDistanceFuzzyMatcher(),
    val similarityThreshold: Double = 0.3,
) {
    /**
     * Searches for certificates across all discovered JDKs.
     *
     * Behavior depends on [searchStrategy]:
     * - **EXACT_MATCH**: Direct alias lookup using keytool's `-alias` option (fastest)
     * - **REGEX**: Pattern matching across all keystore entries (slower, may have multiple matches)
     * - **CLOSEST_MATCH**: Fuzzy matching using edit/vector distance (slowest, single best match)
     *
     * @param alias Certificate alias or search pattern (interpretation depends on strategy)
     * @param keystorePassword Keystore password
     * @param customJdkDirs Optional custom JDK directories
     * @param searchStrategy Search strategy to apply
     * @return List of results, one per JDK (Found, NotFound, or Error)
     */
    suspend fun execute(
        alias: String,
        keystorePassword: String,
        customJdkDirs: List<Path>,
        searchStrategy: SearchStrategy,
    ): List<KeytoolFindCertResult> {
        val command = FindCertKeytoolCommand(alias, keystorePassword, searchStrategy)
        return executeKeytoolCommandUseCase.execute(command, customJdkDirs, dryRun = false).map {
            when (it) {
                is KeytoolCommandResult.Success -> handleKeytoolCommandSuccess(it, alias, searchStrategy)
                is KeytoolCommandResult.Failure -> handleProcessFailure(it)
            }
        }
    }

    private fun handleKeytoolCommandSuccess(
        result: KeytoolCommandResult.Success,
        alias: String,
        searchStrategy: SearchStrategy,
    ): KeytoolFindCertResult =
        when (searchStrategy) {
            SearchStrategy.EXACT_MATCH -> handleExactMatchSearch(result)
            SearchStrategy.REGEX -> handleRegexSearch(result, alias)
            SearchStrategy.CLOSEST_MATCH -> handleClosestMatchSearch(result, alias)
        }

    /**
     * Handles exact alias lookup: keytool -list -v -alias <alias>.
     *
     * Expected: Single certificate or "alias does not exist" error.
     */
    private fun handleExactMatchSearch(result: KeytoolCommandResult.Success): KeytoolFindCertResult {
        val parseResult = certificateInfoParser.parseCertificateInfo(result.processResult.stdout)

        return if (parseResult.hasCertificates) {
            val certificateInfo = parseResult.certificates.firstOrNull()
            if (certificateInfo != null) {
                KeytoolFindCertResult.Found(result.jdk, listOf(certificateInfo))
            } else {
                KeytoolFindCertResult.NotFound(
                    jdk = result.jdk,
                    reason = "No certificates found in keytool output",
                    stdout = result.processResult.stdout,
                    stderr = result.processResult.stderr,
                )
            }
        } else if (parseResult.errors.isNotEmpty()) {
            KeytoolFindCertResult.Error(
                jdk = result.jdk,
                message = "Certificate parsing failed: ${parseResult.errors.firstOrNull()?.reason}",
                cause = parseResult.errors.firstOrNull()?.cause,
            )
        } else {
            KeytoolFindCertResult.NotFound(
                jdk = result.jdk,
                reason = "Output parsing failed: No certificates found",
                stdout = result.processResult.stdout,
                stderr = result.processResult.stderr,
            )
        }
    }

    /**
     * Handles regex strategy: loads all certificates and applies regex matching.
     */
    private fun handleRegexSearch(
        result: KeytoolCommandResult.Success,
        alias: String,
    ): KeytoolFindCertResult {
        val parseResult = certificateInfoParser.parseCertificateInfo(result.processResult.stdout)

        try {
            val aliasRegex = Regex(alias)

            if (parseResult.hasCertificates) {
                val matchedCertificates = parseResult.certificates.filter { it.alias.matches(aliasRegex) }

                return if (matchedCertificates.isNotEmpty()) {
                    KeytoolFindCertResult.Found(
                        result.jdk,
                        certificateInfos = matchedCertificates,
                    )
                } else {
                    KeytoolFindCertResult.NotFound(
                        result.jdk,
                        "No Alias Name found matching regex `$alias`",
                        stdout = result.processResult.stdout,
                        stderr = result.processResult.stderr,
                    )
                }
            } else {
                return KeytoolFindCertResult.Error(
                    jdk = result.jdk,
                    message = "No certificates found in keystore",
                )
            }
        } catch (e: PatternSyntaxException) {
            return KeytoolFindCertResult.Error(
                jdk = result.jdk,
                message = "Invalid regex pattern: ${e.message}",
                cause = e,
            )
        }
    }

    /**
     * Handles closest match strategy: loads all certificates and finds best match by distance.
     */
    private fun handleClosestMatchSearch(
        result: KeytoolCommandResult.Success,
        alias: String,
    ): KeytoolFindCertResult {
        val parseResult = certificateInfoParser.parseCertificateInfo(result.processResult.stdout)

        if (parseResult.hasCertificates) {
            val certificatesWithScores = parseResult.certificates.associateWith { fuzzyMatcher.similarityScore(it.alias, alias) }
            val highestSimilarityScore = certificatesWithScores.values.maxOrNull() ?: 0.0

            if (highestSimilarityScore < similarityThreshold) {
                return KeytoolFindCertResult.NotFound(
                    result.jdk,
                    "No Alias Name found as closest match to `$alias`",
                    stdout = result.processResult.stdout,
                    stderr = result.processResult.stderr,
                )
            }

            val topResults = certificatesWithScores.filterValues { it == highestSimilarityScore }.keys.toList()
            return KeytoolFindCertResult.Found(
                jdk = result.jdk,
                certificateInfos = topResults,
            )
        } else {
            return KeytoolFindCertResult.Error(
                jdk = result.jdk,
                message = "No certificates found in keystore",
            )
        }
    }

    /**
     * Handles process failures: distinguishes missing aliases from system errors.
     *
     * Keytool exit 1 with "does not exist" = missing alias (NotFound).
     * Other failures = system/permission error (Error).
     */
    private fun handleProcessFailure(result: KeytoolCommandResult.Failure): KeytoolFindCertResult {
        val isAliasMissing =
            listOf(result.processResult.stdout, result.processResult.stderr).any { it.contains("does not exist", ignoreCase = true) }

        return if (isAliasMissing) {
            KeytoolFindCertResult.NotFound(
                jdk = result.jdk,
                reason = "Alias not found in keystore",
                stdout = result.processResult.stdout,
                stderr = result.processResult.stderr,
            )
        } else {
            KeytoolFindCertResult.Error(result.jdk, result.errorMessage)
        }
    }
}
