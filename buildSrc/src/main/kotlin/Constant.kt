import org.gradle.api.Project

//Mod options
const val mod_name = "Shimmer"
const val mod_author = "KilaBash"
const val mod_id = "shimmer"

//Common
const val minecraft_version = "1.19.4"
const val parchment_version = "1.19.3:2023.03.12"
const val enabled_platforms = "fabric,forge"

//Fabric
const val fabric_loader_version = "0.14.19"
const val fabric_api_version = "0.81.1+$minecraft_version"
const val cloth_config_version = "10.0.96"

//Forge
const val forge_version = "$minecraft_version-45.0.64"
const val modernui_version = "3.6.1.117"// FIXME

//Project
const val version_major = 0.1
const val version_patch = 15
const val semantics_version = "$minecraft_version-$version_major.$version_patch"
const val maven_path = "snapshots"
const val maven_group = "com.lowdragmc.shimmer"

const val mixinExtras = "com.github.LlamaLad7:MixinExtras:0.1.1"

const val flywheel_version = "0.6.7-8"// FIXME

val Project.archiveBaseName get() = "$mod_name-${project.name.lowercase()}"
