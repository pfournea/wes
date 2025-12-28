plugins {
    kotlin("jvm") version "2.2.21"
    application
    id("org.openjfx.javafxplugin") version "0.1.0"
}

repositories {
    mavenCentral()
}

dependencies {
    testImplementation("org.junit.jupiter:junit-jupiter:5.10.1")
    testRuntimeOnly("org.junit.platform:junit-platform-launcher")
    
    // TestFX for JavaFX UI testing
    testImplementation("org.testfx:testfx-core:4.0.18")
    testImplementation("org.testfx:testfx-junit5:4.0.18")
    
    // Monocle for headless JavaFX testing (independent of physical display)
    testImplementation("org.testfx:openjfx-monocle:21.0.2")
}

tasks.test {
    useJUnitPlatform()
    
    // Headless mode using Monocle - tests run in virtual display, independent of physical screen
    systemProperty("testfx.robot", "glass")
    systemProperty("testfx.headless", "true")
    systemProperty("glass.platform", "Monocle")
    systemProperty("monocle.platform", "Headless")
    systemProperty("prism.order", "sw")
    systemProperty("prism.text", "t2k")
    systemProperty("java.awt.headless", "true")
    
    // JVM args for JavaFX module access in tests (required for TestFX + Monocle headless)
    jvmArgs = listOf(
        // Module exports for Glass/JavaFX internals
        "--add-exports", "javafx.graphics/com.sun.javafx.application=ALL-UNNAMED",
        "--add-exports", "javafx.graphics/com.sun.glass.ui=ALL-UNNAMED",
        "--add-exports", "javafx.graphics/com.sun.glass.ui.delegate=ALL-UNNAMED",
        "--add-exports", "javafx.graphics/com.sun.javafx.tk=ALL-UNNAMED",
        "--add-exports", "javafx.graphics/com.sun.javafx.util=ALL-UNNAMED",
        "--add-exports", "javafx.graphics/com.sun.javafx.scene=ALL-UNNAMED",
        "--add-exports", "javafx.graphics/com.sun.javafx.scene.traversal=ALL-UNNAMED",
        // Required for Monocle headless - logging access
        "--add-exports", "javafx.base/com.sun.javafx.logging=ALL-UNNAMED",
        // Module opens for reflective access (required for Monocle to access JavaFX internals)
        "--add-opens", "javafx.graphics/com.sun.javafx.application=ALL-UNNAMED",
        "--add-opens", "javafx.graphics/com.sun.glass.ui=ALL-UNNAMED",
        "--add-opens", "javafx.graphics/com.sun.javafx.tk=ALL-UNNAMED",
        "--add-opens", "javafx.graphics/com.sun.javafx.util=ALL-UNNAMED",
        // Allow Monocle jar to read javafx.graphics module
        "--add-reads", "javafx.graphics=ALL-UNNAMED",
        "--add-reads", "javafx.base=ALL-UNNAMED"
    )
}

javafx {
    version = "21.0.2"
    modules = listOf("javafx.controls", "javafx.fxml")
}

application {
    mainClass.set("MainKt")
}

java {
    toolchain.languageVersion.set(JavaLanguageVersion.of(21))
}

kotlin {
    compilerOptions {
        jvmTarget.set(org.jetbrains.kotlin.gradle.dsl.JvmTarget.JVM_21)
        freeCompilerArgs.addAll("-Xjsr305=strict", "-Xannotation-default-target=param-property")
    }
}


