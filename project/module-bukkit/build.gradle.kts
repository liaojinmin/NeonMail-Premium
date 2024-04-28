val taboolibVersion: String by project

dependencies {

    compileOnly(fileTree("libs"))
    compileOnly(project(":project:common"))
    compileOnly(project(":project:common-impl"))
    compileOnly(project(":project:module-service"))
    compileOnly(project(":project:module-template"))
    compileOnly(project(":project:module-scheduler"))

    compileOnly(project(":project:module-screen-germ"))
    compileOnly(project(":project:module-screen-vanilla"))

}

// 子模块
taboolib { subproject = true }