import java.util.Properties

plugins {
    id("com.android.application")
    id("org.jetbrains.kotlin.android")
}

val localProperties = Properties().apply {
    val source = rootProject.file("local.properties")
    if (source.exists()) source.inputStream().use { load(it) }
}
val geminiApiKey = localProperties.getProperty("GEMINI_API_KEY", "")
val geminiModel = localProperties.getProperty("GEMINI_MODEL", "gemini-2.0-flash")

android {
    namespace = "com.saathi"
    compileSdk = 35

    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_17
        targetCompatibility = JavaVersion.VERSION_17
    }

    defaultConfig {
        applicationId = "com.saathi"
        minSdk = 26
        targetSdk = 33
        versionCode = 1
        versionName = "1.0.0"
        buildConfigField("String", "GEMINI_API_KEY", "\"${geminiApiKey.replace("\\", "\\\\").replace("\"", "\\\"")}\"")
        buildConfigField("String", "GEMINI_MODEL", "\"${geminiModel.replace("\"", "\\\"")}\"")
    }
    buildFeatures { buildConfig = true }
}

kotlin {
    jvmToolchain(17)
}

dependencies {
    testImplementation("junit:junit:4.13.2")
}
