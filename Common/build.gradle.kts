architectury {
    common((rootProject.extra["enabled_platforms"] as String).split(","))
}

loom {
    accessWidenerPath.set(file("src/main/resources/${rootProject.ext["mod_id"]}.accesswidener"))
}

dependencies {
    modImplementation("net.fabricmc:fabric-loader:${rootProject.ext["fabric_loader_version"]}")
}

publishing {
    publications {
        create<MavenPublication>("MavenCommon") {
            artifactId = rootProject.ext["archivesBaseName"] as String
            from(components.getByName("java"))
        }
    }

}