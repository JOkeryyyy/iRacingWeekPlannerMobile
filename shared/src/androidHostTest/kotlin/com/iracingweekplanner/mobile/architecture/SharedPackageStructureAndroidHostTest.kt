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
}
