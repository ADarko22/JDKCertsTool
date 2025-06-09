package edu.adarko22

import java.nio.file.Files
import java.nio.file.Path

/**
 * Creates a directory hierarchy at the specified parent path using the given name,
 * which can contain nested directories separated by "/".
 */
fun createDir(
    parent: Path,
    name: String,
): Path {
    val dir = name.split("/").fold(parent) { acc, part -> acc.resolve(part) }
    return Files.createDirectories(dir)
}

/**
 * Creates an executable file at the specified parent path with the given name.
 */
fun createExecutableFile(
    parent: Path,
    name: String,
): Path {
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
fun createValidJdk(
    parent: Path,
    name: String,
    isWindows: Boolean,
): Path {
    val jdkHome = createDir(parent, name)
    val binDir = createDir(jdkHome, "bin")
    createExecutableFile(binDir, if (isWindows) "java.exe" else "java")
    createExecutableFile(binDir, if (isWindows) "keytool.exe" else "keytool")
    return jdkHome
}
