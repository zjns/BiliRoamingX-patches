import org.gradle.kotlin.dsl.support.listFilesOrdered

plugins {
    kotlin("jvm") version "1.9.22"
    `maven-publish`
}

group = "app.revanced"

repositories {
    mavenCentral()
    mavenLocal()
    google()
    maven { url = uri("https://jitpack.io") }
    maven {
        // A repository must be speficied for some reason. "registry" is a dummy.
        url = uri("https://maven.pkg.github.com/zjns/registry")
        credentials {
            username = project.findProperty("gpr.user") as String? ?: System.getenv("GITHUB_ACTOR")
            password = project.findProperty("gpr.key") as String? ?: System.getenv("GITHUB_TOKEN")
        }
    }
}

dependencies {
    implementation(libs.revanced.patcher)
    implementation(libs.smali)
    // Used in JsonGenerator.
    implementation(libs.gson)
}

kotlin {
    jvmToolchain(11)
}

tasks.withType(Jar::class) {
    exclude("app/revanced/meta")

    manifest {
        attributes["Name"] = "ReVanced Patches"
        attributes["Description"] = "Patches for ReVanced."
        attributes["Version"] = version
        attributes["Timestamp"] = System.currentTimeMillis().toString()
        attributes["Source"] = "git@github.com:revanced/revanced-patches.git"
        attributes["Author"] = "ReVanced"
        attributes["Contact"] = "contact@revanced.app"
        attributes["Origin"] = "https://revanced.app"
        attributes["License"] = "GNU General Public License v3.0"
    }
}

tasks {
    register<DefaultTask>("generateBundle") {
        description = "Generate dex files from build and bundle them in the jar file"
        dependsOn(build)

        doLast {
            val d8 = File(System.getenv("ANDROID_HOME")).resolve("build-tools")
                .listFilesOrdered().last().resolve("d8").absolutePath

            val artifacts = configurations.archives.get().allArtifacts.files.files.first().absolutePath
            val workingDirectory = layout.buildDirectory.dir("libs").get().asFile

            exec {
                workingDir = workingDirectory
                commandLine = listOf(d8, artifacts)
            }

            exec {
                workingDir = workingDirectory
                commandLine = listOf("zip", "-u", artifacts, "classes.dex")
            }
        }
    }

    register<JavaExec>("generateMeta") {
        description = "Generate metadata for this bundle"
        dependsOn(build)

        classpath = sourceSets["main"].runtimeClasspath
        mainClass.set("app.revanced.meta.IPatchesFileGenerator")
    }

    // Required to run tasks because Gradle semantic-release plugin runs the publish task.
    // Tracking: https://github.com/KengoTODA/gradle-semantic-release-plugin/issues/435
    named("publish") {
        dependsOn("generateBundle")
        dependsOn("generateMeta")
    }
}

publishing {
    publications {
        create<MavenPublication>("revanced-patches-publication") {
            from(components["java"])

            pom {
                name = "ReVanced Patches"
                description = "Patches for ReVanced."
                url = "https://revanced.app"

                licenses {
                    license {
                        name = "GNU General Public License v3.0"
                        url = "https://www.gnu.org/licenses/gpl-3.0.en.html"
                    }
                }
                developers {
                    developer {
                        id = "ReVanced"
                        name = "ReVanced"
                        email = "contact@revanced.app"
                    }
                }
                scm {
                    connection = "scm:git:git://github.com/revanced/revanced-patches.git"
                    developerConnection = "scm:git:git@github.com:revanced/revanced-patches.git"
                    url = "https://github.com/revanced/revanced-patches"
                }
            }
        }
    }
}
