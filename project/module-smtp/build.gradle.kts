dependencies {

    compileOnly(project(":project:common"))

    compileOnly("javax.mail:javax.mail-api:1.6.2")
    compileOnly("javax.mail:mail:1.5.0-b01")
    compileOnly("javax.activation:activation:1.1.1")

}

// 子模块
taboolib { subproject = true }