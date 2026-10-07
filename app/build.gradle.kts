import java.io.FileInputStream
import java.util.Properties

plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.kotlin.compose)
    alias(libs.plugins.kotlin.serialization)
    alias(libs.plugins.ksp)
    alias(libs.plugins.ktlint)
}

fun getProps(path: String): Properties {
    val props = Properties()
    val propsFile = File(path)
    if (propsFile.exists()) {
        props.load(FileInputStream(propsFile))
    }
    return props
}

android {
    namespace = "com.ragicorp.macassette"
    compileSdk {
        version = release(37)
    }

    defaultConfig {
        applicationId = "com.ragicorp.macassette"
        minSdk = 24
        targetSdk = 37
        versionCode = 1
        versionName = "1.0"

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
    }

    signingConfigs {
        create("release") {
            val props = getProps("$rootDir/config/signing.properties")
            keyAlias = props["keyAlias"] as String
            keyPassword = props["keyPassword"] as String
            storeFile = file(props["storeFile"] as String)
            storePassword = props["storePassword"] as String
        }
    }

    buildTypes {
        release {
            optimization {
                signingConfig = signingConfigs["release"]
                enable = true
                packageScope = setOf("androidx.**", "kotlin.**", "kotlinx.**")
            }
        }
    }
    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_11
        targetCompatibility = JavaVersion.VERSION_11
    }
    buildFeatures {
        compose = true
    }
}

kotlin {
    compilerOptions {
        freeCompilerArgs.add("-XXLanguage:+PropertyParamAnnotationDefaultTargetMode")
    }
}

ksp {
    arg("room.schemaLocation", "$projectDir/schemas")
}

dependencies {
    implementation(platform(libs.androidx.compose.bom))
    implementation(libs.androidx.activity.compose)
    implementation(libs.androidx.compose.material3)
    implementation(libs.androidx.compose.ui)
    implementation(libs.androidx.compose.ui.graphics)
    implementation(libs.androidx.compose.ui.tooling.preview)
    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.lifecycle.runtime.ktx)
    implementation(libs.androidx.navigation.compose)
    implementation(libs.androidx.room.runtime)
    implementation(libs.compose.material.symbols)
    implementation(libs.kotlinx.serialization.core)
    implementation(libs.threetenbp)

    ksp(libs.androidx.room.compiler)

    debugImplementation(libs.androidx.compose.ui.test.manifest)
    debugImplementation(libs.androidx.compose.ui.tooling)

    ktlintRuleset(libs.ktlint.compose)
}
