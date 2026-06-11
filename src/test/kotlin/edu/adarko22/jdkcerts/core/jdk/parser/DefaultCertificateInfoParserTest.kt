package edu.adarko22.jdkcerts.core.jdk.parser

import edu.adarko22.jdkcerts.core.jdk.keytool.model.CertificateInfo
import edu.adarko22.jdkcerts.core.jdk.keytool.parser.DefaultCertificateInfoParser
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Test
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.MethodSource
import java.time.LocalDateTime
import java.util.stream.Stream

class DefaultCertificateInfoParserTest {
    private val parser = DefaultCertificateInfoParser()

    @ParameterizedTest(name = "#{index}: {0}")
    @MethodSource("certificateTestCases")
    @DisplayName("Should parse Java version information correctly")
    fun `test certificate parsing`(testCase: CertificateTestCase) {
        val result = parser.parseCertificateInfo(testCase.output)
        assertEquals(true, result.hasCertificates, "Certificate should be parsed for: ${testCase.description}")
        assertEquals(0, result.errors.size, "Should have no errors for: ${testCase.description}")
        assertEquals(testCase.expectedCertificate, result.certificates.first(), "Certificate parsing failed for: ${testCase.description}")
    }

    @Test
    @DisplayName("Should parse empty input as empty list")
    fun `test parse empty multiple certificates output`() {
        val result = parser.parseCertificateInfo("")
        assertEquals(0, result.certificates.size)
        assertEquals(0, result.errors.size)
    }

    @Test
    @DisplayName("Should parse single certificate from multiple format")
    fun `test parse single certificate from multiple format`() {
        val singleCertOutput =
            """Alias name: testcert
Creation date: Nov 28, 2025
Entry type: trustedCertEntry

Owner: CN=TestCert, OU=TestOrg, O=TestCorp, C=US
Issuer: CN=TestCert, OU=TestOrg, O=TestCorp, C=US
Serial number: 5561f3a18424cdf1
Valid from: Fri Nov 28 20:26:15 CET 2025 until: Thu Feb 26 20:26:15 CET 2026
Certificate fingerprints:
         SHA1: 94:06:45:A7:C8:AB:AA:E5:ED:0D:5D:19:C3:32:51:86:DD:12:65:36
         SHA256: D8:4F:EB:77:03:29:A6:E6:EA:66:C0:ED:55:7A:A1:63:99:1A:D3:6B:FF:A1:F4:99:FC:10:06:A9:90:E0:57:E5
Signature algorithm name: SHA256withRSA
Subject Public Key Algorithm: 2048-bit RSA key
Version: 3
"""

        val result = parser.parseCertificateInfo(singleCertOutput)

        assertEquals(1, result.certificates.size)
        assertEquals("testcert", result.certificates[0].alias)
        assertEquals("CN=TestCert, OU=TestOrg, O=TestCorp, C=US", result.certificates[0].owner)
    }

    @Test
    @DisplayName("Should parse multiple certificates")
    fun `test parse multiple certificates`() {
        val multipleCertsOutput =
            """Alias name: testcert1
Creation date: Nov 28, 2025
Entry type: trustedCertEntry

Owner: CN=TestCert1, OU=TestOrg, O=TestCorp, C=US
Issuer: CN=TestCert1, OU=TestOrg, O=TestCorp, C=US
Serial number: 5561f3a18424cdf1
Valid from: Fri Nov 28 20:26:15 CET 2025 until: Thu Feb 26 20:26:15 CET 2026
Certificate fingerprints:
         SHA1: 94:06:45:A7:C8:AB:AA:E5:ED:0D:5D:19:C3:32:51:86:DD:12:65:36
         SHA256: D8:4F:EB:77:03:29:A6:E6:EA:66:C0:ED:55:7A:A1:63:99:1A:D3:6B:FF:A1:F4:99:FC:10:06:A9:90:E0:57:E5
Signature algorithm name: SHA256withRSA
Subject Public Key Algorithm: 2048-bit RSA key
Version: 3

Alias name: testcert2
Creation date: Nov 28, 2025
Entry type: trustedCertEntry

Owner: CN=TestCert2, OU=TestOrg, O=TestCorp, C=US
Issuer: CN=TestCert2, OU=TestOrg, O=TestCorp, C=US
Serial number: 1234567890abcdef
Valid from: Fri Nov 28 20:26:15 CET 2025 until: Thu Feb 26 20:26:15 CET 2026
Certificate fingerprints:
         SHA1: 11:22:33:44:55:66:77:88:99:AA:BB:CC:DD:EE:FF:00:11:22:33:44
         SHA256: AA:BB:CC:DD:EE:FF:00:11:22:33:44:55:66:77:88:99:AA:BB:CC:DD:EE:FF:00:11:22:33:44:55:66:77:88:99
Signature algorithm name: SHA256withRSA
Subject Public Key Algorithm: 2048-bit RSA key
Version: 3
"""

        val result = parser.parseCertificateInfo(multipleCertsOutput)

        assertEquals(2, result.certificates.size)
        assertEquals("testcert1", result.certificates[0].alias)
        assertEquals("CN=TestCert1, OU=TestOrg, O=TestCorp, C=US", result.certificates[0].owner)
        assertEquals("testcert2", result.certificates[1].alias)
        assertEquals("CN=TestCert2, OU=TestOrg, O=TestCorp, C=US", result.certificates[1].owner)
    }

