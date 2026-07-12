import org.gradle.api.artifacts.ExternalModuleDependency

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
    testRuntimeOnly("com.google.guava:guava:33.6.0-jre")
}

// Every Guava release we support, oldest first, grouped into consecutive runs the
// `coverageReport` task confirms have zero removed classes/members between them (i.e. no
// public API break). `main` contains the version-agnostic dispatch logic (see
// GuavaForwarder); each group gets exactly one sourceSet, containing only the fixes
// needed to bridge into the group's oldest member, and is compiled against that oldest
// version. This mirrors GuavaForwarder.SUPPORTED_VERSION_GROUPS - keep both in sync, and
// re-run coverageReport before changing either when adding a new version.
val guavaVersionGroups = listOf(
    listOf("12.0.1"),
    listOf("13.0", "13.0.1"),
    listOf("14.0", "14.0.1"),
    listOf("15.0"),
    listOf("16.0", "16.0.1"),
    listOf("17.0"),
    listOf("18.0"),
    listOf("19.0"),
    listOf("20.0"),
    listOf("21.0"),
    listOf("22.0"),
    listOf("23.0"),
    listOf("23.1-jre"),
    listOf("23.2-jre"),
    listOf("23.3-jre", "23.4-jre", "23.5-jre", "23.6-jre", "23.6.1-jre"),
    listOf("24.0-jre", "24.1-jre", "24.1.1-jre"),
    listOf("25.0-jre", "25.1-jre"),
    listOf("26.0-jre", "27.0-jre"),
    listOf("27.0.1-jre", "27.1-jre"),
    listOf("28.0-jre", "28.1-jre", "28.2-jre"),
    listOf("29.0-jre"),
    listOf("30.0-jre", "30.1-jre", "30.1.1-jre"),
    listOf(
        "31.0-jre", "31.0.1-jre", "31.1-jre", "32.0.0-jre", "32.0.1-jre",
        "32.1.0-jre", "32.1.1-jre", "32.1.2-jre", "32.1.3-jre",
        "33.0.0-jre", "33.1.0-jre", "33.2.0-jre", "33.2.1-jre", "33.3.0-jre", "33.3.1-jre",
        "33.4.0-jre", "33.4.1-jre", "33.4.2-jre", "33.4.3-jre", "33.4.4-jre",
        "33.4.5-jre", "33.4.6-jre", "33.4.7-jre", "33.4.8-jre",
        "33.5.0-jre", "33.6.0-jre"
    )
)
val guavaVersions = guavaVersionGroups.flatten()

fun guavaSourceSetId(version: String) = "g" + version.replace(".", "_").replace("-", "_")

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
    add("downgradeJarClasspath", "com.google.guava:guava:33.6.0-jre")
}

guavaVersionGroups.forEach { group ->
    // Compiled against the group's oldest member - since every version in the group is
    // API-identical to it (that's what makes the group non-breaking), that's the most
    // conservative common denominator for whatever the shared module needs to compile
    // against.
    val id = guavaSourceSetId(group.first())
    sourceSets.create(id) {
        compileClasspath += sourceSets.main.get().output + sourceSets.main.get().compileClasspath
    }
    dependencies {
        add("${id}CompileOnly", "com.google.guava:guava:${group.first()}")
        add("guavaModules", sourceSets[id].output.classesDirs)
        // `test` only sees `main` by default; add the output directly to its runtime
        // classpath so GuavaForwarderTest can reflectively load these classes.
        add("testRuntimeOnly", sourceSets[id].output)
    }
}

// Standalone sourceSet for the coverageReport tool below: it needs `main` (GuavaForwarder)
// and `test` (FakeMappingBuilder/FakeVisitorInfos, reused so the report exercises the
// exact same dispatch path GuavaForwarderTest does) plus ASM to read Guava jars as raw
// bytecode - none of which belongs on the shipped jar's classpath.
sourceSets.create("coverageTool") {
    compileClasspath += sourceSets.main.get().output + sourceSets.main.get().compileClasspath + sourceSets.test.get().output
    // `test`'s own runtimeClasspath already carries a real Guava jar plus every version
    // module's compiled output (see the testRuntimeOnly additions in the loop above) -
    // GuavaStubRegistrar needs both to reflect over stub methods that reference real
    // Guava types.
    runtimeClasspath += sourceSets.test.get().runtimeClasspath + guavaModules
}

