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
            .execute(keytoolCommand, customJdkDirs, false)
            .map { result ->
                when (result) {
                    // CASE 1: Process ran successfully (Exit Code 0)
                    is KeytoolCommandResult.Success -> {
                        try {
                            val info = certificateInfoParser.parseCertificateInfo(result.processResult.stdout)
                            KeytoolFindCertResult.Found(result.jdk, info)
                        } catch (e: IllegalArgumentException) {
                            // In keytool, exit 0 + missing fields usually means "Not Found" or unexpected format
                            KeytoolFindCertResult.NotFound(
                                jdk = result.jdk,
                                reason = e.message ?: "Output parsing failed",
                                stdout = result.processResult.stdout,
                                stderr = result.processResult.stderr,
                            )
                        } catch (e: Exception) {
                            // Unexpected system/logic error during parsing
                            KeytoolFindCertResult.Error(result.jdk, "Unexpected parsing error", e)
                        }
                    }

                    // CASE 2: Process failed (Exit Code != 0)
                    is KeytoolCommandResult.Failure -> {
                        val stderr = result.processResult.stderr
                        // Keytool typically exits with 1 if the alias does not exist
                        if (stderr.contains("does not exist", ignoreCase = true)) {
                            KeytoolFindCertResult.NotFound(
                                jdk = result.jdk,
                                reason = "Alias not found in keystore",
                                stdout = result.processResult.stdout,
                                stderr = stderr,
                            )
                        } else {
                            KeytoolFindCertResult.Error(
                                jdk = result.jdk,
                                message = result.errorMessage,
                            )
                        }
                    }
                }
            }
}
