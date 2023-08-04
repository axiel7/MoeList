import org.jetbrains.kotlin.gradle.plugin.mpp.pm20.util.archivesName

plugins {
    id("com.android.application")
    id("org.jetbrains.kotlin.android")
    id("org.jetbrains.kotlin.plugin.serialization")
}

android.buildFeatures.buildConfig = true

android {
    compileSdk = 34

    defaultConfig {
        applicationId = "com.axiel7.moelist"
        minSdk = 23
        targetSdk = 34
        versionCode = 119
        versionName = "3.4.2"
        archivesName.set("moelist-v$versionName")

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
        resourceConfigurations.addAll(arrayOf(
            "en", "ar-rSA", "bg-rBG", "de", "es", "fr", "in-rID", "ja", "pt-rBR", "pt-rPT", "ru-rRU", "tr", "uk-rUA", "zh", "zh-rTW"
        ))
        vectorDrawables {
            useSupportLibrary = true
        }
    }

    buildTypes {
        debug {
            isDebuggable = true
            isMinifyEnabled = false
            isShrinkResources = false
            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"),
                "proguard-rules.pro"
            )
            buildConfigField("boolean", "IS_DEBUG", "true")
        }
        release {
            isDebuggable = false
            isMinifyEnabled = true
            isShrinkResources = true
            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"),
                "proguard-rules.pro"
            )
            buildConfigField("boolean", "IS_DEBUG", "false")
        }
    }
    splits {
        abi {
            isEnable = true
            reset()
            include("armeabi-v7a", "arm64-v8a", "x86", "x86_64")
            isUniversalApk = true
        }
    }
    buildFeatures {
        compose = true
    }
    compileOptions {
        isCoreLibraryDesugaringEnabled = true
        sourceCompatibility = JavaVersion.VERSION_1_8
        targetCompatibility = JavaVersion.VERSION_1_8
    }
    kotlinOptions {
        jvmTarget = "1.8"
    }
    composeOptions {
        kotlinCompilerExtensionVersion = "1.4.7"
    }
    packaging {
        resources {
            excludes += "/META-INF/{AL2.0,LGPL2.1}"
        }
    }
    namespace = "com.axiel7.moelist"
}

dependencies {

    //AndroidX
    implementation("androidx.core:core-ktx:1.10.1")
    implementation("androidx.appcompat:appcompat:1.6.1")

    implementation("androidx.browser:browser:1.5.0")
    implementation("androidx.datastore:datastore-preferences:1.0.0")
    implementation("androidx.work:work-runtime-ktx:2.8.1")
    implementation("androidx.core:core-splashscreen:1.0.1")

    //Compose
    implementation(platform("androidx.compose:compose-bom:2023.06.01"))
    implementation("androidx.compose.material3:material3-android:1.2.0-alpha04")
    implementation("androidx.compose.ui:ui-tooling-preview")
    debugImplementation("androidx.compose.ui:ui-tooling")
    implementation("androidx.compose.material3:material3-window-size-class:1.2.0-alpha04")

    implementation("androidx.activity:activity-compose:1.7.2")
    implementation("androidx.lifecycle:lifecycle-viewmodel-compose:2.6.1")
    implementation("androidx.navigation:navigation-compose:2.7.0-rc01")

    implementation("androidx.glance:glance-appwidget:1.0.0-rc01")

    val accompanistVersion = "0.30.1"
    implementation("com.google.accompanist:accompanist-insets-ui:$accompanistVersion")
    implementation("com.google.accompanist:accompanist-placeholder-material:$accompanistVersion")
    implementation("com.google.accompanist:accompanist-systemuicontroller:$accompanistVersion")
    implementation("com.google.accompanist:accompanist-permissions:$accompanistVersion")

    //Test
    testImplementation("junit:junit:4.13.2")
    androidTestImplementation("androidx.test.ext:junit:1.1.5")
    androidTestImplementation("androidx.test.espresso:espresso-core:3.5.1")

    //Ktor
    val ktorVersion = "2.3.3"
    implementation("io.ktor:ktor-client-okhttp:$ktorVersion")
    implementation("io.ktor:ktor-serialization-kotlinx-json:$ktorVersion")
    implementation("io.ktor:ktor-client-content-negotiation:$ktorVersion")
    implementation("io.ktor:ktor-client-auth:$ktorVersion")
    implementation("io.ktor:ktor-client-logging:$ktorVersion")

    implementation("org.jetbrains.kotlinx:kotlinx-serialization-json:1.5.1")

    //Utils
    coreLibraryDesugaring("com.android.tools:desugar_jdk_libs:2.0.3")
    implementation("org.apache.commons:commons-lang3:3.13.0")

    //Image
    implementation("io.coil-kt:coil-compose:2.4.0")

}