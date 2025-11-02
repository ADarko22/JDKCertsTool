package edu.adarko22.jdkcerts.core.process

import org.junit.jupiter.api.Assertions
import org.junit.jupiter.api.Test

class DefaultProcessRunnerTest {
    @Test
    fun `dry run returns message and does not execute`() {
        val cmd = listOf("/bin/echo", "hello")
        val result = DefaultProcessRunner().runCommand(cmd, dryRun = true)

        Assertions.assertEquals(0, result.exitCode)
        Assertions.assertEquals("", result.stdout)
        Assertions.assertEquals("", result.stderr)
        Assertions.assertTrue(result.dryRunOutput.contains("Dry run: would run"))
        Assertions.assertTrue(result.dryRunOutput.contains(cmd.joinToString(" ")))
    }

    @Test
    fun `runCommand executes process and captures stdout and exit code`() {
        val cmd = listOf("/bin/echo", "hello")
        val result = DefaultProcessRunner().runCommand(cmd, dryRun = false)

        Assertions.assertEquals(0, result.exitCode)
        Assertions.assertEquals("hello", result.stdout)
        Assertions.assertEquals("", result.stderr)
        Assertions.assertEquals("", result.dryRunOutput)
    }

    @Test
    fun `runCommand returns error result when process start fails`() {
        val cmd = listOf("/nonexistent/command")
        val result = DefaultProcessRunner().runCommand(cmd, dryRun = false)

        Assertions.assertEquals(-1, result.exitCode)
        Assertions.assertEquals("", result.stdout)
        Assertions.assertTrue(result.stderr.startsWith("Error executing command:"))
        Assertions.assertEquals("", result.dryRunOutput)
    }
}
