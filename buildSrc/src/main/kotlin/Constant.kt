import org.gradle.api.Project

//Mod options
const val mod_name = "Shimmer"
const val mod_author = "KilaBash"
const val mod_id = "shimmer"

//Common
const val minecraft_version = "1.20.1"
const val parchment_version = "1.20.1:2023.07.30"
const val enabled_platforms = "fabric,forge"

//Fabric
const val fabric_loader_version = "0.14.21"
const val fabric_api_version = "0.86.1+$minecraft_version"
const val cloth_config_version = "11.1.106"
const val mod_menu_version = "7.1.0"

//Forge
const val forge_version = "$minecraft_version-47.1.43"
const val modernui_core_version = "3.7.1"
const val modernui_version = "3.7.1.3"

//Project
const val version_major = 0.2
const val version_patch = 2
const val semantics_version = "$minecraft_version-$version_major.$version_patch"
const val maven_path = "snapshots"
const val maven_group = "com.lowdragmc.shimmer"

const val mixinExtras = "com.github.LlamaLad7:MixinExtras:0.1.1"

const val fabric_flywheel_version = "0.6.9-1"
const val forge_flywheel_version = "0.6.10-7"

val Project.archiveBaseName get() = "$mod_name-${project.name.lowercase()}"
