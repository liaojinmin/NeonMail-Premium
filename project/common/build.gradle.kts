val taboolibVersion: String by project

dependencies {
    // 引入 API

    compileOnly("ink.ptms.core:v11701:11701-minimize:mapped")
    compileOnly("ink.ptms.core:v11701:11701-minimize:universal")
}

// 子模块
taboolib {
    subproject = true
}
