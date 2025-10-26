package edu.adarko22.jdkcerts.commands

import com.github.ajalt.clikt.core.parse
import edu.adarko22.jdkcerts.jdk.JdkDiscovery
import io.mockk.every
import io.mockk.mockk
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertAll
import java.nio.file.Path
import java.nio.file.Paths

class ListJDKsCommandTest {
    private fun createCommandWithMock(jdkPaths: List<Path>): Pair<ListJDKsCommand, MutableList<String>> {
        val jdkDiscovery = mockk<JdkDiscovery>()
        every { jdkDiscovery.discoverJdkHomes(any()) } returns jdkPaths

        val output = mutableListOf<String>()
        val command = ListJDKsCommand(jdkDiscovery = jdkDiscovery, printer = output::add)
        command.parse(emptyArray())
        return command to output
    }

    @Test
    fun `run prints no jdks found when empty`() {
        val (command, output) = createCommandWithMock(emptyList())
        command.run()
        assertTrue(output.any { it.contains("No JDKs found") }, "Expected output to contain 'No JDKs found'")
    }

    @Test
    fun `run prints discovered jdks`() {
        val jdkPaths = listOf(Paths.get("/fake/jdk1"), Paths.get("/fake/jdk2"))
        val (command, output) = createCommandWithMock(jdkPaths)
        command.run()
        assertAll(
            "List JDKs command output",
            { assertTrue(output.isNotEmpty(), "Expected output to be non-empty") },
            { assertTrue(output.any { it.contains("Found JDKs") }, "Expected output to contain 'Found JDKs'") },
            {
                assertTrue(
                    output.any { it.contains(jdkPaths[0].toString()) },
                    "Expected output to contain ${jdkPaths[0]}",
                )
            },
            {
                assertTrue(
                    output.any { it.contains(jdkPaths[1].toString()) },
                    "Expected output to contain ${jdkPaths[1]}",
                )
            },
        )
    }
}
