package edu.adarko22.jdkcerts.core.jdk.keytool.usecase

import edu.adarko22.jdkcerts.core.algorithms.strings.FuzzyMatcher
import edu.adarko22.jdkcerts.core.algorithms.strings.LevenshteinDistanceFuzzyMatcher
import edu.adarko22.jdkcerts.core.execution.KeytoolProcessRunner
import edu.adarko22.jdkcerts.core.jdk.DiscoverJdksUseCase
import edu.adarko22.jdkcerts.core.jdk.keytool.model.ExecutionContext
import edu.adarko22.jdkcerts.core.jdk.keytool.model.FindCertKeytoolQuery
import edu.adarko22.jdkcerts.core.jdk.keytool.model.KeytoolOperationResult
import edu.adarko22.jdkcerts.core.jdk.keytool.model.KeytoolQueryResult
import edu.adarko22.jdkcerts.core.jdk.keytool.model.SearchStrategy
import edu.adarko22.jdkcerts.core.jdk.keytool.parser.CertificateInfoParser
import java.util.regex.PatternSyntaxException

/**
 * Finds certificates across all JDK keystores using configurable search strategies.
 *
 * Executes keytool on each discovered JDK, parses results, and applies strategy-specific logic.
 *
 * @param keytoolProcessRunner Executes keytool commands
 * @param certificateInfoParser Parses keytool output into certificate objects
 */
class FindKeytoolCertificateUseCase(
    val jdkDiscoverJdksUseCase: DiscoverJdksUseCase,
    val keytoolProcessRunner: KeytoolProcessRunner,
    val certificateInfoParser: CertificateInfoParser,
    val fuzzyMatcher: FuzzyMatcher = LevenshteinDistanceFuzzyMatcher(),
    val similarityThreshold: Double = 0.3,
) {
    /**
     * Searches for certificates across all discovered JDKs.
     *
     * Behavior depends on [FindCertKeytoolQuery.searchStrategy]:
     * - **EXACT_MATCH**: Direct alias lookup using keytool's `-alias` option (fastest)
     * - **REGEX**: Pattern matching across all keystore entries (slower, may have multiple matches)
     * - **CLOSEST_MATCH**: Fuzzy matching using edit/vector distance (slowest, single best match)
     *
     * @param findCertKeytoolQuery The keytool query configuration (responsible for building its own arguments).
     * @param executionContext The context for executing the keytool command on the system.
     * @return List of results, one per JDK (Found, NotFound, or Error)
     */
    suspend fun execute(
        findCertKeytoolQuery: FindCertKeytoolQuery,
        executionContext: ExecutionContext,
    ): List<KeytoolQueryResult> {
        val jdks = jdkDiscoverJdksUseCase.discover(executionContext.customJdkDirs)
        return keytoolProcessRunner
            .runConcurrently(findCertKeytoolQuery, jdks, executionContext.masterPassword, executionContext.dryRun)
            .map {
                when (it) {
                    is KeytoolOperationResult.Success -> {
                        handleKeytoolCommandSuccess(it, findCertKeytoolQuery.alias, findCertKeytoolQuery.searchStrategy)
                    }

                    is KeytoolOperationResult.Failure -> {
                        handleProcessFailure(it)
                    }
                }
            }
    }

    private fun handleKeytoolCommandSuccess(
        result: KeytoolOperationResult.Success,
        alias: String,
        searchStrategy: SearchStrategy,
    ): KeytoolQueryResult =
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
    private fun handleExactMatchSearch(result: KeytoolOperationResult.Success): KeytoolQueryResult {
        val parseResult = certificateInfoParser.parseCertificateInfo(result.processResult.stdout)

        return if (parseResult.hasCertificates) {
            val certificateInfo = parseResult.certificates.firstOrNull()
            if (certificateInfo != null) {
                KeytoolQueryResult.Found(result.jdk, listOf(certificateInfo))
            } else {
                KeytoolQueryResult.NotFound(
                    jdk = result.jdk,
                    reason = "No certificates found in keytool output",
                    stdout = result.processResult.stdout,
                    stderr = result.processResult.stderr,
                )
            }
        } else if (parseResult.errors.isNotEmpty()) {
            KeytoolQueryResult.Error(
                jdk = result.jdk,
                message = "Certificate parsing failed: ${parseResult.errors.firstOrNull()?.reason}",
                cause = parseResult.errors.firstOrNull()?.cause,
            )
        } else {
            KeytoolQueryResult.NotFound(
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
        result: KeytoolOperationResult.Success,
        alias: String,
    ): KeytoolQueryResult {
        val parseResult = certificateInfoParser.parseCertificateInfo(result.processResult.stdout)

        try {
            val aliasRegex = Regex(alias)

            return if (parseResult.hasCertificates) {
                val matchedCertificates = parseResult.certificates.filter { it.alias.matches(aliasRegex) }

                if (matchedCertificates.isNotEmpty()) {
                    KeytoolQueryResult.Found(
                        result.jdk,
                        certificateInfos = matchedCertificates,
                    )
                } else {
                    KeytoolQueryResult.NotFound(
                        result.jdk,
                        "No Alias Name found matching regex `$alias`",
                        stdout = result.processResult.stdout,
                        stderr = result.processResult.stderr,
                    )
                }
            } else {
                KeytoolQueryResult.Error(
                    jdk = result.jdk,
                    message = "No certificates found in keystore",
                )
            }
        } catch (e: PatternSyntaxException) {
            return KeytoolQueryResult.Error(
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
        result: KeytoolOperationResult.Success,
        alias: String,
    ): KeytoolQueryResult {
        val parseResult = certificateInfoParser.parseCertificateInfo(result.processResult.stdout)

        return if (parseResult.hasCertificates) {
            val certificatesWithScores = parseResult.certificates.associateWith { fuzzyMatcher.similarityScore(it.alias, alias) }
            val highestSimilarityScore = certificatesWithScores.values.maxOrNull() ?: 0.0

            if (highestSimilarityScore < similarityThreshold) {
                KeytoolQueryResult.NotFound(
                    result.jdk,
                    "No Alias Name found as closest match to `$alias`",
                    stdout = result.processResult.stdout,
                    stderr = result.processResult.stderr,
                )
            } else {
                val topResults = certificatesWithScores.filterValues { it == highestSimilarityScore }.keys.toList()

                KeytoolQueryResult.Found(
                    jdk = result.jdk,
                    certificateInfos = topResults,
                )
            }
        } else {
            KeytoolQueryResult.Error(
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
    private fun handleProcessFailure(result: KeytoolOperationResult.Failure): KeytoolQueryResult {
        val isAliasMissing =
            listOf(result.processResult.stdout, result.processResult.stderr).any { it.contains("does not exist", ignoreCase = true) }

        return if (isAliasMissing) {
            KeytoolQueryResult.NotFound(
                jdk = result.jdk,
                reason = "Alias not found in keystore",
                stdout = result.processResult.stdout,
                stderr = result.processResult.stderr,
            )
        } else {
            KeytoolQueryResult.Error(result.jdk, result.errorMessage)
        }
    }
}
