package edu.adarko22.jdkcerts.cli.command.jdk

import edu.adarko22.jdkcerts.cli.output.ToolOutputPrinter
import edu.adarko22.jdkcerts.cli.output.blue
import edu.adarko22.jdkcerts.cli.output.green
import edu.adarko22.jdkcerts.cli.output.yellow
import edu.adarko22.jdkcerts.core.jdk.CertificateInfo
import edu.adarko22.jdkcerts.core.jdk.KeytoolFindCertResult
import java.time.format.DateTimeFormatter

class FindCertCliPresenter(
    private val output: ToolOutputPrinter,
) {
    // Formatter for displaying date/time in the CLI
    private val dateFormatter: DateTimeFormatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")

    /**
     * Presents the list of found certificates, adjusting detail level based on the verbose flag.
     *
     * @param results List of successfully found and parsed certificates.
     * @param verbose If true, displays all certificate details.
     * @param alias The alias used in the search (for context).
     */
    fun present(
        results: List<KeytoolFindCertResult>,
        verbose: Boolean,
        alias: String,
    ) {
        if (results.isEmpty()) {
            output.print("Certificate alias '$alias' was not successfully found in any JDK keystores.".yellow())
            output.print("This may be due to the alias not existing, or keytool encountering errors (e.g., bad password).")
            return
        }

        output.print("Found ${results.size} certificates matching alias '$alias':\n".green())

        results.forEach { result ->
            output.print("--------------------------------------------------".blue())

            // 1. JDK Header (Always shown)
            output.print("JDK: ${result.jdk.javaInfo.fullVersion} (${result.jdk.javaInfo.vendor})".blue())
            output.print("Path: ${result.jdk.path}".blue())
            output.print("Keystore: ${result.jdk.keystoreInfo.keystorePath}".blue())

            // 2. Certificate Info
            val cert = result.certificateInfo
            output.print("\nCertificate Details:".green())

            // Minimal Info (Keystore Path + Alias, Owner, Validity)
            output.print("  Alias: ${cert.alias}")
            output.print("  Owner: ${cert.owner}")
            output.print("  Valid From: ${cert.validFrom.format(dateFormatter)}")
            output.print("  Valid Until: ${cert.validUntil.format(dateFormatter)}")

            if (verbose) {
                presentVerboseDetails(cert)
            }
            output.print("\n")
        }
    }

    private fun presentVerboseDetails(cert: CertificateInfo) {
        output.print("\n[Verbose Details]".yellow())
        output.print("  Issuer: ${cert.issuer}")
        output.print("  Serial: ${cert.serialNumber}")
        output.print("  CA Flag: ${cert.isCa}".blue())
        output.print("  Sig Alg: ${cert.signatureAlgorithm}")
        output.print("  SHA1: ${cert.sha1Fingerprint}")
        output.print("  SHA256: ${cert.sha256Fingerprint}")
    }
}
