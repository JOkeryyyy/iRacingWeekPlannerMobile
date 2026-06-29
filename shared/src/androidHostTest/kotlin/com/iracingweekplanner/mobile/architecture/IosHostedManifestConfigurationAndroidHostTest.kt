package com.iracingweekplanner.mobile.architecture

import java.nio.file.Files
import java.nio.file.Path
import kotlin.io.path.Path
import kotlin.test.Test
import kotlin.test.assertFalse
import kotlin.test.assertTrue

class IosHostedManifestConfigurationAndroidHostTest {

    @Test
    fun iosAppPassesConfiguredManifestUrlIntoSharedEntryPoint() {
        val root = repoRoot()
        val contentView = readText(root.resolve("iosApp/iosApp/ContentView.swift"))
        val infoPlist = readText(root.resolve("iosApp/iosApp/Info.plist"))
        val config = readText(root.resolve("iosApp/Configuration/Config.xcconfig"))
        val hostedDev = readText(root.resolve("iosApp/Configuration/HostedDev.xcconfig"))
        val project = readText(root.resolve("iosApp/iosApp.xcodeproj/project.pbxproj"))

        assertTrue(infoPlist.contains("<key>PlannerHostedManifestUrl</key>"))
        assertTrue(infoPlist.contains("<string>$(PLANNER_HOSTED_MANIFEST_URL)</string>"))
        assertTrue(config.contains("PLANNER_HOSTED_MANIFEST_URL="))
        assertTrue(hostedDev.contains("https://ivuwegboyxrzucbfgzvh.supabase.co/storage/v1/object/public/planner-data/data/mobile/v1/manifest.json"))
        assertTrue(hostedDev.contains("PLANNER_HOSTED_MANIFEST_URL=https:/$()/ivuwegboyxrzucbfgzvh.supabase.co/storage/v1/object/public/planner-data/data/mobile/v1/manifest.json"))
        assertFalse(hostedDev.contains("season.json") || hostedDev.contains("cars.json") || hostedDev.contains("tracks.json"))
        assertTrue(contentView.contains("hostedManifestUrl: Bundle.main.plannerHostedManifestUrl"))
        assertTrue(contentView.contains("object(forInfoDictionaryKey: \"PlannerHostedManifestUrl\")"))
        assertTrue(project.contains("HostedDevDebug"))
        assertTrue(project.contains("baseConfigurationReferenceRelativePath = HostedDev.xcconfig;"))
    }

    private fun readText(file: Path): String = Files.readAllLines(file).joinToString("\n")

    private fun repoRoot(): Path {
        val cwd = Path("").toAbsolutePath()
        return when {
            Files.isDirectory(cwd.resolve("iosApp/iosApp.xcodeproj")) -> cwd
            Files.isDirectory(cwd.resolve("../iosApp/iosApp.xcodeproj")) -> cwd.resolve("..").normalize()
            else -> error("Could not locate repository root from $cwd")
        }
    }
}
