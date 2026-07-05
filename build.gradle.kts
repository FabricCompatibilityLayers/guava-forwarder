plugins {
    id("java")
    id("com.gradleup.shadow") version ("9.3.0")
    id("xyz.wagyourtail.jvmdowngrader") version ("1.3.4")
}

group = "io.github.fabriccompatibilitylayers"
version = "1.0-SNAPSHOT"

repositories {
    mavenCentral()
    maven {
        url = uri("https://api.modrinth.com/maven/")
        content {
            includeGroup("maven.modrinth")
        }
    }
}

dependencies {
    implementation("maven.modrinth:mod-remapping-api:1.30.0")

    testImplementation("org.junit.jupiter:junit-jupiter:5.10.2")
    testRuntimeOnly("org.junit.platform:junit-platform-launcher")

    // At real runtime Guava is provided by the host mod/game; simulate that here so
    // tests can reflectively load version modules whose stubs reference real Guava types.
    testRuntimeOnly("com.google.guava:guava:17.0")
}

// One sourceSet per supported Guava release. `main` contains the version-agnostic
// dispatch logic (see GuavaForwarder); each version sourceSet contains only the fixes
// needed for that specific release and is compiled against that exact Guava version.
val guavaVersions = listOf(
    "12.0.1", "13.0", "13.0.1", "14.0", "14.0.1", "15.0", "16.0", "16.0.1", "17.0"
)

fun guavaSourceSetId(version: String) = "g" + version.replace(".", "_")

// Dedicated, standalone configuration (extends nothing) that collects every version
// sourceSet's output for packaging. It's kept separate from `implementation`/
// `runtimeOnly`/the plugin's own `shadow` configuration because those all eventually
// feed back into `main`'s compileClasspath, which every version sourceSet also reads
// from below - reusing one of them here would create a dependency cycle.
val guavaModules: Configuration = configurations.create("guavaModules")

// Resolvable classpath fed to downgradeJar's ASM pass, which needs a real Guava jar to
// resolve supertypes/members of Guava types referenced by stub replacements (e.g.
// HashFunction/HashCode in MessageDigestUtils) - otherwise it logs "Could not find
// class" for each. Kept separate from guavaModules since that holds our own compiled
// classes, not Guava itself.
val downgradeJarClasspath: Configuration = configurations.create("downgradeJarClasspath")

dependencies {
    add("downgradeJarClasspath", "com.google.guava:guava:17.0")
}

guavaVersions.forEach { version ->
    val id = guavaSourceSetId(version)
    sourceSets.create(id) {
        compileClasspath += sourceSets.main.get().output + sourceSets.main.get().compileClasspath
    }
    dependencies {
        add("${id}CompileOnly", "com.google.guava:guava:$version")
        add("guavaModules", sourceSets[id].output.classesDirs)
        // `test` only sees `main` by default; add the output directly to its runtime
        // classpath so GuavaForwarderTest can reflectively load these classes.
        add("testRuntimeOnly", sourceSets[id].output)
    }
}

tasks.test {
    useJUnitPlatform()
}

tasks.compileJava {
    options.encoding = "UTF-8"
    options.release.set(21)
}

jvmdg {
    downgradeTo = JavaVersion.VERSION_1_8
    multiReleaseOriginal.set(true)
    multiReleaseVersions.addAll(JavaVersion.VERSION_17, JavaVersion.VERSION_21)
}

tasks.shadowJar {
    configurations.add(project.configurations.shadow)
    configurations.add(guavaModules)
    exclude("META-INF/**")
}

tasks.downgradeJar {
    classpath += downgradeJarClasspath
}

tasks.shadeDowngradedApi {
    archiveClassifier.set("")
}

tasks.build {
    dependsOn(tasks.shadeDowngradedApi)
}