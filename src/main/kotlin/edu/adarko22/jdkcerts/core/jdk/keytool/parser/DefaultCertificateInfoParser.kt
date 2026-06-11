package edu.adarko22.jdkcerts.core.jdk.keytool.parser

import edu.adarko22.jdkcerts.core.jdk.keytool.model.CertificateInfo
import java.time.ZoneId
import java.time.ZonedDateTime
import java.time.format.DateTimeFormatter
import java.time.format.DateTimeFormatterBuilder

/**
 * Default implementation of [CertificateInfoParser] that parses the detailed keytool output.
 *
 * Supports both single certificate parsing (from `keytool -list -v -alias <alias>`) and
 * multiple certificate parsing (from `keytool -list -v` without alias).
 */
class DefaultCertificateInfoParser : CertificateInfoParser {
    /**
     * Matching keytool's date output, including the day of the week and Time Zone (e.g., "Wed Oct 29 16:59:56 CET 2008").
     */
    private val keytoolZonedDateFormatter: DateTimeFormatter =
        DateTimeFormatterBuilder()
            .parseCaseInsensitive()
            .appendPattern("EEE MMM dd HH:mm:ss z yyyy")
            .toFormatter()
            .withZone(ZoneId.systemDefault())
    private val aliasRegex = "Alias name: (.*)".toRegex()
    private val ownerRegex = "Owner: (.*)".toRegex()
    private val issuerRegex = "Issuer: (.*)".toRegex()
    private val serialRegex = "Serial number: (\\w+)".toRegex()

    // Captures the full raw date strings: Valid from: [date 1] until: [date 2]
    private val validRegex = "Valid from: (.*?) until: (.*)".toRegex()
    private val sha1Regex = "SHA1: ([0-9A-F:]+)".toRegex()
    private val sha256Regex = "SHA256: ([0-9A-F:]+)".toRegex()
    private val sigAlgRegex = "Signature algorithm name: (.*)".toRegex()
    private val caConstraintRegex = "BasicConstraints:\\[\\s+CA:(true|false)".toRegex()

    // Helper function to safely extract a group value from a regex match
    private fun Regex.safeFind(
        input: String,
        groupIndex: Int = 1,
    ) = this
        .find(input)
        ?.groupValues
        ?.getOrNull(groupIndex)
        ?.trim() ?: ""

    /**
     * Parses the output from `keytool -list -v` or `keytool -list -v -alias <alias>` containing one or more certificates.
     *
     * Iterates through the input string using a while loop, finding each certificate block
     * by the "Alias name:" marker. Each certificate is independently parsed. Malformed certificates
     * are captured as errors in the result while successful certificates are accumulated.
     *
     * @param keytoolListOutput The raw output string from 'keytool -list -v' command.
     * @return A [CertificateParseResult] containing successfully parsed certificates and any errors.
     */
    override fun parseCertificateInfo(keytoolListOutput: String): CertificateParseResult {
        if (keytoolListOutput.isBlank()) {
            return CertificateParseResult(emptyList(), emptyList())
        }

        val certificates = mutableListOf<CertificateInfo>()
        val errors = mutableListOf<ParseError>()
        var startIndex = 0

        while (startIndex < keytoolListOutput.length) {
            val aliasMatch = aliasRegex.find(keytoolListOutput, startIndex) ?: break
            val nextAliasMatch = aliasRegex.find(keytoolListOutput, aliasMatch.range.last + 1)
            val endIndex = nextAliasMatch?.range?.first ?: keytoolListOutput.length

            // Extract the current certificate block
            val certificateBlock = keytoolListOutput.substring(aliasMatch.range.first, endIndex)

            // Try to parse this certificate block
            try {
                val cert = parseSingleCertificate(certificateBlock)
                certificates.add(cert)
            } catch (e: Exception) {
                // Capture parsing error with block and details
                errors.add(
                    ParseError(
                        certificateBlock = certificateBlock,
                        reason = e.message ?: "Unknown parsing error",
                        cause = e,
                    ),
                )
            }

            // Move to next certificate
            startIndex = endIndex
        }

        return CertificateParseResult(certificates, errors)
    }

    /**
     * Parses a single certificate block from keytool output.
     *
     * @param certificateBlock The certificate text block to parse.
     * @return A fully populated [CertificateInfo] domain object.
     * @throws IllegalArgumentException if the block cannot be parsed.
     */
    private fun parseSingleCertificate(certificateBlock: String): CertificateInfo {
        try {
            // 1. Core String Extraction
            val alias = aliasRegex.safeFind(certificateBlock)
            val owner = ownerRegex.safeFind(certificateBlock)
            val issuer = issuerRegex.safeFind(certificateBlock)
            val serial = serialRegex.safeFind(certificateBlock)

            val validMatch = validRegex.find(certificateBlock)
            val validFromRaw = validMatch?.groupValues?.getOrNull(1)?.trim()
            val validUntilRaw = validMatch?.groupValues?.getOrNull(2)?.trim()

            val sha1 = sha1Regex.safeFind(certificateBlock)
            val sha256 = sha256Regex.safeFind(certificateBlock)
            val sigAlg = sigAlgRegex.safeFind(certificateBlock)

            val isCa = caConstraintRegex.safeFind(certificateBlock) == "true"

            // 2. Date Parsing
            if (validFromRaw.isNullOrEmpty() || validUntilRaw.isNullOrEmpty()) {
                throw IllegalArgumentException("Missing validity dates in keytool output.")
            }

            // keytool output includes a timezone abbreviation (z) which requires ZonedDateTime parsing first
            val validFromZoned: ZonedDateTime = ZonedDateTime.parse(validFromRaw, keytoolZonedDateFormatter)
            val validUntilZoned: ZonedDateTime = ZonedDateTime.parse(validUntilRaw, keytoolZonedDateFormatter)

            val validFrom = validFromZoned.toLocalDateTime()
            val validUntil = validUntilZoned.toLocalDateTime()

            // 3. Validation and Return
            if (alias.isEmpty() || owner.isEmpty()) {
                throw IllegalArgumentException("Required certificate fields (alias/owner) are missing.")
            }

            return CertificateInfo(
                alias = alias,
                owner = owner,
                issuer = issuer,
                serialNumber = serial,
                validFrom = validFrom,
                validUntil = validUntil,
                sha1Fingerprint = sha1,
                sha256Fingerprint = sha256,
                signatureAlgorithm = sigAlg,
                isCa = isCa,
            )
        } catch (e: Exception) {
            // Catch all parsing errors (e.g., regex failure, date parse failure) and rethrow with context
            throw IllegalArgumentException("Failed to parse keytool output into CertificateInfo. Cause: ${e.message}", e)
        }
    }
}
