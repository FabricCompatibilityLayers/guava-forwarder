plugins {
    id("java")
    id("com.gradleup.shadow") version ("9.3.0")
    id("xyz.wagyourtail.jvmdowngrader") version ("1.3.4")
}

group = "io.github.fabriccompatibilitylayers"
version = "1.0-SNAPSHOT"

repositories {
    mavenCentral()
}

dependencies {

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
    exclude("META-INF/**")
}

tasks.shadeDowngradedApi {
    archiveClassifier.set("")
}

tasks.build {
    dependsOn(tasks.shadeDowngradedApi)
}