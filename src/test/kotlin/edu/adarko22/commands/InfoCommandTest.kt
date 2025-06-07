package edu.adarko22.commands

import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.Test

class InfoCommandTest {

    @Test
    fun `run prints the expected info`() {
        val output = mutableListOf<String>()
        val command = InfoCommand { msg -> output.add(msg) }
        command.run()

        assertAll(
            "Info command output",
            { assertTrue(output.isNotEmpty(), "Expected output to be non-empty") },
            { assertTrue(output.any { it.contains("JDK Management Tool") }, "Expected output to contain 'JDK Management Tool'") },
            { assertTrue(output.any { it.contains("Version:") }, "Expected output to contain 'Version:'") },
            { assertTrue(output.any { it.contains("Author: ") }, "Expected output to contain 'Author: '") }
        )
    }
}