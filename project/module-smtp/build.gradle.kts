dependencies {
    compileOnly("javax.mail:javax.mail-api:1.6.2")

    compileOnly(project(":project:module-api"))
    compileOnly(project(":project:module-libs"))

    implementation("javax.mail:javax.mail-api:1.6.2") { isTransitive = false }
    implementation("javax.activation:activation:1.1.1") { isTransitive = false }
}