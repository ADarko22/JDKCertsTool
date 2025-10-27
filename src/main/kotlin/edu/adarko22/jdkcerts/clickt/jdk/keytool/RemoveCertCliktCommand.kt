package edu.adarko22.jdkcerts.clickt.jdk.keytool

import com.github.ajalt.clikt.core.CliktCommand
import com.github.ajalt.clikt.core.Context
import edu.adarko22.jdkcerts.clickt.aliasOption
import edu.adarko22.jdkcerts.clickt.customJdkDirsOption
import edu.adarko22.jdkcerts.clickt.dryRunOption
import edu.adarko22.jdkcerts.clickt.keystorePasswordOption
import edu.adarko22.jdkcerts.jdk.discovery.UNIXJdkDiscovery
import edu.adarko22.jdkcerts.jdk.impl.keytool.KeytoolCommand
import edu.adarko22.jdkcerts.output.CommandPrinter
import java.nio.file.Path

/**
 * Command for removing certificates from JDK keystore by alias.
 *
 * This command removes a certificate from the cacerts keystore of all discovered
 * JDK installations using the specified alias. It supports dry-run mode for
 * previewing changes and provides feedback on the removal process for each JDK.
 *
 * The alias must match exactly what was used when the certificate was originally installed.
 */
class RemoveCertCliktCommand : CliktCommand(name = "remove-cert") {
    private val customJdkDirs: List<Path> by customJdkDirsOption()
    private val dryRun: Boolean by dryRunOption()
    private val keystorePassword: String by keystorePasswordOption()
    private val alias: String by aliasOption()

    override fun help(context: Context) = "Remove certificate from all JDK keystores"

    override fun run() {
        val command =
            KeytoolCommand()
                .addArg("-delete")
                .addArg("-alias")
                .addArg(alias)
                .addArg("-storepass")
                .addArg(keystorePassword)
                .withKeystoreResolution()

        val commandExecutor = createKeytoolCommandExecutor()
        commandExecutor.execute(command, customJdkDirs, dryRun)
    }

    fun createKeytoolCommandExecutor(): KeytoolCommandExecutor =
        KeytoolCommandExecutor(
            jdkDiscovery = UNIXJdkDiscovery(),
            output = CommandPrinter(),
        )
}
