import org.gradle.api.Project

//Mod options
val mod_name = "Shimmer"
val mod_author = "KilaBash"
val mod_id = "shimmer"

//Common
val minecraft_version = "1.19.2"
val parchment_version = "1.19.2:2022.10.09"
val enabled_platforms = "fabric,forge"

//Archiitectury
val architectury_version = "4.8.79"

//Fabric
val fabric_loader_version = "0.14.9"
val fabric_api_version = "0.62.0+$minecraft_version"
val cloth_config_version = "8.2.88"

//Forge
val forge_version = "$minecraft_version-43.1.34"
val modernui_version = "3.6.1.115"

//Project
val version_major = 0.1
val version_patch = 14
val semantics_version = "$minecraft_version-$version_major.$version_patch"
val maven_path = "snapshots"
val maven_group = "com.lowdragmc.shimmer"

val mixinExtras = "com.github.LlamaLad7:MixinExtras:0.0.12"

val Project.archiveBaseName get() = "$mod_name-${project.name.toLowerCase()}"