import org.gradle.api.Project

//Mod options
const val mod_name = "Shimmer"
const val mod_author = "KilaBash"
const val mod_id = "shimmer"

//Common
const val minecraft_version = "1.20.4"
const val parchment_version = "1.20.4-2024.02.25"
const val enabled_platforms = "fabric,neoforge"

//Fabric
const val fabric_loader_version = "0.15.7"
const val fabric_api_version = "0.96.4+$minecraft_version"
const val cloth_config_version = "11.1.106"
const val mod_menu_version = "9.0.0"

//Forge
const val neoforge_version = "20.4.203"
const val modernui_core_version = "3.10.0"
const val modernui_version = "3.10.0.6"

//Project
const val version_major = 0.2
const val version_patch = 4
const val semantics_version = "$minecraft_version-$version_major.$version_patch"
const val maven_path = "snapshots"
const val maven_group = "com.lowdragmc.shimmer"

const val mixinExtras = "com.github.LlamaLad7:MixinExtras:0.1.1"

// There is no flywheel 1.20.4 version currently
const val fabric_flywheel_version = "0.6.9-1"
const val forge_flywheel_version = "0.6.10-7"

val Project.archiveBaseName get() = "$mod_name-${project.name.lowercase()}"
