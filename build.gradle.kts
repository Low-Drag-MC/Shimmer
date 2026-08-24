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
            setting.property("mixin.dumpTargetOnFailure=true")
            setting.property("mixin.hotSwap=true")
            //`mixin.checks.interfaces` is deliberately absent. It walks the full interface table of every
            //target, and Embeddium 0.3.16+ declares Fabric Rendering API interfaces on WorldSlice that it
            //never ships on Forge, so the walk NPEs and kills the game on world load. Production never sets
            //the flag, so it only ever broke this dev runtime.
            //`mixin.debug.export` is opt-in via `-PmixinExport`: it decompiles every transformed class, and
            //its profiler sections race with the worker threads that transform classes during a resource
            //reload, which intermittently aborts the reload ("Attempted to pop debug.export...").
            if (project.hasProperty("mixinExport")) {
                setting.property("mixin.debug.export=true")
            }
        }
        //`-PquickPlayWorld=<save folder>` boots straight into that world, so a render change can be
        //checked without clicking through the menus every time
        (project.findProperty("quickPlayWorld") as String?)?.let { world ->
            this.runConfigs.findByName("client")?.programArgs("--quickPlaySingleplayer", world)
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
            content {
                includeGroup("icyllis.modernui")
            }
        }
        maven {
            name = "Create maven"
            url = uri("https://maven.createmod.net")
            content {
                includeGroup("dev.engine-room.flywheel")
            }
        }
        maven {
            name = "BlameJared maven"
            url = uri("https://maven.blamejared.com")
            content {
                includeGroup("org.embeddedt")
            }
        }
        maven {
            name = "shedaniel maven"
            url = uri("https://maven.shedaniel.me/")
            content {
                includeGroup("me.shedaniel.cloth")
                includeGroup("me.shedaniel.cloth.api")
            }
        }
    }

    dependencies {
        minecraft("com.mojang:minecraft:$minecraft_version")
        mappings(loom.layered {
            officialMojangMappings()
            parchment("org.parchmentmc.data:parchment-$parchment_version@zip")
        })
        //provided at runtime by fabric-loader / by the bundled forge artifact, so compile-only here
        "compileOnly"(mixinExtras)
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