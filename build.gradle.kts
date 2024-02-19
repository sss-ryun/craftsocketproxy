import org.jetbrains.kotlin.gradle.dsl.jvm.JvmTargetValidationMode
import org.jetbrains.kotlin.gradle.tasks.KotlinJvmCompile

plugins {
    kotlin("jvm") version "1.9.22"
}

kotlin {
    jvmToolchain(8)
}

group = "me.ryun.mcsockproxy"
version = "1.0.0"

repositories {
    mavenCentral()
}

dependencies {
    testImplementation("org.jetbrains.kotlin:kotlin-test")
    implementation("io.netty:netty-codec-http:4.1.107.Final")
    implementation(project(":main"))
}

tasks.test {
    useJUnitPlatform()
}

tasks.getByName<Jar>("jar") {
    //Include all dependencies needed at runtime.
    from(configurations.getByName("runtimeClasspath").map { if (it.isDirectory) it else zipTree(it) })

    duplicatesStrategy = DuplicatesStrategy.EXCLUDE

    manifest {
        attributes["Main-Class"] = "me.ryun.mcsockproxy.MainKt"
    }
}

tasks.withType<KotlinJvmCompile>().configureEach {
    jvmTargetValidationMode.set(JvmTargetValidationMode.WARNING)
}