dependencies {
    add("coverageToolImplementation", "org.ow2.asm:asm:9.7")
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

// Resolved once at configuration time (same jars the per-version sourceSets already
// pull in as compileOnly, so this hits the dependency cache) purely as data files for
// ApiSnapshot to parse with ASM - not put on any classpath. Non-transitive since only
// Guava's own class files matter for the API diff.
val guavaJarsByVersion: Map<String, java.io.File> = guavaVersions.associateWith { version ->
    val dependency = dependencies.create("com.google.guava:guava:$version") as ExternalModuleDependency
    dependency.isTransitive = false
    configurations.detachedConfiguration(dependency).resolve().single()
}

val coverageReportFile = layout.buildDirectory.file("reports/coverageReport/coverageReport.txt")

tasks.register<JavaExec>("coverageReport") {
    group = "verification"
    description = "Diffs each pair of consecutive supported Guava jars' public API and reports which " +
            "removed/changed members aren't yet covered by a registered mapping, visitor redirect, or stub."
    classpath = sourceSets["coverageTool"].runtimeClasspath
    mainClass.set("io.github.fabriccompatibilitylayers.guavaforwarder.coverage.CoverageReportMain")

    val out = coverageReportFile.get().asFile
    outputs.file(out)
    args = listOf(out.absolutePath) + guavaVersions.flatMap { version -> listOf(version, guavaJarsByVersion.getValue(version).absolutePath) }

    doFirst { out.parentFile.mkdirs() }
}

// 1.2.x-1.5.x Forge
tasks.register<JavaExec>("coverageReport12To17") {
    group = "verification"
    description = "Diffs each pair of consecutive supported Guava jars' public API and reports which " +
            "removed/changed members aren't yet covered by a registered mapping, visitor redirect, or stub."
    classpath = sourceSets["coverageTool"].runtimeClasspath
    mainClass.set("io.github.fabriccompatibilitylayers.guavaforwarder.coverage.CoverageReportMain")

    val out = coverageReportFile.get().asFile
    outputs.file(out)
    args = listOf(out.absolutePath) + listOf("12.0.1", "17.0").flatMap { version -> listOf(version, guavaJarsByVersion.getValue(version).absolutePath) }

    doFirst { out.parentFile.mkdirs() }
}

// Vanilla 1.6.x
tasks.register<JavaExec>("coverageReport14To17") {
    group = "verification"
    description = "Diffs each pair of consecutive supported Guava jars' public API and reports which " +
            "removed/changed members aren't yet covered by a registered mapping, visitor redirect, or stub."
    classpath = sourceSets["coverageTool"].runtimeClasspath
    mainClass.set("io.github.fabriccompatibilitylayers.guavaforwarder.coverage.CoverageReportMain")

    val out = coverageReportFile.get().asFile
    outputs.file(out)
    args = listOf(out.absolutePath) + listOf("14.0", "17.0").flatMap { version -> listOf(version, guavaJarsByVersion.getValue(version).absolutePath) }

    doFirst { out.parentFile.mkdirs() }
}

// Vanilla 1.7.x
tasks.register<JavaExec>("coverageReport15To17") {
    group = "verification"
    description = "Diffs each pair of consecutive supported Guava jars' public API and reports which " +
            "removed/changed members aren't yet covered by a registered mapping, visitor redirect, or stub."
    classpath = sourceSets["coverageTool"].runtimeClasspath
    mainClass.set("io.github.fabriccompatibilitylayers.guavaforwarder.coverage.CoverageReportMain")

    val out = coverageReportFile.get().asFile
    outputs.file(out)
    args = listOf(out.absolutePath) + listOf("15.0", "17.0").flatMap { version -> listOf(version, guavaJarsByVersion.getValue(version).absolutePath) }

    doFirst { out.parentFile.mkdirs() }
}