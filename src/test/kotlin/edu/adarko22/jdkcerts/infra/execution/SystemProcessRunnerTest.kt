package edu.adarko22.jdkcerts.infra.execution

import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.test.runTest
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test

class SystemProcessRunnerTest {
    @Test
    fun `runCommand executes process and captures stdout and exit code`() =
        runTest {
            val cmd = listOf("/bin/echo", "hello")
            val result = SystemProcessRunner().runCommand(cmd)

            assertEquals(0, result.exitCode)
            assertEquals("hello", result.stdout)
            assertEquals("", result.stderr)
        }

    @Test
    fun `runCommand returns error result when process start fails`() =
        runTest {
            val cmd = listOf("/nonexistent/command")
            val result = SystemProcessRunner().runCommand(cmd)

            assertEquals(-1, result.exitCode)
            assertEquals("", result.stdout)
            assertTrue(result.stderr.startsWith("Error executing command:"))
        }

    @Test
    fun `multiple concurrent commands respect parallelism limits`() =
        runTest {
            val runner = SystemProcessRunner(maxConcurrentProcesses = 2)
            val results =
                (1..5)
                    .map {
                        async { runner.runCommand(listOf("/bin/sleep", "0.1")) }
                    }.awaitAll()

            assertTrue(results.all { it.exitCode == 0 })
        }

    @Test
    fun `long-running command is killed on timeout`() =
        runTest {
            val runner = SystemProcessRunner(timeoutMillis = 100)
            val result = runner.runCommand(listOf("/bin/sleep", "2"))

            assertEquals(-1, result.exitCode)
            assertTrue(result.stderr.contains("timed out"))
        }
}
