package me.neon.mail.utils


import taboolib.library.configuration.ConfigurationSection
import taboolib.module.configuration.ConfigFile
import taboolib.module.configuration.SecuredFile
import java.io.File

/**
 * @作者: 老廖
 * @时间: 2023/5/12 5:54
 * @包: me.geek.regions.utils
 */
class YamlDsl(val yml: ConfigurationSection) {

    infix fun String.to(any: Any?) {
        yml[this] = any
    }
    infix fun String.to(any: List<List<String>>) {
        if (any.size == 1) {
            yml[this] = any[0]
        } else {
            yml[this] = any
        }
    }

    infix fun String.to(action: YamlDsl.() -> Unit) = YamlDsl(yml.createSection(this)).apply {
        action(this)
    }.yml

}

fun yaml(action: YamlDsl.() -> Unit) = YamlDsl(SecuredFile()).apply {
    action(this)
}

fun YamlDsl.saveToFile(file: File) {
    (this.yml as ConfigFile).saveToFile(file)
}
