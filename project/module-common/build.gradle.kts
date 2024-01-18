val taboolibVersion: String by project

dependencies {
    // 引入 API
    compileOnly(project(":project:module-hook"))
    compileOnly(project(":project:module-api"))


    compileOnly("ink.ptms.core:v11701:11701-minimize:mapped")
    compileOnly("ink.ptms.core:v11701:11701-minimize:universal")
}