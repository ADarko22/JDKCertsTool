package edu.adarko22.jdkcerts.clickt.jdk.keytool

import edu.adarko22.jdkcerts.jdk.Jdk
import edu.adarko22.jdkcerts.jdk.discovery.JdkDiscovery
import edu.adarko22.jdkcerts.jdk.impl.keystore.KeystoreInfo
import edu.adarko22.jdkcerts.jdk.impl.keytool.KeytoolCommand
import edu.adarko22.jdkcerts.output.CommandPrinter
import edu.adarko22.jdkcerts.process.ProcessResult
import edu.adarko22.jdkcerts.process.ProcessRunner
import io.mockk.every
import io.mockk.mockk
import io.mockk.slot
import io.mockk.verify
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test
import java.nio.file.Paths

class KeytoolCommandExecutorTest {
    @Test
    fun `execute prints stdout and summary on success and uses -cacerts when shortcut enabled`() {
        val keytoolPath = Paths.get("/fake/jdk/bin/keytool")
        val keystorePath = Paths.get("/fake/jdk/lib/security/cacerts")

        val jdk = mockk<Jdk>()
        every { jdk.keytoolPath } returns keytoolPath
        every { jdk.keystoreInfo } returns KeystoreInfo(keystorePath = keystorePath, cacertsShortcutEnabled = true)

        val jdkDiscovery = mockk<JdkDiscovery>()
        every { jdkDiscovery.discoverJDKs(any()) } returns listOf(jdk)

        val keytoolCommand = mockk<KeytoolCommand>()
        every { keytoolCommand.shouldResolveKeystore() } returns true
        every { keytoolCommand.toArgsList() } returns listOf("-importcert", "-file", "/tmp/cert.pem")

        val printed = mutableListOf<String>()
        val output = mockk<CommandPrinter>()
        every { output.print(capture(printed)) } answers { }

        val cmdSlot = slot<List<String>>()
        val processRunner = mockk<ProcessRunner>()
        every { processRunner.runCommand(capture(cmdSlot), false) } returns ProcessResult("imported", "", 0, "")

        val executor = KeytoolCommandExecutor(jdkDiscovery, processRunner, output)
        executor.execute(keytoolCommand, emptyList(), dryRun = false)

        assertTrue(printed.any { it.contains("Running on") }, "should print Running on")
        assertTrue(printed.any { it.contains("imported") }, "should print process stdout")
        assertTrue(printed.any { it.contains("Summary:") && it.contains("1/1 succeeded") }, "should print summary success")

        val built = cmdSlot.captured
        assertEquals(keytoolPath.toAbsolutePath().toString(), built.first(), "first arg is keytool executable")
        assertTrue(built.containsAll(listOf("-importcert", "-file", "/tmp/cert.pem")))
        assertTrue(built.contains("-cacerts"), "should include -cacerts when shortcut enabled")

        verify(exactly = 1) { processRunner.runCommand(any(), false) }
    }

    @Test
    fun `execute prints dryRunOutput and keystore when shortcut disabled and honors dryRun`() {
        val keytoolPath = Paths.get("/fake/jdk2/bin/keytool")
        val keystorePath = Paths.get("/fake/jdk2/lib/security/cacerts")

        val jdk = mockk<Jdk>()
        every { jdk.keytoolPath } returns keytoolPath
        every { jdk.keystoreInfo } returns KeystoreInfo(keystorePath = keystorePath, cacertsShortcutEnabled = false)

        val jdkDiscovery = mockk<JdkDiscovery>()
        every { jdkDiscovery.discoverJDKs(any()) } returns listOf(jdk)

        val keytoolCommand = mockk<KeytoolCommand>()
        every { keytoolCommand.shouldResolveKeystore() } returns true
        every { keytoolCommand.toArgsList() } returns listOf("-delete", "-alias", "oldcert")

        val printed = mutableListOf<String>()
        val output = mockk<CommandPrinter>()
        every { output.print(capture(printed)) } answers { }

        val cmdSlot = slot<List<String>>()
        val processRunner = mockk<ProcessRunner>()
        every { processRunner.runCommand(capture(cmdSlot), true) } returns
            ProcessResult(
                "",
                "",
                0,
                "Dry run: would run /fake/jdk2/bin/keytool -delete -alias oldcert -keystore ${keystorePath.toAbsolutePath()}",
            )

        val executor = KeytoolCommandExecutor(jdkDiscovery, processRunner, output)
        executor.execute(keytoolCommand, emptyList(), dryRun = true)

        assertTrue(printed.any { it.contains("Running on") }, "should print Running on")
        assertTrue(printed.any { it.contains("Dry run: would run") }, "should print dry run output")
        assertTrue(printed.any { it.contains("Summary:") && it.contains("1/1 succeeded") }, "should print summary success for dry-run")

        val built = cmdSlot.captured
        assertTrue(built.containsAll(listOf("-delete", "-alias", "oldcert")))
        val idx = built.indexOf("-keystore")
        assertTrue(idx >= 0, "should include -keystore when shortcut disabled")
        assertEquals(keystorePath.toAbsolutePath().toString(), built[idx + 1], "keystore path should follow -keystore")

        verify(exactly = 1) { processRunner.runCommand(any(), true) }
    }
}
