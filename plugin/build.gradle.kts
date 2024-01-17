import com.github.jengelman.gradle.plugins.shadow.tasks.ShadowJar

plugins {
    id("com.github.johnrengelman.shadow") version "7.1.2"
}


val taboolibVersion: String by project
val kotlinVersionNum: String
    get() = project.kotlin.coreLibrariesVersion.replace(".", "")

dependencies {
    implementation(project(":project:module-api"))
    implementation(project(":project:module-common"))
    implementation(project(":project:module-menu"))
    implementation(project(":project:module-bukkit"))
}

tasks {
    withType<ShadowJar> {
       this.archiveFileName.set("${rootProject.name}-${rootProject.version}.jar")
        archiveClassifier.set("")
        exclude("META-INF/maven/**")
        exclude("META-INF/tf/**")
        exclude("module-info.java")

        exclude("com/google/**")
        exclude("org/apache/**")
        exclude("org/slf4j/**")
        exclude("org/json/**")

        // 重定向 TabooLib
        relocate("taboolib", "${rootProject.group}.taboolib")
        // 重定向 Kotlin
        relocate("kotlin.", "kotlin${kotlinVersionNum}.") { exclude("kotlin.Metadata") }

        // hikari
        relocate("com.zaxxer.hikari", "${rootProject.group}.libs.zaxxer.hikari")

        // redis
        relocate("redis.clients", "${rootProject.group}.libs.redis.clients")


    }
    sourcesJar  {
        rootProject.subprojects.forEach { from(it.sourceSets["main"].allSource) }
    }
    build {
        dependsOn(shadowJar)
    }
}
gradle.buildFinished {
    File(buildDir, "libs/${project.name}-${rootProject.version}.jar").delete()
    File(buildDir, "libs/plugin-${rootProject.version}-sources.jar").delete()
}