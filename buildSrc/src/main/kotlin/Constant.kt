import org.gradle.api.Project

//Mod options
const val mod_name = "Shimmer"
const val mod_author = "KilaBash"
const val mod_id = "shimmer"

//Common
const val minecraft_version = "1.20.1"
const val parchment_version = "1.20.1:2023.09.03"
const val enabled_platforms = "fabric,forge"

//Fabric
const val fabric_loader_version = "0.16.14"
const val fabric_api_version = "0.92.11+$minecraft_version"
const val cloth_config_version = "11.1.136"
const val mod_menu_version = "7.2.2"

//Forge
const val forge_version = "$minecraft_version-47.4.23"
const val modernui_core_version = "3.7.1"
const val modernui_version = "3.7.1.3"

//Project
//a String, not a Double: `0.10` as a Double renders as "0.1" and would silently collide with 0.1
const val version_major = "0.3"
const val version_patch = 0
const val semantics_version = "$minecraft_version-$version_major.$version_patch"
const val maven_path = "snapshots"
const val maven_group = "com.lowdragmc.shimmer"

//MixinExtras moved off jitpack; 0.1.1's annotation processor also fights the 0.4.x one that fabric-loader
//and Forge now ship, which breaks the mixin AP's obfuscation mapping lookup.
const val mixinExtrasVersion = "0.4.1"
const val mixinExtras = "io.github.llamalad7:mixinextras-common:$mixinExtrasVersion"
const val mixinExtrasForge = "io.github.llamalad7:mixinextras-forge:$mixinExtrasVersion"

//Flywheel moved from `com.jozufozu.flywheel` (maven.tterrag.com, now gone) to
//`dev.engine-room.flywheel` on maven.createmod.net with the 1.0 rewrite.
const val flywheel_version = "1.0.6-281"
const val fabric_flywheel = "dev.engine-room.flywheel:flywheel-fabric-$minecraft_version:$flywheel_version"
const val forge_flywheel = "dev.engine-room.flywheel:flywheel-forge-$minecraft_version:$flywheel_version"

//Embeddium replaces both Rubidium (forge) and Sodium (fabric).
//The forge builds are on Modrinth's maven; the fabric builds share their version string with the
//forge ones there (Modrinth maven would hand back the forge jar), so they come from BlameJared.
const val embeddium_forge_version = "0.3.31+mc$minecraft_version"
const val embeddium_fabric = "org.embeddedt:embeddium-fabric-$minecraft_version:0.3.26-beta.106+mc$minecraft_version"

//only used by the `-PfabricRuntime=flywheel` dev runtime, see Fabric/build.gradle.kts
const val sodium_version = "mc$minecraft_version-0.5.13-fabric"

//Iris 1.7 / Oculus 1.7 moved every class from `net.coderbot.iris` to `net.irisshaders.iris`.
//Held at 1.7.5 rather than 1.7.6: 1.7.6 started calling `net.caffeinemc.mods.sodium.api.texture.SpriteUtil`,
//which Sodium 0.5.9+ has but Embeddium 0.3.x (forked earlier) does not - its SpriteContents mixin then
//fails to transform and takes the whole texture atlas load down with it.
const val iris_version = "1.7.5+$minecraft_version"
const val oculus_version = "$minecraft_version-1.8.0"

val Project.archiveBaseName get() = "$mod_name-${project.name.lowercase()}"
