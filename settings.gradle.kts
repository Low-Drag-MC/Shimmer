pluginManagement {
    repositories {
        maven (  "https://maven.fabricmc.net/" )
        maven (  "https://maven.architectury.dev/" )
        maven (  "https://maven.minecraftforge.net/" )
        gradlePluginPortal()
    }
    plugins{
        id("architectury-plugin") version ("3.4-SNAPSHOT") apply false
    }
}

include("Common")
include("Fabric")
include("Forge")

rootProject.name = "Shimmer"
