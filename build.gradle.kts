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
        create<MavenPublication>("maven") {
            from(components["java"])
            pom {
                name = "MAX notification Spring Boot starter"
                description = "Spring Boot starter for sending notifications to the MAX messenger: " +
                        "one auto-configured client and max.notification.* settings."
                url = "https://github.com/JimbaJ/max-notification-spring-boot-starter"
                licenses {
                    license {
                        name = "Apache License, Version 2.0"
                        url = "https://www.apache.org/licenses/LICENSE-2.0.txt"
                        distribution = "repo"
                    }
                }
                developers {
                    developer {
                        id = "JimbaJ"
                        name = "Sergey Dorogov"
                        url = "https://github.com/JimbaJ"
                    }
                }
                scm {
                    connection = "scm:git:https://github.com/JimbaJ/max-notification-spring-boot-starter.git"
                    developerConnection = "scm:git:ssh://git@github.com/JimbaJ/max-notification-spring-boot-starter.git"
                    url = "https://github.com/JimbaJ/max-notification-spring-boot-starter"
                }
            }
        }
    }
}