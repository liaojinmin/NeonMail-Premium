val taboolibVersion: String by project

dependencies {
    // 引入 API
    compileOnly(project(":project:module-common"))
    compileOnly(project(":project:module-api"))
    compileOnly(project(":project:module-libs"))
  //  compileOnly(project(":project:module-bukkit"))

}