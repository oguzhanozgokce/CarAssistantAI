import java.util.Properties

plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.jetbrains.kotlin.android)
    id("androidx.navigation.safeargs.kotlin")
}

val localProperties = Properties()
val localPropertiesFile = rootProject.file("local.properties")
if (localPropertiesFile.exists()) {
    localPropertiesFile.inputStream().use { stream ->
        localProperties.load(stream)
    }
}

val geminiApiKey: String = localProperties.getProperty("GEMINI_API_KEY") ?: ""
val youtubeApiKey: String = localProperties.getProperty("YOUTUBE_API_KEY") ?: ""
val phoneNumber: String = localProperties.getProperty("PHONE_NUMBER") ?: ""
val googleApiKey: String = localProperties.getProperty("GOOGLE_API_KEY") ?: ""


android {
    namespace = "com.oguzhanozgokce.carassistantai"
    compileSdk = 34

    defaultConfig {
        applicationId = "com.oguzhanozgokce.carassistantai"
        minSdk = 27
        targetSdk = 34
        versionCode = 1
        versionName = "1.0"

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
    }

    buildTypes {
        getByName("release") {
            isMinifyEnabled = false
            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"),
                "proguard-rules.pro"
            )
            buildConfigField("String", "GEMINI_API_KEY", "\"${geminiApiKey}\"")
            buildConfigField("String", "YOUTUBE_API_KEY", "\"${youtubeApiKey}\"")
            buildConfigField("String", "PHONE_NUMBER", "\"${phoneNumber}\"")
            buildConfigField("String", "GOOGLE_API_KEY", "\"${googleApiKey}\"")
        }
        getByName("debug") {
            buildConfigField("String", "GEMINI_API_KEY", "\"${geminiApiKey}\"")
            buildConfigField("String", "YOUTUBE_API_KEY", "\"${youtubeApiKey}\"")
            buildConfigField("String", "PHONE_NUMBER", "\"${phoneNumber}\"")
            buildConfigField("String", "GOOGLE_API_KEY", "\"${googleApiKey}\"")
        }
    }


    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_1_8
        targetCompatibility = JavaVersion.VERSION_1_8
    }
    kotlinOptions {
        jvmTarget = "1.8"
    }
    buildFeatures {
        viewBinding = true
        buildConfig = true
    }
    packaging {
        resources.excludes.add("META-INF/DEPENDENCIES")
        resources.excludes.add("META-INF/NOTICE")
        resources.excludes.add("META-INF/LICENSE")
    }
}

dependencies {
    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.appcompat)
    implementation(libs.material)
    implementation(libs.androidx.activity)
    implementation(libs.androidx.constraintlayout)
    implementation(libs.androidx.navigation.fragment.ktx)
    implementation(libs.androidx.navigation.ui.ktx)
    implementation(libs.generativeai)
    testImplementation(libs.junit)
    androidTestImplementation(libs.androidx.junit)
    androidTestImplementation(libs.androidx.espresso.core)

    // retrofit
    implementation(libs.retrofit)
    implementation(libs.retrofit.gson)
    // okhttp
    implementation(libs.okhttp)
    implementation(libs.androidx.lifecycle.viewmodel.ktx)
    implementation(libs.androidx.lifecycle.livedata.ktx)
    implementation(libs.androidx.databinding.runtime)

    // google maps
    implementation(libs.play.services.maps)
    implementation(libs.play.services.location)

    // lottie
    implementation(libs.lottie)

    // google vision
    implementation(libs.google.api.client.android)
    implementation(libs.google.api.services.youtube)
    implementation(libs.google.http.client.gson)
    implementation(libs.generativeai.v060)

    // json
    implementation(libs.gson)

}
