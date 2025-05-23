package edu.adarko22.commands

import edu.adarko22.utils.JdkDiscover
import io.mockk.every
import io.mockk.mockk
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test
import java.nio.file.Path
import java.nio.file.Paths

class ListJDKsTest {

    private fun createCommandWithMock(jdkPaths: List<Path>): Pair<ListJDKs, MutableList<String>> {
        val jdkDiscover = mockk<JdkDiscover>()
        every { jdkDiscover.discoverJdkHomes(any()) } returns jdkPaths

        val output = mutableListOf<String>()
        val command = ListJDKs(jdkDiscovery = jdkDiscover, print = output::add)
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
        assertTrue(output.any { it.contains("Found JDKs") }, "Expected output to contain 'Found JDKs'")
        jdkPaths.forEach {
            assertTrue(output.any { msg -> msg.contains(it.toString()) }, "Expected output to contain $it")
        }
    }
}

