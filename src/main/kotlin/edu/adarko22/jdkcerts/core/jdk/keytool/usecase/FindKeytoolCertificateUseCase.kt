package edu.adarko22.jdkcerts.core.jdk.keytool.usecase

import edu.adarko22.jdkcerts.core.algorithms.strings.FuzzyMatcher
import edu.adarko22.jdkcerts.core.algorithms.strings.LevenshteinDistanceFuzzyMatcher
import edu.adarko22.jdkcerts.core.execution.KeytoolProcessResult
import edu.adarko22.jdkcerts.core.execution.KeytoolProcessRunner
import edu.adarko22.jdkcerts.core.jdk.DiscoverJdksUseCase
import edu.adarko22.jdkcerts.core.jdk.keytool.classifier.KeytoolErrorClassifier
import edu.adarko22.jdkcerts.core.jdk.keytool.classifier.KeytoolFailure
import edu.adarko22.jdkcerts.core.jdk.keytool.model.ExecutionContext
import edu.adarko22.jdkcerts.core.jdk.keytool.model.FindCertKeytoolQuery
import edu.adarko22.jdkcerts.core.jdk.keytool.model.KeytoolQueryResult
import edu.adarko22.jdkcerts.core.jdk.keytool.model.SearchStrategy
import edu.adarko22.jdkcerts.core.jdk.keytool.parser.CertificateInfoParser
import edu.adarko22.jdkcerts.core.jdk.keytool.parser.CertificateParseResult
import java.util.regex.PatternSyntaxException

/**
 * CQRS **query** use case: finds certificates across all JDK keystores using configurable strategies.
 *
 * Executes keytool on each discovered JDK, then maps the neutral [KeytoolProcessResult] into a
 * semantic [KeytoolQueryResult]. Raw keytool failures are interpreted by [KeytoolErrorClassifier]
 * (the single interpretation home); "alias does not exist" is a legitimate [KeytoolQueryResult.NotFound]
 * rather than a failure.
 *
 * @param keytoolProcessRunner Executes keytool commands
 * @param certificateInfoParser Parses keytool output into certificate objects
 */
