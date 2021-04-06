import org.jetbrains.kotlin.gradle.tasks.KotlinCompile
import com.google.protobuf.gradle.*

plugins {
    kotlin("jvm") version "1.4.10"
    java
    idea
    id("com.google.protobuf") version "0.8.15"
}

group = "me.testpc"
version = "1.0-SNAPSHOT"

sourceSets{
    create("sample"){
        proto {
            srcDir("src/sample/protobuf")
        }
    }
}

repositories {
    maven("https://plugins.gradle.org/m2/")
    mavenCentral()
}

dependencies {
    implementation(kotlin("stdlib"))

    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-core:1.3.8")
    implementation("com.google.protobuf:protobuf-java:3.8.0")
    implementation("io.grpc:grpc-stub:1.17.1")
    implementation("io.grpc:grpc-kotlin-stub:1.0.0")
    implementation ("io.grpc:grpc-protobuf:1.17.1")
    implementation ("io.grpc:grpc-netty:1.17.1") // grpcVersion
    implementation ("io.grpc:grpc-netty-shaded:1.17.1") // grpcVersion
    testImplementation(kotlin("test-junit"))

    if (JavaVersion.current().isJava9Compatible) {
        implementation("javax.annotation:javax.annotation-api:+")
    }
}

protobuf {
    protoc {
        artifact = "com.google.protobuf:protoc:3.13.0"  // protobufVersion
    }
    plugins {
        id("grpc") {
            artifact = "io.grpc:protoc-gen-grpc-java:1.17.1"    // grpcVersion
        }
        id("grpckt") {
            artifact = "io.grpc:protoc-gen-grpc-kotlin:1.0.0:jdk7@jar"  // grpcKotlinVersion
        }
    }
    generateProtoTasks {
        all().forEach {
            it.plugins {
                id("grpc")
                id("grpckt")
            }
        }
    }
}

tasks.test {
    useJUnit()
}

tasks.withType<KotlinCompile>().configureEach {
    kotlinOptions {
        useIR = true
    }
}