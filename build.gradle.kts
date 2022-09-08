import net.fabricmc.loom.api.LoomGradleExtensionAPI

plugins {
    id("architectury-plugin")
    id("dev.architectury.loom").apply(false)
    id("com.github.johnrengelman.shadow").apply(false)
}

architectury {
    minecraft = minecraft_version
}

subprojects {
    apply(plugin = "dev.architectury.loom")
    apply(plugin = "maven-publish")

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
        minecraft("com.mojang:minecraft:$minecraft_version")
        mappings(loom.layered {
            officialMojangMappings()
            parchment("org.parchmentmc.data:parchment-$parchment_version@zip")
        })
    }

    extensions.getByType<BasePluginExtension>().apply {
        archivesName.set(archiveBaseName)
    }

    extensions.getByType<PublishingExtension>().apply {
        val platformName = project.name.toLowerCase()
        publications {
            create<MavenPublication>(name = platformName) {
                groupId = maven_group
                artifactId = archiveBaseName
                version = semantics_version
                from(components.getByName<SoftwareComponent>("java"))
            }
            repositories {
                maven {
                    setUrl("https://maven.firstdarkdev.xyz/$maven_path")
                    credentials {
                        username = System.getenv("MAVEN_USER")
                        password = System.getenv("MAVEN_PASS")
                    }
                }
            }
        }
    }

}

allprojects {
    apply(plugin = "java")
    apply(plugin = "architectury-plugin")
    apply(plugin = "maven-publish")

    version = semantics_version
    group = maven_group

    tasks.withType<JavaCompile> {
        options.encoding = "UTF-8"
        options.release.set(17)
    }

    extensions.getByType<JavaPluginExtension>().apply {
        withSourcesJar()
    }

}