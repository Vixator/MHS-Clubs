plugins {
    alias(libs.plugins.kotlinJvm)
    alias(libs.plugins.ktor)
}

group = "com.precon.mhsclubs"
version = "1.0.0"
application {
    mainClass = "com.precon.mhsclubs.ApplicationKt"
}

dependencies {
    api(projects.core)
    implementation(libs.firebaseAdmin)
    implementation(libs.logback)
    implementation(libs.ktor.serverCore)
    implementation(libs.ktor.serverNetty)
    implementation(libs.ktor.serverCors)
    testImplementation(libs.ktor.serverTestHost)
    testImplementation(libs.kotlin.testJunit)
}