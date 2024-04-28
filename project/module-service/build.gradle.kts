val taboolibVersion: String by project

dependencies {
    // 引入 API
    compileOnly(project(":project:common"))

    compileOnly(project(":project:module-smtp"))
}
// 子模块
taboolib {
    subproject = true
}
