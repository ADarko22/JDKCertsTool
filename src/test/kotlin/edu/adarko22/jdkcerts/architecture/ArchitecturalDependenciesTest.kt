package edu.adarko22.jdkcerts.architecture

import com.tngtech.archunit.core.domain.JavaConstructor
import com.tngtech.archunit.core.importer.ImportOption
import com.tngtech.archunit.junit.AnalyzeClasses
import com.tngtech.archunit.junit.ArchTest
import com.tngtech.archunit.lang.ArchCondition
import com.tngtech.archunit.lang.ConditionEvents
import com.tngtech.archunit.lang.SimpleConditionEvent
import com.tngtech.archunit.lang.syntax.ArchRuleDefinition.constructors
import com.tngtech.archunit.library.freeze.FreezingArchRule.freeze

@AnalyzeClasses(
    packages = ["edu.adarko22.jdkcerts"],
    importOptions = [ImportOption.DoNotIncludeTests::class],
)
class ArchitecturalDependenciesTest {
    companion object {
        private val ALLOWED_CONCRETE_PACKAGES =
            listOf(
                "java.lang",
                "java.util",
                "java.time",
                "kotlin",
                "kotlin.collections",
            )

        private val ALLOWED_CONCRETE_VALUES =
            listOf(
                "edu.adarko22.jdkcerts.core.jdk.Jdk",
                "edu.adarko22.jdkcerts.core.jdk.java.model.JavaInfo",
                "edu.adarko22.jdkcerts.core.jdk.keytool.model.TruststoreInfo",
                "edu.adarko22.jdkcerts.core.jdk.keytool.model.CertificateInfo",
            )
    }

    @ArchTest
    @JvmField
    val constructorsShouldDependOnlyOnInterfacesOrValueTypes =
        freeze(
            constructors()
                .that()
                .areDeclaredInClassesThat()
                .areNotAnonymousClasses()
                .should(haveOnlyInterfaceOrWhitelistedParameters()),
        )

    private fun haveOnlyInterfaceOrWhitelistedParameters(): ArchCondition<JavaConstructor> =
        object : ArchCondition<JavaConstructor>("have parameters that are interfaces or allowed value types") {
            override fun check(
                constructor: JavaConstructor,
                events: ConditionEvents,
            ) {
                for (param in constructor.parameters) {
                    val rawType = param.rawType
                    val isValid =
                        rawType.isInterface ||
                            rawType.isPrimitive ||
                            rawType.isEnum ||
                            ALLOWED_CONCRETE_PACKAGES.any { rawType.packageName.startsWith(it) } ||
                            ALLOWED_CONCRETE_VALUES.any { rawType.fullName.equals(it) }

                    if (!isValid) {
                        val message =
                            "Constructor parameters must be interfaces or whitelisted value types." +
                                "Parameter at index '${param.index}' is of type '${rawType.name}' at constructor '${constructor.fullName}'."
                        events.add(SimpleConditionEvent.violated(constructor, message))
                    }
                }
            }
        }
}
