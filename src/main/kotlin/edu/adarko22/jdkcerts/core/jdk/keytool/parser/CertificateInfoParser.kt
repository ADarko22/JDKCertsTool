package edu.adarko22.jdkcerts.core.jdk.keytool.parser

/**
 * Contract for parsing raw keytool output into a structured [CertificateParseResult].
 *
 * The parser handles both single and multiple certificate parsing. Results are returned
 * as a [CertificateParseResult] containing successfully parsed certificates and any errors
 * encountered during parsing.
 */
fun interface CertificateInfoParser {
    /**
     * Parses the detailed output from a keytool command (single or multiple certificates).
     *
     * This method attempts to parse all certificates in the provided output. Malformed
     * certificates are captured as errors in the result, allowing partial parsing success.
     *
     * @param keytoolListOutput The raw standard output string from the keytool process.
     * @return A [CertificateParseResult] containing successfully parsed certificates and any errors.
     */
    fun parseCertificateInfo(keytoolListOutput: String): CertificateParseResult
}
