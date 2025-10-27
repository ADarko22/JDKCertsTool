package edu.adarko22.jdkcerts.process

import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test

class DefaultProcessRunnerTest {
    @Test
    fun `dry run returns message and does not execute`() {
        val cmd = listOf("/bin/echo", "hello")
        val result = DefaultProcessRunner.runCommand(cmd, dryRun = true)

        assertEquals(0, result.exitCode)
        assertEquals("", result.stdout)
        assertEquals("", result.stderr)
        assertTrue(result.dryRunOutput.contains("Dry run: would run"))
        assertTrue(result.dryRunOutput.contains(cmd.joinToString(" ")))
    }

    @Test
    fun `runCommand executes process and captures stdout and exit code`() {
        val cmd = listOf("/bin/echo", "hello")
        val result = DefaultProcessRunner.runCommand(cmd, dryRun = false)

        assertEquals(0, result.exitCode)
        assertEquals("hello", result.stdout)
        assertEquals("", result.stderr)
        assertEquals("", result.dryRunOutput)
    }

    @Test
    fun `runCommand returns error result when process start fails`() {
        val cmd = listOf("/nonexistent/command")
        val result = DefaultProcessRunner.runCommand(cmd, dryRun = false)

        assertEquals(-1, result.exitCode)
        assertEquals("", result.stdout)
        assertTrue(result.stderr.startsWith("Error executing command:"))
        assertEquals("", result.dryRunOutput)
    }
}
