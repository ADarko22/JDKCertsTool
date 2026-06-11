package edu.adarko22.jdkcerts.core.jdk.keytool.model

class KeytoolCommandFactory {
    companion object {
        fun findCertificateKeytoolCommand(
            alias: String,
            keystorePassword: String,
        ): KeytoolCommand =
            KeytoolCommand
                .Builder()
                .addArg("-list")
                .addArg("-v")
                .addArg("-alias")
                .addArg(alias)
                .addArg("-storepass")
                .addArg(keystorePassword)
                .withKeystoreResolution()
                .build()

        fun installCertificateKeytoolCommand(
            alias: String,
            keystorePassword: String,
            certificateAbsolutePath: String,
        ): KeytoolCommand =
            KeytoolCommand
                .Builder()
                .addArg("-importcert")
                .addArg("-noprompt")
                .addArg("-trustcacerts")
                .addArg("-alias")
                .addArg(alias)
                .addArg("-file")
                .addArg(certificateAbsolutePath)
                .addArg("-storepass")
                .addArg(keystorePassword)
                .withKeystoreResolution()
                .build()

        fun removeCertificateKeytoolCommand(
            alias: String,
            keystorePassword: String,
        ): KeytoolCommand =
            KeytoolCommand
                .Builder()
                .addArg("-delete")
                .addArg("-alias")
                .addArg(alias)
                .addArg("-storepass")
                .addArg(keystorePassword)
                .withKeystoreResolution()
                .build()
    }
}
