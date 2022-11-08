import org.gradle.api.Project
import java.util.*

//Mod options
val mod_name = "Shimmer"
val mod_author = "KilaBash"
val mod_id = "shimmer"

//Common
val minecraft_version = "1.18.2"
val parchment_version = "1.18.2:2022.09.04"
val enabled_platforms = "fabric,forge"

//Archiitectury
val architectury_version = "4.8.79"

//Fabric
val fabric_loader_version = "0.14.9"
val fabric_api_version = "0.58.0+$minecraft_version"
val cloth_config_version = "6.4.90"

//Forge
val forge_version = "$minecraft_version-40.1.69"
val modernui_version = "3.5.4.113"

//Project
val version_major = 0.1
val version_patch = 14
val semantics_version = "$minecraft_version-$version_major.$version_patch"
val maven_path = "snapshots"
val maven_group = "com.lowdragmc.shimmer"

val mixinExtras = "com.github.LlamaLad7:MixinExtras:0.1.0"

val flywheel_version = "0.6.6-94"

val Project.archiveBaseName get() = "$mod_name-${project.name.toLowerCase(Locale.getDefault())}"