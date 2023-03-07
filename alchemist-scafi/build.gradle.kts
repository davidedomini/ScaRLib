plugins {
    java
    scala
}

group = "io.github.davidedomini"

repositories {
    mavenCentral()
}

dependencies {
    implementation("org.scala-lang:scala-library:2.13.10")
    implementation("it.unibo.alchemist:alchemist:25.14.6")
    implementation("it.unibo.alchemist:alchemist-incarnation-scafi:25.7.2")
    implementation("it.unibo.alchemist:alchemist-incarnation-protelis:25.14.6")
    testImplementation("org.junit.jupiter:junit-jupiter-api:5.6.0")
    testRuntimeOnly("org.junit.jupiter:junit-jupiter-engine")
    implementation(project(":scarlib-core"))
}

tasks.getByName<Test>("test") {
    useJUnitPlatform()
}
/*
publishing.publications {
    withType<MavenPublication> {
        pom {
            developers {
                developer {
                    name.set("Gianluca Aguzzi")
                    email.set("gianluca.aguzzi@unibo.it")
                }
            }
        }
    }
}*/