import org.jetbrains.kotlin.gradle.plugin.mpp.pm20.util.archivesName
import java.util.Properties

plugins {
    id("com.android.application")
    id("org.jetbrains.kotlin.android")
    id("org.jetbrains.kotlin.plugin.serialization")
}

android.buildFeatures.buildConfig = true

val properties = Properties()
properties.load(project.rootProject.file("local.properties").reader())

android {
    compileSdk = 34

    defaultConfig {
        applicationId = "com.axiel7.moelist"
        minSdk = 23
        targetSdk = 34
        versionCode = 132
        versionName = "3.5.4"
        archivesName.set("moelist-v$versionName")

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
        resourceConfigurations.addAll(arrayOf(
            "en", "ar-rSA", "bg-rBG", "cs-rCZ", "de", "es", "fr", "in-rID", "ja", "pt-rBR", "pt-rPT", "ru-rRU",
            "sk-rSK", "tr", "uk-rUA", "zh", "zh-rTW"
        ))
        vectorDrawables {
            useSupportLibrary = true
        }
    }

    buildTypes {
        debug {
            applicationIdSuffix = ".debug"
            versionNameSuffix = "-DEBUG"
            isDebuggable = true
            isMinifyEnabled = false
            isShrinkResources = false
            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"),
                "proguard-rules.pro"
            )
            buildConfigField("boolean", "IS_DEBUG", "true")
            buildConfigField("String", "CLIENT_ID", properties.getProperty("CLIENT_ID"))
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
            buildConfigField("String", "CLIENT_ID", properties.getProperty("CLIENT_ID"))
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
        kotlinCompilerExtensionVersion = "1.5.12"
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
    implementation("androidx.core:core-ktx:1.13.0")
    implementation("androidx.appcompat:appcompat:1.6.1")

    implementation("androidx.browser:browser:1.8.0")
    implementation("androidx.datastore:datastore-preferences:1.1.0")
    implementation("androidx.work:work-runtime:2.9.0")
    implementation("androidx.core:core-splashscreen:1.0.1")

    //Compose
    implementation(platform("androidx.compose:compose-bom:2024.04.01"))
    implementation("androidx.compose.ui:ui")
    implementation("androidx.compose.ui:ui-tooling-preview")
    debugImplementation("androidx.compose.ui:ui-tooling")

    val materialVersion = "1.2.1"
    implementation("androidx.compose.material3:material3-android:$materialVersion")
    implementation("androidx.compose.material3:material3-window-size-class:$materialVersion")

    implementation("androidx.activity:activity-compose:1.9.0")

    val lifecycleVersion = "2.7.0"
    implementation("androidx.lifecycle:lifecycle-runtime-compose:$lifecycleVersion")
    implementation("androidx.lifecycle:lifecycle-viewmodel-savedstate:$lifecycleVersion")

    implementation("androidx.glance:glance-appwidget:1.0.0")

    val accompanistVersion = "0.34.0"
    implementation("com.google.accompanist:accompanist-permissions:$accompanistVersion")

    implementation("io.github.fornewid:placeholder-material3:1.1.2")

    //Test
    testImplementation("junit:junit:4.13.2")
    androidTestImplementation("androidx.test.ext:junit:1.1.5")
    androidTestImplementation("androidx.test.espresso:espresso-core:3.5.1")

    //Ktor
    val ktorVersion = "2.3.10"
    implementation("io.ktor:ktor-client-okhttp:$ktorVersion")
    implementation("io.ktor:ktor-serialization-kotlinx-json:$ktorVersion")
    implementation("io.ktor:ktor-client-content-negotiation:$ktorVersion")
    implementation("io.ktor:ktor-client-auth:$ktorVersion")
    implementation("io.ktor:ktor-client-logging:$ktorVersion")

    implementation("org.jetbrains.kotlinx:kotlinx-serialization-json:1.6.3")

    //Utils
    coreLibraryDesugaring("com.android.tools:desugar_jdk_libs:2.0.4")
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-core:1.8.0")
    implementation("org.apache.commons:commons-text:1.12.0")

    //Image
    val coilVersion = "3.0.0-alpha06"
    implementation("io.coil-kt.coil3:coil-compose:$coilVersion")
    implementation("io.coil-kt.coil3:coil-network-okhttp:$coilVersion")

    //Koin
    implementation(platform("io.insert-koin:koin-bom:3.5.6"))
    implementation("io.insert-koin:koin-androidx-compose")
    implementation("io.insert-koin:koin-androidx-compose-navigation")
    implementation("io.insert-koin:koin-androidx-workmanager")

    //Serialized Navigation
    val serializedNavigationVersion = "e23f84fc1f" // TODO: use proper versioning once the library is released
    implementation("com.github.uragiristereo.serialized-navigation-extension:navigation-compose:$serializedNavigationVersion")
    implementation("com.github.uragiristereo.serialized-navigation-extension:serializer-kotlinx:$serializedNavigationVersion")
}
