package edu.adarko22.jdkcerts.core.jdk

/**
 * Represents a successfully found and parsed certificate within a specific JDK.
 *
 * @property jdk The JDK instance where the certificate was found.
 * @property certificateInfo The structured domain object representing the certificate details.
 */
data class KeytoolFindCertResult(
    val jdk: Jdk,
    val certificateInfo: CertificateInfo,
)
