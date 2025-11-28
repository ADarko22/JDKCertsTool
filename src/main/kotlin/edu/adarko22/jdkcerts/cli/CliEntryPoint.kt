package edu.adarko22.jdkcerts.cli

/**
 * Contract for running the command-line application.
 * Hides the underlying CliktCommand implementation from the main entry point.
 */
interface CliEntryPoint {
    fun run(args: Array<String>)
}
