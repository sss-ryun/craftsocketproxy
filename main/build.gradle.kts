plugins {
    kotlin("jvm")
}

group = "me.ryun.mcsockproxy"
version = "1.1.3"

repositories {
    mavenCentral()
}

dependencies {
    testImplementation("org.jetbrains.kotlin:kotlin-test")
    compileOnly("io.netty:netty-codec-http:4.1.107.Final")
}

tasks.test {
    useJUnitPlatform()
}
kotlin {
    jvmToolchain(8)
}