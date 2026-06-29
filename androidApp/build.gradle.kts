import org.jetbrains.kotlin.gradle.dsl.JvmTarget

plugins {
    alias(libs.plugins.androidApplication)
    alias(libs.plugins.composeMultiplatform)
    alias(libs.plugins.composeCompiler)
}

kotlin {
    compilerOptions {
        jvmTarget = JvmTarget.JVM_11
    }
}
dependencies {
    implementation(projects.shared)

    implementation(libs.androidx.activity.compose)

    implementation(libs.compose.uiToolingPreview)
    debugImplementation(libs.compose.uiTooling)
}

val hostedDevManifestUrl =
    "https://ivuwegboyxrzucbfgzvh.supabase.co/storage/v1/object/public/planner-data/data/mobile/v1/manifest.json"

android {
    namespace = "com.iracingweekplanner.mobile"
    compileSdk = libs.versions.android.compileSdk.get().toInt()

    flavorDimensions += "dataSource"

    defaultConfig {
        applicationId = "com.iracingweekplanner.mobile"
        minSdk = libs.versions.android.minSdk.get().toInt()
        targetSdk = libs.versions.android.targetSdk.get().toInt()
        versionCode = 1
        versionName = "1.0"
        manifestPlaceholders["plannerHostedManifestUrl"] = ""
    }
    productFlavors {
        create("hostedDev") {
            dimension = "dataSource"
            manifestPlaceholders["plannerHostedManifestUrl"] = hostedDevManifestUrl
        }
        create("localMock") {
            dimension = "dataSource"
            manifestPlaceholders["plannerHostedManifestUrl"] = ""
        }
    }
    packaging {
        resources {
            excludes += "/META-INF/{AL2.0,LGPL2.1}"
        }
    }
    buildTypes {
        getByName("release") {
            isMinifyEnabled = false
        }
    }
    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_11
        targetCompatibility = JavaVersion.VERSION_11
    }
}
