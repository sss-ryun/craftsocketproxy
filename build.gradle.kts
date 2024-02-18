plugins {
    kotlin("jvm") version "1.9.22"
}

kotlin {
    jvmToolchain(17)
}

group = "me.ryun.mcsockproxy"
version = "1.0-SNAPSHOT"

repositories {
    mavenCentral()
}

dependencies {
    testImplementation("org.jetbrains.kotlin:kotlin-test")

    implementation("io.netty:netty-all:4.1.107.Final")
}

tasks.test {
    useJUnitPlatform()
}

tasks.getByName<Jar>("jar") {
    //Include all dependencies needed at runtime.
    from(configurations.getByName("runtimeClasspath").map { if (it.isDirectory) it else zipTree(it) })

    duplicatesStrategy = DuplicatesStrategy.EXCLUDE

    manifest {
        attributes["Main-Class"] = "me.ryun.mcsockproxy.Main2Kt"
    }
}