plugins {
    `kotlin-dsl`
}

repositories {
    mavenCentral()
    gradlePluginPortal()
    maven("https://maven.architectury.dev/")
    maven("https://maven.fabricmc.net/")
    maven("https://maven.minecraftforge.net/")
}

dependencies {
    //only apply to self, otherwise other projects can't apply plugins as plugins are already in classpath
    compileOnlyApi("architectury-plugin:architectury-plugin.gradle.plugin:3.4-SNAPSHOT")
    compileOnlyApi("dev.architectury:architectury-loom:0.12.0-SNAPSHOT")
}