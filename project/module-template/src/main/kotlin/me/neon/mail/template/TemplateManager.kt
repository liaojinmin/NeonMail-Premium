package me.neon.mail.template

import me.neon.mail.utils.forFile
import me.neon.mail.utils.saveToFile
import me.neon.mail.utils.yaml
import org.bukkit.Bukkit
import org.bukkit.configuration.file.YamlConfiguration
import taboolib.common.LifeCycle
import taboolib.common.platform.Awake
import taboolib.common.platform.function.getDataFolder
import taboolib.module.chat.colored
import taboolib.platform.util.bukkitPlugin
import java.io.File
import kotlin.system.measureTimeMillis


/**
 * NeonMail
 * me.neon.mail.common
 *
 * @author 老廖
 * @since 2024/2/18 19:53
 */
object TemplateManager {


    private var templateMap: MutableMap<String, TemplatePack> = mutableMapOf()

    private val templateTypeRegister: MutableMap<Any, iTemplateType<*>> = mutableMapOf()

    private val saveDefaultTemp by lazy {
        val dir = File(getDataFolder(), "template")
        if (!dir.exists()) {
            arrayOf(
                "template/def.yml",
                "template/sch-1.yml",
                "template/sch-2.yml",
            ).forEach { bukkitPlugin.saveResource(it, true) }
        }
        dir
    }

    @JvmStatic
    fun registerType(key: String, type: iTemplateType<*>) {
        templateTypeRegister[key] = type
    }

    @JvmStatic
    fun getTemplatePack(id: String): TemplatePack? {
        return templateMap[id]
    }

    @JvmStatic
    fun getTemplatePackKeys(): MutableSet<String> {
        return templateMap.keys
    }

    fun TemplatePack.saveToFile() {
        // 如果是新建则加入缓存
        val loc = getTemplatePack(uniqueId)
        if (loc == null) {
            templateMap[uniqueId] = this
        }
        yaml {
            "template" to {
                "uniqueId" to uniqueId
                "type" to type
                "package" to {
                    "title" to title
                    "context" to context
                    "appendix" to {
                        appendix.forEach {
                            it.applyYaml(this)
                        }
                    }
                }
            }
        }.saveToFile(File(saveDefaultTemp, "$uniqueId.yml"))
    }

    @JvmStatic
    fun loaderTemplate() {
        val map = mutableMapOf<String, TemplatePack>()
        measureTimeMillis {
            saveDefaultTemp.forFile("yml").forEach {
                val yaml = YamlConfiguration.loadConfiguration(it)
                val uniqueId = yaml.getString("template.uniqueId") ?: error("模板找不到唯一ID -> ${it.name}")
                val type = yaml.getString("template.type") ?: error("模板找不到唯一邮件种类")
                val title = yaml.getString("template.package.title")?.colored() ?: "未知标题"
                val context = yaml.getStringList("template.package.context").colored()
                val pack = mutableListOf<iTemplateType<*>>()
                val section = yaml.getConfigurationSection("template.package.appendix")
                section?.getKeys(false)?.let { node ->
                    node.forEach { n ->
                        templateTypeRegister[n]?.let { type ->
                            pack.add(type.createNewInstance(section.get(n)))
                        }
                    }
                }
                map[uniqueId] = TemplatePack(uniqueId, type, title, context, pack)
            }
            templateMap = map
        }.also {
            Bukkit.getConsoleSender().sendMessage("§8[§bNeon§9Mail§8-§ePremium§8] §7已加载 §f${map.size} §7个邮件模板... §8(耗时 $it Ms)")
        }
    }

    @Awake(LifeCycle.ENABLE)
    private fun registerType() {
        templateTypeRegister["money"] = MoneyType(0.0)
        templateTypeRegister["points"] = PointsType(0)
        templateTypeRegister["command"] = CommandType(emptyList())
        templateTypeRegister["itemStack"] = ItemStackType(emptyList())
        loaderTemplate()
    }




}