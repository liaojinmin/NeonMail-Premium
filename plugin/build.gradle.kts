import com.github.jengelman.gradle.plugins.shadow.tasks.ShadowJar

plugins {
    id("com.github.johnrengelman.shadow") version "7.1.2"
}

val kotlinVersionNum: String
    get() = project.kotlin.coreLibrariesVersion.replace(".", "")

val generatedYamlDir = File("$buildDir/generated")

dependencies {
    implementation(project(":project:module-hook"))
    implementation(project(":project:module-smtp"))
    implementation(project(":project:module-api"))
    implementation(project(":project:module-common"))
    implementation(project(":project:module-menu"))
    implementation(project(":project:module-libs"))
}

tasks {

    withType<ShadowJar> {
        this.archiveFileName.set("${rootProject.name}-${rootProject.version}.jar")
        archiveClassifier.set("")
        exclude("META-INF/maven/**")
        exclude("META-INF/tf/**")
        exclude("META-INF/LICENSE.txt")
        exclude("META-INF/NOTICE.txt")
        exclude("module-info.java")

        exclude("com/google/**")
        exclude("com/sun/**")
        exclude("org/apache/**")
        exclude("org/slf4j/**")
        exclude("org/json/**")

        // 重定向 Kotlin
        relocate("kotlin.", "kotlin${kotlinVersionNum}.") {
            exclude("kotlin.Metadata")
        }
        // asm
        relocate("org.objectweb.asm", "${rootProject.group}.libraries.asm")

        // tabooproject reflex
        relocate("org.tabooproject.reflex", "${rootProject.group}.libraries.reflex")

        // hikari
        relocate("com.zaxxer.hikari", "${rootProject.group}.libraries.zaxxer.hikari")

        // redis
        relocate("redis.clients", "${rootProject.group}.libraries.redis.clients")

        // javax
        relocate("javax.mail", "${rootProject.group}.libraries.javax.mail")
        relocate("javax.activation", "${rootProject.group}.libraries.javax.activation")

        classifier = null
    }

    shadowJar {
        println("> Apply plugin.yml")
        dependsOn("generateYaml")
        from(generatedYamlDir)
    }

    build {
        dependsOn(shadowJar)
    }

}

tasks.register("generateYaml") {
    doLast {
        if (!generatedYamlDir.exists()) {
            generatedYamlDir.mkdirs()
        }
        val yamlContent = """
            name: ${project.name}
            version: ${project.version}
            main:  ${project.group}.NeonMailLoader
            authors:
              - '老廖'
            depend:
              - PlaceholderAPI
            api-version: 1.13
        """.trimIndent()
        val outputFile = File("$generatedYamlDir/plugin.yml")
        outputFile.writeText(yamlContent)
    }
}

gradle.buildFinished {
    File(buildDir, "libs/${project.name}-${rootProject.version}.jar").delete()
    File(buildDir, "libs/plugin-${rootProject.version}-sources.jar").delete()
}
