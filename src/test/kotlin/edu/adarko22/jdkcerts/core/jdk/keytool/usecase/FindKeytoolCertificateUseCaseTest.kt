package edu.adarko22.jdkcerts.core.jdk.keytool.usecase

import edu.adarko22.jdkcerts.core.execution.KeytoolProcessResult
import edu.adarko22.jdkcerts.core.execution.KeytoolProcessRunner
import edu.adarko22.jdkcerts.core.jdk.DiscoverJdksUseCase
import edu.adarko22.jdkcerts.core.jdk.Jdk
import edu.adarko22.jdkcerts.core.jdk.java.model.JavaInfo
import edu.adarko22.jdkcerts.core.jdk.keytool.model.CertificateInfo
import edu.adarko22.jdkcerts.core.jdk.keytool.model.ExecutionContext
import edu.adarko22.jdkcerts.core.jdk.keytool.model.FindCertKeytoolQuery
import edu.adarko22.jdkcerts.core.jdk.keytool.model.KeystoreInfo
import edu.adarko22.jdkcerts.core.jdk.keytool.model.KeytoolQueryResult
import edu.adarko22.jdkcerts.core.jdk.keytool.model.SearchStrategy
import edu.adarko22.jdkcerts.core.jdk.keytool.parser.CertificateInfoParser
import edu.adarko22.jdkcerts.core.jdk.keytool.parser.CertificateParseResult
import edu.adarko22.jdkcerts.core.jdk.keytool.parser.ParseError
import io.mockk.coEvery
import io.mockk.mockk
import kotlinx.coroutines.test.runTest
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertInstanceOf
import org.junit.jupiter.api.Test
import java.nio.file.Path
import java.time.LocalDateTime

class FindKeytoolCertificateUseCaseTest {
    private val jdk = jdk("jdk17")
    private val discoverJdks = mockk<DiscoverJdksUseCase>()
    private val processRunner = mockk<KeytoolProcessRunner>()
    private val context = ExecutionContext(masterPassword = "pw")

    private fun useCase(parser: CertificateInfoParser): FindKeytoolCertificateUseCase =
        FindKeytoolCertificateUseCase(
            jdkDiscoverJdksUseCase = discoverJdks,
            keytoolProcessRunner = processRunner,
            certificateInfoParser = parser,
        )

    private suspend fun run(
        query: FindCertKeytoolQuery,
        outcomes: List<KeytoolProcessResult>,
        parser: CertificateInfoParser = CertificateInfoParser { CertificateParseResult(emptyList(), emptyList()) },
    ): List<KeytoolQueryResult> {
        coEvery { discoverJdks.discover(any()) } returns outcomes.map { it.jdk }
        coEvery { processRunner.runConcurrently(any(), any(), any(), any()) } returns outcomes
        return useCase(parser).execute(query, context)
    }

    // ---- EXACT_MATCH ----

    @Test
    fun `exact match returns Found when parser yields a certificate`() =
        runTest {
            val query = FindCertKeytoolQuery("my-cert", SearchStrategy.EXACT_MATCH)
            val cert = cert("my-cert")

            val results = run(query, listOf(executed()), parser = { CertificateParseResult(listOf(cert), emptyList()) })

            val found = assertInstanceOf(KeytoolQueryResult.Found::class.java, results.single())
            assertEquals(listOf(cert), found.certificateInfos)
        }

    @Test
    fun `exact match returns NotFound when parser yields neither certificates nor errors`() =
        runTest {
            val query = FindCertKeytoolQuery("my-cert", SearchStrategy.EXACT_MATCH)

            val results = run(query, listOf(executed()))

            assertInstanceOf(KeytoolQueryResult.NotFound::class.java, results.single())
        }

