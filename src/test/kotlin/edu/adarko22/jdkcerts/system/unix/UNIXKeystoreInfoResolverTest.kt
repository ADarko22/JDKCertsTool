package edu.adarko22.jdkcerts.system.unix

import edu.adarko22.jdkcerts.core.jdk.JavaInfo
import org.junit.jupiter.api.Assertions
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.io.TempDir
import java.nio.file.Files
import java.nio.file.Path

class UNIXKeystoreInfoResolverTest {
    @Test
    fun `resolve picks lib security cacerts when present`(
        @TempDir tempDir: Path,
    ) {
        val jdkPath = tempDir
        val expected = jdkPath.resolve("lib/security/cacerts")
        Files.createDirectories(expected.parent)
        Files.createFile(expected)

        val javaInfo = JavaInfo(major = 11, fullVersion = "11.0.11", vendor = "OpenJDK")
        val result = UNIXKeystoreInfoResolver().resolve(jdkPath, javaInfo)

        Assertions.assertEquals(expected, result.keystorePath)
        Assertions.assertTrue(result.cacertsShortcutEnabled, "major > 8 should enable cacerts shortcut")
    }

    @Test
    fun `resolve falls back to jre lib security cacerts when lib missing`(
        @TempDir tempDir: Path,
    ) {
        val jdkPath = tempDir
        val expected = jdkPath.resolve("jre/lib/security/cacerts")
        Files.createDirectories(expected.parent)
        Files.createFile(expected)

        val javaInfo = JavaInfo(major = 8, fullVersion = "1.8.0_332", vendor = "Oracle")
        val result = UNIXKeystoreInfoResolver().resolve(jdkPath, javaInfo)

        Assertions.assertEquals(expected, result.keystorePath)
        Assertions.assertFalse(result.cacertsShortcutEnabled, "major = 8 should not enable cacerts shortcut")
    }

    @Test
    fun `resolve throws when no cacerts found`(
        @TempDir tempDir: Path,
    ) {
        val jdkPath = tempDir
        val javaInfo = JavaInfo(major = 11, fullVersion = "11.0.11", vendor = "OpenJDK")

        val ex =
            Assertions.assertThrows(IllegalStateException::class.java) {
                UNIXKeystoreInfoResolver().resolve(jdkPath, javaInfo)
            }
        Assertions.assertTrue(ex.message?.contains("Could not find cacerts file") == true)
    }
}
