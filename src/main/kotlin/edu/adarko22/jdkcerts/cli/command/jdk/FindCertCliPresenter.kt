package edu.adarko22.jdkcerts.cli.command.jdk

import edu.adarko22.jdkcerts.cli.output.ToolOutputPrinter
import edu.adarko22.jdkcerts.cli.output.blue
import edu.adarko22.jdkcerts.cli.output.green
import edu.adarko22.jdkcerts.cli.output.red
import edu.adarko22.jdkcerts.cli.output.yellow
import edu.adarko22.jdkcerts.core.jdk.keytool.model.CertificateInfo
import edu.adarko22.jdkcerts.core.jdk.keytool.model.KeytoolQueryResult
import java.time.format.DateTimeFormatter

/**
 * Formats and prints results of the `find-cert` command.
 *
 * Supports a verbose mode for extra certificate details and debug output.
 */
class FindCertCliPresenter(
    private val output: ToolOutputPrinter,
) {
    private val dateFormatter: DateTimeFormatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")

    /**
     * Presents the results of the certificate search across all JDKs.
     *
     * @param results One result per JDK (Found, NotFound, DryRun, or a typed Failure).
     * @param verbose If true, displays full certificate fingerprints and debug info for failures.
     * @param alias The alias used in the search.
     */
    fun present(
        results: List<KeytoolQueryResult>,
        verbose: Boolean,
        alias: String,
    ) {
        // 1. Summary Header
        val found = results.filterIsInstance<KeytoolQueryResult.Found>()
        if (found.isNotEmpty()) {
            output.print("Found ${found.size} certificates matching alias '$alias':\n".green())
        } else {
            output.print("Certificate alias '$alias' was not found in any JDK keystores.\n".yellow())
        }

        // 2. Iterate through all results and process one at a time
        results.forEach { result -> processResult(result, verbose) }

        // 3. Footer Summary for Errors
        val failures = results.filterIsInstance<KeytoolQueryResult.Failure>()
        if (failures.isNotEmpty()) {
            output.print("Note: ${failures.size} JDKs encountered execution errors. Use --verbose for details.".red())
        }
    }

    private fun processResult(
        result: KeytoolQueryResult,
        verbose: Boolean,
    ) {
        output.print("--------------------------------------------------".blue())
        output.print("JDK: ${result.jdk.javaInfo.fullVersion} (${result.jdk.javaInfo.vendor})".blue())
        output.print("Path: ${result.jdk.path}".blue())

        when (result) {
            is KeytoolQueryResult.Found -> {
                result.certificateInfos.forEach { presentFound(it, verbose) }
            }

            is KeytoolQueryResult.NotFound -> {
                output.print("Status: NOT FOUND".yellow())
                output.print("Reason: ${result.reason}")
            }

            is KeytoolQueryResult.DryRun -> {
                output.print("Status: DRY RUN".yellow())
                output.print("Command: ${result.previewCommand}")
            }

            is KeytoolQueryResult.Failure -> {
                presentFailure(result, verbose)
            }
        }
    }

    private fun presentFailure(
        failure: KeytoolQueryResult.Failure,
        verbose: Boolean,
    ) {
        output.print("Status: ERROR".red())
        val message =
            when (failure) {
                is KeytoolQueryResult.Failure.WrongPassword -> "Incorrect keystore password."
                is KeytoolQueryResult.Failure.ParseError -> failure.message
                is KeytoolQueryResult.Failure.InvalidPattern -> failure.message
                is KeytoolQueryResult.Failure.Unknown -> "Keytool failed (exit code ${failure.exitCode})."
            }
        output.print("Message: $message")

        if (verbose && failure.rawStderr.isNotBlank()) {
            output.print("Raw StdErr: ${failure.rawStderr}")
        }
    }

    private fun presentFound(
        cert: CertificateInfo,
        verbose: Boolean,
    ) {
        output.print("\nCertificate Details:".green())
        output.print("  Alias: ${cert.alias}")
        output.print("  Owner: ${cert.owner}")
        output.print("  Valid From: ${cert.validFrom.format(dateFormatter)}")
        output.print("  Valid Until: ${cert.validUntil.format(dateFormatter)}")

        if (verbose) {
            output.print("\n[Verbose Details]".yellow())
            output.print("  Issuer: ${cert.issuer}")
            output.print("  Serial: ${cert.serialNumber}")
            output.print("  CA Flag: ${cert.isCa}".blue())
            output.print("  Sig Alg: ${cert.signatureAlgorithm}")
            output.print("  SHA1: ${cert.sha1Fingerprint}")
            output.print("  SHA256: ${cert.sha256Fingerprint}")
        }
    }
}
