import net.fabricmc.loom.api.LoomGradleExtensionAPI

plugins {
    id("architectury-plugin") version ("3.4-SNAPSHOT")
    id("dev.architectury.loom") version ("0.12.0-SNAPSHOT") apply false
    id("com.github.johnrengelman.shadow").version("7.1.2") apply false
}

architectury {
    minecraft = extra["minecraft_version"] as String
}



subprojects {
    apply(plugin = "dev.architectury.loom")

    val loom = extensions.getByType<LoomGradleExtensionAPI>()
    loom.run {
        silentMojangMappingsLicense()
    }

    repositories {
        maven {
            url = uri("https://maven.parchmentmc.org/")
            content {
                includeGroup("org.parchmentmc.data")
            }
        }
        maven {
            url = uri("https://cursemaven.com")
            content {
                includeGroup("curse.maven")
            }
        }
        maven {
            name = "Modrinth"
            url = uri("https://api.modrinth.com/maven")
            content {
                includeGroup("maven.modrinth")
            }
        }
    }

    dependencies {
        minecraft("com.mojang:minecraft:${rootProject.extra["minecraft_version"]}")
        mappings(loom.layered {
            officialMojangMappings()
            parchment("org.parchmentmc.data:parchment-${rootProject.extra["parchment_version"]}@zip")
        })
    }
}

allprojects{
    apply(plugin = "java")
    apply(plugin = "architectury-plugin")
    apply(plugin = "maven-publish")

    extra["archivesBaseName"] = rootProject.extra["mod_name"]
    version = "${rootProject.ext["version_major"]}.${rootProject.ext["version_patch"]}"
    group = rootProject.ext["maven_group"]!!

    tasks.withType<JavaCompile>(){
        options.encoding = "UTF-8"
        options.release.set(17)
    }

    extensions.getByType<JavaPluginExtension>().apply {
        withSourcesJar()
    }

//    tasks.withType<Jar>{
//        archiveBaseName.set(project.ext["mod_name"] as String)
//        val loom = this@allprojects.extensions.getByType<LoomGradleExtensionAPI>()
//        archiveAppendix.set(loom.platform.get().name)
//    }

}