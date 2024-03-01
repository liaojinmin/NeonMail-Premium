val taboolibVersion: String by project

dependencies {
    compileOnly(fileTree("libs"))

    compileOnly("org.black_ixx:playerpoints:3.1.1")
    compileOnly("com.github.MilkBowl:VaultAPI:-SNAPSHOT") { isTransitive = false }
    compileOnly("me.clip:placeholderapi:2.10.9") { isTransitive = false }
}

// 子模块
taboolib { subproject = true }