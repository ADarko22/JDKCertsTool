package edu.adarko22.commands

import edu.adarko22.process.JdkDiscovery

/**
 * Command that lists all discovered JDK installations on the system.
 *
 * This command searches for JDK installations in standard locations and any
 * custom directories specified by the user, then displays a formatted list
 * of all found JDKs. It's useful for verifying which JDKs the tool can
 * operate on before performing certificate operations.
 */
class ListJDKsCommand(
    private val jdkDiscovery: JdkDiscovery,
    override val printer: (String) -> Unit = { msg -> println(msg) },
) : BaseJdkCommand(name = "list-jdks", help = "List all discovered JDKs") {
    override fun run() {
        discoverAndListJdks(jdkDiscovery)
    }
}
