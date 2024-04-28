import io.izzel.taboolib.gradle.*


dependencies {
    // 引入 API
    compileOnly(project(":project:common"))
    compileOnly(project(":project:module-template"))
}

// 子模块
taboolib {
    subproject = true
}