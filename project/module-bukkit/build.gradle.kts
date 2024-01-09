val taboolibVersion: String by project

plugins {
    id("io.izzel.taboolib") version "1.50"
}

dependencies {
    // 引入 API
    compileOnly(project(":project:common"))
    compileOnly("ink.ptms.core:v11604:11604")
    compileOnly("ink.ptms.core:v11701:11701-minimize:mapped")
    compileOnly("ink.ptms.core:v11701:11701-minimize:universal")
}

taboolib {
    description {
        name(rootProject.name)
        contributors {
            name("HSDLao_liao")
        }
        dependencies {
            bukkitApi("1.13")
        }
    }
    install("common", "platform-bukkit")
    options("skip-minimize", "keep-kotlin-module", "skip-taboolib-relocate")
    classifier = null
    version = taboolibVersion
}