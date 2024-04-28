dependencies {

    compileOnly(project(":project:common"))

    compileOnly(project(":project:module-service"))
    compileOnly(project(":project:module-template"))
    compileOnly(project(":project:module-smtp"))

}

// 子模块
taboolib { subproject = true }