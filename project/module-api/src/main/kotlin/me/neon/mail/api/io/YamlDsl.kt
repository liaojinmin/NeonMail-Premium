package me.neon.mail.api.io


import org.bukkit.configuration.ConfigurationSection
import org.bukkit.configuration.file.FileConfiguration
import org.bukkit.configuration.file.YamlConfiguration
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

fun yaml(action: YamlDsl.() -> Unit) = YamlDsl(YamlConfiguration()).apply {
    action(this)
}

fun YamlDsl.saveToFile(file: File) {
    (this.yml as FileConfiguration).save(file)
}

@Suppress("UNCHECKED_CAST")
fun <K, V> ConfigurationSection.getMap(path: String): Map<K, V> {
    val map = HashMap<K, V>()
    getConfigurationSection(path)?.let { section ->
        section.getKeys(false).forEach { key ->
            try {
                map[key as K] = section[key] as V
            } catch (ex: Throwable) {
                ex.printStackTrace()
            }
        }
    }
    return map
}