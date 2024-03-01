
plugins {
    id("com.github.johnrengelman.shadow") version "7.1.2"
}

val kotlinVersionNum: String
    get() = project.kotlin.coreLibrariesVersion.replace(".", "")


dependencies {
    implementation(project(":project:module-hook"))
    implementation(project(":project:module-smtp"))
    implementation(project(":project:module-api"))
    implementation(project(":project:runtime-bukkit"))
}


taboolib {
    description {
        name(rootProject.name)
        contributors {
            name("老廖")
        }
    }

    env {
        enableIsolatedClassloader = false

        version {
            coroutines = null
        }

    }
    // hikari
    relocate("com.zaxxer.hikari", "${rootProject.group}.libraries.zaxxer.hikari")

    // redis
    relocate("redis.clients", "${rootProject.group}.libraries.redis.clients")

    // javax
    relocate("javax.mail", "${rootProject.group}.libraries.javax.mail")
    relocate("com.sun", "${rootProject.group}.libraries.com.sun")
    relocate("javax.activation", "${rootProject.group}.libraries.javax.activation")
}

tasks {
    jar {
        // 构件名
        archiveFileName.set("${rootProject.name}-${archiveFileName.get().substringAfter('-')}")
        // 打包子项目源代码
        rootProject.subprojects.forEach { from(it.sourceSets["main"].output) }
    }
}

gradle.buildFinished {
    File(buildDir, "libs/${project.name}-${rootProject.version}.jar").delete()
    File(buildDir, "libs/plugin-${rootProject.version}-sources.jar").delete()
}
