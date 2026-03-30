package edu.adarko22.jdkcerts.core.jdk.keytool.usecase

import edu.adarko22.jdkcerts.core.jdk.keytool.model.FindCertKeytoolCommand
import edu.adarko22.jdkcerts.core.jdk.keytool.model.KeytoolCommandResult
import edu.adarko22.jdkcerts.core.jdk.keytool.model.KeytoolFindCertResult
import edu.adarko22.jdkcerts.core.jdk.keytool.model.SearchStrategy
import edu.adarko22.jdkcerts.core.jdk.keytool.parser.CertificateInfoParser
import java.nio.file.Path

/**
 * Use case that executes a keytool command to retrieve certificate information from all discovered JDKs
 * (e.g., `keytool -list -v -alias <alias>`) and transforms the successful result into a list of [KeytoolFindCertResult].
 *
 * This use case delegates the execution to [ExecuteKeytoolCommandUseCase] and handles the subsequent parsing
 * with strategy-specific logic based on the [SearchStrategy] used.
 *
 * @param executeKeytoolCommandUseCase The underlying use case for running keytool commands.
 * @param certificateInfoParser Component responsible for parsing raw keytool output into [CertificateParseResult].
 */
class FindKeytoolCertificateUseCase(
    val executeKeytoolCommandUseCase: ExecuteKeytoolCommandUseCase,
    val certificateInfoParser: CertificateInfoParser,
) {
    /**
     * Executes the find certificate command across all JDKs and returns structured information
     * only for those where the command executed successfully (exit code 0) and the
     * certificate details could be successfully parsed.
     *
     * The behavior depends on the [SearchStrategy]:
     * - EXACT_MATCH: Treats result as Found, NotFound, or Error based on parsing
     * - REGEX, CLOSEST_MATCH: Placeholder for future implementation (returns Error for now)
     *
     * Note: Results where the alias was not found or a system error occurred are filtered out.
     *
     * @param keytoolCommand The keytool command (must be a LIST command with alias).
     * @param customJdkDirs Optional custom JDK directories.
     * @return List of [KeytoolFindCertResult] for successfully found and parsed certificates.
     */
    fun execute(
        alias: String,
        keystorePassword: String,
        customJdkDirs: List<Path>,
        searchStrategy: SearchStrategy,
    ): List<KeytoolFindCertResult> {
        val command = FindCertKeytoolCommand(alias, keystorePassword, searchStrategy)
        val results =
            executeKeytoolCommandUseCase
                .execute(command, customJdkDirs, dryRun = false)

        return results.map { commandResult ->
            when (searchStrategy) {
                SearchStrategy.EXACT_MATCH -> toFindCertResultExactMatch(commandResult)
                SearchStrategy.REGEX -> handleRegexSearch(commandResult)
                SearchStrategy.CLOSEST_MATCH -> handleClosestMatchSearch(commandResult)
            }
        }
    }

    /**
     * Maps the raw process result to a domain-specific certificate search result for EXACT_MATCH strategy.
     */
    private fun toFindCertResultExactMatch(result: KeytoolCommandResult): KeytoolFindCertResult =
        when (result) {
            is KeytoolCommandResult.Success -> tryParseOutputExactMatch(result)
            is KeytoolCommandResult.Failure -> mapProcessFailure(result)
        }

    /**
     * Handles the Happy Path: Process succeeded, now try to parse the output using exact match logic.
     */
    private fun tryParseOutputExactMatch(result: KeytoolCommandResult.Success): KeytoolFindCertResult {
        val parseResult = certificateInfoParser.parseCertificateInfo(result.processResult.stdout)

        return if (parseResult.hasCertificates) {
            // For exact match, should have exactly one certificate
            val certificateInfo = parseResult.certificates.firstOrNull()
            if (certificateInfo != null) {
                KeytoolFindCertResult.Found(result.jdk, certificateInfo)
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
                message = "Certificate parsing failed: ${parseResult.errors.first().reason}",
                cause = parseResult.errors.first().cause,
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
     * Handles the Failure Path: Process failed, check if it was just a missing alias.
     */
    private fun mapProcessFailure(result: KeytoolCommandResult.Failure): KeytoolFindCertResult {
        // Standard Keytool behavior: Exit code 1 + "does not exist" message means missing alias.
        val isAliasMissing =
            listOf(result.processResult.stdout, result.processResult.stderr)
                .any { it.contains("does not exist", ignoreCase = true) }

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

    /**
     * Placeholder for REGEX search strategy.
     *
     * TODO: Implement regex-based certificate search logic
     */
    private fun handleRegexSearch(result: KeytoolCommandResult): KeytoolFindCertResult =
        when (result) {
            is KeytoolCommandResult.Success -> {
                // TODO: Parse output and apply regex search logic
                KeytoolFindCertResult.Error(
                    jdk = result.jdk,
                    message = "Regex search strategy not yet implemented",
                )
            }
            is KeytoolCommandResult.Failure -> {
                KeytoolFindCertResult.Error(
                    jdk = result.jdk,
                    message = result.errorMessage,
                )
            }
        }

    /**
     * Placeholder for CLOSEST_MATCH search strategy.
     *
     * TODO: Implement closest match (edit distance/vector distance) search logic
     */
    private fun handleClosestMatchSearch(result: KeytoolCommandResult): KeytoolFindCertResult =
        when (result) {
            is KeytoolCommandResult.Success -> {
                // TODO: Parse output and apply closest match logic
                KeytoolFindCertResult.Error(
                    jdk = result.jdk,
                    message = "Closest match search strategy not yet implemented",
                )
            }
            is KeytoolCommandResult.Failure -> {
                KeytoolFindCertResult.Error(
                    jdk = result.jdk,
                    message = result.errorMessage,
                )
            }
        }
}
