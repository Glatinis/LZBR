plugins {
    id("java-library")
    id("xyz.jpenilla.run-paper") version "3.0.2"
}

repositories {
    mavenCentral()
    maven("https://repo.papermc.io/repository/maven-public/")
    maven("https://repo.onarandombox.com/content/groups/public/")
    maven("https://repo.codemc.io/repository/maven-releases/")
    maven("https://maven.enginehub.org/repo/")

}

dependencies {
    compileOnly("io.papermc.paper:paper-api:26.1.2.build.+")
    compileOnly("org.mvplugins.multiverse.core:multiverse-core:5.7.1")

    // WorldEdit API — FastAsyncWorldEdit implements this same API and provides the async paste at
    // runtime, so we compile against WorldEdit and require FAWE on the server (see ArenaResetService).
    compileOnly("com.sk89q.worldedit:worldedit-core:7.3.0")
    compileOnly("com.sk89q.worldedit:worldedit-bukkit:7.3.0") { isTransitive = false }
}

java {
    toolchain.languageVersion = JavaLanguageVersion.of(25)
}

tasks {
    jar {
        destinationDirectory.set(file("""C:\Users\rayan\Desktop\paper testing servs\26.1.2\plugins"""))
    }

    runServer {
        // Configure the Minecraft version for our task.
        // This is the only required configuration besides applying the plugin.
        // Your plugin's jar (or shadowJar if present) will be used automatically.
        minecraftVersion("26.1.2")
        jvmArgs("-Xms2G", "-Xmx2G")
    }

    processResources {
        val props = mapOf("version" to version, "description" to project.description)
        filesMatching("plugin.yml") {
            expand(props)
        }
    }
}
