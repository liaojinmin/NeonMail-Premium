import io.izzel.taboolib.gradle.*


dependencies {
    // 引入 API
    compileOnly(project(":project:module-hook"))
    compileOnly(project(":project:module-api"))
    compileOnly(project(":project:module-smtp"))

    compileOnly("ink.ptms.core:v11701:11701-minimize:mapped")
    compileOnly("ink.ptms.core:v11701:11701-minimize:universal")

}

// 子模块
taboolib {
    subproject = true
}