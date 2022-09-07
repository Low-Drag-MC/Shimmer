plugins {
    id("com.github.johnrengelman.shadow").version("7.1.2")
}

architectury {
    platformSetupLoomIde()
    forge()
}

dependencies {
    forge("net.minecraftforge:forge:${rootProject.ext["forge_version"]}")
}

loom {
    accessWidenerPath.set(project(":Common").loom.accessWidenerPath)

    forge {
        convertAccessWideners.set(true)
        extraAccessWideners.add(loom.accessWidenerPath.get().asFile.name)

        mixinConfig("${rootProject.ext["mod_id"]}.mixins.json")
        mixinConfig("${rootProject.ext["mod_id"]}.forge.mixins.json")

    }

}

extra["archivesBaseName"] = "${rootProject.ext["archivesBaseName"]}-forge"

val common by configurations.creating
val shadowCommon by configurations.creating
val developmentForge = configurations.named("developmentForge")

configurations {
    compileClasspath.get().extendsFrom(common)
    runtimeClasspath.get().extendsFrom(common)
    developmentForge.get().extendsFrom(common)
}

dependencies {
    forge("net.minecraftforge:forge:${rootProject.ext["forge_version"]}")

    common(project(path = ":Common", configuration = "namedElements")) { isTransitive = false }
    shadowCommon(project(path = ":Common", configuration = "transformProductionForge")) { isTransitive = false }

    modImplementation("curse.maven:rubidium-574856:3749094")
    modRuntimeOnly("curse.maven:flywheel-486392:3871082")

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

components.getByName("java") {
    (this as AdhocComponentWithVariants).apply {
        withVariantsFromConfiguration(project.configurations.shadowRuntimeElements.get()) {
            skip()
        }
    }
}