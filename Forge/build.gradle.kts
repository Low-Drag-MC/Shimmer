plugins {
    id("com.github.johnrengelman.shadow")
}

architectury {
    platformSetupLoomIde()
    forge()
}

dependencies {
    forge("net.minecraftforge:forge:$forge_version")
}

loom {
    accessWidenerPath.set(project(":Common").loom.accessWidenerPath)

    forge {
        convertAccessWideners.set(true)
        extraAccessWideners.add(loom.accessWidenerPath.get().asFile.name)

        mixinConfig("$mod_id.mixins.json")
        mixinConfig("$mod_id.forge.mixins.json")

    }

}

val common by configurations.creating
val shadowCommon by configurations.creating
val developmentForge = configurations.named("developmentForge")

configurations {
    compileClasspath.get().extendsFrom(common)
    runtimeClasspath.get().extendsFrom(common)
    developmentForge.get().extendsFrom(common)
}

dependencies {
    forge("net.minecraftforge:forge:$forge_version")

    common(project(path = ":Common", configuration = "namedElements")) { isTransitive = false }
    shadowCommon(project(path = ":Common", configuration = "transformProductionForge")) { isTransitive = false }

    include(mixinExtras)
    forgeRuntimeLibrary(mixinExtras)

//    modImplementation("com.jozufozu.flywheel:flywheel-forge-$minecraft_version:$flywheel_version")

    forgeRuntimeLibrary("icyllis.modernui:ModernUI-Core:$modernui_core_version")
    modCompileOnly("icyllis.modernui:ModernUI-Forge:${minecraft_version}-${modernui_version}")

    modImplementation("maven.modrinth:embeddium:0.2.10+mc1.20.1")
    modImplementation("maven.modrinth:oculus:1.20.1-1.6.9")

}

tasks.processResources {
    inputs.property("version", project.version)

    filesMatching("META-INF/mods.toml") {
        expand("version" to project.version)
    }
}

tasks.shadowJar {
    exclude("fabric.mod.json")
    exclude("architectury.common.json")

    configurations = listOf(shadowCommon)

    archiveClassifier.set("dev-shadow")
}

tasks.remapJar {
    val shadowJarTask = tasks.shadowJar.get()
    inputFile.set(shadowJarTask.archiveFile)
    dependsOn(shadowJarTask)
    archiveClassifier.set(null as String?)
}

tasks.jar {
    archiveClassifier.set("dev")
}

tasks.sourcesJar {
    val commonSources = project(":Common").tasks.sourcesJar
    dependsOn(commonSources)
    from(commonSources.get().archiveFile.map(project::zipTree))
}

components.getByName<SoftwareComponent>("java") {
    (this as AdhocComponentWithVariants).apply {
        withVariantsFromConfiguration(project.configurations.shadowRuntimeElements.get()) {
            skip()
        }
    }
}