import java.util.Properties

plugins {
    id("com.android.application")
    id("org.jetbrains.kotlin.android")
    id("org.jetbrains.kotlin.plugin.compose")
    id("org.jetbrains.kotlin.plugin.serialization")
    id("com.google.devtools.ksp")
}

val privateProps = Properties().also {
    it.load(project.rootProject.file("private.properties").reader())
}
val versionProps = Properties().also {
    it.load(project.rootProject.file("version.properties").reader())
}

android {
    compileSdk = 35

    defaultConfig {
        applicationId = "com.axiel7.moelist"
        minSdk = 23
        targetSdk = 35
        versionCode = versionProps.getProperty("code").toInt()
        versionName = versionProps.getProperty("name")

        base {
            archivesName = "moelist-v$versionName"
        }

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
        vectorDrawables {
            useSupportLibrary = true
        }
    }

    androidResources {
        localeFilters += listOf(
            "en", "ar-rSA", "bg-rBG", "cs-rCZ", "de", "es", "fr", "in-rID", "it-rIT", "ja",
            "pl-rPL", "pt-rBR", "pt-rPT", "ru-rRU", "sk-rSK", "tr", "uk-rUA", "zh", "zh-rTW"
        )
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
            buildConfigField("String", "CLIENT_ID", privateProps.getProperty("CLIENT_ID"))
            resValue("string", "app_name", "MoeList Debug")
        }
        release {
            isDebuggable = false
            isMinifyEnabled = true
            isShrinkResources = true
            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"),
                "proguard-rules.pro"
            )
            buildConfigField("String", "CLIENT_ID", privateProps.getProperty("CLIENT_ID"))
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
        buildConfig = true
    }
    compileOptions {
        isCoreLibraryDesugaringEnabled = true
        sourceCompatibility = JavaVersion.VERSION_17
        targetCompatibility = JavaVersion.VERSION_17
    }
    kotlinOptions {
        jvmTarget = "17"
    }
    ksp {
        arg("room.schemaLocation", "$projectDir/schemas")
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
    implementation("androidx.core:core-ktx:1.16.0")
    implementation("androidx.appcompat:appcompat:1.7.0")

    implementation("androidx.browser:browser:1.8.0")
    implementation("androidx.datastore:datastore-preferences:1.1.5")
    implementation("androidx.work:work-runtime:2.10.1")
    implementation("androidx.core:core-splashscreen:1.0.1")

    //Compose
    implementation(platform("androidx.compose:compose-bom:2025.04.01"))
    implementation("androidx.compose.ui:ui")
    implementation("androidx.compose.ui:ui-tooling-preview")
    debugImplementation("androidx.compose.ui:ui-tooling")

    val materialVersion = "1.3.2"
    implementation("androidx.compose.material3:material3-android:$materialVersion")
    implementation("androidx.compose.material3:material3-window-size-class:$materialVersion")

    implementation("androidx.activity:activity-compose:1.10.1")
    implementation("androidx.navigation:navigation-compose:2.8.9")

    val lifecycleVersion = "2.8.7"
    implementation("androidx.lifecycle:lifecycle-runtime-compose:$lifecycleVersion")
    implementation("androidx.lifecycle:lifecycle-viewmodel-savedstate:$lifecycleVersion")

    implementation("androidx.glance:glance-appwidget:1.1.1")

    val accompanistVersion = "0.37.2"
    implementation("com.google.accompanist:accompanist-permissions:$accompanistVersion")

    implementation("io.github.fornewid:placeholder-material3:2.0.0")

    //Test
    testImplementation("junit:junit:4.13.2")
    androidTestImplementation("androidx.test.ext:junit:1.2.1")
    androidTestImplementation("androidx.test.espresso:espresso-core:3.6.1")

    //Ktor
    val ktorVersion = "3.1.2"
    implementation("io.ktor:ktor-client-okhttp:$ktorVersion")
    implementation("io.ktor:ktor-serialization-kotlinx-json:$ktorVersion")
    implementation("io.ktor:ktor-client-content-negotiation:$ktorVersion")
    implementation("io.ktor:ktor-client-auth:$ktorVersion")
    implementation("io.ktor:ktor-client-logging:$ktorVersion")

    implementation("org.jetbrains.kotlinx:kotlinx-serialization-json:1.8.1")

    //Utils
    coreLibraryDesugaring("com.android.tools:desugar_jdk_libs:2.1.5")
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-core:1.10.2")
    implementation("org.apache.commons:commons-text:1.13.1")

    //Image
    val coilVersion = "3.1.0"
    implementation("io.coil-kt.coil3:coil-compose:$coilVersion")
    implementation("io.coil-kt.coil3:coil-network-okhttp:$coilVersion")

    //Koin
    implementation(platform("io.insert-koin:koin-bom:4.0.4"))
    implementation("io.insert-koin:koin-androidx-compose")
    implementation("io.insert-koin:koin-androidx-compose-navigation")
    implementation("io.insert-koin:koin-androidx-workmanager")
    implementation("io.insert-koin:koin-androidx-startup")

    //Room
    val roomVersion = "2.7.0"
    implementation("androidx.room:room-runtime:$roomVersion")
    implementation("androidx.room:room-ktx:$roomVersion")
    ksp("androidx.room:room-compiler:$roomVersion")
}
