package edu.adarko22.jdkcerts.core.jdk.parser

import edu.adarko22.jdkcerts.core.jdk.CertificateInfo
import java.time.ZoneId
import java.time.ZonedDateTime
import java.time.format.DateTimeFormatter
import java.time.format.DateTimeFormatterBuilder

/**
 * Default implementation of [CertificateInfoParser] that parses the detailed keytool output.
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

    override fun parseCertificateInfo(keytoolListAliasOutput: String): CertificateInfo {
        try {
            // 1. Core String Extraction
            val alias = aliasRegex.safeFind(keytoolListAliasOutput)
            val owner = ownerRegex.safeFind(keytoolListAliasOutput)
            val issuer = issuerRegex.safeFind(keytoolListAliasOutput)
            val serial = serialRegex.safeFind(keytoolListAliasOutput)

            val validMatch = validRegex.find(keytoolListAliasOutput)
            val validFromRaw = validMatch?.groupValues?.getOrNull(1)?.trim()
            val validUntilRaw = validMatch?.groupValues?.getOrNull(2)?.trim()

            val sha1 = sha1Regex.safeFind(keytoolListAliasOutput)
            val sha256 = sha256Regex.safeFind(keytoolListAliasOutput)
            val sigAlg = sigAlgRegex.safeFind(keytoolListAliasOutput)

            val isCa = caConstraintRegex.safeFind(keytoolListAliasOutput) == "true"

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
