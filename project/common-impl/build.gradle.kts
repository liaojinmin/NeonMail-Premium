val taboolibVersion: String by project

dependencies {
    // 引入 API
    compileOnly(project(":project:common"))
    compileOnly(project(":project:module-service"))
    compileOnly(project(":project:module-template"))
    compileOnly(project(":project:module-scheduler"))
    compileOnly(project(":project:module-hook"))
    compileOnly(project(":project:module-smtp"))

    compileOnly(project(":project:module-screen-vanilla"))
  //  compileOnly(project(":project:module-screen-germ"))

    compileOnly("ink.ptms.core:v11701:11701-minimize:mapped")
    compileOnly("ink.ptms.core:v11701:11701-minimize:universal")

}
// 子模块
taboolib {
    subproject = true
}
