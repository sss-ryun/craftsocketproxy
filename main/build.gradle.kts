plugins {
    kotlin("jvm")
}

group = "me.ryun.mcsockproxy"
version = "1.0.0"

repositories {
    mavenCentral()
}

dependencies {
    testImplementation("org.jetbrains.kotlin:kotlin-test")
    compileOnly("io.netty:netty-all:4.1.107.Final")
}

tasks.test {
    useJUnitPlatform()
}
kotlin {
    jvmToolchain(8)
}