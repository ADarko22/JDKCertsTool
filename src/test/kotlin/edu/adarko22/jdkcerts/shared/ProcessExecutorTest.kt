package edu.adarko22.jdkcerts.shared

import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test

class ProcessExecutorTest {
    private val executor = DefaultProcessExecutor()

    @Test
    fun `runCommand should capture stdout from a simple echo`() {
        val result = executor.runCommand(listOf("echo", "Hello, World!"))

        assertEquals(0, result.exitCode)
        assertEquals("Hello, World!", result.stdout)
        assertTrue(result.stderr.isEmpty(), "stderr should be empty")
    }

    @Test
    fun `runCommand should capture non-zero exit code for invalid command`() {
        val result = executor.runCommand(listOf("bash", "-c", "exit 42"))

        assertEquals(42, result.exitCode)
        assertTrue(result.stdout.isEmpty(), "stdout should be empty")
        assertTrue(result.stderr.isEmpty(), "stderr should be empty for clean exit")
    }

    @Test
    fun `runCommand should capture stderr from failing command`() {
        val result = executor.runCommand(listOf("bash", "-c", "echo error 1>&2; exit 1"))

        assertEquals(1, result.exitCode)
        assertEquals("error", result.stderr)
        assertTrue(result.stdout.isEmpty(), "stdout should be empty")
    }
}
