import com.vanniktech.maven.publish.JavadocJar
import com.vanniktech.maven.publish.KotlinJvm
import com.vanniktech.maven.publish.SonatypeHost

plugins {
    kotlin("jvm")
    alias(libs.plugins.openapi.generator)
    alias(libs.plugins.maven.publish)
}

repositories {
    mavenCentral()
}

dependencies {
    // Dependencies of the generated code
    implementation(libs.moshi.kotlin)
    implementation(libs.moshi.adapters)

    testImplementation(kotlin("test"))
    testImplementation(libs.kotest.junit5)
    testImplementation(libs.kotest.assertions.core)
    testImplementation(libs.kotest.assertions.json)
}

kotlin {
    jvmToolchain(17)
}

java {
    withSourcesJar()
}

tasks.test {
    useJUnitPlatform()
}

val openApiOutputDir = layout.buildDirectory.dir("generated/openapi")

openApiGenerate {
    generatorName.set("kotlin")
    inputSpec.set("$projectDir/openapi.yaml")
    outputDir.set(openApiOutputDir.map { it.asFile.absolutePath })
    packageName.set("com.valhalla.api")
    globalProperties.set(mapOf(
        "models" to ""
    ))
    configOptions.set(mapOf(
        "groupId" to "com.valhalla",
        "serializationLibrary" to "moshi"
    ))
    skipValidateSpec.set(true)
}

// Run code gen before compiling
tasks.compileKotlin.configure {
    dependsOn(tasks.openApiGenerate)
}

tasks.named("sourcesJar") {
    dependsOn(tasks.openApiGenerate)
}

sourceSets {
    main {
        kotlin.srcDir(openApiOutputDir.map { it.dir("src/main/kotlin") })
    }
}

val libraryVersion: String by extra

mavenPublishing {
    publishToMavenCentral(SonatypeHost.CENTRAL_PORTAL)
    signAllPublications()

    if (libraryVersion == "unspecified") {
        throw GradleException("libraryVersion must be specified in settings.gradle.kts")
    }

    coordinates("io.github.rallista", "valhalla-models-config", libraryVersion)

    // TODO: Convert to Dokka?
    configure(KotlinJvm(sourcesJar = true, javadocJar = JavadocJar.Javadoc()))

    pom {
        name.set("Valhalla Config Models")
        description.set("Serializable objects to build Valhalla's config JSON.")
        url.set(ProjectConfig.PROJECT_URL)

        licenses {
            license {
                name.set(ProjectConfig.LICENSE_NAME)
                url.set(ProjectConfig.LICENSE_URL)
            }
        }

        developers {
            developer {
                id.set(ProjectConfig.DEVELOPER_ID)
                name.set(ProjectConfig.DEVELOPER_NAME)
            }
        }

        scm {
            connection.set(ProjectConfig.SCM_CONNECTION)
            developerConnection.set(ProjectConfig.SCM_DEV_CONNECTION)
            url.set(ProjectConfig.SCM_URL)
        }
    }
}