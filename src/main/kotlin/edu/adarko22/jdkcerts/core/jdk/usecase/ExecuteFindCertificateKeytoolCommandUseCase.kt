package edu.adarko22.jdkcerts.core.jdk.usecase

import edu.adarko22.jdkcerts.core.jdk.KeytoolCommand
import edu.adarko22.jdkcerts.core.jdk.KeytoolCommandResult
import edu.adarko22.jdkcerts.core.jdk.KeytoolFindCertResult
import edu.adarko22.jdkcerts.core.jdk.parser.CertificateInfoParser
import java.nio.file.Path

/**
 * Use case that executes a keytool command to retrieve certificate information from all discovered JDKs
 * (e.g., `keytool -list -v -alias <alias>`) and transforms the successful result into a list of [KeytoolFindCertResult].
 *
 * This use case delegates the execution to [ExecuteKeytoolCommandUseCase] and handles the subsequent parsing.
 *
 * @param executeKeytoolCommandUseCase The underlying use case for running keytool commands.
 * @param certificateInfoParser Component responsible for parsing raw keytool output into [KeytoolFindCertResult].
 */
class ExecuteFindCertificateKeytoolCommandUseCase(
    val executeKeytoolCommandUseCase: ExecuteKeytoolCommandUseCase,
    val certificateInfoParser: CertificateInfoParser,
) {
    /**
     * Executes the find certificate command across all JDKs and returns structured information
     * only for those where the command executed successfully (exit code 0) and the
     * certificate details could be successfully parsed.
     *
     * Note: Results where the alias was not found or a system error occurred are filtered out.
     *
     * @param keytoolCommand The keytool command (must be a LIST command with alias).
     * @param customJdkDirs Optional custom JDK directories.
     * @return List of [KeytoolFindCertResult] for successfully found and parsed certificates.
     */
    fun execute(
        keytoolCommand: KeytoolCommand,
        customJdkDirs: List<Path>,
    ): List<KeytoolFindCertResult> =
        executeKeytoolCommandUseCase
            .execute(keytoolCommand, customJdkDirs, dryRun = false)
            .map { it.toFindCertResult() }

    /**
     * Maps the raw process result to a domain-specific certificate search result.
     */
    private fun KeytoolCommandResult.toFindCertResult(): KeytoolFindCertResult =
        when (this) {
            is KeytoolCommandResult.Success -> tryParseOutput(this)
            is KeytoolCommandResult.Failure -> mapProcessFailure(this)
        }

    /**
     * Handles the Happy Path: Process succeeded, now try to parse the output.
     */
    private fun tryParseOutput(result: KeytoolCommandResult.Success): KeytoolFindCertResult =
        try {
            val info = certificateInfoParser.parseCertificateInfo(result.processResult.stdout)
            KeytoolFindCertResult.Found(result.jdk, info)
        } catch (e: IllegalArgumentException) {
            // Parser contract: Missing expected fields implies the output format didn't match  a valid certificate
            KeytoolFindCertResult.NotFound(
                jdk = result.jdk,
                reason = e.message ?: "Output parsing failed",
                stdout = result.processResult.stdout,
                stderr = result.processResult.stderr,
            )
        } catch (e: Exception) {
            KeytoolFindCertResult.Error(result.jdk, "Unexpected parsing error", e)
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
}