    @Test
    fun `exact match returns ParseError when parser reports errors`() =
        runTest {
            val query = FindCertKeytoolQuery("my-cert", SearchStrategy.EXACT_MATCH)
            val parseError = ParseError("block", "malformed cert", RuntimeException("bad"))

            val results = run(query, listOf(executed()), parser = { CertificateParseResult(emptyList(), listOf(parseError)) })

            val failure = assertInstanceOf(KeytoolQueryResult.Failure.ParseError::class.java, results.single())
            assertEquals("Certificate parsing failed: malformed cert", failure.message)
        }

    // ---- REGEX ----

    @Test
    fun `regex returns Found with all matching certificates`() =
        runTest {
            val query = FindCertKeytoolQuery("alpha.*", SearchStrategy.REGEX)
            val matching = listOf(cert("alpha"), cert("alpha-2"))
            val certs = matching + cert("beta")

            val results = run(query, listOf(executed()), parser = { CertificateParseResult(certs, emptyList()) })

            val found = assertInstanceOf(KeytoolQueryResult.Found::class.java, results.single())
            assertEquals(matching, found.certificateInfos)
        }

    @Test
    fun `regex returns NotFound when no alias matches`() =
        runTest {
            val query = FindCertKeytoolQuery("alpha.*", SearchStrategy.REGEX)

            val results = run(query, listOf(executed()), parser = { CertificateParseResult(listOf(cert("beta")), emptyList()) })

            assertInstanceOf(KeytoolQueryResult.NotFound::class.java, results.single())
        }

    @Test
    fun `regex returns NotFound when keystore has no certificates`() =
        runTest {
            val query = FindCertKeytoolQuery("alpha.*", SearchStrategy.REGEX)

            val results = run(query, listOf(executed()))

            assertInstanceOf(KeytoolQueryResult.NotFound::class.java, results.single())
        }

    @Test
    fun `regex returns InvalidPattern for a malformed pattern`() =
        runTest {
            val query = FindCertKeytoolQuery("[", SearchStrategy.REGEX)

            val results = run(query, listOf(executed()), parser = { CertificateParseResult(listOf(cert("alpha")), emptyList()) })

            assertInstanceOf(KeytoolQueryResult.Failure.InvalidPattern::class.java, results.single())
        }

    // ---- CLOSEST_MATCH ----

    @Test
    fun `closest match returns Found for best scoring alias above threshold`() =
        runTest {
            val query = FindCertKeytoolQuery("aliasx", SearchStrategy.CLOSEST_MATCH)
            val best = cert("aliasx")
            val certs = listOf(best, cert("zzzzzzzz"))

            val results = run(query, listOf(executed()), parser = { CertificateParseResult(certs, emptyList()) })

            val found = assertInstanceOf(KeytoolQueryResult.Found::class.java, results.single())
            assertEquals(listOf(best), found.certificateInfos)
        }

    @Test
    fun `closest match returns all tied best matches`() =
        runTest {
            val query = FindCertKeytoolQuery("abc", SearchStrategy.CLOSEST_MATCH)
            val tied = listOf(cert("abd"), cert("abe"))

            val results = run(query, listOf(executed()), parser = { CertificateParseResult(tied, emptyList()) })

            val found = assertInstanceOf(KeytoolQueryResult.Found::class.java, results.single())
            assertEquals(tied.toSet(), found.certificateInfos.toSet())
        }

    @Test
    fun `closest match returns NotFound when best score is below threshold`() =
        runTest {
            val query = FindCertKeytoolQuery("aaaa", SearchStrategy.CLOSEST_MATCH)

            val results = run(query, listOf(executed()), parser = { CertificateParseResult(listOf(cert("zzzzzzzz")), emptyList()) })

            assertInstanceOf(KeytoolQueryResult.NotFound::class.java, results.single())
        }

    @Test
    fun `closest match returns NotFound when keystore has no certificates`() =
        runTest {
            val query = FindCertKeytoolQuery("aaaa", SearchStrategy.CLOSEST_MATCH)

            val results = run(query, listOf(executed()))

            assertInstanceOf(KeytoolQueryResult.NotFound::class.java, results.single())
        }

    // ---- non-zero exits, classified ----

