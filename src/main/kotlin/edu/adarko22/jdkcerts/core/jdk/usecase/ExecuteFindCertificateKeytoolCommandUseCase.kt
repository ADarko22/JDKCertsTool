package edu.adarko22.jdkcerts.core.jdk.usecase

import edu.adarko22.jdkcerts.core.execution.ProcessResult
import edu.adarko22.jdkcerts.core.jdk.KeytoolCommand
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
 * @param certificateInfoParser Component responsible for parsing raw keytool output into [CertificateInfo].
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
            // todo handle not found causes or error cases: make KeytoolFindCertResult a sealed class and support Found, NotFound and Error
            .filter { it.processResult.exitCode == 0 }
            .map { KeytoolFindCertResult(it.jdk, parseCertificateInfo(it.processResult)) }

    private fun parseCertificateInfo(processResult: ProcessResult) = certificateInfoParser.parseCertificateInfo(processResult.stdout)
}
