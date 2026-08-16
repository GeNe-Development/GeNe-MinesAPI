// genemines-api - a GeNe-Mines publikus fejlesztői szerződése.
//
// SZABÁLYOK:
//  - CSAK interfészek, event-osztályok és adat-rekordok. Semmi implementáció,
//    semmi GeNeLib, semmi utalás a plugin belső működésére.
//  - Egyetlen függőség: Paper API compileOnly - a szerver adja futásidőben.
//  - EZT A CSOMAGOT (dev.gene.genemines.api) SENKI NEM RELOKÁLJA. Ettől működik
//    a ServicesManager-megosztás: minden pluginnak bájtra ugyanaz az osztály
//    kell lássa, és azt futásidőben a GeNe-Mines adja.
//  - A felület STABIL: bővíteni default metódussal szabad, meglévőt törni tilos.
plugins {
    `java-library`
    `maven-publish`
}

group = "dev.gene"
version = "1.0.0"

repositories {
    mavenCentral()
    maven("https://repo.papermc.io/repository/maven-public/")
}

dependencies {
    // A szerver adja futásidőben - nem kerül az API jarba.
    compileOnly("io.papermc.paper:paper-api:1.21.8-R0.1-SNAPSHOT")
}

java {
    toolchain.languageVersion = JavaLanguageVersion.of(21)
    withSourcesJar()
    withJavadocJar()
}

tasks.withType<JavaCompile>().configureEach {
    options.encoding = "UTF-8"
    options.release = 21
}

tasks.withType<Javadoc>().configureEach {
    (options as StandardJavadocDocletOptions).encoding = "UTF-8"
    (options as StandardJavadocDocletOptions).addStringOption("Xdoclint:none", "-quiet")
}

publishing {
    publications {
        create<MavenPublication>("maven") {
            from(components["java"])
            pom {
                name.set("GeNe-Mines API")
                description.set("Public developer API for the GeNe-Mines Paper plugin.")
                url.set("https://genedev.hu/")
                licenses {
                    license {
                        name.set("MIT License")
                        url.set("https://opensource.org/licenses/MIT")
                    }
                }
            }
        }
    }
    repositories {
        maven {
            name = "reposilite"
            url = uri("https://maven.genedev.hu/releases")
            credentials {
                username = providers.gradleProperty("reposiliteUser").orNull ?: ""
                password = providers.gradleProperty("reposilitePass").orNull ?: ""
            }
        }
    }
}
