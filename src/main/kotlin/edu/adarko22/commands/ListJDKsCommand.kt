package edu.adarko22.commands

import edu.adarko22.utils.JdkDiscover

class ListJDKsCommand(
    private val jdkDiscovery: JdkDiscover,
    override val print: (String) -> Unit = { msg -> println(msg) }
) : BaseJdkCommand(name = "list-jdk", help = "List all discovered JDKs") {

    override fun run() {
        discoverAndListJdks(jdkDiscovery)
    }
}