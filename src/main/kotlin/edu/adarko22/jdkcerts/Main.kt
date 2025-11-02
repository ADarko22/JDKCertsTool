package edu.adarko22.jdkcerts

import edu.adarko22.jdkcerts.cli.CliBuilder
import edu.adarko22.jdkcerts.core.jdk.parser.DefaultJavaInfoParser
import edu.adarko22.jdkcerts.core.jdk.usecase.DiscoverJdksUseCase
import edu.adarko22.jdkcerts.core.jdk.usecase.ResolveJavaInfoUseCase
import edu.adarko22.jdkcerts.core.process.DefaultProcessRunner
import edu.adarko22.jdkcerts.system.SystemType

/**
 * Main entry point for the JDK Certificate Management Tool.
 *
 * This application provides a command-line interface for managing certificates
 * across multiple JDK installations. It automates the process of adding and
 * removing certificates from JDK keystores using the keytool utility.
 *
 * The application supports four main commands:
 * - info: Display tool information and version
 * - list-jdks: List all discovered JDK installations
 * - install-cert: Install certificates into JDK keystores
 * - remove-cert: Remove certificates from JDK keystores by alias
 *
 * @author Angelo Buono (adarko22)
 */
fun main(args: Array<String>) {
    // Details
    val systemType = SystemType.UNIX
    val processRunner = DefaultProcessRunner()
    val javaInfoParser = DefaultJavaInfoParser()

    // Use-cases
    val resolveJavaInfo = ResolveJavaInfoUseCase(processRunner, javaInfoParser)
    val discoverJdks =
        DiscoverJdksUseCase(
            systemType.jdkPathDiscovery(),
            systemType.keystoreInfoResolver(),
            resolveJavaInfo,
        )

    // Build the CLI and Run with args
    CliBuilder(
        discoverJdks,
        processRunner,
    ).withInfo()
        .withInfo()
        .withListJdks()
        .withInstallCert()
        .withRemoveCert()
        .build()
        .run(args)
}