    @Test
    @DisplayName("Should parse three certificates")
    fun `test parse three certificates`() {
        val threeCertsOutput =
            """Alias name: cert1
Creation date: Nov 28, 2025
Entry type: trustedCertEntry

Owner: CN=Cert1, O=Org1, C=US
Issuer: CN=Cert1, O=Org1, C=US
Serial number: 1111111111111111
Valid from: Fri Nov 28 20:26:15 CET 2025 until: Thu Feb 26 20:26:15 CET 2026
Certificate fingerprints:
         SHA1: 11:11:11:11:11:11:11:11:11:11:11:11:11:11:11:11:11:11:11:11
         SHA256: 11:11:11:11:11:11:11:11:11:11:11:11:11:11:11:11:11:11:11:11:11:11:11:11:11:11:11:11:11:11:11:11
Signature algorithm name: SHA256withRSA
Subject Public Key Algorithm: 2048-bit RSA key
Version: 3

*******************************************
*******************************************

Alias name: cert2
Creation date: Nov 28, 2025
Entry type: trustedCertEntry

Owner: CN=Cert2, O=Org2, C=US
Issuer: CN=Cert2, O=Org2, C=US
Serial number: 2222222222222222
Valid from: Fri Nov 28 20:26:15 CET 2025 until: Thu Feb 26 20:26:15 CET 2026
Certificate fingerprints:
         SHA1: 22:22:22:22:22:22:22:22:22:22:22:22:22:22:22:22:22:22:22:22
         SHA256: 22:22:22:22:22:22:22:22:22:22:22:22:22:22:22:22:22:22:22:22:22:22:22:22:22:22:22:22:22:22:22:22
Signature algorithm name: SHA256withRSA
Subject Public Key Algorithm: 2048-bit RSA key
Version: 3

Alias name: cert3
Creation date: Nov 28, 2025
Entry type: trustedCertEntry

Owner: CN=Cert3, O=Org3, C=US
Issuer: CN=Cert3, O=Org3, C=US
Serial number: 3333333333333333
Valid from: Fri Nov 28 20:26:15 CET 2025 until: Thu Feb 26 20:26:15 CET 2026
Certificate fingerprints:
         SHA1: 33:33:33:33:33:33:33:33:33:33:33:33:33:33:33:33:33:33:33:33
         SHA256: 33:33:33:33:33:33:33:33:33:33:33:33:33:33:33:33:33:33:33:33:33:33:33:33:33:33:33:33:33:33:33:33
Signature algorithm name: SHA256withRSA
Subject Public Key Algorithm: 2048-bit RSA key
Version: 3
"""

        val result = parser.parseCertificateInfo(threeCertsOutput)

        assertEquals(3, result.certificates.size)
        assertEquals("cert1", result.certificates[0].alias)
        assertEquals("cert2", result.certificates[1].alias)
        assertEquals("cert3", result.certificates[2].alias)
    }

