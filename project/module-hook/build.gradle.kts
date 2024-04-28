val taboolibVersion: String by project

dependencies {

    compileOnly(fileTree("libs"))
    compileOnly(project(":project:common"))
    compileOnly(project(":project:module-service"))

    compileOnly("org.black_ixx:playerpoints:3.1.1")
    compileOnly("net.milkbowl.vault:Vault:1")
    compileOnly("me.clip:placeholderapi:2.10.9") { isTransitive = false }
}

// 子模块
taboolib { subproject = true }