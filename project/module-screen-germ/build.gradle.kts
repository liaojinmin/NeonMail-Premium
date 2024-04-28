val taboolibVersion: String by project

dependencies {
    // 引入 API
    compileOnly(fileTree("libs"))

    compileOnly(project(":project:common-impl"))
    compileOnly(project(":project:common"))
    compileOnly(project(":project:module-service"))
    compileOnly(project(":project:module-hook"))
}

// 子模块
taboolib {
    subproject = true
}
