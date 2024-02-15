val taboolibVersion: String by project

dependencies {
    compileOnly(fileTree("libs"))

    compileOnly(project(":project:module-libs"))
    compileOnly("org.black_ixx:playerpoints:3.1.1")
    compileOnly("com.github.MilkBowl:VaultAPI:-SNAPSHOT") { isTransitive = false }
}