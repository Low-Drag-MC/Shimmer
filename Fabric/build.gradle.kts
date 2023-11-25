plugins {
    id("com.github.johnrengelman.shadow")
}

architectury {
    platformSetupLoomIde()
    fabric()
}

loom {
    accessWidenerPath.set(project(":Common").loom.accessWidenerPath)
}

val common by configurations.creating
val shadowCommon by configurations.creating
val developmentFabric = configurations.named("developmentFabric")

configurations {
    compileClasspath.get().extendsFrom(common)
    runtimeClasspath.get().extendsFrom(common)
    developmentFabric.get().extendsFrom(common)
}

dependencies {
    modImplementation("net.fabricmc:fabric-loader:$fabric_loader_version")
    modApi("net.fabricmc.fabric-api:fabric-api:$fabric_api_version")

    common(project(path = ":Common", configuration = "namedElements")) { isTransitive = false }
    shadowCommon(project(path = ":Common", configuration = "transformProductionFabric")) { isTransitive = false }

    include(mixinExtras)
    modApi("me.shedaniel.cloth:cloth-config-fabric:$cloth_config_version")
    include("me.shedaniel.cloth:cloth-config-fabric:$cloth_config_version")

    // Sodium
    modImplementation("maven.modrinth:sodium:mc1.20.1-0.5.3") {
        exclude(group = "net.fabricmc.fabric-api")
    }

    modImplementation("maven.modrinth:iris:1.6.10+1.20.1") {
        exclude(group = "net.fabricmc.fabric-api")
    }

    implementation("org.anarres:jcpp:1.4.14") {isTransitive = false}// for iris
    implementation("io.github.douira:glsl-transformer:2.0.0-pre13") // for iris
    implementation("org.antlr:antlr4-runtime:4.11.1") // for iris

    modImplementation("maven.modrinth:modmenu:$mod_menu_version")

}

tasks.processResources {
    inputs.property("version", project.version)

    filesMatching("fabric.mod.json") {
        expand("versions" to project.version)
    }
}

tasks.shadowJar {
    exclude("architectury.common.json")
    configurations = listOf(shadowCommon)
    archiveClassifier.set("dev-shadow")
}

tasks.remapJar {
    val shadowJarTask = tasks.shadowJar.get()
    injectAccessWidener.set(true)
    inputFile.set(shadowJarTask.archiveFile)
    dependsOn(shadowJarTask)
    archiveClassifier.set(null as String?)
}

tasks.jar {
    archiveClassifier.set("dev")
}

tasks.sourcesJar {
    val commonSources = project(":Common").tasks.sourcesJar.get()
    dependsOn(commonSources)
    from(commonSources.archiveFile.map(project::zipTree))
}

components.getByName<SoftwareComponent>("java") {
    (this as AdhocComponentWithVariants).apply {
        withVariantsFromConfiguration(project.configurations.shadowRuntimeElements.get()) {
            skip()
        }
    }
}
