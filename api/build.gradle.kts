plugins {
    alias(libs.plugins.android.library)
    alias(libs.plugins.jetbrains.kotlin.android)
    id("org.jetbrains.dokka") version("1.9.20")
}

android {
    namespace = "org.avmedia.translateApi"

    compileSdk = 35

    defaultConfig {
        minSdk = 24

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
        consumerProguardFiles("consumer-rules.pro")
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
}

dependencies {
    implementation(libs.androidx.core.ktx)
    implementation(libs.translator)
    implementation (libs.androidx.datastore.preferences)
}

tasks.dokkaHtml {

    outputDirectory.set(file("${rootDir}/docs"))

    dokkaSourceSets {
        configureEach {
            includes.from("module.md")

            includeNonPublic.set(false)
            skipDeprecated.set(true)
            reportUndocumented.set(false) // Emit warnings about not documented members
        }
    }
}

