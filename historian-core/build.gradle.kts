plugins {
    alias(libs.plugins.android.library)
    alias(libs.plugins.kotlin.android)
    alias(libs.plugins.maven.publish)
}

android {
    namespace = "net.yslibrary.historian"
    compileSdk = libs.versions.compileSdk.get().toInt()

    defaultConfig {
        minSdk = libs.versions.minSdk.get().toInt()

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
    }

    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_11
        targetCompatibility = JavaVersion.VERSION_11
    }

    kotlinOptions {
        jvmTarget = "11"
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

    buildFeatures {
        buildConfig = false
    }
}

dependencies {
    implementation(libs.annotation)

    testImplementation(libs.junit)
    testImplementation(libs.robolectric)
    testImplementation(libs.test.core)
}

mavenPublishing {
    publishToMavenCentral()

    if (project.hasProperty("signingInMemoryKey")) {
        signAllPublications()
    }

    coordinates(
        project.property("GROUP").toString(),
        project.property("POM_ARTIFACT_ID").toString(),
        project.property("VERSION_NAME").toString()
    )

    pom {
        name.set(project.property("POM_NAME").toString())
        description.set(project.property("POM_DESCRIPTION").toString())
        url.set(project.property("POM_URL").toString())
    }
}
