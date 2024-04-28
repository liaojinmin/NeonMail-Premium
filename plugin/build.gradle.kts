
plugins {
    id("com.github.johnrengelman.shadow") version "7.1.2"
}

val kotlinVersionNum: String
    get() = project.kotlin.coreLibrariesVersion.replace(".", "")


dependencies {

    implementation(project(":project:module-hook"))
    implementation(project(":project:module-smtp"))
    implementation(project(":project:common"))
    implementation(project(":project:common-impl"))
    implementation(project(":project:module-bukkit"))
    implementation(project(":project:module-service"))
    implementation(project(":project:module-template"))
    implementation(project(":project:module-scheduler"))

    implementation(project(":project:module-screen-germ"))
    implementation(project(":project:module-screen-vanilla"))

}


taboolib {
    description {
        name(rootProject.name)
        desc("${rootProject.name} 是一个高效的 Minecraft 邮箱插件")
        contributors {
            name("老廖")
        }
        dependencies {
            name("GermPlugin").optional(true)
            name("GeekEconomy").optional(true)
            name("PlayerPoints").optional(true)
            name("placeholderAPI").optional(true)
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
