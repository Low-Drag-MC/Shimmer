import java.util.zip.ZipEntry
import java.util.zip.ZipOutputStream

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

// Iris hard-depends on `sodium`, and Embeddium satisfies that by shipping a stub jar-in-jar. Loom doesn't
// unpack nested jars for mods on the dev classpath, so fabric loader never sees that stub and refuses to
// start. Rebuilding the same stub here keeps the dev runtime matching what users actually get; it exists
// only on the run classpath and is never part of the published jar.
// Written during configuration rather than by a task because loom resolves the run classpath while it sets
// Minecraft up, which happens before any task would have run.
val sodiumStubJar: File = layout.buildDirectory.file("devlibs/sodium-stub.jar").get().asFile.apply {
    if (!exists() || length() == 0L) {
        parentFile.mkdirs()
        ZipOutputStream(outputStream()).use { zip ->
            zip.putNextEntry(ZipEntry("fabric.mod.json"))
            zip.write("""{"schemaVersion":1,"id":"sodium","version":"0.5.11","name":"Sodium","description":"Embeddium-provided stub for mod compatibility"}""".toByteArray())
            zip.closeEntry()
        }
    }
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

    //nothing bundles MixinExtras here - fabric-loader 0.16+ already ships it, and that's our declared floor

    modApi("me.shedaniel.cloth:cloth-config-fabric:$cloth_config_version")
    include("me.shedaniel.cloth:cloth-config-fabric:$cloth_config_version")

    // Flywheel's fabric build declares `breaks: embeddium: "*"`, so fabric loader refuses to start with both
    // and the dev runtime has to pick one. Embeddium is the default; `-PfabricRuntime=flywheel` swaps in
    // Sodium + Flywheel instead, which is enough to exercise flywheel.LevelRenderMixin (it targets vanilla
    // LevelRenderer, not Flywheel) - the terrain mixins skip themselves when Embeddium is absent.
    if (project.findProperty("fabricRuntime") == "flywheel") {
        modImplementation("maven.modrinth:sodium:$sodium_version") {
            exclude(group = "net.fabricmc.fabric-api")
        }
        modImplementation(fabric_flywheel)
    } else {
        // Embeddium: drop-in replacement for Sodium
        modImplementation(embeddium_fabric) {
            exclude(group = "net.fabricmc.fabric-api")
        }
        //plain runtime entry, not modLocalRuntime: there's nothing in it to remap
        "localRuntime"(files(sodiumStubJar))
    }

    modImplementation("maven.modrinth:iris:$iris_version") {
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
