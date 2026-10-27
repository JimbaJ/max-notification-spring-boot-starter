plugins {
    `java-library`
    `maven-publish`
}

group = "com.github.JimbaJ"
version = "0.1.0"

java {
    toolchain { languageVersion = JavaLanguageVersion.of(21) }
    withSourcesJar()
    withJavadocJar()
}

repositories { mavenCentral() }

dependencies {
    implementation(platform("org.springframework.boot:spring-boot-dependencies:3.4.2"))
    compileOnly("org.springframework.boot:spring-boot-autoconfigure")
    compileOnly("org.springframework:spring-web")
    compileOnly("org.springframework:spring-webmvc")
    compileOnly("jakarta.servlet:jakarta.servlet-api")
    testImplementation("org.springframework.boot:spring-boot-starter-test")
    testImplementation("org.springframework:spring-webmvc")
    testImplementation("jakarta.servlet:jakarta.servlet-api")
    testRuntimeOnly("org.junit.platform:junit-platform-launcher")
}

tasks.test { useJUnitPlatform() }

publishing {
    publications {
        create<MavenPublication>("maven") { from(components["java"]) }
    }
}