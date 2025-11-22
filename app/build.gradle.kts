import java.util.Properties

plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.kotlin.android)
    alias(libs.plugins.kotlin.compose)
    alias(libs.plugins.kotlin.serialization)
    alias(libs.plugins.ksp)
}

val privateProps = Properties().also {
    it.load(project.rootProject.file("private.properties").reader())
}
val versionProps = Properties().also {
    it.load(project.rootProject.file("version.properties").reader())
}

android {
    compileSdk = 36

    base {
        archivesName = "moelist-v${versionProps.getProperty("name")}"
    }

    defaultConfig {
        applicationId = "com.axiel7.moelist"
        minSdk = 23
        targetSdk = 36
        versionCode = versionProps.getProperty("code").toInt()
        versionName = versionProps.getProperty("name")

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
    }
    composeCompiler {
        stabilityConfigurationFiles.add(rootProject.layout.projectDirectory.file("stability_config.conf"))
    }
    kotlin {
        jvmToolchain(17)
    }
    ksp {
        arg("room.schemaLocation", "$projectDir/schemas")
    }
    packaging {
        resources {
            excludes += "/META-INF/{AL2.0,LGPL2.1}"
        }
    }
    dependenciesInfo {
        includeInApk = false
    }
    namespace = "com.axiel7.moelist"
}

dependencies {
    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.appcompat)

    implementation(libs.androidx.browser)
    implementation(libs.androidx.datastore.preferences)
    implementation(libs.androidx.work.runtime)
    implementation(libs.androidx.core.splashscreen)

    implementation(platform(libs.androidx.compose.bom))
    implementation(libs.bundles.compose)
    debugImplementation(libs.androidx.compose.ui.tooling)

    implementation(libs.bundles.material3)

    implementation(libs.androidx.activity.compose)
    implementation(libs.androidx.navigation3.runtime)
    implementation(libs.androidx.navigation3.ui)

    implementation(libs.androidx.lifecycle.runtime.compose)
    implementation(libs.androidx.lifecycle.viewmodel.savedstate)
    implementation(libs.androidx.lifecycle.viewmodel.navigation3)

    implementation(libs.androidx.glance.appwidget)

    implementation(libs.accompanist.permissions)

    implementation(libs.placeholder.material3)
    implementation(libs.material.kolor)

    testImplementation(libs.junit)
    androidTestImplementation(libs.androidx.test.ext.junit)
    androidTestImplementation(libs.androidx.test.espresso.core)

    implementation(libs.bundles.ktor)

    implementation(libs.kotlinx.serialization.json)

    coreLibraryDesugaring(libs.desugar.jdk.libs)
    implementation(libs.kotlinx.coroutines.core)
    implementation(libs.commons.text)

    implementation(libs.bundles.coil)

    implementation(platform(libs.koin.bom))
    implementation(libs.bundles.koin)

    implementation(libs.bundles.room)
    ksp(libs.androidx.room.compiler)
}
