package edu.adarko22.jdkcerts.core.jdk

import java.time.LocalDateTime

/**
 * Represents the details of a generic X.509 certificate.
 *
 * @property alias The unique alias name of the certificate entry in the keystore.
 * @property owner The Distinguished Name (DN) of the certificate subject (owner).
 * @property issuer The Distinguished Name (DN) of the certificate issuer (CA).
 * @property serialNumber The unique serial number assigned by the issuer to this certificate.
 * @property validFrom The start date of the certificate's validity period (e.g., "Oct 29, 2008").
 * @property validUntil The expiration date of the certificate (e.g., "Jan 01, 2030").
 * @property sha1Fingerprint The SHA-1 cryptographic fingerprint of the certificate.
 * @property sha256Fingerprint The SHA-256 cryptographic fingerprint of the certificate.
 * @property signatureAlgorithm The algorithm used to sign the certificate (e.g., "SHA256withRSA").
 * @property isCa True if the certificate belongs to a Certificate Authority (CA), derived from the BasicConstraints extension.
 */
data class CertificateInfo(
    val alias: String,
    val owner: String,
    val issuer: String,
    val serialNumber: String,
    val validFrom: LocalDateTime,
    val validUntil: LocalDateTime,
    val sha1Fingerprint: String,
    val sha256Fingerprint: String,
    val signatureAlgorithm: String,
    val isCa: Boolean = false,
)
