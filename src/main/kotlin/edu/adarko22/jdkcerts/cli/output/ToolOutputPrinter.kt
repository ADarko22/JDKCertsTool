package edu.adarko22.jdkcerts.cli.output

/**
 * Abstraction for printing output from CLI commands.
 *
 * Implementations define how messages are displayed to the user (e.g., console, file, or test capture).
 */
interface ToolOutputPrinter {
    fun print(message: String)
}
