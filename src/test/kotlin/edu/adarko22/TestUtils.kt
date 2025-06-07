package edu.adarko22

import java.nio.file.Files
import java.nio.file.Path

/**
 * Creates a directory at the specified parent path with the given name.
 */
fun createDir(parent: Path, name: String): Path {
    val dir = parent.resolve(name)
    Files.createDirectory(dir)
    return dir
}

/**
 * Creates an executable file at the specified parent path with the given name.
 */
fun createExecutableFile(parent: Path, name: String): Path {
    val file = parent.resolve(name)
    Files.createFile(file)
    file.toFile().setExecutable(true)
    return file
}

/**
 * Creates a mock valid JDK home structure (e.g., `jdkHome/bin/java` and `jdkHome/bin/keytool`).
 * @param parent The parent directory where the JDK home will be created.
 * @param name The name of the JDK home directory.
 * @param isWindows If true, creates `java.exe` and `keytool.exe`; otherwise, `java` and `keytool`.
 * @return The Path to the created JDK home directory.
 */
fun createValidJdk(parent: Path, name: String, isWindows: Boolean): Path {
    val jdkHome = createDir(parent, name)
    val binDir = createDir(jdkHome, "bin")
    createExecutableFile(binDir, if (isWindows) "java.exe" else "java")
    createExecutableFile(binDir, if (isWindows) "keytool.exe" else "keytool")
    return jdkHome
}

/**
 * Creates a mock macOS-style JDK structure (`.jdk` bundle).
 * @param parent The parent directory where the `.jdk` bundle will be created.
 * @param name The base name of the JDK (e.g., "openjdk-17").
 * @return The Path to the created `.jdk` bundle root (e.g., `parent/openjdk-17.jdk`).
 */
fun createMacOsJdkBundle(parent: Path, name: String): Path {
    val jdkRoot = createDir(parent, "$name.jdk")
    val contents = createDir(jdkRoot, "Contents")
    val home = createDir(contents, "Home")
    val binDir = createDir(home, "bin")
    createExecutableFile(binDir, "java")
    createExecutableFile(binDir, "keytool")
    return jdkRoot
}

/**
 * Creates a mock JetBrains Runtime (JBR) structure within a given parent path.
 * This simulates the structure found in IntelliJ IDEA app bundles or Toolbox installations.
 * @param jbrParent The directory where the "Contents" directory for JBR will be created.
 * @return The Path to the created "Contents" directory (which `JdkDiscovery` considers the JBR home).
 */
fun createJetBrainsRuntime(jbrParent: Path): Path {
    val contentsDir = createDir(jbrParent, "Contents")
    createExecutableFile(createDir(contentsDir, "bin"), "java")
    createExecutableFile(createDir(contentsDir, "bin"), "keytool")
    return contentsDir
}

/**
 * Creates a directory for a specific version/build of IntelliJ IDEA under JetBrains Toolbox structure.
 * Example: `toolboxBase/IDEA-U/build-version`
 * @param editionRoot The root directory for the IDEA edition (e.g., `toolboxBase/IDEA-U`).
 * @param buildVersion The build version string (e.g., "233.11799.300").
 * @return The path to the created build version directory.
 */
fun createToolboxIdeaBuild(editionRoot: Path, buildVersion: String): Path {
    return createDir(editionRoot, buildVersion)
}

