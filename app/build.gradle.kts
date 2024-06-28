plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.jetbrains.kotlin.android)
    id("androidx.navigation.safeargs.kotlin")
}

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
        release {
            isMinifyEnabled = false
            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"),
                "proguard-rules.pro"
            )
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
    }
    packagingOptions {
        exclude ("META-INF/DEPENDENCIES")
        exclude ("META-INF/NOTICE")
        exclude ("META-INF/LICENSE")
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

    //retrofit
    implementation(libs.retrofit)
    implementation(libs.retrofit.gson)
    //okhttp
    implementation(libs.okhttp)
    implementation(libs.androidx.lifecycle.viewmodel.ktx)
    implementation(libs.androidx.lifecycle.livedata.ktx)
    implementation(libs.androidx.databinding.runtime)

    //google maps
    implementation(libs.play.services.maps)
    implementation(libs.play.services.location)



    implementation("com.google.api-client:google-api-client-android:1.32.1")
    implementation("com.google.apis:google-api-services-youtube:v3-rev222-1.25.0")
    implementation("com.google.http-client:google-http-client-gson:1.42.2")


}