    @Test
    @DisplayName("Should skip malformed certificates and return valid ones")
    fun `test parse with one malformed certificate`() {
        val mixedOutput =
            """Alias name: goodcert1
Creation date: Nov 28, 2025
Entry type: trustedCertEntry

Owner: CN=GoodCert1, O=Org1, C=US
Issuer: CN=GoodCert1, O=Org1, C=US
Serial number: 1111111111111111
Valid from: Fri Nov 28 20:26:15 CET 2025 until: Thu Feb 26 20:26:15 CET 2026
Certificate fingerprints:
         SHA1: 11:11:11:11:11:11:11:11:11:11:11:11:11:11:11:11:11:11:11:11
         SHA256: 11:11:11:11:11:11:11:11:11:11:11:11:11:11:11:11:11:11:11:11:11:11:11:11:11:11:11:11:11:11:11:11
Signature algorithm name: SHA256withRSA
Subject Public Key Algorithm: 2048-bit RSA key
Version: 3

Alias name: badcert
Creation date: Nov 28, 2025
Entry type: trustedCertEntry

Owner: CN=BadCert, O=Org, C=US

Alias name: goodcert2
Creation date: Nov 28, 2025
Entry type: trustedCertEntry

Owner: CN=GoodCert2, O=Org2, C=US
Issuer: CN=GoodCert2, O=Org2, C=US
Serial number: 2222222222222222
Valid from: Fri Nov 28 20:26:15 CET 2025 until: Thu Feb 26 20:26:15 CET 2026
Certificate fingerprints:
         SHA1: 22:22:22:22:22:22:22:22:22:22:22:22:22:22:22:22:22:22:22:22
         SHA256: 22:22:22:22:22:22:22:22:22:22:22:22:22:22:22:22:22:22:22:22:22:22:22:22:22:22:22:22:22:22:22:22
Signature algorithm name: SHA256withRSA
Subject Public Key Algorithm: 2048-bit RSA key
Version: 3
"""

        val result = parser.parseCertificateInfo(mixedOutput)

        // Should only return the 2 valid certificates, skipping the malformed one
        assertEquals(2, result.certificates.size)
        assertEquals(1, result.errors.size)
        assertEquals("goodcert1", result.certificates[0].alias)
        assertEquals("goodcert2", result.certificates[1].alias)
    }

    companion object {
        @JvmStatic
        fun certificateTestCases(): Stream<CertificateTestCase> =
            Stream.of(
                CertificateTestCase.TestCertificate,
                CertificateTestCase.JdkCertificate,
            )
    }
}

