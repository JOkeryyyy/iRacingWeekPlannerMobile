package com.iracingweekplanner.mobile.architecture

import java.nio.file.Files
import java.nio.file.Path
import kotlin.io.path.Path
import kotlin.test.Test
import kotlin.test.assertTrue

class SharedPackageStructureAndroidHostTest {

    @Test
    fun domainFilesAreGroupedByRolePackages() {
        val domainRoot = commonMainPackageRoot().resolve("domain")

        assertDirectoriesExist(
            root = domainRoot,
            directories = listOf("model", "repository", "usecase"),
        )
        assertNoDirectKotlinFiles(domainRoot)
    }

    @Test
    fun dataFilesAreGroupedByInfrastructurePackages() {
        val dataRoot = commonMainPackageRoot().resolve("data")

        assertDirectoriesExist(
            root = dataRoot,
            directories = listOf("datasource", "dto", "local", "mapper", "repository"),
        )
        assertNoDirectKotlinFiles(dataRoot)
    }

    @Test
    fun domainRepositoryInterfacesUseOneFilePerInterface() {
        val repositoryRoot = commonMainPackageRoot().resolve("domain/repository")
        val violations = Files.newDirectoryStream(repositoryRoot, "*.kt").use { stream ->
            stream
                .mapNotNull { file ->
                    val interfaceNames = repositoryInterfaceNames(file)
                    when {
                        interfaceNames.size != 1 ->
                            "${file.fileName} should declare exactly one repository interface. Found: $interfaceNames"

                        file.fileName.toString() != "${interfaceNames.single()}.kt" ->
                            "${file.fileName} should be named ${interfaceNames.single()}.kt"

                        else -> null
                    }
                }
                .sorted()
        }

        assertTrue(
            actual = violations.isEmpty(),
            message = "Expected one domain repository interface per file. Violations: $violations",
        )
    }

    @Test
    fun appComposableUsesSharedAppThemePackage() {
        val presentationRoot = commonMainPackageRoot().resolve("presentation")
        val appSource = Files.readAllLines(presentationRoot.resolve("App.kt")).joinToString(separator = "\n")

        assertTrue(
            actual = Files.isDirectory(presentationRoot.resolve("theme")),
            message = "Expected app theme to live under presentation/theme.",
        )
        assertTrue(
            actual = Files.isRegularFile(presentationRoot.resolve("theme/IwpAppTheme.kt")),
            message = "Expected IwpAppTheme.kt to define the shared app theme.",
        )
        assertTrue(
            actual = appSource.contains("IwpAppTheme {"),
            message = "App should use the shared app theme.",
        )
        assertTrue(
            actual = !appSource.contains("MaterialTheme {"),
            message = "App should not create raw MaterialTheme wrappers.",
        )
    }

    private fun commonMainPackageRoot(): Path =
        sharedProjectRoot()
            .resolve("src/commonMain/kotlin/com/iracingweekplanner/mobile")

    private fun sharedProjectRoot(): Path {
        val cwd = Path("").toAbsolutePath()
        return when {
            Files.isDirectory(cwd.resolve("src/commonMain/kotlin")) -> cwd
            Files.isDirectory(cwd.resolve("shared/src/commonMain/kotlin")) -> cwd.resolve("shared")
            else -> error("Could not locate shared project root from $cwd")
        }
    }

    private fun assertDirectoriesExist(root: Path, directories: List<String>) {
        directories.forEach { directory ->
            assertTrue(
                actual = Files.isDirectory(root.resolve(directory)),
                message = "Expected ${root.resolve(directory)} to exist",
            )
        }
    }

    private fun assertNoDirectKotlinFiles(root: Path) {
        val directFiles = Files.newDirectoryStream(root, "*.kt").use { stream ->
            stream.map { it.fileName.toString() }.sorted()
        }

        assertTrue(
            actual = directFiles.isEmpty(),
            message = "Expected Kotlin files under $root to live in role-specific subpackages. Found: $directFiles",
        )
    }

    private fun repositoryInterfaceNames(file: Path): List<String> =
        repositoryInterfacePattern.findAll(Files.readAllLines(file).joinToString(separator = "\n"))
            .map { match -> match.groupValues[1] }
            .toList()

    private companion object {
        val repositoryInterfacePattern = Regex("""(?m)^\s*interface\s+(\w+Repository)\b""")
    }
}
