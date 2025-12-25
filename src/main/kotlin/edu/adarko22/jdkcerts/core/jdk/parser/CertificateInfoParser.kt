package edu.adarko22.jdkcerts.core.jdk.parser

import edu.adarko22.jdkcerts.core.jdk.CertificateInfo

/**
 * Contract for parsing raw keytool output into a structured [CertificateInfo] object.
 *
 * Implementations are responsible for converting raw 'keytool -list -v -alias <alias>' command
 * output into a [CertificateInfo] object.
 */
interface CertificateInfoParser {
    /**
     * Parses the detailed output from a 'keytool -list -v -alias <alias>' command.
     *
     * @param keytoolListAliasOutput The raw standard output string from the keytool process.
     * @return A fully populated [CertificateInfo] domain object.
     * @throws IllegalArgumentException if the output cannot be parsed due to missing fields or incorrect date format.
     */
    fun parseCertificateInfo(keytoolListAliasOutput: String): CertificateInfo
}
