package edu.adarko22.jdkcerts.cli.command.jdk

import edu.adarko22.jdkcerts.cli.output.ToolOutputPrinter
import edu.adarko22.jdkcerts.cli.output.blue
import edu.adarko22.jdkcerts.cli.output.green
import edu.adarko22.jdkcerts.cli.output.red
import edu.adarko22.jdkcerts.cli.output.yellow
import edu.adarko22.jdkcerts.core.jdk.CertificateInfo
import edu.adarko22.jdkcerts.core.jdk.KeytoolFindCertResult
import java.time.format.DateTimeFormatter

class FindCertCliPresenter(
    private val output: ToolOutputPrinter,
) {
    private val dateFormatter: DateTimeFormatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")

    /**
     * Presents the results of the certificate search across all JDKs.
     *
     * @param results List of result states (Found, NotFound, or Error).
     * @param verbose If true, displays full certificate fingerprints and debug info for failures.
     * @param alias The alias used in the search.
     */
    fun present(
        results: List<KeytoolFindCertResult>,
        verbose: Boolean,
        alias: String,
    ) {
        // 1. Summary Header
        val found = results.filterIsInstance<KeytoolFindCertResult.Found>()
        if (found.isNotEmpty()) {
            output.print("Found ${found.size} certificates matching alias '$alias':\n".green())
        } else {
            output.print("Certificate alias '$alias' was not found in any JDK keystores.\n".yellow())
        }

        // 2. Iterate through all results using exhaustive when
        results.forEach { result ->
            output.print("--------------------------------------------------".blue())
            output.print("JDK: ${result.jdk.javaInfo.fullVersion} (${result.jdk.javaInfo.vendor})".blue())
            output.print("Path: ${result.jdk.path}".blue())

            when (result) {
                is KeytoolFindCertResult.Found -> {
                    presentFound(result.certificateInfo, verbose)
                }

                is KeytoolFindCertResult.NotFound -> {
                    output.print("Status: NOT FOUND".yellow())
                    output.print("Reason: ${result.reason}")
                    if (verbose) {
                        if (result.stdout.isNotBlank()) {
                            output.print("Raw Stdout: ${result.stdout.take(500)}...")
                        }
                        if (result.stderr.isNotBlank()) {
                            output.print("Raw StdErr: ${result.stderr.take(500)}...")
                        }
                    }
                }

                is KeytoolFindCertResult.Error -> {
                    output.print("Status: ERROR".red())
                    output.print("Message: ${result.message}")
                    if (verbose && result.cause != null) {
                        output.print("Trace: ${result.cause.message}")
                    }
                }
            }
        }

        // 3. Footer Summary for Errors
        val errors = results.filterIsInstance<KeytoolFindCertResult.Error>()
        if (errors.isNotEmpty()) {
            output.print("Note: ${errors.size} JDKs encountered execution errors. Use --verbose for details.".red())
        }
    }

    private fun presentFound(
        cert: CertificateInfo,
        verbose: Boolean,
    ) {
        output.print("Status: FOUND".green())
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
