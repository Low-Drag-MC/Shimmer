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
        this.runConfigs.forEach { setting ->
            setting.property("mixin.debug=true")
            setting.property("mixin.debug.export=true")
            setting.property("mixin.dumpTargetOnFailure=true")
            setting.property("mixin.checks.interfaces=true")
            setting.property("mixin.hotSwap=true")
        }
    }
    repositories {
        flatDir {
            dir("libs")
        }
        maven("https://jitpack.io")
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
        maven {
            name = "IzzelAliz Maven"
            url = uri("https://maven.izzel.io/releases/")
        }
        maven {
            name = "tterrag maven"
            url = uri("https://maven.tterrag.com/")
        }
    }

    dependencies {
        minecraft("com.mojang:minecraft:$minecraft_version")
        mappings(loom.layered {
            officialMojangMappings()
            parchment("org.parchmentmc.data:parchment-$parchment_version@zip")
        })
        "implementation"(mixinExtras)
        "annotationProcessor"(mixinExtras)
        "implementation"("org.jetbrains:annotations:24.0.1")
    }

    extensions.getByType<BasePluginExtension>().apply {
        archivesName.set(archiveBaseName)
    }

    extensions.getByType<PublishingExtension>().apply {
        val platformName = project.name.lowercase()
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

    tasks.create("checkMixinPlugin"){
        val jarTask = tasks.withType<Jar> {
            finalizedBy(this@create)
        }
        dependsOn(jarTask)
        doLast {
            this.inputs.files.files.forEach(::check)
        }
    }

    tasks.withType<Jar>{
        doFirst{
            this.inputs.files.files.forEach(::check)
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