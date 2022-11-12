pluginManagement {
    repositories {
        mavenCentral()
        maven (  "https://maven.fabricmc.net/" )
        maven (  "https://maven.architectury.dev/" )
        maven (  "https://maven.minecraftforge.net/" )
        gradlePluginPortal()
    }
    plugins{
        id("architectury-plugin") version ("3.4-SNAPSHOT") apply false
        id("dev.architectury.loom") version ("0.12.0-SNAPSHOT") apply false
        id("com.github.johnrengelman.shadow").version("7.1.2").apply(false)
    }
}

include("Common")
include("Fabric")
include("Forge")

rootProject.name = "Shimmer"
