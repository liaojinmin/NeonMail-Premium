val taboolibVersion: String by project

plugins {
    id("io.izzel.taboolib") version "1.50"
}

dependencies {
    // 引入 API
    compileOnly(project(":project:module-common"))
    compileOnly(project(":project:module-api"))
    compileOnly(project(":project:module-menu"))
}

taboolib {
    description {
        name(rootProject.name)
        contributors {
            name("老廖")
        }
        dependencies {
            bukkitApi("1.13")
            name("PlaceholderAPI").optional(true)
        }
    }
    install("common", "platform-bukkit")
    options("skip-minimize", "keep-kotlin-module", "skip-taboolib-relocate")
    classifier = null
    version = taboolibVersion
}