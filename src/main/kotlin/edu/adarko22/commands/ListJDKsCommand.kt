package edu.adarko22.commands

import edu.adarko22.process.JdkDiscovery

class ListJDKsCommand(
    private val jdkDiscovery: JdkDiscovery,
    override val printer: (String) -> Unit = { msg -> println(msg) },
) : BaseJdkCommand(name = "list-jdks", help = "List all discovered JDKs") {
    override fun run() {
        discoverAndListJdks(jdkDiscovery)
    }
}
