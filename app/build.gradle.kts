import java.util.Properties

plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.kotlin.android)
    id("com.google.android.libraries.mapsplatform.secrets-gradle-plugin")
}

android {
    namespace = "com.mock.mockpaymentsdk"
    compileSdk = 35
    buildFeatures.buildConfig = true

    defaultConfig {
        applicationId = "com.mock.mockpaymentsdk"
        minSdk = 24
        targetSdk = 35
        versionCode = 1
        versionName = "1.0"

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"

        val properties = Properties()
        val envFile = rootProject.file(".env")

        if (envFile.exists()) {
            envFile.inputStream().use { properties.load(it) }
        }

        val baseUrl = properties.getProperty("BASE_URL", "")

        if (baseUrl.isEmpty()) {
            throw IllegalArgumentException("BASE_URL is missing in .env file")
        }

        val maxRetries = properties.getProperty("REQUEST_MAX_RETRIES", "")

        if (maxRetries.isEmpty()) {
            throw IllegalArgumentException("REQUEST_MAX_RETRIES is missing in .env file")
        }

        buildConfigField("String", "BASE_URL", "\"$baseUrl\"")
        buildConfigField("int", "REQUEST_MAX_RETRIES", maxRetries)

    }

    buildTypes {
        release {
            isMinifyEnabled = false
            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"),
                "proguard-rules.pro"
            )
        }
    }
    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_11
        targetCompatibility = JavaVersion.VERSION_11
    }
    kotlinOptions {
        jvmTarget = "11"
    }

}

dependencies {

    // implementation
    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.appcompat)
    implementation(libs.material)
    implementation(libs.kotlinx.coroutines.android)
    implementation(libs.retrofit)
    implementation(libs.converter.gson)

    // android
    androidTestImplementation(libs.androidx.junit)
    androidTestImplementation(libs.androidx.espresso.core)

    // test
    testImplementation(libs.junit)

    testImplementation(libs.mockito.core)
    testImplementation(libs.mockito.kotlin)
    testImplementation(libs.kotlinx.coroutines.test)

}