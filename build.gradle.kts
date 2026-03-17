plugins {
    id("java")
    id("application")
}

group = "com.caffeinesoft.githubtag"
version = "1.0.0"

java {
    toolchain {
        languageVersion.set(JavaLanguageVersion.of(25))
    }
}

application {
    mainClass.set("com.caffeinesoft.githubtag.Main")
}

repositories {
    mavenCentral()
}

dependencies {
    implementation("org.json:json:20251224")
    testImplementation("org.junit.jupiter:junit-jupiter:5.10.2")
    testRuntimeOnly("org.junit.platform:junit-platform-launcher")
}

tasks.withType<Test> {
    useJUnitPlatform()
}

tasks.jar {
    archiveFileName.set("github-tag-java.jar")

    manifest {
        attributes["Main-Class"] = "com.caffeinesoft.githubtag.Main"
    }
    duplicatesStrategy = DuplicatesStrategy.EXCLUDE
    from(sourceSets.main.get().output)
    dependsOn(configurations.runtimeClasspath)
    from({
        configurations.runtimeClasspath.get().filter { it.name.endsWith("jar") }.map { zipTree(it) }
    })
}