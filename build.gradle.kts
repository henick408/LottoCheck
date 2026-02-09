plugins {
    kotlin("jvm") version "2.2.20"
    id("com.vanniktech.maven.publish") version "0.36.0"
}

group = "io.github.henick408"
version = "1.0.0"

repositories {
    mavenCentral()
}

dependencies {
    implementation("com.squareup.retrofit2:retrofit:2.9.0")
    implementation("com.squareup.retrofit2:converter-gson:2.9.0")
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-core:1.10.2")
    testImplementation(kotlin("test"))
}

tasks.test {
    useJUnitPlatform()
}
kotlin {
    jvmToolchain(17)
}

mavenPublishing {
    coordinates("io.github.henick408", "lottocheck", "1.0.0")

    pom {
        name.set("Lotto Check")
        description.set("Biblioteka służąca do porównywania swoich losów Lotto z ich wynikami. Wymaga własnego klucza do Lotto OpenApi.")
        inceptionYear.set("2025")
        url.set("https://github.com/henick408/LottoCheck/")
        licenses {
            license {
                name.set("MIT License")
                url.set("https://opensource.org/licenses/MIT")
                distribution.set("https://opensource.org/licenses/MIT")
            }
        }
        developers {
            developer {
                id.set("henick408")
                name.set("Henick")
                url.set("https://github.com/henick408")
            }
        }
        scm {
            url.set("https://github.com/henick408/LottoCheck/")
            connection.set("scm:git:git://github.com/henick408/LottoCheck.git")
            developerConnection.set("scm:git:ssh://git@github.com/henick408/LottoCheck.git")
        }
    }
}