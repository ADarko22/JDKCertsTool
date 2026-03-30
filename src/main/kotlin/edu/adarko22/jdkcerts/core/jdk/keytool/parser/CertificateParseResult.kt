package edu.adarko22.jdkcerts.core.jdk.keytool.parser

import edu.adarko22.jdkcerts.core.jdk.keytool.model.CertificateInfo

/**
 * Result of parsing keytool output containing both successfully parsed certificates and errors.
 *
 * @property certificates List of successfully parsed [CertificateInfo] objects.
 * @property errors List of parsing errors encountered during parsing.
 */
data class CertificateParseResult(
    val certificates: List<CertificateInfo>,
    val errors: List<ParseError>,
) {
    /**
     * Indicates whether parsing was completely successful (no errors).
     */
    val isSuccess: Boolean
        get() = errors.isEmpty()

    /**
     * Indicates whether any certificates were successfully parsed.
     */
    val hasCertificates: Boolean
        get() = certificates.isNotEmpty()
}

/**
 * Represents a single parsing error.
 *
 * @property certificateBlock The certificate block that failed to parse (may be partial).
 * @property reason The error reason describing why parsing failed.
 * @property cause The underlying exception that caused the parse failure.
 */
data class ParseError(
    val certificateBlock: String,
    val reason: String,
    val cause: Exception,
)
