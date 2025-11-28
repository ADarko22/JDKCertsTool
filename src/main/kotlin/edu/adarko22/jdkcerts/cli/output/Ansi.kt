package edu.adarko22.jdkcerts.cli.output

typealias PrintFn = (String) -> Unit

/**
 * ANSI color and formatting utilities for terminal output.
 *
 * These extension functions add ANSI escape sequences to strings for colored
 * and formatted output in terminal environments. They automatically reset
 * formatting after each string to prevent color bleeding between outputs.
 *
 * Supported formatting includes colors (red, green, yellow, blue) and
 * text styles (bold, italic) for consistent user interface presentation.
 */
fun String.red() = "\u001B[31m$this\u001B[0m"

fun String.green() = "\u001B[32m$this\u001B[0m"

fun String.blue() = "\u001B[34m$this\u001B[0m"

fun String.yellow() = "\u001B[33m$this\u001B[0m"

fun String.bold() = "\u001B[1m$this\u001B[0m"

fun String.italic() = "\u001B[3m$this\u001B[0m"
