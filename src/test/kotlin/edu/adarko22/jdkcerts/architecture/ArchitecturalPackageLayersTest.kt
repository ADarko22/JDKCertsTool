package edu.adarko22.jdkcerts.architecture

import com.tngtech.archunit.junit.AnalyzeClasses
import com.tngtech.archunit.junit.ArchTest
import com.tngtech.archunit.library.Architectures
import com.tngtech.archunit.library.Architectures.layeredArchitecture

@AnalyzeClasses(packages = ["edu.adarko22.jdkcerts"])
class ArchitecturalPackageLayersTest {
    companion object {
        private const val CLI_LAYER = "cli"
        private const val CORE_LAYER = "core"
        private const val INFRA_LAYER = "infra"

        const val CLI_PACKAGE = "..cli.."
        const val CORE_PACKAGE = "..core.."
        const val INFRA_PACKAGE = "..infra.."
    }

    @JvmField
    val architecturalLayers: Architectures.LayeredArchitecture =
        layeredArchitecture()
            // Ignore unlayered files like Main.kt and system/stdlib libraries
            .consideringOnlyDependenciesInLayers()
            .layer(CLI_LAYER)
            .definedBy(CLI_PACKAGE)
            .layer(CORE_LAYER)
            .definedBy(CORE_PACKAGE)
            .layer(INFRA_LAYER)
            .definedBy(INFRA_PACKAGE)

    @ArchTest
    @JvmField
    val coreLayerDependsOnNothing =
        architecturalLayers
            .whereLayer(CORE_LAYER)
            .mayNotAccessAnyLayer()
            .whereLayer(CORE_LAYER)
            .mayOnlyBeAccessedByLayers(CLI_LAYER, INFRA_LAYER)

    @ArchTest
    @JvmField
    val infraLayerDependsOnlyOnCore =
        architecturalLayers
            .whereLayer(INFRA_LAYER)
            .mayOnlyAccessLayers(CORE_LAYER)
            .whereLayer(INFRA_LAYER)
            .mayNotBeAccessedByAnyLayer()

    @ArchTest
    @JvmField
    val cliLayerDependsOnlyOnCore =
        architecturalLayers
            .whereLayer(CLI_LAYER)
            .mayOnlyAccessLayers(CORE_LAYER)
            .whereLayer(CLI_LAYER)
            .mayNotBeAccessedByAnyLayer()
}
