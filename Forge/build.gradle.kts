plugins {
    id("com.github.johnrengelman.shadow")
}

architectury {
    platformSetupLoomIde()
    forge()
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

    // `mixinextras-forge` is a GAMELIBRARY shell whose real classes sit in a jar-in-jar, which FML only
    // unpacks for a shipped jar - in dev it would leave MixinExtras off the classpath and take our mixin
    // config plugin (and Embeddium's @Local sugar) down with it. So: ship the shell, run the plain classes.
    include(mixinExtrasForge)
    forgeRuntimeLibrary(mixinExtras)

    modImplementation(forge_flywheel)

    forgeRuntimeLibrary("icyllis.modernui:ModernUI-Core:$modernui_core_version")
    modCompileOnly("icyllis.modernui:ModernUI-Forge:${minecraft_version}-${modernui_version}")

    modImplementation("maven.modrinth:embeddium:$embeddium_forge_version")

    // Oculus is opt-in for the dev runtime (`-PwithOculus`): Embeddium 0.3.16+ declares WorldSlice as
    // implementing Fabric Rendering API interfaces it doesn't ship, and Oculus mixes into classes that make
    // ModLauncher walk that hierarchy, which takes the game down before it reaches the world. Compiling
    // against it is fine either way, so the oculus mixins still get built.
    if (project.hasProperty("withOculus")) {
        modImplementation("maven.modrinth:oculus:$oculus_version")
    } else {
        modCompileOnly("maven.modrinth:oculus:$oculus_version")
    }

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