sealed class CertificateTestCase(
    val output: String,
    val expectedCertificate: CertificateInfo,
    val description: String,
) {
    override fun toString() = description

    object TestCertificate : CertificateTestCase(
        output =
            """Warning: use -cacerts option to access cacerts keystore
Alias name: testcert
Creation date: Nov 28, 2025
Entry type: trustedCertEntry

Owner: CN=TestCert, OU=TestOrg, O=TestCorp, C=US
Issuer: CN=TestCert, OU=TestOrg, O=TestCorp, C=US
Serial number: 5561f3a18424cdf1
Valid from: Fri Nov 28 20:26:15 CET 2025 until: Thu Feb 26 20:26:15 CET 2026
Certificate fingerprints:
         SHA1: 94:06:45:A7:C8:AB:AA:E5:ED:0D:5D:19:C3:32:51:86:DD:12:65:36
         SHA256: D8:4F:EB:77:03:29:A6:E6:EA:66:C0:ED:55:7A:A1:63:99:1A:D3:6B:FF:A1:F4:99:FC:10:06:A9:90:E0:57:E5
Signature algorithm name: SHA256withRSA
Subject Public Key Algorithm: 2048-bit RSA key
Version: 3

Extensions: 

#1: ObjectId: 2.5.29.14 Criticality=false
SubjectKeyIdentifier [
KeyIdentifier [
0000: 24 5C BE 5C 9D B7 47 51   FE 8F AA CF 89 BA 63 C6  $\.\..GQ......c.
0010: 70 1A 71 AB                                        p.q.
]
]""",
        expectedCertificate =
            CertificateInfo(
                alias = "testcert",
                owner = "CN=TestCert, OU=TestOrg, O=TestCorp, C=US",
                issuer = "CN=TestCert, OU=TestOrg, O=TestCorp, C=US",
                serialNumber = "5561f3a18424cdf1",
                validFrom = LocalDateTime.parse("2025-11-28T20:26:15"),
                validUntil = LocalDateTime.parse("2026-02-26T20:26:15"),
                sha1Fingerprint = "94:06:45:A7:C8:AB:AA:E5:ED:0D:5D:19:C3:32:51:86:DD:12:65:36",
                sha256Fingerprint = "D8:4F:EB:77:03:29:A6:E6:EA:66:C0:ED:55:7A:A1:63:99:1A:D3:6B:FF:A1:F4:99:FC:10:06:A9:90:E0:57:E5",
                signatureAlgorithm = "SHA256withRSA",
                isCa = false,
            ),
        description = "Test Certificate without CA",
    )

    object JdkCertificate : CertificateTestCase(
        output =
            """Alias name: ou_ac_raiz_fnmtrcm,o_fnmtrcm,c_es [jdk]
Creation date: Oct 29, 2008
Entry type: trustedCertEntry

Owner: OU=AC RAIZ FNMT-RCM, O=FNMT-RCM, C=ES
Issuer: OU=AC RAIZ FNMT-RCM, O=FNMT-RCM, C=ES
Serial number: 5d938d306736c8061d1ac754846907
Valid from: Wed Oct 29 16:59:56 CET 2008 until: Tue Jan 01 01:00:00 CET 2030
Certificate fingerprints:
         SHA1: EC:50:35:07:B2:15:C4:95:62:19:E2:A8:9A:5B:42:99:2C:4C:2C:20
         SHA256: EB:C5:57:0C:29:01:8C:4D:67:B1:AA:12:7B:AF:12:F7:03:B4:61:1E:BC:17:B7:DA:B5:57:38:94:17:9B:93:FA
Signature algorithm name: SHA256withRSA
Subject Public Key Algorithm: 4096-bit RSA key
Version: 3

Extensions: 

#1: ObjectId: 2.5.29.19 Criticality=true
BasicConstraints:[
  CA:true
  PathLen: no limit
]

#2: ObjectId: 2.5.29.32 Criticality=false
CertificatePolicies [
  [CertificatePolicyId: [2.5.29.32.0]
[PolicyQualifierInfo: [
  qualifierID: 1.3.6.1.5.5.7.2.1
  qualifier: 0000: 16 1D 68 74 74 70 3A 2F   2F 77 77 77 2E 63 65 72  ..http://www.cer
0010: 74 2E 66 6E 6D 74 2E 65   73 2F 64 70 63 73 2F     t.fnmt.es/dpcs/

]]  ]
]

#3: ObjectId: 2.5.29.15 Criticality=true
KeyUsage [
  Key_CertSign
  Crl_Sign
]

#4: ObjectId: 2.5.29.14 Criticality=false
SubjectKeyIdentifier [
KeyIdentifier [
0000: F7 7D C5 FD C4 E8 9A 1B   77 64 A7 F5 1D A0 CC BF  ........wd......
0010: 87 60 9A 6D                                        .`.m
]
]
""",
        expectedCertificate =
            CertificateInfo(
                alias = "ou_ac_raiz_fnmtrcm,o_fnmtrcm,c_es [jdk]",
                owner = "OU=AC RAIZ FNMT-RCM, O=FNMT-RCM, C=ES",
                issuer = "OU=AC RAIZ FNMT-RCM, O=FNMT-RCM, C=ES",
                serialNumber = "5d938d306736c8061d1ac754846907",
                validFrom = LocalDateTime.parse("2008-10-29T16:59:56"),
                validUntil = LocalDateTime.parse("2030-01-01T01:00:00"),
                sha1Fingerprint = "EC:50:35:07:B2:15:C4:95:62:19:E2:A8:9A:5B:42:99:2C:4C:2C:20",
                sha256Fingerprint = "EB:C5:57:0C:29:01:8C:4D:67:B1:AA:12:7B:AF:12:F7:03:B4:61:1E:BC:17:B7:DA:B5:57:38:94:17:9B:93:FA",
                signatureAlgorithm = "SHA256withRSA",
                isCa = true,
            ),
        description = "JDK Certificate with CA",
    )
}
