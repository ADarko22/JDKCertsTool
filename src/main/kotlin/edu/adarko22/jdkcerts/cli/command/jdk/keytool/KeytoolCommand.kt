package edu.adarko22.jdkcerts.cli.command.jdk.keytool

data class KeytoolCommand(
    val args: List<String>,
    val resolveKeystore: Boolean = false,
) {
    class Builder(
        private val args: MutableList<String> = mutableListOf(),
        private var resolveKeystore: Boolean = false,
    ) {
        fun addArg(arg: String): Builder {
            args.add(arg)
            return this
        }

        fun withKeystoreResolution(enable: Boolean = true): Builder {
            resolveKeystore = enable
            return this
        }

        fun build(): KeytoolCommand = KeytoolCommand(args.toList(), resolveKeystore)
    }
}
