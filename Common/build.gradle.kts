architectury {
    common((enabled_platforms).split(","))
}

loom {
    accessWidenerPath.set(file("src/main/resources/$mod_id.accesswidener"))
}

dependencies {
    modImplementation("net.fabricmc:fabric-loader:${fabric_loader_version}")
    implementation("net.java.dev.jna:jna:5.13.0")
}