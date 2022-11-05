import net.fabricmc.loom.api.LoomGradleExtensionAPI
import net.fabricmc.loom.util.Constants.Configurations as FabricConstantConfiguration
import org.gradle.api.Project
import org.gradle.api.artifacts.Dependency
import org.gradle.kotlin.dsl.DependencyHandlerScope
import org.gradle.kotlin.dsl.get
import org.gradle.kotlin.dsl.getByType

val Project.common get() = project.project(":Common")

fun DependencyHandlerScope.minecraft(any: Any){
    FabricConstantConfiguration.MINECRAFT(any)
}

fun DependencyHandlerScope.mappings(dependency : Dependency){
    FabricConstantConfiguration.MAPPINGS(dependency)
}

fun cusrseMaven(modName:String,projectID : Long, fileId : Long) = "curse.maven:$modName-$projectID:$fileId"