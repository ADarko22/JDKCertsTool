package edu.adarko22.jdkcerts.cli.command

import com.github.ajalt.clikt.core.ParameterHolder
import com.github.ajalt.clikt.parameters.options.OptionDelegate
import com.github.ajalt.clikt.parameters.options.convert
import com.github.ajalt.clikt.parameters.options.default
import com.github.ajalt.clikt.parameters.options.flag
import com.github.ajalt.clikt.parameters.options.option
import com.github.ajalt.clikt.parameters.options.required
import java.nio.file.Path
import java.nio.file.Paths

fun ParameterHolder.dryRunOption(): OptionDelegate<Boolean> = option("--dry-run", help = "Preview changes only").flag()

fun ParameterHolder.customJdkDirsOption(): OptionDelegate<List<Path>> =
    option("--custom-jdk-dirs", help = "Comma-separated paths to JDK dirs")
        .convert { it.toPaths() }
        .default(emptyList())

fun ParameterHolder.keystorePasswordOption(): OptionDelegate<String> =
    option("--keystore-password", help = "Keystore password").default("changeit")

fun ParameterHolder.certPathOption(): OptionDelegate<Path> =
    option("--cert", help = "Path to the certificate file")
        .convert { it.toPath() }
        .required()

fun ParameterHolder.aliasOption(): OptionDelegate<String> =
    option("--alias", help = "Certificate alias")
        .default("custom-cert")

private fun String.toPaths(): List<Path> = split(",").map { it.trim().toPath() }

private fun String.toPath(): Path = Paths.get(this.expandHome())

private fun String.expandHome(): String = if (startsWith("~")) System.getProperty("user.home") + drop(1) else this