    @Test
    fun `missing alias exit maps to NotFound`() =
        runTest {
            val query = FindCertKeytoolQuery("missing", SearchStrategy.EXACT_MATCH)
            val outcome = executed(exitCode = 1, stderr = "keytool error: Alias <missing> does not exist")

            val results = run(query, listOf(outcome))

            assertInstanceOf(KeytoolQueryResult.NotFound::class.java, results.single())
        }

    @Test
    fun `wrong password exit maps to WrongPassword failure`() =
        runTest {
            val query = FindCertKeytoolQuery("my-cert", SearchStrategy.EXACT_MATCH)
            val outcome = executed(exitCode = 1, stderr = "keystore password was incorrect")

            val results = run(query, listOf(outcome))

            assertInstanceOf(KeytoolQueryResult.Failure.WrongPassword::class.java, results.single())
        }

    @Test
    fun `other exit maps to Unknown failure carrying the exit code`() =
        runTest {
            val query = FindCertKeytoolQuery("my-cert", SearchStrategy.EXACT_MATCH)
            val outcome = executed(exitCode = 7, stderr = "permission denied")

            val results = run(query, listOf(outcome))

            val failure = assertInstanceOf(KeytoolQueryResult.Failure.Unknown::class.java, results.single())
            assertEquals(7, failure.exitCode)
        }

    // ---- dry run ----

    @Test
    fun `dry run outcome maps to DryRun result`() =
        runTest {
            val query = FindCertKeytoolQuery("my-cert", SearchStrategy.EXACT_MATCH)

            val results = run(query, listOf(KeytoolProcessResult.DryRun(jdk, "keytool -list -v")))

            val dryRun = assertInstanceOf(KeytoolQueryResult.DryRun::class.java, results.single())
            assertEquals("keytool -list -v", dryRun.previewCommand)
        }

    @Test
    fun `preserves one result per JDK for mixed outcomes`() =
        runTest {
            val jdk2 = jdk("jdk11")
            val query = FindCertKeytoolQuery("my-cert", SearchStrategy.EXACT_MATCH)
            val outcomes =
                listOf(
                    executed(jdk, stdout = "raw"),
                    executed(jdk2, exitCode = 1, stderr = "does not exist"),
                )
            coEvery { discoverJdks.discover(any()) } returns listOf(jdk, jdk2)
            coEvery { processRunner.runConcurrently(any(), any(), any(), any()) } returns outcomes

            val results =
                useCase { CertificateParseResult(listOf(cert("my-cert")), emptyList()) }.execute(query, context)

            assertEquals(2, results.size)
            assertInstanceOf(KeytoolQueryResult.Found::class.java, results[0])
            assertInstanceOf(KeytoolQueryResult.NotFound::class.java, results[1])
        }

    // ---- fixtures ----

    private fun executed(
        target: Jdk = jdk,
        exitCode: Int = 0,
        stdout: String = "raw",
        stderr: String = "",
    ): KeytoolProcessResult.Executed = KeytoolProcessResult.Executed(target, exitCode, stdout, stderr)

    private fun cert(alias: String): CertificateInfo =
        CertificateInfo(
            alias = alias,
            owner = "CN=Test",
            issuer = "CN=Test",
            serialNumber = "1",
            validFrom = LocalDateTime.of(2020, 1, 1, 0, 0),
            validUntil = LocalDateTime.of(2030, 1, 1, 0, 0),
            sha1Fingerprint = "AA",
            sha256Fingerprint = "BB",
            signatureAlgorithm = "SHA256withRSA",
        )
}

private fun jdk(name: String): Jdk {
    val jdkPath = Path.of("/opt/$name")
    return Jdk(
        path = jdkPath,
        javaInfo = JavaInfo(vendor = "OpenJDK", fullVersion = "17.0.7", major = 17),
        keystoreInfo = KeystoreInfo(keystorePath = jdkPath.resolve("lib/security/cacerts"), cacertsShortcutEnabled = true),
    )
}
