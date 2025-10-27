package edu.adarko22.jdkcerts.jdk

import edu.adarko22.jdkcerts.jdk.impl.JavaInfo
import edu.adarko22.jdkcerts.jdk.impl.JavaInfoResolver
import edu.adarko22.jdkcerts.jdk.impl.keystore.KeystoreInfo
import edu.adarko22.jdkcerts.jdk.impl.keystore.KeystoreInfoResolver
import io.mockk.confirmVerified
import io.mockk.every
import io.mockk.mockk
import io.mockk.slot
import io.mockk.verify
import io.mockk.verifySequence
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertNotNull
import org.junit.jupiter.api.Assertions.assertThrows
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.io.TempDir
import java.nio.file.Files
import java.nio.file.Path

class JdkCreatorTest {
    @Test
    fun `createJdk returns Jdk with resolved javaInfo and keystoreInfo`(
        @TempDir tempDir: Path,
    ) {
        val javaResolver = mockk<JavaInfoResolver>()
        val keystoreResolver = mockk<KeystoreInfoResolver>()

        val jdkPath = tempDir.resolve("jdk1")
        Files.createDirectories(jdkPath)

        val expectedJavaInfo = JavaInfo(major = 11, fullVersion = "11.0.11", vendor = "OpenJDK")
        val expectedKeystoreInfo =
            KeystoreInfo(
                keystorePath = jdkPath.resolve("lib/security/cacerts"),
                cacertsShortcutEnabled = true,
            )

        every { javaResolver.resolve(jdkPath) } returns expectedJavaInfo
        every { keystoreResolver.resolve(jdkPath, expectedJavaInfo) } returns expectedKeystoreInfo

        val creator = JdkCreator(javaResolver, keystoreResolver)
        val jdk = creator.createJdk(jdkPath)

        assertEquals(jdkPath, jdk.path)
        assertEquals(expectedJavaInfo, jdk.javaInfo)
        assertEquals(expectedKeystoreInfo, jdk.keystoreInfo)

        verify(exactly = 1) { javaResolver.resolve(jdkPath) }
        verify(exactly = 1) { keystoreResolver.resolve(jdkPath, expectedJavaInfo) }
        confirmVerified(javaResolver, keystoreResolver)
    }

    @Test
    fun `createJdk passes resolved JavaInfo to KeystoreInfoResolver`(
        @TempDir tempDir: Path,
    ) {
        val javaResolver = mockk<JavaInfoResolver>()
        val keystoreResolver = mockk<KeystoreInfoResolver>()

        val jdkPath = tempDir.resolve("jdk2")
        Files.createDirectories(jdkPath)

        val producedJavaInfo = JavaInfo(major = 8, fullVersion = "1.8.0_332", vendor = "Oracle")
        val keystoreReturned = KeystoreInfo(keystorePath = jdkPath.resolve("lib/security/cacerts"), cacertsShortcutEnabled = false)

        val slot = slot<JavaInfo>()
        every { javaResolver.resolve(jdkPath) } returns producedJavaInfo
        every { keystoreResolver.resolve(jdkPath, capture(slot)) } returns keystoreReturned

        val creator = JdkCreator(javaResolver, keystoreResolver)
        val jdk = creator.createJdk(jdkPath)

        assertNotNull(slot.captured)
        assertEquals(producedJavaInfo, slot.captured)
        assertEquals(producedJavaInfo, jdk.javaInfo)

        verifySequence {
            javaResolver.resolve(jdkPath)
            keystoreResolver.resolve(jdkPath, producedJavaInfo)
        }
        confirmVerified(javaResolver, keystoreResolver)
    }

    @Test
    fun `createJdk propagates exception from java resolver`(
        @TempDir tempDir: Path,
    ) {
        val javaResolver = mockk<JavaInfoResolver>()
        val keystoreResolver = mockk<KeystoreInfoResolver>()

        val jdkPath = tempDir.resolve("jdk3")
        Files.createDirectories(jdkPath)

        every { javaResolver.resolve(jdkPath) } throws IllegalStateException("java resolver failed")
        // ensure keystore resolver has no specific behavior - it must not be called

        val creator = JdkCreator(javaResolver, keystoreResolver)

        val ex =
            assertThrows(IllegalStateException::class.java) {
                creator.createJdk(jdkPath)
            }
        assertEquals("java resolver failed", ex.message)

        verify(exactly = 1) { javaResolver.resolve(jdkPath) }
        verify(exactly = 0) { keystoreResolver.resolve(any(), any()) }
        confirmVerified(javaResolver, keystoreResolver)
    }
}
