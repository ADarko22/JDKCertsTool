package edu.adarko22.jdkcerts.jdk.impl.keytool

class KeytoolCommand(
    private val args: MutableList<String> = mutableListOf(),
    private var resolveKeystore: Boolean = false,
) {
    fun addArg(arg: String): KeytoolCommand {
        args.add(arg)
        return this
    }

    fun withKeystoreResolution(enable: Boolean = true): KeytoolCommand {
        resolveKeystore = enable
        return this
    }

    fun shouldResolveKeystore(): Boolean = resolveKeystore

    fun toArgsList(): List<String> = args.toList()

    override fun toString(): String = args.joinToString(" ")
}
