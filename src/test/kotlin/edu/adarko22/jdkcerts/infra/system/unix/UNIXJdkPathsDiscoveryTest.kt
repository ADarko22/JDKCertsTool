package edu.adarko22.jdkcerts.infra.system.unix

import edu.adarko22.jdkcerts.createDir
import edu.adarko22.jdkcerts.createExecutableFile
import edu.adarko22.jdkcerts.createValidJdkPath
import edu.adarko22.jdkcerts.infra.system.SystemInfoProvider
import org.junit.jupiter.api.Assertions
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.TestInstance
import org.junit.jupiter.api.io.TempDir
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.Arguments
import org.junit.jupiter.params.provider.MethodSource
import org.mockito.kotlin.mock
import org.mockito.kotlin.whenever
import java.nio.file.Files
import java.nio.file.Path

@TestInstance(TestInstance.Lifecycle.PER_CLASS)
class UNIXJdkPathsDiscoveryTest {
    private lateinit var systemInfoProvider: SystemInfoProvider
    private lateinit var jdkDiscovery: UNIXJdkPathsDiscovery

    @TempDir
    lateinit var tempDir: Path

    @BeforeEach
    fun setUp() {
        systemInfoProvider = mock()
        // Default mock behavior for common properties
        whenever(systemInfoProvider.getUserHome()).thenReturn(tempDir.resolve("user_home"))
        Files.createDirectories(systemInfoProvider.getUserHome())
        whenever(systemInfoProvider.getOsName()).thenReturn("Linux")
        whenever(systemInfoProvider.getProgramFilesEnv()).thenReturn(null)
        whenever(systemInfoProvider.getProgramFilesX86Env()).thenReturn(null)

        jdkDiscovery = UNIXJdkPathsDiscovery(systemInfoProvider)
    }

    @Test
    fun `should detect JBR inside IntelliJ IDEA app in Applications folder`() {
        val jbrContents =
            createDir(systemInfoProvider.getUserHome(), "Applications/IntelliJ IDEA Ultimate.app/Contents/jbr/Contents")

        val result = jdkDiscovery.discoverJetBrainsRuntimeHomes()
        Assertions.assertEquals(listOf(jbrContents), result)
    }

    @ParameterizedTest(name = "should detect JBR from Toolbox at {0}")
    @MethodSource("toolboxPathsProvider")
    fun `should detect JBR inside JetBrains Toolbox installations`(toolboxBase: Path) {
        val toolboxDir = createDir(systemInfoProvider.getUserHome(), toolboxBase.toString())
        val jbrContents = createDir(toolboxDir, "IDEA-U/2024.1.0/jbr/Contents")

        val result = jdkDiscovery.discoverJetBrainsRuntimeHomes()

        Assertions.assertEquals(listOf(jbrContents), result)
    }

    @Test
    fun `should return empty list when no valid paths exist`() {
        val result = jdkDiscovery.discoverJetBrainsRuntimeHomes()
        Assertions.assertTrue(result.isEmpty())
    }

    @ParameterizedTest(name = "path ''{0}'' isJdkBundle={1}")
    @MethodSource("jdkPathsProvider")
    fun `should correctly convert or return JDK path`(
        dirName: String,
        isJdkBundle: Boolean,
    ) {
        val jdkPath = createDir(tempDir, dirName)
        val expected = if (isJdkBundle) jdkPath.resolve("Contents/Home") else jdkPath
        val javaHome = jdkDiscovery.toJavaHome(jdkPath)
        Assertions.assertEquals(expected, javaHome)
    }

    @ParameterizedTest(name = "valid home on {0}")
    @MethodSource("validJavaHomesProvider")
    fun `should return true for valid Java home`(
        osName: String,
        dirName: String,
        isWindows: Boolean,
    ) {
        whenever(systemInfoProvider.getOsName()).thenReturn(osName)
        val jdkHome = createValidJdkPath(tempDir, dirName, isWindows)
        Assertions.assertTrue(jdkDiscovery.isValidJavaHome(jdkHome))
    }

    @ParameterizedTest(name = "invalid home on {0}, missing {2}")
    @MethodSource("invalidJavaHomesProvider")
    fun `should return false for invalid Java home`(
        osName: String,
        dirName: String,
        presentFiles: List<String>,
    ) {
        whenever(systemInfoProvider.getOsName()).thenReturn(osName)
        val jdkHome = createDir(tempDir, dirName)
        val binDir = createDir(jdkHome, "bin")
        presentFiles.forEach { createExecutableFile(binDir, it) }
        Assertions.assertFalse(jdkDiscovery.isValidJavaHome(jdkHome))
    }

    @Test
    fun `should return false if the provided path does not exist`() {
        val nonExistentPath = tempDir.resolve("non_existent_jdk")
        Assertions.assertFalse(jdkDiscovery.isValidJavaHome(nonExistentPath))
    }

    companion object Companion {
        @JvmStatic
        fun toolboxPathsProvider(): List<Arguments> =
            listOf(
                Arguments.of("Library/Application Support/JetBrains/Toolbox/apps"),
                Arguments.of(".local/share/JetBrains/Toolbox/apps"),
            )

        @JvmStatic
        fun jdkPathsProvider() =
            listOf(
                Arguments.of("jdk-11.0.12", false),
                Arguments.of("bad-jdk.jdk", true),
            )

        @JvmStatic
        fun validJavaHomesProvider() =
            listOf(
                Arguments.of("Linux", "openjdk-17", false),
            )

        @JvmStatic
        fun invalidJavaHomesProvider() =
            listOf(
                Arguments.of("macOS", "invalid_jdk_no_java", listOf("keytool")),
                Arguments.of("Linux", "invalid_jdk_no_keytool", listOf("java")),
            )
    }
}
