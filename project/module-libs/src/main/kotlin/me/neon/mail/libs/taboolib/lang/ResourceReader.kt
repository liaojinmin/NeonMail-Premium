@file:Suppress("DEPRECATION")

package me.neon.mail.libs.taboolib.lang

import me.neon.mail.libs.NeonLibsLoader
import me.neon.mail.libs.taboolib.lang.type.TypeList
import me.neon.mail.libs.taboolib.lang.type.TypeText
import org.bukkit.configuration.ConfigurationSection
import org.bukkit.configuration.file.YamlConfiguration
import org.bukkit.plugin.Plugin
import java.io.File
import java.nio.charset.StandardCharsets
import java.util.jar.JarFile

/**
 * TabooLib
 * me.neon.mail.libs.lang.ResourceReader
 *
 * @author sky
 * @since 2021/6/21 11:48 下午
 */
class ResourceReader(val plugin: Plugin = NeonLibsLoader.pluginId) {

    val files = HashMap<String, LanguageFile>()
    init {
        val pluginFile: File = NeonLibsLoader.loader.getPluginFile()
        JarFile(pluginFile).use { jar ->
            jar.entries().iterator().forEachRemaining {
                if (it.name.startsWith("${LangLoader.path}/") && it.name.endsWith(".yml")) {
                    val code = it.name.substringAfterLast('/').substringBeforeLast('.')

                    val source = jar.getInputStream(it).readBytes().toString(StandardCharsets.UTF_8)
                    val nodes = HashMap<String, Type>()

                    val sourceFile = YamlConfiguration()
                    sourceFile.loadFromString(source)

                    // 加载内存中的原件
                    loadNodes(sourceFile, nodes, code)

                    // 释放文件
                    val file = File(plugin.dataFolder,"${LangLoader.path}/$code.yml")
                    if (!file.exists()) {
                        plugin.saveResource("${LangLoader.path}/$code.yml", true)
                    }

                    val exists = HashMap<String, Type>()
                    // 加载文件
                    loadNodes(YamlConfiguration.loadConfiguration(file), exists, code)
                    // 检查缺失
                    // val missingKeys = nodes.keys.filter { !exists.containsKey(it) }
                    /*
                    if (missingKeys.isNotEmpty() && migrate) {
                        // 更新文件
                        migrateFile(missingKeys, sourceFile, file)
                    }
                     */
                    nodes += exists
                    files[code] = LanguageFile(file, nodes).also { f ->
                        files[code] = f
                        // 文件变动监听
                        // TODO
                    }
                }
            }
        }
    }

    private fun loadNodes(file: YamlConfiguration, nodesMap: HashMap<String, Type>, code: String) {

        file.getKeys(false).forEach { node ->
            when (val obj = file[node]) {
                is String -> {
                    nodesMap[node] = TypeText(obj)
                }
                is List<*> -> {
                    nodesMap[node] = TypeList(obj.mapNotNull { sub ->
                        if (sub is Map<*, *>) {
                            loadNode(sub.map { it.key.toString() to it.value!! }.toMap(), code, node)
                        } else {
                            TypeText(sub.toString())
                        }
                    })
                }
                is ConfigurationSection -> {
                    val type = loadNode(obj.getValues(false).map { it.key to it.value!! }.toMap(), code, node)
                    if (type != null) {
                        nodesMap[node] = type
                    }
                }
                else -> {
                    plugin.logger.warning("Unsupported language node: $node ($code)")
                }
            }
        }
    }

    private fun loadNode(map: Map<String, Any>, code: String, node: String?): Type? {
        return if (map.containsKey("type") || map.containsKey("==")) {
            val type = (map["type"] ?: map["=="]).toString().lowercase()
            val typeInstance = LangLoader.languageType[type]?.getDeclaredConstructor()?.newInstance()
            if (typeInstance != null) {
                typeInstance.init(map)
            } else {
                plugin.logger.warning("Unsupported language type: $node > $type ($code)")
            }
            typeInstance
        } else {
            plugin.logger.warning("Missing language type: $map ($code)")
            null
        }
    }

    /*
    @Suppress("DEPRECATION")
    private fun migrateFile(missing: List<String>, source: Configuration, file: File) {
        asyncRunner {
            val append = ArrayList<String>()
            append += "# ------------------------- #"
            append += "#  UPDATE ${dateFormat.format(System.currentTimeMillis())}  #"
            append += "# ------------------------- #"
            append += ""
            missing.forEach { key ->
                val obj = source[key]
                if (obj != null) {
                    append += SecuredFile.dumpAll(key, obj)
                }
            }
            file.appendText("\n${append.joinToString("\n")}")
        }
    }

     */
}