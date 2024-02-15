package me.neon.mail.libs.utils.io

import org.bukkit.configuration.ConfigurationSection
import java.io.File


fun File.forFile(end: String): List<File> {
    return mutableListOf<File>().run {
        if (isDirectory) {
            listFiles()?.forEach {
                addAll(it.forFile(end))
            }
        } else if (exists() && absolutePath.endsWith(end)) {
            add(this@forFile)
        }
        this
    }
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