class FindKeytoolCertificateUseCase(
    val jdkDiscoverJdksUseCase: DiscoverJdksUseCase,
    val keytoolProcessRunner: KeytoolProcessRunner,
    val certificateInfoParser: CertificateInfoParser,
    val errorClassifier: KeytoolErrorClassifier = KeytoolErrorClassifier(),
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
     * @param findCertKeytoolQuery The keytool query configuration.
     * @param executionContext The context for executing the keytool command on the system.
     * @return One [KeytoolQueryResult] per discovered JDK.
     */
    suspend fun execute(
        findCertKeytoolQuery: FindCertKeytoolQuery,
        executionContext: ExecutionContext,
    ): List<KeytoolQueryResult> {
        val jdks = jdkDiscoverJdksUseCase.discover(executionContext.customJdkPaths)
        return keytoolProcessRunner
            .runConcurrently(findCertKeytoolQuery, jdks, executionContext.masterPassword, executionContext.dryRun)
            .map { outcome ->
                when (outcome) {
                    is KeytoolProcessResult.DryRun -> {
                        KeytoolQueryResult.DryRun(outcome.jdk, outcome.previewCommand)
                    }

                    is KeytoolProcessResult.Executed -> {
                        if (outcome.exitCode == 0) {
                            handleSuccess(outcome, findCertKeytoolQuery.alias, findCertKeytoolQuery.searchStrategy)
                        } else {
                            handleFailure(outcome, findCertKeytoolQuery.alias)
                        }
                    }
                }
            }
    }

    private fun handleSuccess(
        outcome: KeytoolProcessResult.Executed,
        alias: String,
        searchStrategy: SearchStrategy,
    ): KeytoolQueryResult {
        val parseResult = certificateInfoParser.parseCertificateInfo(outcome.stdout)

        return when (searchStrategy) {
            SearchStrategy.EXACT_MATCH -> handleExactMatchSearch(outcome, parseResult)
            SearchStrategy.REGEX -> handleRegexSearch(outcome, parseResult, alias)
            SearchStrategy.CLOSEST_MATCH -> handleClosestMatchSearch(outcome, parseResult, alias)
        }
    }

    /**
     * Handles exact alias lookup: keytool -list -v -alias <alias>. Expected: single certificate.
     */
    private fun handleExactMatchSearch(
        outcome: KeytoolProcessResult.Executed,
        parseResult: CertificateParseResult,
    ): KeytoolQueryResult =
        if (parseResult.hasCertificates) {
            KeytoolQueryResult.Found(outcome.jdk, listOf(parseResult.certificates.first()))
        } else {
            noCertificates(outcome, parseResult, "No certificates found in keytool output")
        }

    /**
     * Handles regex strategy: loads all certificates and applies regex matching.
     */
    private fun handleRegexSearch(
        outcome: KeytoolProcessResult.Executed,
        parseResult: CertificateParseResult,
        alias: String,
    ): KeytoolQueryResult {
        val aliasRegex =
            try {
                Regex(alias)
            } catch (e: PatternSyntaxException) {
                return KeytoolQueryResult.Failure.InvalidPattern(outcome.jdk, alias, "Invalid regex pattern: ${e.message}")
            }

        if (!parseResult.hasCertificates) {
            return noCertificates(outcome, parseResult, "No certificates found in keystore")
        }

        val matchedCertificates = parseResult.certificates.filter { it.alias.matches(aliasRegex) }
        return if (matchedCertificates.isNotEmpty()) {
            KeytoolQueryResult.Found(outcome.jdk, matchedCertificates)
        } else {
            KeytoolQueryResult.NotFound(outcome.jdk, "No Alias Name found matching regex `$alias`")
        }
    }

    /**
     * Handles closest match strategy: loads all certificates and finds the best match by distance.
     */
    private fun handleClosestMatchSearch(
        outcome: KeytoolProcessResult.Executed,
        parseResult: CertificateParseResult,
        alias: String,
    ): KeytoolQueryResult {
        if (!parseResult.hasCertificates) {
            return noCertificates(outcome, parseResult, "No certificates found in keystore")
        }

        val certificatesWithScores = parseResult.certificates.associateWith { fuzzyMatcher.similarityScore(it.alias, alias) }
        val highestSimilarityScore = certificatesWithScores.values.maxOrNull() ?: 0.0

        return if (highestSimilarityScore < similarityThreshold) {
            KeytoolQueryResult.NotFound(outcome.jdk, "No Alias Name found as closest match to `$alias`")
        } else {
            val topResults = certificatesWithScores.filterValues { it == highestSimilarityScore }.keys.toList()
            KeytoolQueryResult.Found(outcome.jdk, topResults)
        }
    }

    /**
     * Common handling for an exit-0 run that yielded no usable certificates: a parse error if the
     * parser choked, otherwise a plain not-found.
     */
    private fun noCertificates(
        outcome: KeytoolProcessResult.Executed,
        parseResult: CertificateParseResult,
        notFoundReason: String,
    ): KeytoolQueryResult =
        if (parseResult.errors.isNotEmpty()) {
            val firstError = parseResult.errors.first()
            KeytoolQueryResult.Failure.ParseError(
                jdk = outcome.jdk,
                message = "Certificate parsing failed: ${firstError.reason}",
                rawStdout = outcome.stdout,
                rawStderr = outcome.stderr,
                cause = firstError.cause,
            )
        } else {
            KeytoolQueryResult.NotFound(outcome.jdk, notFoundReason)
        }

    /**
     * Maps a non-zero keytool exit into a query result: a missing alias is a legitimate NotFound;
     * everything else is a typed failure.
     */
    private fun handleFailure(
        outcome: KeytoolProcessResult.Executed,
        alias: String,
    ): KeytoolQueryResult =
        when (val failure = errorClassifier.classify(outcome.exitCode, outcome.stdout, outcome.stderr)) {
            is KeytoolFailure.AliasNotFound -> {
                KeytoolQueryResult.NotFound(outcome.jdk, "Alias `$alias` not found in keystore")
            }

            is KeytoolFailure.WrongPassword -> {
                KeytoolQueryResult.Failure.WrongPassword(outcome.jdk, failure.rawStderr)
            }

            else -> {
                KeytoolQueryResult.Failure.Unknown(outcome.jdk, outcome.exitCode, failure.rawStderr)
            }
        }
}
