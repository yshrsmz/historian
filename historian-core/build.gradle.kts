plugins {
    alias(libs.plugins.android.library)
    alias(libs.plugins.kotlin.android)
    alias(libs.plugins.maven.publish)
    id("jacoco")
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
        debug {
            enableUnitTestCoverage = true
        }
        release {
            isMinifyEnabled = false
            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"),
                "proguard-rules.pro"
            )
        }
    }

    testOptions {
        unitTests.all {
            it.extensions.configure<JacocoTaskExtension> {
                isIncludeNoLocationClasses = true
            }
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

// A list of directories which should be included in coverage report
val coverageSourceDirs = listOf("src/main/java")
// A list of files which should be excluded from coverage report since they are generated and/or framework code
val coverageExcludeFiles = listOf("**/R.class", "**/R$*.class", "**/com/android/**/*.*")

jacoco {
    toolVersion = libs.versions.jacoco.get()
}

// https://issuetracker.google.com/issues/178015739
tasks.withType<Test>().configureEach {
    extensions.configure<JacocoTaskExtension> {
        excludes = listOf("*")
        isIncludeNoLocationClasses = true
    }
}

tasks.register<JacocoReport>("jacocoTestReportDebug") {
    dependsOn("testDebugUnitTest")
    group = "verification"
    description = "Generate Jacoco coverage reports after running tests."

    reports {
        xml.required.set(true)
        html.required.set(true)
        csv.required.set(false)
    }

    sourceDirectories.setFrom(files(coverageSourceDirs))
    classDirectories.setFrom(
        files(
            fileTree(layout.buildDirectory.dir("intermediates/javac/debug/classes")) {
                exclude(coverageExcludeFiles)
            }
        )
    )
    executionData.setFrom(files(layout.buildDirectory.file("outputs/unit_test_code_coverage/debugUnitTest/testDebugUnitTest.exec")))

    doLast {
        val buildDir = layout.buildDirectory.get().asFile
        println("jacoco xml report has been generated to file://${buildDir}/reports/jacoco/test/jacocoTestReportDebug.xml")
        println("jacoco html report has been generated to file://${reports.html.outputLocation.get().asFile}/index.html")
    }
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
