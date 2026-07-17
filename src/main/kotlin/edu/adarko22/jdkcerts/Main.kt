package edu.adarko22.jdkcerts

import edu.adarko22.jdkcerts.cli.CliBuilder
import edu.adarko22.jdkcerts.core.jdk.DiscoverJdksUseCase
import edu.adarko22.jdkcerts.core.jdk.java.parser.DefaultJavaInfoParser
import edu.adarko22.jdkcerts.core.jdk.java.usecase.ResolveJavaInfoUseCase
import edu.adarko22.jdkcerts.core.jdk.keytool.parser.DefaultCertificateInfoParser
import edu.adarko22.jdkcerts.core.jdk.keytool.usecase.ExecuteKeytoolCommandUseCase
import edu.adarko22.jdkcerts.core.jdk.keytool.usecase.FindKeytoolCertificateUseCase
import edu.adarko22.jdkcerts.infra.execution.KeytoolProcessRunnerImpl
import edu.adarko22.jdkcerts.infra.execution.SystemProcessRunner
import edu.adarko22.jdkcerts.infra.system.SystemType

/**
 * Entry point for the `jdkcerts` CLI application.
 *
 * Sets up system-specific dependencies, use cases, and the CLI builder
 * with all available commands, then executes the CLI using the provided command-line arguments.
 *
 * Supported commands include:
 *  - info: display system and JDK information
 *  - list-jdks: list all discovered JDKs
 *  - install-cert: install a certificate into JDK keystores
 *  - remove-cert: remove a certificate from JDK keystores
 *
 * @author Angelo Buono (adarko22)
 *
 * @param args Command-line arguments passed to the CLI.
 */
fun main(args: Array<String>) {
    // Details
    val systemType = SystemType.UNIX
    val processRunner =
        SystemProcessRunner(
            Runtime.getRuntime().availableProcessors().coerceAtLeast(2),
            10_000L,
        )
    val keytoolProcessRunner = KeytoolProcessRunnerImpl(processRunner)
    val javaInfoParser = DefaultJavaInfoParser()
    val certificateInfoParser = DefaultCertificateInfoParser()

    // Use-cases
    val resolveJavaInfoUseCase = ResolveJavaInfoUseCase(processRunner, javaInfoParser)
    val discoverJdksUseCase =
        DiscoverJdksUseCase(
            systemType.jdkPathDiscovery(),
            systemType.keystoreInfoResolver(),
            resolveJavaInfoUseCase,
        )

    val executeKeytoolCommandUseCase = ExecuteKeytoolCommandUseCase(discoverJdksUseCase, keytoolProcessRunner)
    val findKeytoolCertificateUseCase =
        FindKeytoolCertificateUseCase(discoverJdksUseCase, keytoolProcessRunner, certificateInfoParser)

    // Build the CLI and Run with args
    CliBuilder(
        discoverJdksUseCase,
        executeKeytoolCommandUseCase,
        findKeytoolCertificateUseCase,
    ).withInfo()
        .withListJdks()
        .withInstallCert()
        .withRemoveCert()
        .withFindCert()
        .build()
        .run(args)
}
