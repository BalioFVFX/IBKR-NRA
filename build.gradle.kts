import org.jetbrains.compose.desktop.application.dsl.TargetFormat

plugins {
    kotlin("jvm")
    id("org.jetbrains.compose")
    id("org.jetbrains.kotlin.plugin.compose")
}

group = "com.erikbaliov"
version = "1.0-SNAPSHOT"

repositories {
    mavenCentral()
    maven("https://maven.pkg.jetbrains.space/public/p/compose/dev")
    google()
}

dependencies {
    // Note, if you develop a library, you should use compose.desktop.common.
    // compose.desktop.currentOs should be used in launcher-sourceSet
    // (in a separate module for demo project and in testMain).
    // With compose.desktop.common you will also lose @Preview functionality
    implementation(compose.desktop.currentOs)
    implementation(compose.components.resources)

    // File picker
    implementation("io.github.vinceglb:filekit-dialogs-compose:0.10.0-beta01")

    // CSV
    implementation("com.jsoizo:kotlin-csv-jvm:1.10.0")

    // Jsoup
    implementation("org.jsoup:jsoup:1.19.1")

    // XLS
    implementation("org.apache.poi:poi:5.4.1")
    implementation("org.apache.poi:poi-ooxml:5.4.1")

    // Access app dir in various operating systems
    implementation("ca.gosyer:kotlin-multiplatform-appdirs:2.0.0")
}

compose.desktop {
    application {
        mainClass = "MainKt"

        nativeDistributions {
            targetFormats(TargetFormat.Dmg, TargetFormat.Msi, TargetFormat.Deb)
            packageName = "IBKR-NRA"
            packageVersion = "1.0.0"
        }
    }
}
