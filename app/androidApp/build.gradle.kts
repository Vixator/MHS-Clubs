import org.jetbrains.kotlin.gradle.dsl.JvmTarget

val apiBaseUrl = providers.gradleProperty("mhsClubsApiBaseUrl")
    .orElse("http://10.0.2.2:8080")

plugins {
    alias(libs.plugins.androidApplication)
    alias(libs.plugins.composeMultiplatform)
    alias(libs.plugins.composeCompiler)
    id("com.google.gms.google-services")
}

kotlin {
    compilerOptions {
        jvmTarget = JvmTarget.JVM_11
    }
}
dependencies {
    implementation(projects.app.shared)

    implementation(libs.androidx.activity.compose)
    implementation(libs.firebaseAuth)
    implementation(libs.playServicesAuth)
    implementation(libs.coroutinesPlayServices)

    implementation(libs.compose.uiToolingPreview)
    debugImplementation(libs.compose.uiTooling)

    implementation(platform("com.google.firebase:firebase-bom:33.1.0"))
    implementation("com.google.firebase:firebase-auth-ktx")
    implementation("com.google.android.gms:play-services-auth:21.2.0")
}

android {
    namespace = "com.precon.mhsclubs"
    compileSdk = libs.versions.android.compileSdk.get().toInt()

    buildFeatures {
        resValues = true
    }

    defaultConfig {
        applicationId = "com.precon.mhsclubs"
        minSdk = libs.versions.android.minSdk.get().toInt()
        targetSdk = libs.versions.android.targetSdk.get().toInt()
        versionCode = 1
        versionName = "1.0"
        resValue("string", "mhs_clubs_api_base_url", apiBaseUrl.get())
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
