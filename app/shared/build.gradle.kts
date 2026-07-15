import org.jetbrains.kotlin.gradle.ExperimentalWasmDsl
import org.jetbrains.kotlin.gradle.dsl.JvmTarget

plugins {
    alias(libs.plugins.kotlinMultiplatform)
    alias(libs.plugins.androidMultiplatformLibrary)
    alias(libs.plugins.composeMultiplatform)
    alias(libs.plugins.composeCompiler)
    id("com.squareup.sqldelight") version "2.0.2"
}

kotlin {
    listOf(
        iosArm64(),
        iosSimulatorArm64()
    ).forEach { iosTarget ->
        iosTarget.binaries.framework {
            baseName = "Shared"
            isStatic = true
        }
    }
    
    js {
        browser()
    }
    
    @OptIn(ExperimentalWasmDsl::class)
    wasmJs {
        browser()
    }
    
    androidLibrary {
       namespace = "com.precon.mhsclubs.app.shared"
       compileSdk = libs.versions.android.compileSdk.get().toInt()
       minSdk = libs.versions.android.minSdk.get().toInt()
    
       compilerOptions {
           jvmTarget = JvmTarget.JVM_11
       }
       androidResources {
           enable = true
       }
       withHostTest {
           isIncludeAndroidResources = true
       }
    }
    
    sourceSets {
        androidMain.dependencies {
            implementation(libs.compose.uiToolingPreview)
        }
        commonMain.dependencies {
            api(projects.core)
            implementation(libs.compose.runtime)
            implementation(libs.compose.foundation)
            implementation(libs.compose.material3)
            implementation(libs.compose.ui)
            implementation(libs.compose.components.resources)
            implementation(libs.compose.uiToolingPreview)
            implementation(libs.androidx.lifecycle.viewmodelCompose)
            implementation(libs.androidx.lifecycle.runtimeCompose)
        }
        commonTest.dependencies {
            implementation(libs.kotlin.test)
        }
        jsMain.dependencies {
            implementation(libs.wrappers.browser)
        }
    }
}

sqldelight {
    databases {
        create("MHSClubsDatabase") {
            dialect("com.squareup.sqldelight:postgresql-dialect:2.0.2")
            // Versioned migration strategy: SQLDelight will apply .sqm files
            // in the migrations/ directory in sorted order. The schema.sqm
            // file serves as the current full-schema reference for code gen.
            schemaFiles = fileTree("src/commonMain/sqldelight/mhs_clubs") {
                include("schema.sqm")
            }
            migrationFiles = fileTree("src/commonMain/sqldelight/mhs_clubs/migrations") {
                include("*.sqm")
            }
            // Verify migrations produce the same schema as schema.sqm
            verifyMigrations = true
        }
    }
}

// SQLDelight PostgreSQL dialect dependency
dependencies {
    commonMainApi("com.squareup.sqldelight:postgresql-dialect:2.0.2")
    commonMainApi("com.squareup.sqldelight:runtime:2.0.2")
    commonMainApi("com.squareup.sqldelight:coroutines-extensions:2.0.2")
    androidMainApi("com.squareup.sqldelight:sqlite-driver:2.0.2")
    commonMainApi("com.squareup.sqldelight:sqlite-driver:2.0.2")

    androidRuntimeClasspath(libs.compose.uiTooling)
}